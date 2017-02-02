package cs652.repl;


import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaREPL {

	public static void main(String[] args) throws IOException {
		exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;
		Path tempDir = Files.createTempDirectory("tmp");
		//String java2=null;
		while (true) {
			System.out.print(">");
			String java2 = reader.getNestedString();
			//if(!java2.equals("999")) {
				//System.out.println(java2);
			//break;
			// TODO
			//String def=stdin.readLine()
				boolean ok = isDeclaration(java2);
				String def = null;
				String stat = null;
				if (ok) {
					def = java2;
				} else {
					stat = java2;
				}
				String filename = "Interp_" + classNumber + ".java";
				String classname = "Interp_" + classNumber;
				String extendSuper = getSuperClass(classNumber);
				String content = getCode(classname, extendSuper, def, stat);
				//System.out.println("Path of the tmp directory:" + tempDir);
				//System.out.println("Is declaration:" + ok);
				writeFile(tempDir.toString(), filename, content);
				boolean success = compile(tempDir.toString());
				/*if (!success) {
					stat = def;
					def = null;
					content = getCode(classname, extendSuper, def, stat);
					writeFile(tempDir.toString(), filename, content);
					compile(tempDir.toString());
				}*/
			classNumber++;
				if (success == true) {
					try {
						getOutput(classname, tempDir.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					classNumber--;
					System.err.println();
				}
				//System.out.println(success);
			//}else {
			//	break;
		// 	}
		}
	}
	public static boolean isDeclaration(String line) throws IOException {
		Path tempDir = Files.createTempDirectory("tmp");
		//System.out.println("Path 2:"+tempDir);
		String content=getCode("Bogus",null,line,null);
		writeFile(tempDir.toString(),"Bogus.java",content);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromStrings(Arrays.asList(tempDir.toString()+"/Bogus.java"));
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
				null, compilationUnits);
		boolean success = task.call();
		fileManager.close();
		return diagnostics.getDiagnostics().size()==0;
		//System.out.println(diagnostics.getDiagnostics().size());
	}
	private static String getSuperClass(int classNumber)
	{
		String es=null;
		if(classNumber!=0)
		{
			es="Interp_"+(classNumber-1);
		}
		return es;
	}

	private static void writeFile(String dir, String filename, String content) throws IOException {
		BufferedWriter bufferedWriter;
		FileWriter fileWriter;
		String absoluteFilePath = dir + "/"+filename;
		fileWriter = new FileWriter(absoluteFilePath);
		bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(content);
		//System.out.println(filename);
		bufferedWriter.close();
		fileWriter.close();
	}
	private static String getCode(String className, String extendSuper, String def, String stat) {
		StringBuilder sb=new StringBuilder();
		sb.append("import java.io.*;\n" +
				"import java.util.*;\n\n\n" );
		if(extendSuper!=null) {
			if (stat == null) {
				sb.append("public class " + className + " extends " + extendSuper + "{\n" + "public static" +
						" " + def + "\n" +
						"    public static void exec() {\n" +
						"    }\n" +
						"}");
			}else {
				sb.append("public class " + className + " extends " + extendSuper + "{\n"+
						"    public static void exec() {\n" +
						"" +stat +"   \n }\n" +
						"}");
			}
		}
		else{ if (stat == null) {
			sb.append("public class " + className + "{\n" + "public static" +
					" " + def + "\n" +
					"    public static void exec() {\n" +
					"    }\n" +
					"}");
		}else {
			sb.append("public class " + className + "{\n"+
					"    public static void exec() {\n" +
							"" +stat +"   \n }\n" +
							"}");
		}
		}
		return sb.toString();
	}
	public static Boolean compile(String tempDir) throws IOException {
		//This code snippet is from http://www.java2s.com/Code/Java/JDK-6/CompileaJavafilewithJavaCompiler.htm
		// and http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html, modified according to the requirements.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		File[] files = new File(tempDir).listFiles();
		//System.out.println(files.length);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		List<File> cfiles=new ArrayList<>();
		for (File file : files) {
			String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
			if(extension.equals("java")){
				cfiles.add(file);
			}
		}
			Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(cfiles);
		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
				null, compilationUnits);
		boolean success = task.call();
		fileManager.close();
		//System.out.println("Success: " + success);
		//Following code snippet is from
		// http://www.programcreek.com/java-api-examples/index.php?source_dir=blogix-master/src/main/java/net/mindengine/blogix/compiler/BlogixCompiler.java
		//System.setErr(new PrintStream(new ByteArrayOutputStream()));
		for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
			//System.err.format("line %d: %s\n", diagnostic.getLineNumber(), diagnostic.getMessage(null));
			System.err.println("line "+ diagnostic.getLineNumber()+": "+ diagnostic.getMessage(null));
		}
		//System.out.println();
		return success;
	}


	public static void getOutput(String filename, String tempDir) throws Exception {
		URL tmpURL = new File(tempDir).toURI().toURL();
		ClassLoader loader = new URLClassLoader(new URL[]{tmpURL});
		Class cl = loader.loadClass(filename);
		Method m=cl.getDeclaredMethod("exec");
		m.invoke(null);
	}
}
