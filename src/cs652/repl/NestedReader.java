package cs652.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Created by Anjani Bajaj on 1/28/2017.
 */
class NestedReader {

    private StringBuilder buf;    // fill this as you process, character by character
    private BufferedReader input; // where are we reading from?
    private int c; // current character of lookahead; reset upon each getNestedString() call

    NestedReader(BufferedReader stdin) {
        input = stdin;
        buf = new StringBuilder();
    }

    private void consume() throws IOException {
        buf.append((char) c);
        c = input.read();
    }

    public String getNestedString() throws IOException {
        Stack<Character> stack = new Stack<>();
        c=input.read();
        buf=new StringBuilder();
        while(true){
        //c=input.read();
                switch (c) {
                    case '{':
                        stack.push('}');
                        consume();
                        break;
                    case '}':
                        if (stack.peek().equals('}')) {
                            consume();
                            stack.pop();
                            //System.out.println("In closing bracket");
                        } else {
                            return buf.toString();
                        }
                        //case -1: break;
                    default:
                        if (c == '\n' && stack.isEmpty()) {
                            return buf.toString();
                        } else {
                            consume();
                            break;
                        }
                }
            }
        }
            //System.out.println("String:"+buf.toString());
            //c=input.read();
}

