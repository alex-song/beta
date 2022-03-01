package alex.beta.portablecinema.gui;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static java.awt.Image.SCALE_SMOOTH;

@SuppressWarnings({"squid:S1948", "squid:S3776", "squid:S116", "squid:S117", "squid:S110"})
public class PortableCinemaFrame extends JFrame {

    public static final String TITLE = "移动电影院";
    public static final String ROOT_FOLDER_CHOOSE_ACTION = "开始";
    public static final String SCAN_ACTION = "索引";
    public static final String ANALYZE_ACTION = "分析";
    public static final String NAME_ACTION = "按名字查询";
    public static final String TAG_ACTION = "按标签查询";
    public static final String WHERE_ACTION = "高级查询";
    public static final String EXPORT_ACTION = "导出";
    public static final String RESET_ACTION = "重置";

    private static final Logger logger = LoggerFactory.getLogger(PortableCinemaFrame.class);

    Icon logo50Icon;
    private JButton rootChooserButton;
    private JButton scanButton;
    private JButton analyzeButton;
    private JButton findByNameButton;
    private JButton findByTagButton;
    private JButton findByWhereButton;
    private JButton exportButton;
    private JButton resetButton;
    private JLabel statusLabel;
    private JTextPane resultPane;
    private JScrollPane resultScrollPane;
    private Image LOGO_IMAGE;
    private Icon SEARCH_ICON;
    private Icon FOLDER_ICON;
    private Icon SCAN_ICON;
    private Icon ANALYZE_ICON;
    private Icon EXPORT_ICON;
    private Icon RESET_ICON;

    public PortableCinemaFrame() {
        super();
        //init UI
        createUIComponents();
        //load icons and templates
        loadResourcesLater();
    }

    private void createUIComponents() {
        String EMPTY_HTML_TEMPLATE;
        try {
            LOGO_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Logo_2.png"));
            EMPTY_HTML_TEMPLATE = Resources.asCharSource(Resources.getResource("templates/Empty.tpl"), StandardCharsets.UTF_8).read();
        } catch (Exception ex) {
            logger.error("Failed to load icon or template files", ex);
            UIManager.getLookAndFeel().provideErrorFeedback(this);
            return;
        }
        //Frame
        setTitle(TITLE);
        setIconImage(LOGO_IMAGE);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //Toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMargin(new Insets(1, 0, 5, 1));
        getContentPane().add(toolbar, BorderLayout.NORTH);

        //Buttons
        rootChooserButton = new JButton(ROOT_FOLDER_CHOOSE_ACTION);
        rootChooserButton.setToolTipText("电影文件所在的根目录");
        rootChooserButton.setMargin(new Insets(5, 10, 5, 10));
        toolbar.add(rootChooserButton);

        toolbar.add(Box.createHorizontalStrut(10));

        scanButton = new JButton(SCAN_ACTION);
        scanButton.setToolTipText("检索并更新电影信息");
        scanButton.setMargin(new Insets(5, 10, 5, 10));
        toolbar.add(scanButton);

        toolbar.add(Box.createHorizontalStrut(10));

        analyzeButton = new JButton(ANALYZE_ACTION);
        analyzeButton.setToolTipText("分析影库，查找重复影片");
        analyzeButton.setMargin(new Insets(5, 10, 5, 10));
        toolbar.add(analyzeButton);

        toolbar.addSeparator();

        JPanel commonActionButtonPanel = new JPanel();
        commonActionButtonPanel.setLayout(new BoxLayout(commonActionButtonPanel, BoxLayout.X_AXIS));

        findByNameButton = new JButton(NAME_ACTION);
        findByNameButton.setToolTipText("名字中包含指定关键字的电影");
        findByNameButton.setMargin(new Insets(5, 10, 5, 10));
        commonActionButtonPanel.add(findByNameButton);

        commonActionButtonPanel.add(Box.createHorizontalStrut(10));

        findByTagButton = new JButton(TAG_ACTION);
        findByTagButton.setToolTipText("带有指定标签的电影");
        findByTagButton.setMargin(new Insets(5, 10, 5, 10));
        commonActionButtonPanel.add(findByTagButton);

        commonActionButtonPanel.add(Box.createHorizontalStrut(10));

        findByWhereButton = new JButton(WHERE_ACTION);
        findByWhereButton.setToolTipText("满足指定查询条件的电影");
        findByWhereButton.setMargin(new Insets(5, 10, 5, 10));
        commonActionButtonPanel.add(findByWhereButton);
        toolbar.add(commonActionButtonPanel);

        toolbar.addSeparator();

        exportButton = new JButton(EXPORT_ACTION);
        exportButton.setToolTipText("导出电影信息（支持JDON/XLS/XLSX格式）");
        exportButton.setMargin(new Insets(5, 10, 5, 10));
        toolbar.add(exportButton);

        toolbar.add(Box.createHorizontalGlue());

        resetButton = new JButton(RESET_ACTION);
        resetButton.setToolTipText("删除影库中所有数据文件和数据库记录");
        resetButton.setMargin(new Insets(5, 10, 5, 10));
        toolbar.add(resetButton);

        //TextPane - Result
        resultPane = new JTextPane();
        resultPane.setContentType("text/html; charset=utf-8");
        resultPane.setText(EMPTY_HTML_TEMPLATE);
        resultPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        resultPane.setEditable(false);
//        resultPane.setDoubleBuffered(true);
        resultPane.setDragEnabled(false);
        resultScrollPane = new JScrollPane(resultPane);
        resultScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        resultScrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

        getContentPane().add(resultScrollPane, BorderLayout.CENTER);

        //Status bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setPreferredSize(new Dimension(this.getWidth(), 24));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel("Portable Cinema");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setIcon(new ImageIcon(LOGO_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH)));
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        //For the sake of performance
        tryToFixMemory();
    }

    private void loadResourcesLater() {
        SwingUtilities.invokeLater(() -> {
            Image FOLDER_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Scan-icon.png"));
            FOLDER_ICON = new ImageIcon(FOLDER_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            rootChooserButton.setIcon(FOLDER_ICON);

            Image SCAN_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Scan-icon.png"));
            SCAN_ICON = new ImageIcon(SCAN_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            scanButton.setIcon(SCAN_ICON);

            Image ANALYZE_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Analyze-icon.png"));
            ANALYZE_ICON = new ImageIcon(ANALYZE_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            analyzeButton.setIcon(ANALYZE_ICON);

            Image SEARCH_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Search-icon.png"));
            SEARCH_ICON = new ImageIcon(SEARCH_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            findByNameButton.setIcon(SEARCH_ICON);
            findByTagButton.setIcon(SEARCH_ICON);
            findByWhereButton.setIcon(SEARCH_ICON);

            Image EXPORT_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Export-icon.png"));
            EXPORT_ICON = new ImageIcon(EXPORT_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            exportButton.setIcon(EXPORT_ICON);

            Image RESET_IMAGE = Toolkit.getDefaultToolkit().createImage(this.getClass().getClassLoader().getResource("images/Reset-icon.png"));
            RESET_ICON = new ImageIcon(RESET_IMAGE.getScaledInstance(20, 20, SCALE_SMOOTH));
            resetButton.setIcon(RESET_ICON);

            logo50Icon = new ImageIcon(LOGO_IMAGE.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        });
    }

    /**
     * Try to fix memory leak of JTextPane
     */
    private void tryToFixMemory() {
        // https://stackoverflow.com/questions/30903003/how-to-prevent-memory-leak-in-jtextpane-setcaretpositionint
        // disable caret updates
        Caret caret = resultPane.getCaret();
        if (caret instanceof DefaultCaret) {
            if (logger.isDebugEnabled()) {
                logger.debug("For the sake of performance, and avoid memory leak, we disable Caret update");
            }
            ((DefaultCaret) caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        } // end if DefaultCaret

        // remove registered UndoableEditListeners if any
        Document doc = resultPane.getDocument();
        if (doc != null) {
            //http://java-sl.com/JEditorPanePerformance.html
            doc.putProperty("i18n", Boolean.FALSE);
            if (doc instanceof AbstractDocument) {
                UndoableEditListener[] undoListeners = ((AbstractDocument) doc).getUndoableEditListeners();
                if (undoListeners != null) {
                    for (UndoableEditListener undoListener : undoListeners) {
                        doc.removeUndoableEditListener(undoListener);
                    } // end for undoListener
                } // end if undoListeners
            } // end if AbstractDocument
        }
    } // end method tryToFixMemory

    JTextPane getResultPane() {
        return resultPane;
    }

    public void enableUIActions(ButtonActionHandler buttonActionHandler, HyperlinkActionHandler hyperlinkActionHandler) {
        //set frame in action handlers
        buttonActionHandler.setFrame(this);
        hyperlinkActionHandler.setFrame(this);
        //button action listener
        rootChooserButton.addActionListener(buttonActionHandler);
        scanButton.addActionListener(buttonActionHandler);
        analyzeButton.addActionListener(buttonActionHandler);
        findByNameButton.addActionListener(buttonActionHandler);
        findByTagButton.addActionListener(buttonActionHandler);
        findByWhereButton.addActionListener(buttonActionHandler);
        exportButton.addActionListener(buttonActionHandler);
        resetButton.addActionListener(buttonActionHandler);

        //hyperlink listener to handle  clicks on file info
        resultPane.addMouseListener(hyperlinkActionHandler);
    }

    public void setStatusText(String text) {
        statusLabel.setText(text);
    }

    public void setErrorStatusText(String text) {
        statusLabel.setText("<html><font color='red'>" + text + "</font></html>");
    }

    private void outputResultInternal(String content) {
        try {
            HTMLDocument doc = (HTMLDocument) resultPane.getStyledDocument();
            Element body = doc.getDefaultRootElement().getElement(1);
            doc.insertBeforeEnd(body, content);
        } catch (Exception ex) {
            logger.error("Failed to insert content into text pane", ex);
        }
    }

    private void appendResultText(String text, Exception ex, boolean newLine) {
        try {
            if (text == null) {
                outputResultInternal("NULL");
            } else {
                outputResultInternal(text);
            }
            if (ex != null) {
                String exMsg = "（" + ex.getMessage() + "）";
                outputResultInternal(exMsg);
            }
            if (newLine) {
                outputResultInternal("<br/>");
            }
        } finally {
            //auto scroll
            SwingUtilities.invokeLater(() -> {
                if (resultScrollPane != null) {
                    resultScrollPane.getVerticalScrollBar().setValue(resultScrollPane.getVerticalScrollBar().getMaximum());
                }
                if (false)
                    try {
                        HTMLDocument doc = (HTMLDocument) resultPane.getStyledDocument();
                        EditorKit kit = resultPane.getEditorKit();
                        StringWriter writer = new StringWriter();
                        kit.write(writer, doc, 0, doc.getLength());
                        System.out.println(writer.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            });
        }
    }

    public void appendResultText(String text) {
        appendResultText(text, null, true);
    }
}
