package alex.beta.portablecinema.filesystem;

public interface VisitorMessageCallback {

    /**
     * Output messages, while visitor is working
     * @param messages
     */
    void output(String... messages);
}
