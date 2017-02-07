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

    /**
     * This method takes input from console, compiles it and returns appropriate output or errors as required.
     * @param r  Reader that takes input from the console.
     * @throws IOException
     */
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
            if (!java2.equals("999")) { // What is this?
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

    /**
     * This method returns true if the given line is a declartion and false otherwise.
     * @param line input from the user
     * @return true if line is a declaration
     * @throws IOException
     */
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

    /**
     * checks if the input has "print expr;" type statement, replaces the prit expr; to System.out.println(expr);
     * @param line given input
     * @return replaced String
     */
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

    /**
     * Checks if the given class will have a superClass or not.
     * @param classNumber
     * @return name of the super class
     */
    private static String getSuperClass(int classNumber) {
        String es = null;
        if (classNumber != 0) {
            es = "Interp_" + (classNumber - 1);
        }
        return es;
    }

    /**
     * This method writes to file given the content
     * @param dir the temporary directory
     * @param filename
     * @param content
     * @throws IOException
     */
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

    /**
     * For a given line, generates String of code with apt classname with superclass
     * @param className
     * @param extendSuper SuperClass for given class
     * @param def declaration
     * @param stat statement
     * @return String of code
     */
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

    /**
     * Compiles the code and generates .class file in the temporary directory
     * @param tempDir temp directory to store all the .class files.
     * @return true if compilation is successful.
     * @throws IOException
     */
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

    /**
     * For given diagnostics array this method generates list of errors for the compiler.
     * @param diagnostics array of diagnostics
     */
    private static void getError(DiagnosticCollector<JavaFileObject> diagnostics) {
        /*Following code snippet is from
		 http://www.programcreek.com/java-api-examples/index.php?source_dir=blogix-master/src/main/java/net/mindengine/blogix/compiler/BlogixCompiler.java*/
        for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println("line " + diagnostic.getLineNumber() + ": " + diagnostic.getMessage(null));
        }
    }

    /**
     * Generates appropriate output for a given .class file.
     * @param filename
     * @throws Exception
     */
    public static void getOutput(String filename) throws Exception {
        Class cl = loader.loadClass(filename);
        Method m = cl.getDeclaredMethod("exec");
        m.invoke(null);
    }
}
