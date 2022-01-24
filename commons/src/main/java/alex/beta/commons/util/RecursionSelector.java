package alex.beta.commons.util;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RecursionSelector<T> {

    private T[] all;

    public RecursionSelector(T[] all) {
        this.all = all;
    }

    public List<T[]> select(int count) {
        if (all == null || all.length == 0 || count <= 0) {
            return Collections.emptyList();
        }

        LinkedList<T[]> resultSet = new LinkedList<T[]>();

        if (count >= all.length) {
            resultSet.add(all);
        } else {
            recursionSub(resultSet, count, all, 0, -1);
        }
        return resultSet;
    }

    @SuppressWarnings("unchecked")
    private void recursionSub(List<T[]> list, int count, T[] array, int ind, int start, int... indexs) {
        start++;
        if (start > count - 1) {
            return;
        }
        if (start == 0) {
            indexs = new int[array.length];
        }
        for (indexs[start] = ind; indexs[start] < array.length; indexs[start]++) {
            recursionSub(list, count, array, indexs[start] + 1, start, indexs);
            if (start == count - 1) {
                T[] temp = (T[]) Array.newInstance(array.getClass().getComponentType(), count);
                for (int i = count - 1; i >= 0; i--) {
                    temp[start - i] = array[indexs[start - i]];
                }
                list.add(temp);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        BigDecimal[] all = new BigDecimal[10];
        for (int i = 1; i <= 10; i++) {
            all[i - 1] = BigDecimal.ONE.divide(BigDecimal.valueOf(i), 5, RoundingMode.HALF_UP);
        }
        RecursionSelector<BigDecimal> rs = new RecursionSelector<>(all);

        List<BigDecimal[]> result = new LinkedList<BigDecimal[]>();
//        result.addAll(select(rs, 1));
//        result.addAll(select(rs, 3));
//        result.addAll(select(rs, 5));
//        result.addAll(select(rs, 7));
//        result.addAll(select(rs, 9));

        result.addAll(select(rs, 2));
        result.addAll(select(rs, 4));
        result.addAll(select(rs, 6));
        result.addAll(select(rs, 8));
        result.addAll(select(rs, 10));

        BigDecimal value = new BigDecimal(0);
        for (BigDecimal[] tmp : result) {
            value = value.add(calculate(tmp));
        }
        System.out.println(value.setScale(5, RoundingMode.HALF_UP));
    }

    private static List<BigDecimal[]> select(RecursionSelector<BigDecimal> rs, int count) {
        return rs.select(count);
    }

    private static BigDecimal calculate(BigDecimal[] a) {
        BigDecimal result = new BigDecimal(1);
        for (BigDecimal t : a) {
            result = result.multiply(t);
        }
        return result;
    }

}
