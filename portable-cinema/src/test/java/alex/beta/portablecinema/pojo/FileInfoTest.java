package alex.beta.portablecinema.pojo;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileInfoTest {
    private FileInfo fi1 = null;
    private FileInfo fi2 = null;

    @Before
    public void setUp() {
        fi1 = new FileInfo();
        fi1.setDuration(9605);

        fi2 = new FileInfo();
        fi2.setDuration(8211);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetFormattedDuration() throws Exception {
        Assert.assertEquals("2小时40分钟5秒", fi1.getFormattedDuration());
        Assert.assertEquals("2小时16分钟51秒", fi2.getFormattedDuration());
    }

    @Test
    public void testParseDurationText() throws Exception {
        Assert.assertEquals(3600, FileInfo.parseDurationText("1小时"));
        Assert.assertEquals(3610, FileInfo.parseDurationText("1小时10秒"));
        Assert.assertEquals(4210, FileInfo.parseDurationText("1小时10分钟10秒"));
        Assert.assertEquals(4200, FileInfo.parseDurationText("1小时10分钟"));
        Assert.assertEquals(70, FileInfo.parseDurationText("1分钟10秒"));
        Assert.assertEquals(60, FileInfo.parseDurationText("1分钟"));
        Assert.assertEquals(10, FileInfo.parseDurationText("10秒"));
        Assert.assertEquals(9605, FileInfo.parseDurationText("2小时40分钟5秒"));
        Assert.assertEquals(9605, FileInfo.parseDurationText("9605"));
        Assert.assertEquals(9600, FileInfo.parseDurationText("2小时40分钟5"));
        Assert.assertEquals(3600, FileInfo.parseDurationText("1小时40"));
    }

    @Test
    public void testParseDurationTextWrongFormat1() throws Exception {
        Assert.assertThat("1小时秒", new BaseMatcher<String>() {
            @Override
            public boolean matches(Object actualValue) {
                try {
                    FileInfo.parseDurationText(actualValue.toString());
                    return false;
                } catch (NumberFormatException e1) {
                    return true;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("无法被解析为时间值");
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendValue("解析" + item + "成功，或出现异常");
            }
        });
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDurationTextWrongFormat2() throws Exception {
        FileInfo.parseDurationText("你好");
    }
}
