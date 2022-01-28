package alex.beta.portablecinema;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BannerTest {
    private Banner banner;

    @Before
    public void setUp() {
        banner = Banner.getInstance();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConvertUTCTimestamp() throws Exception {
        assertEquals("ABCDEF2022-01-29 20:04:111234", banner.convertTimestamp("ABCDEF#{2022-01-29T12:04:11Z}1234"));
        assertEquals("ABC@#{DEF2022-01-29 20:04:11!@#$", banner.convertTimestamp("ABC@#{DEF#{2022-01-29T12:04:11Z}!@#$"));
        assertEquals("AAAA2022-01-29 20:04:11BBBB2022-01-29 20:04:12CCCC", banner.convertTimestamp("AAAA#{2022-01-29T12:04:11Z}BBBB#{2022-01-29T12:04:12Z}CCCC"));
    }

    @Test
    public void testConvertNormalizedTimestamp() throws Exception {
        assertEquals("ABCDEF2022-01-29 12:04:111234", banner.convertTimestamp("ABCDEF#{20220129120411}1234"));
        assertEquals("ABC@#{DEF2022-01-29 12:04:11!@#$", banner.convertTimestamp("ABC@#{DEF#{20220129120411}!@#$"));
        assertEquals("AAAA2022-01-29 12:04:11BBBB2022-01-29 12:04:12CCCC", banner.convertTimestamp("AAAA#{20220129120411}BBBB#{20220129120412}CCCC"));
    }

    @Test
    public void testConvertTimestamp() throws Exception {
        assertEquals("AAAA2022-01-29 20:04:11BBBB2022-01-29 12:04:12CCCC", banner.convertTimestamp("AAAA#{2022-01-29T12:04:11Z}BBBB#{20220129120412}CCCC"));
    }
}
