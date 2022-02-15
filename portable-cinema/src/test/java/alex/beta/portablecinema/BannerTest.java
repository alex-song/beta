package alex.beta.portablecinema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BannerTest {
    private Banner banner;

    @Before
    public void setUp() {
        banner = Banner.read();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConvertUTCTimestamp() {
        assertEquals("ABCDEF2022-01-29 20:04:111234", banner.convertTimestamp("ABCDEF#{2022-01-29T12:04:11Z}1234"));
        assertEquals("ABC@#{DEF2022-01-29 20:04:11!@#$", banner.convertTimestamp("ABC@#{DEF#{2022-01-29T12:04:11Z}!@#$"));
        assertEquals("AAAA2022-01-29 20:04:11BBBB2022-01-29 20:04:12CCCC", banner.convertTimestamp("AAAA#{2022-01-29T12:04:11Z}BBBB#{2022-01-29T12:04:12Z}CCCC"));
    }

    @Test
    public void testConvertNormalizedTimestamp() {
        assertEquals("ABCDEF2022-01-29 12:04:111234", banner.convertTimestamp("ABCDEF#{20220129120411}1234"));
        assertEquals("ABC@#{DEF2022-01-29 12:04:11!@#$", banner.convertTimestamp("ABC@#{DEF#{20220129120411}!@#$"));
        assertEquals("AAAA2022-01-29 12:04:11BBBB2022-01-29 12:04:12CCCC", banner.convertTimestamp("AAAA#{20220129120411}BBBB#{20220129120412}CCCC"));
    }

    @Test
    public void testConvertTimestamp() {
        assertEquals("AAAA2022-01-29 20:04:11BBBB2022-01-29 12:04:12CCCC", banner.convertTimestamp("AAAA#{2022-01-29T12:04:11Z}BBBB#{20220129120412}CCCC"));
    }

    @Test
    public void testInvalidConvertTimestamp() {
        assertEquals("2022-01-29T12:04:11Z", banner.convertTimestamp("2022-01-29T12:04:11Z"));
        assertEquals("#{2022-01-29T12:04:11}", banner.convertTimestamp("#{2022-01-29T12:04:11}"));
        assertEquals("20220129120411", banner.convertTimestamp("20220129120411"));
        assertEquals("#{2022012912041}", banner.convertTimestamp("#{2022012912041}"));
//        assertEquals("2022-01-29 12:04:11", banner.convertTimestamp("#{2022-01-29 12:04:11}"));
    }
}
