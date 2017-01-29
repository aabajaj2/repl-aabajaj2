package cs652.repl;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by Anjani Bajaj on 1/28/2017.
 */
public class NestedReader {

    private BufferedReader br;
    public NestedReader(BufferedReader stdin) {
        br=stdin;
    }

    public String getNestedString() throws IOException {
        String line = br.readLine();
        //System.out.println(line);
        StringBuffer sb=new StringBuffer();
        if (line.contains("{")) {
            if (line != "}") {
                sb.append(line);
                System.out.println("BRAC:"+line);
            }else{
                System.out.println("BRAC 2");
            }
            return line;
        } else if (line.endsWith(";")) {
            sb.append(line);
            System.out.println("; line:"+sb.toString());
        }
    /*  StringBuffer sb = new StringBuffer();
        while (!line.isEmpty()) {
            if (line.contains("{")) {
                while (line != "}") {
                    sb.append(line);
                }
                return line;
            } else if (line.endsWith(";")) {
                sb.append(line);
            }
        }
        System.out.println(sb.toString());
        return sb.toString();*

        /*switch (br.readLine()){
                case ";":  System.out.println("In cASe ;");returnString=br.readLine();
                break;
                case "{": System.out.println("In case {");while(br.readLine()!="}"){
                    returnString=br.readLine();
                }break;
                default: returnString=null;
                    break;
            }
            return returnString;
    }*/
    return line;
    }
}
