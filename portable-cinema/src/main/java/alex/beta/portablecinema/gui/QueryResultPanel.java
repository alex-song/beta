package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.*;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import static alex.beta.portablecinema.command.EditCommand.resultText;
import static alex.beta.portablecinema.gui.PortableCinemaFrame.*;
import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.HAND_CURSOR;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * https://docs.oracle.com/javase/tutorial/uiswing/components/table.html
 */
public class QueryResultPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(QueryResultPanel.class);

    private static final String[] columnNames = new String[]{"序号", "预览", "编辑", "详细", "片名", "片长", "标签", "分辨率"};
    private static final int[] columnWidths = new int[]{45, 28, 28, 28, 390, 120, 300, 120};
    private static final boolean[] resizableColumns = new boolean[]{false, false, false, false, true, false, true, false};

    private PortableCinemaFrame frame;
    private PortableCinemaConfig config;
    private String queryOption;
    private String initialInput;

    private ImageIcon previewIcon;
    private ImageIcon editIcon;
    private ImageIcon detailIcon;
    private JComboBox<String> queryOptions;
    private JTextField userInputField;
    private JButton refreshBtn;
    private JFormattedTextField jumpToField;
    private JButton jumpToBtn;
    private JTable fileInfoTable;
    private AbstractTableModel fileInfoTableModel;
    private FileInfo[] fileInfos;

    public QueryResultPanel(PortableCinemaFrame frame, PortableCinemaConfig config, String queryOption, String userInput, FileInfo... fileInfos) {
        super(new BorderLayout());
        this.frame = frame;
        this.config = config;
        this.queryOption = queryOption;
        this.initialInput = userInput;
        this.fileInfos = fileInfos;

        this.setSize(1000, 700);
        this.setPreferredSize(new Dimension(1000, 700));

        createUIComponents();
        enableUIActions();
        initModelData();
    }

    public static void showDialog(PortableCinemaFrame frame, PortableCinemaConfig config, String queryOption, String userInput) {
        QueryResultPanel qrp = new QueryResultPanel(frame, config, queryOption, userInput);
        JOptionPane jop = new JOptionPane(qrp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, null, null);
        JDialog dialog = new JDialog(frame, TITLE + " - 查询", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(jop);
        dialog.setBounds(frame.getBounds());
        dialog.setLocationRelativeTo(null);
        jop.addPropertyChangeListener(evt -> {
            if (JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())
                    && (evt.getNewValue().equals(JOptionPane.DEFAULT_OPTION) || evt.getNewValue().equals(JOptionPane.OK_OPTION))) {
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
        dialog.dispose();
    }

    private void createUIComponents() {
        // load resources
        try {
            previewIcon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("images/Preview-icon.png")).getScaledInstance(15, 15, Image.SCALE_SMOOTH));
            editIcon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("images/Edit-icon.png")).getScaledInstance(15, 15, Image.SCALE_SMOOTH));
            detailIcon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("images/Detail-icon.png")).getScaledInstance(15, 15, Image.SCALE_SMOOTH));
        } catch (Exception ex) {
            logger.error("Failed to load preview/edit icon", ex);
            return;
        }
        // render UI
        add(initTopPanel(), BorderLayout.PAGE_START);
        add(new JScrollPane(initResultTable()), BorderLayout.CENTER);
    }

    private JPanel initTopPanel() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        queryOptions = new JComboBox<>(new String[]{NAME_ACTION, TAG_ACTION, WHERE_ACTION});
        queryOptions.setEditable(false);
        topPanel.add(queryOptions, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userInputField = new JTextField();
        topPanel.add(userInputField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        refreshBtn = new JButton("刷新");
        refreshBtn.setEnabled(false);
        topPanel.add(refreshBtn, gbc);

        NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance()) {
            @Override
            public Object stringToValue(String s) throws ParseException {
                if (isBlank(s)) {
                    return null;
                } else
                    return super.stringToValue(s);
            }
        };
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(fileInfos == null ? 0 : 1);
        formatter.setMaximum(fileInfos == null ? 0 : fileInfos.length);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        jumpToField = new JFormattedTextField(formatter);
        jumpToField.setHorizontalAlignment(SwingConstants.TRAILING);
        jumpToField.setPreferredSize(new Dimension(100, 24));
        gbc.gridx = 3;
        gbc.gridy = 0;
        topPanel.add(jumpToField, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        jumpToBtn = new JButton("跳转");
        jumpToBtn.setEnabled(false);
        topPanel.add(jumpToBtn, gbc);

        return topPanel;
    }

    private JTable initResultTable() {
        // init table model
        fileInfoTable = new RollOverTable(initTableModel());
        // column columnWidths
        for (int i = 0; i < fileInfoTableModel.getColumnCount(); i++) {
            if (!resizableColumns[i]) {
                fileInfoTable.getColumnModel().getColumn(i).setMaxWidth(columnWidths[i]);
                fileInfoTable.getColumnModel().getColumn(i).setMinWidth(columnWidths[i]);
            }
            fileInfoTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            fileInfoTable.getColumnModel().getColumn(i).setResizable(resizableColumns[i]);
        }
        return fileInfoTable;
    }

    private AbstractTableModel initTableModel() {
        fileInfoTableModel = new AbstractTableModel() {
            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public int getRowCount() {
                return fileInfos == null ? 0 : fileInfos.length;
            }

            @Override
            public Class getColumnClass(int col) {
                switch (col) {
                    case 0:
                        return Number.class;
                    case 1:
                    case 2:
                    case 3:
                        return Icon.class;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    default:
                        return String.class;
                }
            }

            @Override
            public String getColumnName(int col) {
                return col < 8 ? columnNames[col] : null;
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (row >= getRowCount()) {
                    return null;
                }
                FileInfo fileInfo = fileInfos[row];
                switch (col) {
                    case 0:
                        return row + 1;
                    case 1:
                        if (isNotBlank(fileInfo.getCover1()) || isNotBlank(fileInfo.getCover2()))
                            return previewIcon;
                        else
                            return null;
                    case 2:
                        return editIcon;
                    case 3:
                        return detailIcon;
                    case 4:
                        return fileInfo.getName();
                    case 5:
                        return fileInfo.getFormattedDuration();
                    case 6:
                        if (fileInfo.getTags() != null && !fileInfo.getTags().isEmpty())
                            return join(fileInfo.getTags(), ", ");
                        else
                            return null;
                    case 7:
                        if (fileInfo.getResolution() != null)
                            return fileInfo.getResolution().toString();
                        else
                            return null;
                    default:
                        return null;
                }
            }
        };
        return fileInfoTableModel;
    }

    private void enableUIActions() {
        // mouse click and cursor
        fileInfoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fileInfoTable.getSelectedRow();
                int col = fileInfoTable.getSelectedColumn();

                if (fileInfos == null || row < 0 || row >= fileInfos.length || col >= fileInfoTableModel.getColumnCount())
                    return;

                FileInfo fileInfo = fileInfos[row];
                if (SwingUtilities.isLeftMouseButton(e) && col == 1 && (isNotBlank(fileInfo.getCover1()) || isNotBlank(fileInfo.getCover2()))) {
                    if (logger.isDebugEnabled())
                        logger.debug("Open preview dialog of {}", fileInfo);
                    if (PreviewPanel.showDialog(frame, fileInfo, config)) {
                        fileInfos[row] = new ViewCommand(fileInfo.getOtid()).execute(config);
                        fileInfoTableModel.fireTableRowsUpdated(row, row);
                    }
                } else if (SwingUtilities.isLeftMouseButton(e) && col == 2) {
                    if (logger.isDebugEnabled())
                        logger.debug("Open edit dialog of {}", fileInfo);
                    if (FileInfoEditPanel.showDialog(frame, fileInfo)) {
                        int result = new EditCommand(fileInfo).execute(config);
                        if (logger.isDebugEnabled())
                            logger.debug("Update file info [{}], result is [{}]", fileInfo, result);
                        JOptionPane.showMessageDialog(frame, resultText(result), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                        // update table UI
                        fileInfos[row] = new ViewCommand(fileInfo.getOtid()).execute(config);
                        fileInfoTableModel.fireTableRowsUpdated(row, row);
                    }
                } else if (SwingUtilities.isLeftMouseButton(e) && col == 3) {
                    if (logger.isDebugEnabled())
                        logger.debug("Open view detail dialog of {}", fileInfo);
                    JOptionPane.showMessageDialog(frame, fileInfo.toPrettyString(), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                } else if (SwingUtilities.isLeftMouseButton(e) && col == 4 && e.getClickCount() == 2) {
                    if (logger.isDebugEnabled())
                        logger.debug("Open folder of [{}]", fileInfo);
                    try {
                        if (Desktop.isDesktopSupported())
                            Desktop.getDesktop().open(new File(fileInfo.getPath()));
                        else
                            JOptionPane.showMessageDialog(frame, fileInfo.getPath(), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        logger.warn("Cannot open folder [{}]", fileInfo.getPath(), ex);
                    }
                }
            }
        });

        fileInfoTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = fileInfoTable.rowAtPoint(e.getPoint());
                int column = fileInfoTable.columnAtPoint(e.getPoint());
                if (fileInfos == null || row >= fileInfos.length || row < 0) return;
                FileInfo fileInfo = fileInfos[row];
                if (column == 1 && (isNotBlank(fileInfo.getCover1()) || isNotBlank(fileInfo.getCover2()))) {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
                } else if (column == 2 || column == 3) {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
                } else {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(DEFAULT_CURSOR));
                }
            }
        });

        fileInfoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && fileInfos != null) {
                int row = fileInfoTable.getSelectedRow();
                SwingUtilities.invokeLater(() -> jumpToField.setText(String.valueOf(row + 1)));
            }
        });

        fileInfoTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && fileInfos != null
                        && fileInfoTable.getSelectedRow() >= 0 && fileInfoTable.getSelectedRow() < fileInfos.length) {
                    int row = fileInfoTable.getSelectedRow();
                    FileInfo fileInfo = fileInfos[row];
                    if (isNotBlank(fileInfo.getCover1()) || isNotBlank(fileInfo.getCover2())) {
                        if (logger.isDebugEnabled())
                            logger.debug("Open preview dialog of {}", fileInfo);
                        if (PreviewPanel.showDialog(frame, fileInfo, config)) {
                            fileInfos[row] = new ViewCommand(fileInfo.getOtid()).execute(config);
                            fileInfoTableModel.fireTableRowsUpdated(row, row);
                        }
                    }
                } else
                    super.keyPressed(e);
            }
        });

        userInputField.addKeyListener(newEnterKeyListener(refreshBtn));

        refreshBtn.addKeyListener(newEnterKeyListener(refreshBtn));

        refreshBtn.addActionListener(e -> {
            String inputValue = userInputField.getText();
            if ((NAME_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem()) && isBlank(inputValue))
                    || (WHERE_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem()) && isBlank(inputValue))) {
                JOptionPane.showMessageDialog(this, "请输入查询条件", TITLE, JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
            } else
                executeQuery();
        });

        jumpToField.addKeyListener(newEnterKeyListener(jumpToBtn));

        jumpToBtn.addActionListener(e -> {
            if (fileInfos == null || fileInfos.length == 0) return;
            int jumpToRow = 0;
            try {
                jumpToRow = Integer.parseInt(jumpToField.getText());
            } catch (Exception ex) {
                logger.debug("Jump to row value must be an integer", ex);
            }
            if (jumpToRow <= 0 || jumpToRow > fileInfos.length) {
                fileInfoTable.clearSelection();
            } else {
                fileInfoTable.setRowSelectionInterval(jumpToRow - 1, jumpToRow - 1);
                fileInfoTable.scrollRectToVisible(new Rectangle(fileInfoTable.getCellRect(jumpToRow - 1, 0, true)));
            }
        });

        jumpToBtn.addKeyListener(newEnterKeyListener(jumpToBtn));
    }

    private KeyListener newEnterKeyListener(@NonNull JButton clickBtn) {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (clickBtn.isEnabled())
                        clickBtn.doClick();
                    e.consume();
                } else
                    super.keyPressed(e);
            }
        };
    }

    private void initModelData() {
        if (queryOption != null)
            queryOptions.setSelectedItem(queryOption);
        if (initialInput != null) {
            userInputField.setText(initialInput);
        }
        executeQuery();
    }

    private void executeQuery() {
        if (config != null)
            new SwingWorker<Void, FileInfo[]>() {
                @Override
                protected Void doInBackground() {
                    publish(doQuery());
                    return null;
                }

                @Override
                protected void process(List<FileInfo[]> chunks) {
                    if (chunks != null && !chunks.isEmpty()) {
                        fileInfos = chunks.get(0);
                        fileInfoTableModel.fireTableDataChanged();

                        jumpToField.setText("");
                        ((NumberFormatter) jumpToField.getFormatter()).setMinimum(fileInfos == null ? 0 : 1);
                        ((NumberFormatter) jumpToField.getFormatter()).setMaximum(fileInfos == null ? 0 : fileInfos.length);
                    }
                }

                @Override
                protected void done() {
                    jumpToBtn.setEnabled(true);
                    refreshBtn.setEnabled(true);
                }

                private FileInfo[] doQuery() {
                    String inputValue = userInputField.getText();
                    if (NAME_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem())
                            && isNotBlank(inputValue)) {
                        return new NameCommand(inputValue.trim()).execute(config);
                    } else if (TAG_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem())) {
                        return new TagCommand(split(inputValue, ",")).execute(config);
                    } else if (WHERE_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem())
                            && isNotBlank(inputValue)) {
                        return new WhereCommand(inputValue).execute(config);
                    }
                    return new FileInfo[]{};
                }
            }.execute();
        else {
            jumpToBtn.setEnabled(true);
            refreshBtn.setEnabled(true);
        }
    }
}
