package alex.beta.commons.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class MathTest {
    private Math.ExponentMap<BigDecimal, Integer> map1;
    private Math.ExponentMap<BigDecimal, Integer> map2;
    private Math.ExponentMap<BigDecimal, Integer> map15;
    private Math.ExponentMap<BigDecimal, Integer> map45;


    @Before
    public void setUp() {
        map1 = new Math.ExponentMap<>();
        map1.put(BigDecimal.ONE, 1);

        map2 = new Math.ExponentMap<>();
        map2.put(BigDecimal.valueOf(2), 1);

        map15 = new Math.ExponentMap<>();
        map15.put(BigDecimal.valueOf(3), 1);
        map15.put(BigDecimal.valueOf(5), 1);

        map45 = new Math.ExponentMap<>();
        map45.put(BigDecimal.valueOf(3), 2);
        map45.put(BigDecimal.valueOf(5), 1);
    }

    @After
    public void tearDown() {
        map1 = null;
        map2 = null;
        map15 = null;
        map45 = null;
    }

    @Test
    public void testFactorial() {
        Assert.assertEquals(Math.factorial(BigDecimal.ZERO), BigDecimal.ONE);
        Assert.assertEquals(Math.factorial(BigDecimal.ONE), BigDecimal.ONE);
        Assert.assertEquals(Math.factorial(BigDecimal.valueOf(10)), BigDecimal.valueOf(3628800));

        Assert.assertEquals(Math.factorial(5), BigDecimal.valueOf(120));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialNegative() {
        Math.factorial(BigDecimal.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFactorialFloat() {
        Math.factorial(BigDecimal.valueOf(1.2F));
    }

    @Test
    public void testResolvePrime() {
        Assert.assertEquals(Math.resolvePrime(BigDecimal.valueOf(1)), map1);
        Assert.assertEquals(Math.resolvePrime(BigDecimal.valueOf(2)), map2);
        Assert.assertEquals(Math.resolvePrime(BigDecimal.valueOf(15)), map15);
        Assert.assertEquals(Math.resolvePrime(BigDecimal.valueOf(45)), map45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolvePrimeNegative() {
        Math.resolvePrime(BigDecimal.valueOf(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolvePrimeZero() {
        Math.resolvePrime(BigDecimal.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolvePrimeFloat() {
        Math.resolvePrime(BigDecimal.valueOf(2.3F));
    }

    @Test
    public void testSumInverseSquareRoot() {
        BigDecimal s = BigDecimal.ZERO;
        for (int i = 1; i <= 2019; i++) {
            s = s.add(Math.inverseSquareRoot(BigDecimal.valueOf(i)));
        }
        System.out.println(s);
        Assert.assertTrue(88.4173403952 < s.doubleValue() && 88.4173403953 > s.doubleValue());
    }
}
