package alex.beta.portablecinema.tag;

import alex.beta.portablecinema.PortableCinemaConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TagServiceTest {
    private PortableCinemaConfig config;
    private TagService tagService;

    @Before
    public void setUp() {
        config = PortableCinemaConfig.getDefault();

        Glossary g = new Glossary();

        Set<String> actors = new HashSet<>();
        actors.add("actor-0");
        actors.add("actor-1");
        actors.add("actor-2");
        g.setActors(StringUtils.join(actors, Glossary.DELIMITER));

        Glossary.Term[] ts = new Glossary.Term[3];

        ts[0] = new Glossary.Term();
        ts[0].setText("term-text-0");
        Set<String> alias0 = new HashSet<>();
        alias0.add("term-alias-00");
        alias0.add("term-alias-01");
        alias0.add("term-alias-02");
        ts[0].setAlias(alias0);
        Set<String> tag0 = new HashSet<>();
        tag0.add("term-tag-0");
        ts[0].setTags(tag0);

        ts[1] = new Glossary.Term();
        ts[1].setText("term-text-1");
        Set<String> alias1 = new HashSet<>();
        alias1.add("term-alias-10");
        alias1.add("term-alias-01");
        alias1.add("term-alias-12");
        ts[1].setAlias(alias1);
        Set<String> tag1 = new HashSet<>();
        tag1.add("term-tag-10");
        tag1.add("term-tag-11");
        ts[1].setTags(tag1);

        ts[2] = new Glossary.Term();
        ts[2].setText("term-text-2");
        Set<String> alias2 = new HashSet<>();
        alias2.add("term-alias-20");
        alias2.add("term-alias-21");
        alias2.add("term-alias-22");
        ts[2].setAlias(alias2);
        Set<String> tag2 = new HashSet<>();
        tag2.add("term-tag-2");
        ts[2].setTags(tag2);

        g.setKeywords(ts);

        tagService = TagService.getInstance(config, g);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testJaroWinklerSimilarity() throws Exception {
        SimilarityScore<Double> similarity = new JaroWinklerSimilarity();
        double similarity1 = similarity.apply("光明酸奶", "蒙牛 风味酸牛奶 100克");
        double similarity2 = similarity.apply("酸奶", "蒙牛 风味酸牛奶 100克");
        System.out.println(String.format("%s:%s", String.valueOf(similarity1), String.valueOf(similarity2)));
        Assert.assertTrue(similarity1 > similarity2);
    }

    @Test
    public void testJaroWinklerDistance() throws Exception {
        EditDistance<Double> distance = new JaroWinklerDistance();
        double distance1 = distance.apply("光明酸奶", "蒙牛 风味酸牛奶 100克");
        double distance2 = distance.apply("酸奶", "蒙牛 风味酸牛奶 100克");
        System.out.println(String.format("%s:%s", String.valueOf(distance1), String.valueOf(distance2)));
        Assert.assertTrue(distance1 > distance2);
    }

    @Test
    public void testSimilarTags() throws Exception {
        Set<String> result = tagService.similarTags("TestActor-0G^&##");
        Assert.assertTrue(result.size() == 1);

        result = tagService.similarTags("TestActor-0G^&#ACTOR-1#");
        Assert.assertTrue(result.size() == 2);

        result = tagService.similarTags("term-alias-21");
        Assert.assertTrue(result.size() == 1 && result.contains("term-tag-2"));

        result = tagService.similarTags("AAAAterm-alias-01BBB555678CCCC###$$");
        Assert.assertTrue(result.size() == 3 && result.contains("term-tag-0") && result.contains("term-tag-10") && result.contains("term-tag-11"));

        result = tagService.similarTags("term-text");
        Assert.assertTrue(result.size() >= 1);

        result = tagService.similarTags("term-text-0");
        Assert.assertTrue(result.size() == 1);

        result = tagService.similarTags("term-text-1");
        Assert.assertTrue(result.size() == 2);
    }
}
