package cs652.repl;


import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JavaREPL {
	public static void main(String[] args) throws IOException {
		exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		BufferedReader stdin = new BufferedReader(r);
		NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;

		while (true) {
			System.out.print(">");
			//String java2 = reader.getNestedString();
			//System.out.println(java2);
			// TODO
			//String java=stdin.readLine();
			//String java=stdin.readLine();
			String def=stdin.readLine();
			String stat=null;
			String filename = "Interp_"+classNumber+".java";
			String classname="Interp_"+classNumber;
			String extendSuper=getSuperClass(classNumber);
			String content = getCode(classname,extendSuper,def,stat);
			writeFile("/temp",filename,content);
			boolean success=compile();
			if(!success){
				stat=def;
				def=null;
				content = getCode(classname,extendSuper,def,stat);
				writeFile("/temp",filename,content);
				compile();
			}
			try {
				getOutput(classname);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//System.out.println(success);
			classNumber++;
		}
	}
	/*public static boolean isDeclaration(String line) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager
				.getJavaFileObjectsFromFiles(cfiles);
		String[] compileOptions = new String[]{"-d", classesDir.getAbsolutePath()} ;
		Iterable<String> compilationOptions = Arrays.asList(compileOptions);		JavacTask task = (JavacTask)
				compiler.getTask(null, fileManager, diagnostics,
						compileOptions, null, compilationUnits);
		boolean ok = task.call();
		return diagnostics.getDiagnostics().size() == 0;
	}*/

	private static String getSuperClass(int classNumber) {
		String es=null;
		if(classNumber!=0){
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
		System.out.println(filename);
		bufferedWriter.close();
		fileWriter.close();
	}
	private static String getCode(String className, String extendSuper, String def, String stat) {
		StringBuilder sb=new StringBuilder();
		sb.append("import java.io.*;\n" +
				"import java.util.*;\n" );
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
						"" +stat +"    }\n" +
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
							"" +stat +"    }\n" +
							"}");
		}
		}
		return sb.toString();
	}
    public static Boolean compile() throws IOException {
		//This code snippet is from http://www.java2s.com/Code/Java/JDK-6/CompileaJavafilewithJavaCompiler.htm
		// and http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html, modified according to the requirements.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		File[] files = new File("/Temp").listFiles();
		System.out.println(files.length);
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
		System.out.println("Success: " + success);
		//System.out.println(diagnostics.getDiagnostics());
		return success;
	}
	public static void getOutput(String filename) throws Exception {
		URL tmpURL = new File("/Temp").toURI().toURL();
		ClassLoader loader = new URLClassLoader(new URL[]{tmpURL});
		Class cl = loader.loadClass(filename);
		Method m=cl.getDeclaredMethod("exec");
		m.invoke(null);
	}
}
