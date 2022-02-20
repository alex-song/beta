package alex.beta.simpleocr;

public class OcrException extends Exception {
    public static final String CONFIG_FILE_READ_EXCEPTION = "CONFIG_FILE_READ_EXCEPTION";
    public static final String IMAGE_FILE_READ_EXCEPTION = "IMAGE_FILE_READ_EXCEPTION";
    public static final String SERVER_ERROR_EXCEPTION = "SERVER_ERROR_EXCEPTION";

    private String serverErrorMsg;

    public OcrException() {
        super();
    }

    public OcrException(String message) {
        super(message);
    }

    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }

    public OcrException(Throwable cause) {
        super(cause);
    }

    public String getServerErrorMsg() {
        return serverErrorMsg;
    }

    public OcrException setServerErrorMsg(String serverErrorMsg) {
        this.serverErrorMsg = serverErrorMsg;
        return this;
    }
}
