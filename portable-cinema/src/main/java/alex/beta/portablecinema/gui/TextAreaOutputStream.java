package alex.beta.portablecinema.gui;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Deprecated
public class TextAreaOutputStream extends OutputStream {

// *************************************************************************************************
// INSTANCE MEMBERS
// *************************************************************************************************

    /**
     * array for write(int val);
     */
    private byte[] oneByte;
    /**
     * most recent action
     */
    private Appender appender;

    public TextAreaOutputStream(JTextArea txtara) {
        this(txtara, 5000);
    }

    public TextAreaOutputStream(JTextArea txtara, int maxlin) {
        if (maxlin < 1) {
            throw new IllegalArgumentException("TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")");
        }
        oneByte = new byte[1];
        appender = new Appender(txtara, maxlin);
    }

    /**
     * Clear the current console text area.
     */
    public synchronized void clear() {
        if (appender != null) {
            appender.clear();
        }
    }

    @Override
    public synchronized void close() {
        appender = null;
    }

    @Override
    public synchronized void flush() {
        //Nothing to do
    }

    public synchronized void write(int val) {
        oneByte[0] = (byte) val;
        write(oneByte, 0, 1);
    }

    @Override
    public synchronized void write(byte[] ba) {
        write(ba, 0, ba.length);
    }

    @Override
    public synchronized void write(byte[] ba, int str, int len) {
        if (appender != null) {
            appender.append(new String(ba, str, len, StandardCharsets.UTF_8));
        }
    }

// *************************************************************************************************
// STATIC MEMBERS
// *************************************************************************************************

    static class Appender implements Runnable {
        private static final String EOL1 = "\n";
        private static final String EOL2 = System.getProperty("line.separator", EOL1);
        private final JTextArea textArea;
        /**
         * maximum lines allowed in text area
         */
        private final int maxLines;
        /**
         * length of lines within text area
         */
        private final LinkedList<Integer> lengths;
        /**
         * values waiting to be appended
         */
        private final List<String> values;
        /**
         * length of current line
         */
        private int curLength;
        private boolean clear;
        private boolean queue;

        Appender(JTextArea txtara, int maxlin) {
            textArea = txtara;
            maxLines = maxlin;
            lengths = new LinkedList<>();
            values = new ArrayList<>();

            curLength = 0;
            clear = false;
            queue = true;
        }

        synchronized void append(String val) {
            values.add(val);
            if (queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        synchronized void clear() {
            clear = true;
            curLength = 0;
            lengths.clear();
            values.clear();
            if (queue) {
                queue = false;
                EventQueue.invokeLater(this);
            }
        }

        // MUST BE THE ONLY METHOD THAT TOUCHES textArea!
        public synchronized void run() {
            if (clear) {
                textArea.setText("");
            }
            for (String val : values) {
                curLength += val.length();
                if (val.endsWith(EOL1) || val.endsWith(EOL2)) {
                    if (lengths.size() >= maxLines) {
                        textArea.replaceRange("", 0, lengths.removeFirst());
                    }
                    lengths.addLast(curLength);
                    curLength = 0;
                }
                textArea.append(val);
            }
            values.clear();
            clear = false;
            queue = true;
        }
    }
}