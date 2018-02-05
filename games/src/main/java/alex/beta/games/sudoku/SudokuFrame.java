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
public class SudokuFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(SudokuFrame.class);

    private JButton restartBtn;
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
    private SudokuEngine engine;

    private boolean customizedGame;
    private boolean shownResult;
    private boolean gameStarted;
    private boolean gameStopped;
    //private boolean gamePaused;

    private int[][] initialData;

    public SudokuFrame() {
        setTitle("数独游戏");
        createUIComponents();
        createUIActions();
    }

    public static void main(String[] args) {
        SudokuFrame frame = new SudokuFrame();
        frame.setSize(600, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @SuppressWarnings("deprecation")
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

        countingField = new JLabel("00:00:00 000");
        countingField.setFont(new Font("宋体", Font.BOLD, 20));
        countingField.setPreferredSize(new Dimension(150, 30));
        countingField.setMinimumSize(new Dimension(150, 30));
        toolbarPanel.add(countingField);

        startBtn = new JButton("开始");
        startBtn.setToolTipText("开始游戏并计时");
        startBtn.setMargin(new Insets(5, 0, 5, 0));
        toolbarPanel.add(startBtn);

        restartBtn = new JButton("重新开始");
        restartBtn.setToolTipText("重新开始这局数独游戏");
        restartBtn.setMargin(new Insets(5, 0, 5, 0));
        toolbarPanel.add(restartBtn);

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
                positionTextField(k, n, subGridPanels, textFields[k][n]);      //添加文本框
            }
        }
        textFields[8][8].setNextFocusableComponent(textFields[0][0]);
    }


    //---------private methods----------

    private void createUIActions() {
        //Menu actions
        quickStartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted && !gameStopped) {
                    return;
                }
                String inputValue = "18";
                do {
                    inputValue = JOptionPane.showInputDialog("请输入显示的数字个数(9 - 45)：", inputValue);
                    logger.debug("Input string is {}", inputValue);
                }
                while (!inputValue.matches("\\d+") || Integer.parseInt(inputValue) < 9 || Integer.parseInt(inputValue) > 45);
                //初始化游戏引擎
                engine = new SudokuEngine();
                initialData = new int[9][9];
                engine.setTip(Integer.parseInt(inputValue));
                countingField.setText("00:00:00 000");
                boolean canSolve = true;
                do {
                    engine.genSudo();
                    engine.printSudo();
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
                //初始化九宫格
                setInitialData();
                //初始化系统变量
                customizedGame = false;
                gameStarted = false;
                gameStopped = true;
                //gamePaused = false;
                shownResult = false;
                //初始化计数器
                countingField.setText("00:00:00 000");
                //初始化按钮
                startBtn.setEnabled(true);
            }
        });

        customizedMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                setInitialData();
                textFields[0][0].requestFocusInWindow();
                customizedGame = true;
                gameStarted = false;
                gameStopped = true;
                //gamePaused = false;
                shownResult = false;
                countingField.setText("00:00:00 000");
                startBtn.setEnabled(true);
            }
        });

        showMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shownResult || engine == null) {
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
                startBtn.setEnabled(false);
            }
        });

        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStopped = true;
                System.exit(0);
            }
        });

        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(getContentPane(), "作者：Alex Song \n E-mail: song_liping@hotmail.com");
            }
        });

        //Toolbar buttons
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

                if (customizedGame) {
                    for (int k = 0; k < 9; k++) {
                        for (int n = 0; n < 9; n++) {
                            initialData[k][n] = textFields[k][n].getInputValue();
                        }
                    }
                    engine.setData(initialData);

                    if (engine.solveSudo(false)) {
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
                        engine.printSudo();
                    } else {
                        JOptionPane.showMessageDialog(getContentPane(), "无解，请修改游戏");
                        return;
                    }
                }
                //初始化计时器
                countingField.setText("00:00:00 000");
                //启动计时程序
                Thread countingThread = new Thread() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        do {
                            countingField.setText(formatMilliseconds(System.currentTimeMillis() - startTime));

                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                                logger.error("Counter thread is interrupted", ex);
                                countingField.setText("99:99:99 999");
                            }
                        } while (!gameStopped);
                    }
                };
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
            private final String formatMilliseconds(long elapsed) {
                int hour, minute, second, milli;

                milli = (int) (elapsed % 1000);
                elapsed = elapsed / 1000;

                second = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                minute = (int) (elapsed % 60);
                elapsed = elapsed / 60;

                hour = (int) (elapsed % 60);

                return String.format("%02d:%02d:%02d %03d", hour, minute, second, milli);
            }
        });

        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameStopped = true;

                SudokuValidator.SudokuValidationMessages messages = SudokuValidator.getInstance().validate(textFields);
                if (messages.isPassed()) {
                    JOptionPane.showMessageDialog(getContentPane(), "恭喜，你在" + countingField.getText() + "完成了本局数独游戏");
                } else {
                    StringBuilder sb = new StringBuilder("错误信息:\n");
                    for (int i = 0; i < messages.getMessages().size() && i < 5; i++) {
                        sb.append(messages.getMessages().get(i).getMessage());
                    }
                    if (messages.getMessages().size() > 5) {
                        sb.append("\n还有" + (messages.getMessages().size() - 5) + "个错误等待修正......");
                    }
                    JOptionPane.showMessageDialog(getContentPane(), sb.toString());
                }

            }
        });
    }

    /**
     * @param k             行
     * @param n             列
     * @param subGridPanels 子托盘数组
     * @param textField     数字框
     */
    private void positionTextField(int k, int n, JPanel[] subGridPanels, JTextField textField) {
        if (k < 3) {
            if (n < 3) {
                subGridPanels[0].add(textField);
            } else if (n < 6) {
                subGridPanels[1].add(textField);
            } else {
                subGridPanels[2].add(textField);
            }
        } else if (k < 6) {
            if (n < 3) {
                subGridPanels[3].add(textField);
            } else if (n < 6) {
                subGridPanels[4].add(textField);
            } else {
                subGridPanels[5].add(textField);
            }
        } else {
            if (n < 3) {
                subGridPanels[6].add(textField);
            } else if (n < 6) {
                subGridPanels[7].add(textField);
            } else {
                subGridPanels[8].add(textField);
            }
        }
    }

    /**
     * 显示游戏初始值
     */
    private void setInitialData() {
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

    private void cacheInitialData(int[][] data) {
        this.initialData = new int[9][9];
        for (int k = 0; k < 9; k++) {
            for (int n = 0; n < 9; n++) {
                this.initialData[k][n] = data[k][n];
            }
        }
    }
}
