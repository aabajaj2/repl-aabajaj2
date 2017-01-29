package cs652.repl;

import java.io.*;

public class JavaREPL {
	public static void main(String[] args) throws IOException {
		exec(new InputStreamReader(System.in));
	}

	public static void exec(Reader r) throws IOException {
		BufferedReader stdin = new BufferedReader(r);
		//NestedReader reader = new NestedReader(stdin);
		int classNumber = 0;

		while (true) {
			System.out.print(">");
			//String java = reader.getNestedString();
			//System.out.println(java);
			// TODO
			String java=stdin.readLine();
			String def=null;
			String stat=null;
			String filename = "Interp_"+classNumber+".java";
			String classname="Interp_"+classNumber;
			String extendSuper=getSuperClass(classNumber);
			String content = getCode(classname,extendSuper,java,stat);
			writeFile("/temp",filename,content);
			classNumber++;
		}
	}

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
		//System.out.println("File created");
		bufferedWriter.close();
		fileWriter.close();
	}
	private static String getCode(String className, String extendSuper, String def, String stat)
	{
		StringBuilder sb=new StringBuilder();
		sb.append("import java.io.*;\n" +
				"import java.util.*;\n" );
		if(extendSuper!=null)
            sb.append("public class" + className + " extends " + extendSuper + "{\n" + "public static" +
                    " " + def + "\n" +
                    "    public static void exec() {\n" +
                    "    }\n" +
                    "}");
        else sb.append("public class" + className + "{\n" + "public static" +
                " " + def + "\n" +
                "    public static void exec() {\n" +
                "    }\n" +
                "}");
		return sb.toString();
	}
}
