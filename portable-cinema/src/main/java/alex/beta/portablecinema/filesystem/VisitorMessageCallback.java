package alex.beta.portablecinema.filesystem;

public interface VisitorMessageCallback {

    /**
     * Output messages, while visitor is working
     *
     * @param messages to display
     */
    void output(String... messages);
}
