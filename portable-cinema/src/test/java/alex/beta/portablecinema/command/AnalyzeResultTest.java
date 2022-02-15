package alex.beta.portablecinema.command;

import alex.beta.portablecinema.pojo.FileInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AnalyzeResultTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddSimilarVideos1() {
        AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand.AnalyzeResult();
        FileInfo[] fis = new FileInfo[3];

        fis[0] = new FileInfo();
        fis[0].setSize(100);
        fis[0].setResolution(new FileInfo.Resolution(100, 100));

        fis[1] = new FileInfo();
        fis[1].setSize(100);
        fis[1].setResolution(new FileInfo.Resolution(100, 100));

        fis[2] = new FileInfo();
        fis[2].setSize(100);
        fis[2].setResolution(new FileInfo.Resolution(200, 500));

        result.addSimilarVideos(100, fis);

        Assert.assertEquals(2, result.getSimilarVideos().get(0).length);
    }

    @Test
    public void testAddSimilarVideos2() {
        AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand.AnalyzeResult();
        FileInfo[] fis = new FileInfo[3];

        fis[0] = new FileInfo();
        fis[0].setSize(100);
        fis[0].setResolution(new FileInfo.Resolution());

        fis[1] = new FileInfo();
        fis[1].setSize(100);
        fis[1].setResolution(new FileInfo.Resolution(100, 100));

        fis[2] = new FileInfo();
        fis[2].setSize(100);
        fis[2].setResolution(new FileInfo.Resolution(200, 500));

        result.addSimilarVideos(100, fis);

        Assert.assertTrue(result.getSimilarVideos().isEmpty());
    }

    @Test
    public void testAddSimilarVideos3() {
        AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand.AnalyzeResult();
        FileInfo[] fis = new FileInfo[3];

        fis[0] = new FileInfo();
        fis[0].setSize(100);

        fis[1] = new FileInfo();
        fis[1].setSize(100);
        fis[1].setResolution(new FileInfo.Resolution(100, 100));

        fis[2] = new FileInfo();
        fis[2].setSize(100);

        result.addSimilarVideos(100, fis);

        Assert.assertEquals(2, result.getSimilarVideos().get(0).length);
    }

    @Test
    public void testAddSimilarVideos4() {
        AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand.AnalyzeResult();
        FileInfo[] fis = new FileInfo[3];

        fis[0] = new FileInfo();
        fis[0].setSize(100);

        fis[1] = new FileInfo();
        fis[1].setSize(100);
        fis[1].setResolution(new FileInfo.Resolution(100, 100));

        fis[2] = new FileInfo();
        fis[2].setSize(100);
        fis[2].setResolution(new FileInfo.Resolution(0, 0));

        result.addSimilarVideos(100, fis);

        Assert.assertEquals(2, result.getSimilarVideos().get(0).length);
    }
}
