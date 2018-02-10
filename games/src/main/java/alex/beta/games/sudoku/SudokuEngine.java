/**
 * <p>
 * File Name: SudokuEngine.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/1/31 下午2:54
 * </p>
 */
package alex.beta.games.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * 参考 https://segmentfault.com/a/1190000004641936
 *
 * @author alexsong
 * @version ${project.version}
 */
public class SudokuEngine {
    private static final Logger logger = LoggerFactory.getLogger(SudokuEngine.class);

    private int[][] data = new int[9][9]; //muti_array
    private int lef; //the number of zero in array
    private int tip; //the number of nozero_digit in array

    /**
     * 构造函数
     * 初始化变量
     */
    public SudokuEngine() {
        lef = 0;
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                data[i][j] = 0;
            }
        }
    }

    public int[][] getData() {
        return data;
    }

    public void setData(int[][] data) {
        this.data = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                this.data[k][n] = data[k][n];
            }
        }
        lef = 81;
        tip = 0;
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                if (this.data[k][n] != 0) {
                    lef--;
                    tip++;
                }
            }
        }
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }

    /**
     * 生成数独
     * 方法：挖洞法
     */
    public void genSudo() {
        /*将1-9 9个数字放在二维数组中随机位置*/
        lef = 81 - 9;
        for (int i = 0; i < 9; ++i) {
            data[0][i] = i + 1;
        }
        /*打乱顺序*/
        for (int i = 0; i < 9; ++i) {
            int ta = new Random().nextInt(9);
            int tb = new Random().nextInt(9);
            int tem = data[0][ta];
            data[0][ta] = data[0][tb];
            data[0][tb] = tem;
        }
        for (int i = 0; i < 9; ++i) {
            int ta = new Random().nextInt(9);
            int tb = new Random().nextInt(9);
            int tem = data[0][i];
            data[0][i] = data[ta][tb];
            data[ta][tb] = tem;
        }
        /*通过9个数字求出一个可行解*/
        solveSudo(true);
        lef = 81 - tip;
        for (int i = 0; i < lef; ++i) {
            int ta = new Random().nextInt(9);
            int tb = new Random().nextInt(9);
            if (data[ta][tb] != 0)
                data[ta][tb] = 0;
            else
                i--;
        }
    }

    /**
     * 求解数独
     *
     * @param isGen 是生成数独矩阵
     * @return 是否有解的boolean标识
     */
    public boolean solveSudo(boolean isGen) {
        if (dfs()) {
            if (isGen)
                logger.info("Generate sudoku completed.");
            else
                logger.info("Solve completed.");
            return true;
        } else {
            logger.info("Error:There are no solution.");
            return false;
        }
    }

    /**
     * 输出数独数组
     */
    public void printSudo() {
        StringBuilder sb = new StringBuilder(System.lineSeparator());

        sb.append("----------------------");
        sb.append(System.lineSeparator());
        for (int i = 0; i < 9; ++i) {
            if (i == 3 || i == 6) {
                sb.append("----------------------");
                sb.append(System.lineSeparator());
            }
            for (int j = 0; j < 9; ++j) {
                if (j == 0 || j == 3 || j == 6) {
                    sb.append("|");
                }
                if (data[i][j] > 0) {
                    sb.append(data[i][j]);
                    sb.append(" ");
                } else
                    sb.append("* ");
            }
            sb.append("|");
            sb.append(System.lineSeparator());
        }
        sb.append("----------------------");
        sb.append(System.lineSeparator());
        if (logger.isInfoEnabled()) {
            logger.info(sb.toString());
        }
    }

    /**
     * 计算某格子的可填数字个数，即不确定度
     *
     * @param r
     * @param c
     * @param mark
     * @return 不确定度
     */
    private int calcount(int r, int c, int[] mark) {
        for (int ti = 0; ti < 10; ++ti)
            mark[ti] = 0;
        for (int i = 0; i < 9; ++i) {
            mark[data[i][c]] = 1;
            mark[data[r][i]] = 1;
        }
        int rs = (r / 3) * 3;
        int cs = (c / 3) * 3;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                mark[data[rs + i][cs + j]] = 1;
            }
        }
        int count = 0;
        for (int i = 1; i <= 9; ++i) {
            if (mark[i] == 0)
                count++;
        }
        return count;
    }

    /**
     * 供solve调用的深度优先搜索
     *
     * @return 是否有解的boolean标识
     */
    private boolean dfs() {
        if (lef == 0) {
            return true;
        }
        if (!SudokuValidator.getInstance().preview(data)) {
            return false;
        }
        int mincount = 10;
        int mini = 0;
        int minj = 0;
        int[] mark = new int[10];
        /*找到不确定度最小的格子*/
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (data[i][j] != 0) continue;

                int count = calcount(i, j, mark);
                if (count == 0) return false;
                if (count < mincount) {
                    mincount = count;
                    mini = i;
                    minj = j;
                }
            }
        }
        /*优先处理不确定度最小的格子*/
        calcount(mini, minj, mark);
        for (int i = 1; i <= 9; ++i) {
            if (mark[i] == 0) {
                data[mini][minj] = i;
                lef--;
                dfs();
                if (lef == 0) return true;
                data[mini][minj] = 0;//回溯法
                lef++;
            }
        }
        return true;
    }
}
