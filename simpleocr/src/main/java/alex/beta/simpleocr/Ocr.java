package alex.beta.simpleocr;

import java.io.File;
import java.util.List;

public interface Ocr {

    /**
     * 获取识别图片后的结果
     *
     * @param file Local file
     * @return List of recognized words
     * @throws OcrException
     */
    List<String> analyse(File file) throws OcrException;

    /**
     * 获取识别图片后的结果
     *
     * @param image Image data
     * @return List of recognized words
     * @throws OcrException
     */
    List<String> analyse(byte[] image) throws OcrException;
}
