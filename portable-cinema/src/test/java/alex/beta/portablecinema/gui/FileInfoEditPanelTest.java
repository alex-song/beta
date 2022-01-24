package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.pojo.FileInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FileInfoEditPanelTest {
    private FileInfo fi1;

    @Before
    public void setUp() {
        fi1 = new FileInfo();
        fi1.setPath("/Users/alexsong/Development/my_workspace/beta/portable-cinema/sample");
        fi1.setCover1("cover.jpg");
        fi1.setCover2("www.png");
        Set<String> tags = new HashSet<>();
        tags.add("tag 1");
        tags.add("tag 2");
        tags.add("tag 3");
        fi1.setTags(tags);
        fi1.setDuration(9605);
        fi1.setName("变形金刚4绝迹重生.mp4");
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSplit() throws Exception {
        Set<String> tags = new HashSet<>();
        String text = "abc,def;ghi  \r\n   jkl\n\nmno，你好；测试";
        BufferedReader reader = new BufferedReader(new StringReader(text));
        String tagsLine = null;
        while ((tagsLine = reader.readLine()) != null)
            if (StringUtils.isNotBlank(tagsLine))
                tags.addAll(Arrays.stream(StringUtils.trim(tagsLine).split("[,，;；]")).map(StringUtils::trim).collect(Collectors.toSet()));

        Assert.assertEquals(7, tags.size());
        Assert.assertTrue(tags.contains("abc")
                && tags.contains("def")
                && tags.contains("ghi")
                && tags.contains("jkl")
                && tags.contains("mno")
                && tags.contains("你好")
                && tags.contains("测试")
        );
        reader.close();
    }
}
