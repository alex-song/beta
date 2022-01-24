package alex.beta.commons.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecursionSelectorTest {

    private RecursionSelector<String> rs0;
    private RecursionSelector<String> rs1;

    @Before
    public void setUp() {
        rs0 = new RecursionSelector<>(new String[]{});
        rs1 = new RecursionSelector<>("0123456789".split("(?!\\b)"));
    }

    @After
    public void tearDown() {
        rs0 = null;
        rs1 = null;
    }

    @Test
    public void testSelect() {
        Assert.assertEquals(210, rs1.select(4).size());

        List<String[]> result = rs1.select(1);
        StringBuffer temp = new StringBuffer();
        for (String[] strings : result) {
            temp.append(Arrays.toString(strings).replaceAll("[\\[\\]\\s,]", ""));
        }
        Assert.assertEquals("0123456789", temp.toString());
    }

    @Test
    public void testExceptionalSelect() {
        Assert.assertEquals(1, rs1.select(10).size());
        Assert.assertEquals(1, rs1.select(11).size());
        Assert.assertEquals(Collections.EMPTY_LIST, rs0.select(2));
        Assert.assertEquals(Collections.EMPTY_LIST, rs1.select(0));
    }
}
