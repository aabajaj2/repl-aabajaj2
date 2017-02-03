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
        c = input.read();
        buf = new StringBuilder();
        while (true) {
                //c=input.read();
            if (c != -1) {
                  switch (c) {
                        case '{':
                            stack.push('}');
                            consume();
                            break;
                        case '}':
                            //consume();
                            if (stack.peek().equals('}')) {
                                consume();
                                stack.pop();
                                break;
                            } else {
                                return buf.toString();
                            }
                        case '(':
                            stack.push(')');
                            consume();
                            break;
                        case ')':
                            //consume();
                            if (stack.peek().equals(')')) {
                                consume();
                                stack.pop();
                                break;
                                //System.out.println("In closing bracket");
                            }
                        case '/':
                            c = input.read();
                            // consume();
                            if (c == '/') {
                                while (c != '\n') {
                                    c = input.read();
                                }
                            } else {
                                consume();
                                break;
                            }
                            //input.readLine();
                            break;
                        default:
                            if (c == '\n' && stack.isEmpty()) {
                                return buf.toString().trim();
                            } else {
                                consume();
                                break;
                            }
                    }
                } else {
                    return "999";
            }
        }
    }
}
