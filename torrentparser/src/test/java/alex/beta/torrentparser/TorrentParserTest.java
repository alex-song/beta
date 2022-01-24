package alex.beta.torrentparser;

import org.junit.Assert;
import org.junit.Test;

public class TorrentParserTest {
    @Test
    public void testSampleTorrent() throws Exception {
        Torrent t = TorrentParser.parseTorrent("./1.torrent");
        Assert.assertEquals("Windows XP Pro SP3 - Activated", t.getName());
        Assert.assertEquals(2, t.getFileList().size());
        Assert.assertEquals("http://bigfoot1942.sektori.org:6969/announce", t.getAnnounce());
    }
}
