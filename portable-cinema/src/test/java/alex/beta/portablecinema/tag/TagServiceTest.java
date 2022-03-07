package alex.beta.portablecinema.tag;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.tag.xml.Glossary;
import alex.beta.portablecinema.tag.xml.Term;
import org.apache.commons.text.similarity.EditDistance;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.SimilarityScore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class TagServiceTest {
    private PortableCinemaConfig config;
    private TagService tagService;

    @Before
    public void setUp() {
        config = PortableCinemaConfig.getDefault();

        Glossary g = new Glossary();

        g.getActor().add(new Term("actor-0"));
        g.getActor().add(new Term("actor-1"));
        g.getActor().add(new Term("actor-2"));


        Term[] ts = new Term[3];

        ts[0] = new Term();
        ts[0].setKeyword("term-text-0");
        ts[0].getAlias().add("term-alias-00");
        ts[0].getAlias().add("term-alias-01");
        ts[0].getAlias().add("term-alias-02");
        ts[0].getTag().add("term-tag-0");
        g.getOther().add(ts[0]);

        ts[1] = new Term();
        ts[1].setKeyword("term-text-1");
        ts[1].getAlias().add("term-alias-10");
        ts[1].getAlias().add("term-alias-01");
        ts[1].getAlias().add("term-alias-12");
        ts[1].getTag().add("term-tag-10");
        ts[1].getTag().add("term-tag-11");
        g.getOther().add(ts[1]);

        ts[2] = new Term();
        ts[2].setKeyword("term-text-2");
        ts[2].getAlias().add("term-alias-20");
        ts[2].getAlias().add("term-alias-21");
        ts[2].getAlias().add("term-alias-22");
        ts[2].getTag().add("term-tag-2");
        g.getOther().add(ts[2]);

        tagService = TagService.getInstance(config, g);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testJaroWinklerSimilarity() {
        SimilarityScore<Double> similarity = new JaroWinklerSimilarity();
        double similarity1 = similarity.apply("光明酸奶", "蒙牛 风味酸牛奶 100克");
        double similarity2 = similarity.apply("酸奶", "蒙牛 风味酸牛奶 100克");
        System.out.printf("%s:%s", similarity1, similarity2);
        Assert.assertTrue(similarity1 > similarity2);
    }

    @Test
    public void testJaroWinklerDistance() {
        EditDistance<Double> distance = new JaroWinklerDistance();
        double distance1 = distance.apply("光明酸奶", "蒙牛 风味酸牛奶 100克");
        double distance2 = distance.apply("酸奶", "蒙牛 风味酸牛奶 100克");
        System.out.printf("%s:%s", distance1, distance2);
        Assert.assertTrue(distance1 > distance2);
    }

    @Test
    public void testSimilarTags() throws Exception {
        Set<String> result = tagService.suggest("TestActor-0G^&##");
        Assert.assertEquals(1, result.size());

        result = tagService.suggest("TestActor-0G^&#ACTOR-1#");
        Assert.assertEquals(2, result.size());

        result = tagService.suggest("term-alias-21");
        Assert.assertTrue(result.size() == 1 && result.contains("term-tag-2"));

        result = tagService.suggest("AAAAterm-alias-01BBB555678CCCC###$$");
        Assert.assertTrue(result.size() == 3 && result.contains("term-tag-0") && result.contains("term-tag-10") && result.contains("term-tag-11"));

        result = tagService.suggest("term-text");
        Assert.assertTrue(result.size() >= 1);

        result = tagService.suggest("term-text-0");
        Assert.assertEquals(1, result.size());

        result = tagService.suggest("term-text-1");
        Assert.assertEquals(2, result.size());
    }
}
