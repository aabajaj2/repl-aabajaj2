package cs652.repl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

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
        //System.out.println("c=" + (char) c);
        consume();
        while (true) {
            c=input.read();
            switch (c) {
                case '{':
                    while (c!='}'){
                        consume();
                    }
                default:
                    consume();
                    break;
            }
            return buf.toString();
        }
    }
}

