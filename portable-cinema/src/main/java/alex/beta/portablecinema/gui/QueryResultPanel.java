package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.*;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
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

public class QueryResultPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(QueryResultPanel.class);

    private static final Font HEADER_FONT = new Font(Font.SERIF, Font.PLAIN, 12);
    private static final Font TABLE_FONT = new Font(Font.SERIF, Font.PLAIN, 12);
    private static final String[] COLUMN_NAMES = new String[]{"序号", "预览", "编辑", "详细", "片名", "片长", "标签", "分辨率"};
    private static final String[] COLUMN_HEADER_TOOLTIPS = new String[]{"序号", "浏览封面图", "编辑影片信息", "查看详细信息", "片名", "影片时长", "标签", "影片分辨率"};
    private static final int[] COLUMN_WIDTHS = new int[]{45, 28, 28, 28, 390, 120, 300, 120};
    private static final boolean[] COLUMN_RESIZABLE = new boolean[]{false, false, false, false, true, false, true, false};

    private PortableCinemaFrame frame;
    private PortableCinemaConfig config;
    private String initialOption;
    private String initialInput;

    private ImageIcon previewIcon;
    private ImageIcon editIcon;
    private ImageIcon detailIcon;
    private ImageIcon hdIcon;
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
        this.initialOption = queryOption;
        this.initialInput = userInput;
        this.fileInfos = fileInfos;
        //init UI
        createUIComponents();
        enableUIActions();
        // init data
        if (initialOption != null)
            queryOptions.setSelectedItem(initialOption);
        if (initialInput != null)
            userInputField.setText(initialInput);
        executeQuery(initialOption, trimToEmpty(initialInput));
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
            hdIcon = new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("images/HD-icon.png")).getScaledInstance(15, 15, Image.SCALE_SMOOTH));
        } catch (Exception ex) {
            logger.error("Failed to load preview/edit icon", ex);
            return;
        }
        // create UI
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
                return isBlank(s) ? null : super.stringToValue(s);
            }

            @Override
            public Class getValueClass() {
                return Integer.class;
            }

            @Override
            public boolean getAllowsInvalid() {
                return false;
            }

            @Override
            public boolean getCommitsOnValidEdit() {
                /**
                 * If you want the value to be committed on each keystroke instead of focus lost
                 */
                return true;
            }
        };
        formatter.setMinimum(fileInfos == null ? 0 : 1);
        formatter.setMaximum(fileInfos == null ? 0 : fileInfos.length);
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
        add(topPanel, BorderLayout.PAGE_START);

        fileInfoTableModel = new QueryTableModel();
        fileInfoTable = new QueryResultTable(fileInfoTableModel);
        add(new JScrollPane(fileInfoTable), BorderLayout.CENTER);
    }

    private void enableUIActions() {
        /**
         * Customize mouse click actions
         */
        fileInfoTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fileInfoTable.getSelectedRow();
                int col = fileInfoTable.getSelectedColumn();

                if (fileInfos == null || row < 0 || row >= fileInfos.length || col >= fileInfoTableModel.getColumnCount())
                    return;

                FileInfo fileInfo = fileInfos[row];
                if (SwingUtilities.isLeftMouseButton(e) && col == 1 && fileInfo.hasCover()) {
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

        /**
         * Customize mouse cursor
         */
        fileInfoTable.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = fileInfoTable.rowAtPoint(e.getPoint());
                int column = fileInfoTable.columnAtPoint(e.getPoint());
                if (fileInfos == null || row >= fileInfos.length || row < 0) return;
                FileInfo fileInfo = fileInfos[row];
                if (column == 1 && fileInfo.hasCover()) {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
                } else if (column == 2 || column == 3) {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(HAND_CURSOR));
                } else {
                    fileInfoTable.setCursor(Cursor.getPredefinedCursor(DEFAULT_CURSOR));
                }
            }
        });

        /**
         * Set jump to value, on row selection
         */
        fileInfoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && fileInfos != null) {
                int row = fileInfoTable.getSelectedRow();
                SwingUtilities.invokeLater(() -> jumpToField.setText(String.valueOf(row + 1)));
            }
        });

        /**
         * Open preview dialog, on key ENTER
         */
        fileInfoTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && fileInfos != null
                        && fileInfoTable.getSelectedRow() >= 0 && fileInfoTable.getSelectedRow() < fileInfos.length) {
                    int row = fileInfoTable.getSelectedRow();
                    FileInfo fileInfo = fileInfos[row];
                    if (fileInfo.hasCover()) {
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

        /**
         * Click refresh button on key ENTER, when user input field is focused
         */
        userInputField.addKeyListener(newEnterKeyListener(refreshBtn));

        /**
         * Click refresh button on key ENTER, when refresh button is focused
         */
        refreshBtn.addKeyListener(newEnterKeyListener(refreshBtn));

        /**
         * User input is mandatory, when query according to name or do advanced search
         * Do query according to given condition
         */
        refreshBtn.addActionListener(e -> {
            String inputValue = trimToEmpty(userInputField.getText());
            if ((NAME_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem()) && isBlank(inputValue))
                    || (WHERE_ACTION.equalsIgnoreCase((String) queryOptions.getSelectedItem()) && isBlank(inputValue)))
                JOptionPane.showMessageDialog(this, "请输入查询条件", TITLE, JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
            else
                executeQuery(String.valueOf(queryOptions.getSelectedItem()), inputValue);
        });

        /**
         * Click jump to button on key ENTER, when jump to field is focused
         */
        jumpToField.addKeyListener(newEnterKeyListener(jumpToBtn));

        /**
         * Click jump to button on key ENTER, when jump to button is focused
         */
        jumpToBtn.addKeyListener(newEnterKeyListener(jumpToBtn));

        /**
         * Select specific row and jump to it
         */
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

    private void executeQuery(final String option, final String inputValue) {
        if (config != null)
            new SwingWorker<Void, FileInfo[]>() {
                @Override
                protected Void doInBackground() {
                    if (NAME_ACTION.equalsIgnoreCase(option)
                            && isNotBlank(inputValue)) {
                        publish(new NameCommand(inputValue).execute(config));
                    } else if (TAG_ACTION.equalsIgnoreCase(option)) {
                        publish(new TagCommand(split(inputValue, ",")).execute(config));
                    } else if (WHERE_ACTION.equalsIgnoreCase(option)
                            && isNotBlank(inputValue)) {
                        publish(new WhereCommand(inputValue).execute(config));
                    }
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
            }.execute();
        else {
            jumpToBtn.setEnabled(true);
            refreshBtn.setEnabled(true);
        }
    }

    public class QueryTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
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
                    return String.class;
                case 7:
                    return FileInfo.Resolution.class;
                default:
                    return String.class;
            }
        }

        @Override
        public String getColumnName(int col) {
            return col < getColumnCount() ? COLUMN_NAMES[col] : null;
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
                    if (fileInfo.hasCover())
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
                    if (fileInfo != null)
                        return fileInfo.getResolution();
                    else
                        return null;
                default:
                    return null;
            }
        }
    }

    /**
     * Add tooltip support
     * Disable reordering of column
     * Set font of header and cell
     */
    @SuppressWarnings({"squid:S110"})
    public class QueryResultTable extends RollOverTable {
        private static final String TOOLTIP_FORMAT = "<html>%s<br/>更新时间：%s</html>";

        public QueryResultTable(TableModel model) {
            super(model);
            // customize table
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getColumnModel().setColumnSelectionAllowed(false);
            // customize header
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            // column columnWidths
            for (int i = 0; i < model.getColumnCount(); i++) {
                if (!COLUMN_RESIZABLE[i]) {
                    getColumnModel().getColumn(i).setMaxWidth(COLUMN_WIDTHS[i]);
                    getColumnModel().getColumn(i).setMinWidth(COLUMN_WIDTHS[i]);
                }
                getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
                getColumnModel().getColumn(i).setResizable(COLUMN_RESIZABLE[i]);
            }
            // customize resolution column renderer
            setDefaultRenderer(FileInfo.Resolution.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                    JLabel r = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                    if (value != null && value instanceof FileInfo.Resolution) {
                        r.setHorizontalTextPosition(SwingConstants.LEADING);
                        r.setHorizontalAlignment(SwingConstants.CENTER);
                        r.setIcon(((FileInfo.Resolution) value).isHD() ? hdIcon : null);
                        r.setText(value.toString());
                    }
                    return r;
                }
            });
        }

        @Override
        public int getRowHeight() {
            return 20;
        }

        @Override
        public Font getFont() {
            return TABLE_FONT;
        }

        @Override
        public boolean getDragEnabled() {
            return false;
        }

        @Override
        protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
                private Dimension headerPreferredSize = new Dimension(1, 24);

                @Override
                public String getToolTipText(MouseEvent e) {
                    if (e != null) {
                        /**
                         * 如果考虑列的拖动，那就需要计算model里面real index
                         * columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getModelIndex()
                         */
                        int realIndex = columnAtPoint(e.getPoint());
                        if (realIndex >= 0 && realIndex < COLUMN_HEADER_TOOLTIPS.length)
                            return COLUMN_HEADER_TOOLTIPS[realIndex];
                    }
                    return null;
                }

                @Override
                public boolean getReorderingAllowed() {
                    return false;
                }

                @Override
                public Color getBackground() {
                    return Color.GRAY;
                }

                @Override
                public Color getForeground() {
                    return Color.WHITE;
                }

                @Override
                public Dimension getPreferredSize() {
                    return headerPreferredSize;
                }

                @Override
                public Font getFont() {
                    return HEADER_FONT;
                }
            };
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            String tip = null;
            if (e != null) {
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                if (colIndex != 1 && colIndex != 2 && colIndex != 3)
                    try {
                        Object v = getValueAt(rowIndex, colIndex);
                        tip = (v == null ? null : v.toString());
                        if (colIndex == 4)
                            if (fileInfos[rowIndex].getLastModifiedOn() != null) {
                                tip = String.format(TOOLTIP_FORMAT, tip, DateFormatUtils.format(fileInfos[rowIndex].getLastModifiedOn(), PortableCinemaConfig.DATE_FORMATTER));
                            } else {
                                tip = String.format(TOOLTIP_FORMAT, tip, "N/A");
                            }
                    } catch (Exception ex) {
                        //catch null pointer exception if mouse is over an empty line
                    }
            }
            return tip;
        }
    }
}
