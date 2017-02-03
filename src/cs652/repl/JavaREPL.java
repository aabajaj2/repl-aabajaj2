package cs652.repl;


import com.sun.source.util.JavacTask;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaREPL {
    private static URL tmpURL;
    private static ClassLoader loader;

    public static void main(String[] args) throws IOException {
        exec(new InputStreamReader(System.in));
    }

    public static void exec(Reader r) throws IOException {
        BufferedReader stdin = new BufferedReader(r);
        NestedReader reader = new NestedReader(stdin);
        int classNumber = 0;
        Path tempDir = Files.createTempDirectory("tmp");
        tmpURL = new File(tempDir.toString()).toURI().toURL();
        loader = new URLClassLoader(new URL[]{tmpURL});
        String java2 = "";
        while (true) {
            if(stdin.toString().isEmpty()) {
            System.out.print(">");
            }
            java2 = reader.getNestedString();
            if (!java2.equals("999")) {
                //break;
                // TODO
                //String def=stdin.readLine()
                java2 = checkforPrint(java2);
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
                writeFile(tempDir.toString(), filename, content);
                boolean success = compile(tempDir.toString());
                classNumber++;
                if (success) {
                    try {
                        getOutput(classname);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    classNumber--;
                }
            } else {
                break;
            }
        }
    }

    public static boolean isDeclaration(String line) throws IOException {
        Path tempDir = Files.createTempDirectory("tmp");
        String content = getCode("Bogus", null, line, null);
        writeFile(tempDir.toString(), "Bogus.java", content);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(Arrays.asList(tempDir.toString() + "/Bogus.java"));
        JavacTask task = (JavacTask)
                compiler.getTask(null, fileManager, diagnostics,
                        null, null, compilationUnits);
        task.parse();
        fileManager.close();
        return diagnostics.getDiagnostics().size() == 0;
    }

    private static String checkforPrint(String line) {
        line = line.replaceAll("\n", "");
        line = line.replaceAll("\r", "");

        if (line.matches("print .*")) {
            String printLines[] = line.split("print ");
            if (printLines[1].contains(";")) {
                String pl2[] = printLines[1].split(";");
                line = "System.out.println(" + pl2[0] + ");";
            }
        }
        return line;
    }

    private static String getSuperClass(int classNumber) {
        String es = null;
        if (classNumber != 0) {
            es = "Interp_" + (classNumber - 1);
        }
        return es;
    }

    private static void writeFile(String dir, String filename, String content) throws IOException {
        BufferedWriter bufferedWriter;
        FileWriter fileWriter;
        String absoluteFilePath = dir + "/" + filename;
        fileWriter = new FileWriter(absoluteFilePath);
        bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(content);
        bufferedWriter.close();
        fileWriter.close();
    }

    private static String getCode(String className, String extendSuper, String def, String stat) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.io.*;\n" +
                "import java.util.*;\n\n\n");
        if (extendSuper != null) {
            if (stat == null) {
                sb.append("public class " + className + " extends " + extendSuper + "{\n" + "public static" +
                        " " + def + "\n" +
                        "    public static void exec() {\n" +
                        "    }\n" +
                        "}");
            } else {
                sb.append("public class " + className + " extends " + extendSuper + "{\n" +
                        "    public static void exec() {\n" +
                        "" + stat + "   \n }\n" +
                        "}");
            }
        } else {
            if (stat == null) {
                sb.append("public class " + className + "{\n" + "public static" +
                        " " + def + "\n" +
                        "    public static void exec() {\n" +
                        "    }\n" +
                        "}");
            } else {
                sb.append("public class " + className + "{\n" +
                        "    public static void exec() {\n" +
                        "" + stat + "   \n }\n" +
                        "}");
            }
        }
        return sb.toString();
    }

    public static Boolean compile(String tempDir) throws IOException {
        /*This code snippet is from http://www.java2s.com/Code/Java/JDK-6/CompileaJavafilewithJavaCompiler.htm
         and http://docs.oracle.com/javase/7/docs/api/javax/tools/JavaCompiler.html, modified according to the requirements.*/
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File[] files = new File(tempDir).listFiles();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        List<File> cfiles = new ArrayList<>();
        for (File file : files) {
            String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
            if (extension.equals("java")) {
                cfiles.add(file);
            }
        }
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromFiles(cfiles);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
                null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
        getError(diagnostics);
        return success;
    }

    private static void getError(DiagnosticCollector<JavaFileObject> diagnostics) {
        /*Following code snippet is from
		 http://www.programcreek.com/java-api-examples/index.php?source_dir=blogix-master/src/main/java/net/mindengine/blogix/compiler/BlogixCompiler.java*/
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println("line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));
        }
    }

    public static void getOutput(String filename) throws Exception {
        Class cl = loader.loadClass(filename);
        Method m = cl.getDeclaredMethod("exec");
        m.invoke(null);
    }
}
