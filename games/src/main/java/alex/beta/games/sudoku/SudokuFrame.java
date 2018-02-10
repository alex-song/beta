/**
 * <p>
 * File Name: SudokuFrame.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/5 下午11:01
 * </p>
 */
package alex.beta.games.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author alexsong
 * @version ${project.version}
 */
@SuppressWarnings("squid:S3776")
public class SudokuFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(SudokuFrame.class);

    private static final String INITIAL_COUNTING_TEXT = "00:00:00 000";
    private static final String INVALID_DATA = "无解，请修改游戏";

    //Toolbar buttons/field
    private JButton startBtn;
    private JButton submitBtn;
    private JLabel countingField;

    //Customized UI code
    private SudokuField[][] textFields;
    private JMenuItem aboutMenuItem;
    private JMenuItem quickStartMenuItem;
    private JMenuItem customizedMenuItem;
    private JMenuItem showMenuItem;
    private JMenuItem exitMenuItem;

    //Controller
    private transient SudokuEngine engine;

    private boolean customizedGame;
    private boolean shownResult;
    private boolean gameStarted;
    private boolean gameStopped;

    private int[][] initialData;

    public SudokuFrame() {
        setTitle("数独游戏");
        createUIComponents();
        createUIActions();
    }

    public static void main(String[] args) {
        SudokuFrame frame = new SudokuFrame();
        frame.setSize(600, 900);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"})
    private void createUIComponents() {
        //Menu
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("游戏");
        quickStartMenuItem = new JMenuItem("快速开始");
        quickStartMenuItem.setToolTipText("随机生成一局数独游戏");
        gameMenu.add(quickStartMenuItem);

        customizedMenuItem = new JMenuItem("定制游戏");
        customizedMenuItem.setToolTipText("自己定制一局数独游戏");
        gameMenu.add(customizedMenuItem);

        showMenuItem = new JMenuItem("显示结果");
        showMenuItem.setToolTipText("显示当前游戏结果，并停止计时");
        gameMenu.add(showMenuItem);

        gameMenu.addSeparator();

        exitMenuItem = new JMenuItem("退出");
        exitMenuItem.setToolTipText("退出游戏");
        gameMenu.add(exitMenuItem);
        menuBar.add(gameMenu);

        JMenu helpMenu = new JMenu("帮助");
        aboutMenuItem = new JMenuItem("关于");
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        //ContentPane
        getContentPane().setLayout(new BorderLayout(5, 5));

        //Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout());
        getContentPane().add(toolbarPanel, BorderLayout.NORTH);

        countingField = new JLabel(INITIAL_COUNTING_TEXT);
        countingField.setFont(new Font("宋体", Font.BOLD, 20));
        countingField.setPreferredSize(new Dimension(150, 30));
        countingField.setMinimumSize(new Dimension(150, 30));
        toolbarPanel.add(countingField);

        startBtn = new JButton("开始");
        startBtn.setToolTipText("开始游戏并计时");
        startBtn.setMargin(new Insets(5, 0, 5, 0));
        toolbarPanel.add(startBtn);

        submitBtn = new JButton("提交");
        submitBtn.setToolTipText("提交答案，并结束计时");
        submitBtn.setMargin(new Insets(5, 0, 5, 0));
        toolbarPanel.add(submitBtn);

        //Content grid
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 15, 15));
        getContentPane().add(gridPanel, BorderLayout.CENTER);

        JPanel[] subGridPanels = new JPanel[9];
        for (int i = 0; i < 9; i++) {
            subGridPanels[i] = new JPanel(new GridLayout(3, 3, 3, 3));
            gridPanel.add(subGridPanels[i]);
        }
        textFields = new SudokuField[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                textFields[k][n] = new SudokuField();
                textFields[k][n].setEditable(false);         //只可显示不可修改
                subGridPanels[(k / 3) * 3 + n / 3].add(textFields[k][n]);   //添加文本框
            }
        }
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                if (k == 0) {
                    textFields[0][n].setUpComponent(null);
                    textFields[0][n].setDownComponent(textFields[1][n]);
                } else if (k == 8) {
                    textFields[8][n].setUpComponent(textFields[7][n]);
                    textFields[8][n].setDownComponent(null);
                } else {
                    textFields[k][n].setUpComponent(textFields[k - 1][n]);
                    textFields[k][n].setDownComponent(textFields[k + 1][n]);
                }
                if (n == 0) {
                    textFields[k][0].setLeftComponent(null);
                    textFields[k][0].setRightComponent(textFields[k][1]);
                } else if (n == 8) {
                    textFields[k][8].setLeftComponent(textFields[k][7]);
                    textFields[k][8].setRightComponent(null);
                } else {
                    textFields[k][n].setLeftComponent(textFields[k][n - 1]);
                    textFields[k][n].setRightComponent(textFields[k][n + 1]);
                }
            }
        }
        textFields[8][8].setNextFocusableComponent(textFields[0][0]);
    }


    //---------private methods----------
    private void createUIActions() {
        //Menu actions
        quickStartMenuItem.addActionListener(e -> {
            if (gameStarted && !gameStopped) {
                return;
            }
            String inputValue = "18";
            do {
                inputValue = JOptionPane.showInputDialog("请输入显示的数字个数(9 - 45)：", inputValue);
                logger.debug("Input string is {}", inputValue);
            }
            while (inputValue != null && (!inputValue.matches("\\d+") || Integer.parseInt(inputValue) < 9 || Integer.parseInt(inputValue) > 45));
            if (inputValue == null) {
                return;
            }
            //初始化游戏引擎
            engine = new SudokuEngine();
            initialData = new int[9][9];
            engine.setTip(Integer.parseInt(inputValue));
            countingField.setText(INITIAL_COUNTING_TEXT);
            boolean canSolve = true;
            do {
                engine.genSudo();
                engine.printSudo();//打印游戏
                cacheInitialData(engine.getData());

                canSolve = engine.solveSudo(false);
                if (canSolve) {
                    int[][] resultData = engine.getData();
                    for (int k = 0; k < 9; k++) {
                        for (int n = 0; n < 9; n++) {
                            textFields[k][n].setSuggestedValue(resultData[k][n]);
                        }
                    }
                }
            } while (!canSolve);
            engine.printSudo();//打印游戏结果
            //初始化九宫格
            diaplayInitialData();
            //初始化系统变量
            customizedGame = false;
            gameStarted = false;
            gameStopped = true;
            //gamePaused = false;
            shownResult = false;
            //初始化计数器
            countingField.setText(INITIAL_COUNTING_TEXT);
            //初始化按钮
            startBtn.setEnabled(true);
        });

        customizedMenuItem.addActionListener(e -> {
            if (gameStarted && !gameStopped) {
                return;
            }
            //初始化游戏引擎
            engine = new SudokuEngine();
            initialData = new int[9][9];
            for (int k = 0; k < 9; k++) {
                for (int n = 0; n < 9; n++) {
                    initialData[k][n] = 0;
                }
            }
            diaplayInitialData();
            textFields[0][0].requestFocusInWindow();
            customizedGame = true;
            gameStarted = false;
            gameStopped = true;
            //gamePaused = false;
            shownResult = false;
            countingField.setText(INITIAL_COUNTING_TEXT);
            startBtn.setEnabled(true);
        });

        showMenuItem.addActionListener(e -> {
            if (shownResult || engine == null) {//还未生成游戏或者已经显示了结果
                JOptionPane.showMessageDialog(getContentPane(), "游戏还没有开始，或已经结束\n请从菜单中选择游戏方式");
                return;
            }
            if (!gameStarted && customizedGame && validateSudokuData()) {//还未开始并且已经生成了游戏，并且是定制游戏，并且无解
                shownResult = true;
                gameStopped = true;
                engine = null;
                startBtn.setEnabled(false);
                return;
            }
            for (int k = 0; k < 9; k++) {
                for (int n = 0; n < 9; n++) {
                    if (textFields[k][n].isSame()) {
                        textFields[k][n].showSuggestion(Color.BLACK, null, false);
                    } else {
                        textFields[k][n].showSuggestion(Color.RED, null, false);
                    }
                }
            }
            shownResult = true;
            gameStopped = true;
            engine = null;
            startBtn.setEnabled(false);
        });

        exitMenuItem.addActionListener(e -> {
            gameStopped = true;
            engine = null;
            System.exit(0);
        });

        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(getContentPane(), "作者：Alex Song \n E-mail: song_liping@hotmail.com"));

        //Toolbar actions
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (engine == null) {
                    JOptionPane.showMessageDialog(getContentPane(), "请从菜单中选择游戏方式");
                    return;
                }
                if (gameStarted && !gameStopped) {
                    //已有游戏在进行
                    return;
                }

                if (customizedGame && validateSudokuData()) {//定制游戏，并且无解
                    return;
                }
                //初始化计时器
                countingField.setText(INITIAL_COUNTING_TEXT);
                //启动计时程序
                Thread countingThread = new Thread(() -> {
                    long startTime = System.currentTimeMillis();
                    do {
                        countingField.setText(formatMilliseconds(System.currentTimeMillis() - startTime));

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            logger.error("Counter thread is interrupted", ex);
                            countingField.setText("99:99:99 999");
                            Thread.currentThread().interrupt();
                        }
                    } while (!gameStopped);
                });
                countingThread.setDaemon(true);
                countingThread.start();
                //初始化控制变量
                gameStarted = true;
                gameStopped = false;
                shownResult = false;
                startBtn.setEnabled(false);
            }

            /**
             * 将毫秒数格式化
             */
            private String formatMilliseconds(long elapsed) {
                int milli = (int) (elapsed % 1000);
                elapsed = elapsed / 1000;

                int second = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                int minute = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                int hour = (int) (elapsed % 60);

                return String.format("%02d:%02d:%02d %03d", hour, minute, second, milli);
            }
        });

        submitBtn.addActionListener(e -> {
            if (engine == null) {
                JOptionPane.showMessageDialog(getContentPane(), "游戏还没有开始，或已经结束\n请从菜单中选择游戏方式");
                return;
            }
            gameStopped = true;

            SudokuValidator.SudokuValidationMessages messages = SudokuValidator.getInstance().validate(textFields);
            if (messages.isPassed()) {
                JOptionPane.showMessageDialog(getContentPane(), "恭喜，你在" + countingField.getText() + "完成了本局数独游戏");
            } else {
                StringBuilder sb = new StringBuilder("错误信息:\n\n");
                for (int i = 0; i < messages.getMessages().size() && i < 5; i++) {
                    sb.append(messages.getMessages().get(i).getMessage());
                }
                if (messages.getMessages().size() > 5) {
                    sb.append(String.format("还有%d个错误等待修正......", messages.getMessages().size() - 5));
                }
                JOptionPane.showMessageDialog(getContentPane(), sb.toString());
            }

        });
    }

    /**
     * 显示游戏初始值
     */
    private void diaplayInitialData() {
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                textFields[k][n].setInputValue(this.initialData[k][n]);
                if (this.initialData[k][n] != 0) {
                    textFields[k][n].showInput(Color.BLACK, Color.GRAY, false);
                } else {
                    textFields[k][n].showInput(Color.BLACK, Color.WHITE, true);
                }
            }
        }
    }

    /**
     * 缓存游戏初始值
     *
     * @param data
     */
    private void cacheInitialData(int[][] data) {
        this.initialData = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                this.initialData[k][n] = data[k][n];
            }
        }
    }

    /**
     * 验算数独游戏
     *
     * @return true, if there is no answer
     */
    private boolean validateSudokuData() {
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                initialData[k][n] = textFields[k][n].getInputValue();
            }
        }
        engine.setData(initialData);
        engine.printSudo();//打印游戏

        if (SudokuValidator.getInstance().preview(initialData) && engine.solveSudo(false)) {
            int[][] resultData = engine.getData();
            for (int k = 0; k < 9; k++) {
                for (int n = 0; n < 9; n++) {
                    textFields[k][n].setSuggestedValue(resultData[k][n]);
                    if (initialData[k][n] != 0) {
                        textFields[k][n].showInput(Color.BLACK, Color.GRAY, false);
                    } else {
                        textFields[k][n].showInput(Color.BLACK, Color.WHITE, true);
                    }
                }
            }
            engine.printSudo();//打印游戏结果
            return false;
        } else {
            JOptionPane.showMessageDialog(getContentPane(), INVALID_DATA);
            return true;
        }
    }
}