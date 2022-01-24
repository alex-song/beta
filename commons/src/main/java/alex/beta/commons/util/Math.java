package alex.beta.commons.util;

import com.google.common.base.Joiner;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Math {

    public static BigDecimal factorial(long num) {
        return factorial(BigDecimal.valueOf(num));
    }

    /**
     * 阶乘
     *
     * @param num
     * @return
     */
    public static BigDecimal factorial(BigDecimal num) {
        if (num.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        } else if (num.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("%d is not supported, please provide a non-negative integer.", num));
        } else if (num.scale() >= 1) {
            throw new IllegalArgumentException(String.format("%f is not supported, please provide a non-negative integer.", num));
        } else {
            return factorial(num.subtract(BigDecimal.ONE)).multiply(num);
        }
    }

    public static Map<BigDecimal, Integer> resolvePrime(long num) {
        return resolvePrime(BigDecimal.valueOf(num));
    }

    /**
     * 分解质因数
     *
     * @param num
     * @return
     */
    public static Map<BigDecimal, Integer> resolvePrime(BigDecimal num) {
        ExponentMap<BigDecimal, Integer> exponentMap = new ExponentMap<>();
        if (num.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("%d is not supported, please provide a positive integer.", num));
        } else if (num.compareTo(BigDecimal.ONE) == 0) {
            exponentMap.put(BigDecimal.ONE, 1);
            return exponentMap;
        } else if (num.scale() >= 1) {
            throw new IllegalArgumentException(String.format("%f is not supported, please provide a positive integer.", num));
        } else {
            // 定义最小素数
            BigDecimal i = BigDecimal.valueOf(2);
            // 进行辗转相除法
            while (i.compareTo(num) <= 0) {
                // 若num 能整除 i ，则i 是num 的一个因数
                if (num.divideAndRemainder(i)[1].equals(BigDecimal.ZERO)) {
                    // 将i 保存进缓存
                    exponentMap = countExponent(exponentMap, i);
                    // 同时将 num除以i 的值赋给 num
                    num = num.divide(i);
                    // 将i重新置为2
                    i = BigDecimal.valueOf(2);
                } else {
                    // 若无法整除，则i 自增
                    i = i.add(BigDecimal.ONE);
                }
            }
            return exponentMap;
        }
    }

    /**
     * Default precision: 100
     *
     * @param num
     * @return
     */
    public static BigDecimal inverseSquareRoot(BigDecimal num) {
        if (num.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(String.format("%d is not supported, please provide a positive decimal.", num));
        } else {
            return BigDecimal.ONE.divide(sqrt4BigDecimal(num, 100), new MathContext(100, RoundingMode.HALF_UP));
        }
    }

    /**
     * 牛顿迭代法求大数开方
     *
     * @param num
     * @param precision
     * @return
     */
    public static BigDecimal sqrt4BigDecimal(BigDecimal num, int precision) {
        if (num.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("%d is not supported, please provide a non-negative decimal.", num));
        } else if (num.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        } else if (num.compareTo(BigDecimal.ONE) == 0) {
            return BigDecimal.ONE;
        }
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal x = num;
        for (int c = 0; c < precision; c++) {
            x = (x.add(num.divide(x, mc))).divide(BigDecimal.valueOf(2), mc);
        }
        return x;
    }

    /**
     * 指数缓存
     *
     * @param exponentMap
     * @param base
     * @return
     */
    private static ExponentMap<BigDecimal, Integer> countExponent(ExponentMap<BigDecimal, Integer> exponentMap, BigDecimal base) {
        if (exponentMap.containsKey(base)) {
            exponentMap.put(base, exponentMap.get(base) + 1);
        } else {
            exponentMap.put(base, 1);
        }
        return exponentMap;
    }

    static class ExponentMap<BigDecimal, Integer> extends TreeMap<BigDecimal, Integer> {
        private static Joiner.MapJoiner mapJoiner = Joiner.on(" * ").withKeyValueSeparator("^");

        @Override
        public String toString() {
            return mapJoiner.join(this);
        }

        @Override
        public boolean equals(Object that) {
            if (that == null || !(that instanceof Map)) {
                return false;
            } else {
                Map thatMap = (Map) that;
                if (this.size() != thatMap.size()) {
                    return false;
                } else {
                    for (BigDecimal key : this.keySet()) {
                        if (!this.get(key).equals(thatMap.get(key))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
//        int t = 0x10CB1;
//        System.out.println((char)t);
        //Matcher m_mat = Pattern.compile( "xyz" ).matcher( null );
        //System.out.println(factorial(new BigDecimal(20)).subtract(factorial(new BigDecimal(15))));
        System.out.println(resolvePrime(new BigDecimal(1740)));

    }
}
