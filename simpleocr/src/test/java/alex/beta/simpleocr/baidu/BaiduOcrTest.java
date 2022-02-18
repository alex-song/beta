package alex.beta.simpleocr.baidu;

import alex.beta.simpleocr.Ocr;
import alex.beta.simpleocr.OcrFactory;
import com.google.common.io.Resources;
import org.junit.*;

import java.util.List;

@Ignore
public class BaiduOcrTest {
    private Ocr ocr;

    @Before
    public void setUp() {
        ocr = OcrFactory.newInstance(OcrFactory.Provider.BAIDU);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAnalyseMix() throws Exception {
        byte[] image = Resources.toByteArray(Resources.getResource("image-mix.jpg"));
        List<String> result = ocr.analyse(image);
        for (String txt : result) {
            System.out.println(txt);
        }
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.get(0).contains("頑張ろう！"));
        Assert.assertTrue(result.get(1).contains("Let ' s do our best ! "));
    }

    @Test
    public void testAnalyseCN() throws Exception {
        byte[] image = Resources.toByteArray(Resources.getResource("image-cn.png"));
        List<String> result = ocr.analyse(image);
        for (String txt : result) {
            System.out.println(txt);
        }
        Assert.assertEquals(13, result.size());
        Assert.assertTrue(result.get(10).contains("语言处理基础技术"));
    }

    @Test
    public void testAnalyseJP() throws Exception {
        byte[] image = Resources.toByteArray(Resources.getResource("image-jp.png"));
        List<String> result = ocr.analyse(image);
        for (String txt : result) {
            System.out.println(txt);
        }
        Assert.assertEquals(5, result.size());
        Assert.assertTrue(result.get(3).contains("の筆順も縦書きを前提としており"));
    }

    @Test
    public void testAnalysePlaybill() throws Exception {
        byte[] image = Resources.toByteArray(Resources.getResource("harry_potter_jp.jpeg"));
        List<String> result = ocr.analyse(image);
        for (String txt : result) {
            System.out.println(txt);
        }
        Assert.assertTrue(result.contains("史上最強のファンタジーがやってくる。"));
    }
}
