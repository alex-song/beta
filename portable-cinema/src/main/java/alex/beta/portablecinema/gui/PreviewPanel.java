package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
import alex.beta.portablecinema.command.ViewCommand;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.simpleocr.baidu.BaiduOcr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.util.Properties;

import static alex.beta.portablecinema.command.EditCommand.UPDATE_SUCCESS;
import static alex.beta.portablecinema.command.EditCommand.resultText;
import static alex.beta.portablecinema.gui.TagSuggestionPanel.*;
import static alex.beta.simpleocr.OcrFactory.PROXY_HOST;
import static alex.beta.simpleocr.OcrFactory.PROXY_PORT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SuppressWarnings({"squid:S1948", "squid:S3776"})
public class PreviewPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
    private final PortableCinemaConfig config;
    private FileInfo fileInfo;
    private BaiduOcr ocrClient;
    private boolean isUpdated = false;

    private JTabbedPane tabbedPane;
    private CoverImagePanel coverImagePanel;
    private PlayerPanel playerPanel;

    public PreviewPanel(PortableCinemaConfig config, FileInfo fileInfo, int width, int height) {
        super(new BorderLayout());
        this.config = config;
        this.fileInfo = fileInfo;
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));

        createUIComponents();

        if (playerPanel != null || coverImagePanel != null) {
            PortableCinemaConfig.BaiduOCR ocrConfig = config.getBaiduOCR();
            if (ocrConfig != null && isNotBlank(ocrConfig.getAppId())) {
                initOcrClient();
            } else {
                ocrClient = null;
            }

            // short cut key to OCR, ctrl+R
            if (ocrClient != null)
                registerKeyboardAction(ae -> doOCR(), KeyStroke.getKeyStroke("ctrl R"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
    }

    /**
     * @param owner
     * @param fileInfo
     * @param config
     * @return true, if file info is updated
     */
    public static boolean showDialog(PortableCinemaConfig config, Frame owner, FileInfo fileInfo) {
        PreviewPanel pp = new PreviewPanel(config, fileInfo, 800, 700);
        Object[] options;
        if (pp.ocrClient != null) {
            options = new Object[]{"确定", "文字识别(Ctrl + R)"};
        } else {
            options = new Object[]{"确定"};
        }
        JOptionPane jop = new JOptionPane(pp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = new JDialog(owner, fileInfo.getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(jop);
        jop.addPropertyChangeListener(evt -> {
            if (JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
                if (options[0].equals(evt.getNewValue()) || evt.getNewValue().equals(JOptionPane.DEFAULT_OPTION) || evt.getNewValue().equals(JOptionPane.OK_OPTION)) {
                    dialog.dispose();
                } else if (options.length > 1 && options[1].equals(evt.getNewValue()) && pp.isImageShown()) {
                    if (pp.ocrClient != null) {
                        pp.doOCR();
                    } else {
                        JOptionPane.showMessageDialog(pp, "请检查OCR程序配置", fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, null);
                    }
                }
            }
            jop.setValue(JOptionPane.UNINITIALIZED_VALUE);
        });
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        if (pp.playerPanel != null) {
            pp.playerPanel.close();
            pp.playerPanel = null;
        }
        if (pp.coverImagePanel != null) {
            pp.coverImagePanel.close();
            pp.coverImagePanel = null;
        }
        dialog.dispose();
        return pp.isUpdated;
    }

    private void createUIComponents() {
        tabbedPane = new JTabbedPane();
        if (fileInfo.hasCover()) {
            coverImagePanel = new CoverImagePanel(config, fileInfo, this.getWidth() - 20, this.getHeight() - 30);
            tabbedPane.add("预览图", coverImagePanel);
        }
        if (!fileInfo.isDecodeError()) {
            playerPanel = new PlayerPanel(config, fileInfo, this.getWidth() - 20, this.getHeight() - 30);
            tabbedPane.add("截图", playerPanel);
        }
        tabbedPane.setForeground(Color.BLACK);
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            private final Insets borderInsets = new Insets(0, 0, 0, 0);

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                // Do nothing
            }

            @Override
            protected Insets getContentBorderInsets(int tabPlacement) {
                return borderInsets;
            }
        });
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void doOCR() {
        int option = JOptionPane.DEFAULT_OPTION;
        if (isCoverImagePanelSelected()) {
            option = TagSuggestionPanel.showDialog(this.config, this, ocrClient, coverImagePanel.getCurrentImageName(), null);
        } else if (isPlayPanelSelected()) {
            option = TagSuggestionPanel.showDialog(this.config, this, ocrClient, playerPanel.getCurrentTimestamp(), playerPanel.toBytes());
        }
        if (option == SAVE_CHANGES_OPTION || option == SAVE_CHANGES_OPEN_EDITOR_OPTION) {
            int result = new EditCommand(fileInfo).execute(config);
            logger.debug("Update tags of file info [{}], result is [{}]", fileInfo, result);
            if (option == SAVE_CHANGES_OPTION)
                JOptionPane.showMessageDialog(this, resultText(result), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, null);
            else {
                //open editor
                showEditorDialog(fileInfo.getOtid());
            }
            this.isUpdated = true;
        } else if (option == DISCARD_CHANGES_OPEN_EDITOR_OPTION || option == NO_CHANGE_OPEN_EDITOR_OPTION) {
            //open editor
            this.isUpdated = showEditorDialog(fileInfo.getOtid());
        }
    }

    private boolean showEditorDialog(String otid) {
        boolean isUpdated2 = false;
        FileInfo fileInfoToEdit = new ViewCommand(otid).execute(config);
        if (FileInfoEditPanel.showDialog(config, this, fileInfoToEdit)) {
            int result = new EditCommand(fileInfoToEdit).execute(config);
            logger.debug("Update file info [{}], result is [{}]", fileInfoToEdit, result);
            if (result == UPDATE_SUCCESS) {
                this.fileInfo = fileInfoToEdit;
                isUpdated2 = true;
            }
            JOptionPane.showMessageDialog(this, resultText(result), fileInfoToEdit.getName(), JOptionPane.PLAIN_MESSAGE, null);
        }
        return isUpdated2;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    private void initOcrClient() {
        PortableCinemaConfig.BaiduOCR ocrConfig = config.getBaiduOCR();
        Properties ocrProps = new Properties();
        ocrProps.setProperty(BaiduOcr.BAIDU_API_KEY, ocrConfig.getApiKey());
        ocrProps.setProperty(BaiduOcr.BAIDU_SECRET_KEY, ocrConfig.getSecretKey());
        ocrProps.setProperty(BaiduOcr.BAIDU_APP_ID, ocrConfig.getAppId());
        if (isNotBlank(ocrConfig.getProxyHost())) {
            ocrProps.setProperty(PROXY_HOST, ocrConfig.getProxyHost());
            if (ocrConfig.getProxyPort() == 0) {
                ocrProps.setProperty(PROXY_PORT, "80");
            } else {
                ocrProps.setProperty(PROXY_PORT, String.valueOf(ocrConfig.getProxyPort()));
            }
        }
        this.ocrClient = new BaiduOcr(ocrProps);
    }

    private boolean isCoverImagePanelSelected() {
        if (coverImagePanel != null) {
            return tabbedPane.getSelectedComponent() == coverImagePanel;
        }
        return false;
    }

    private boolean isPlayPanelSelected() {
        if (playerPanel != null) {
            return tabbedPane.getSelectedComponent() == playerPanel;
        }
        return false;
    }

    private boolean isImageShown() {
        if (isCoverImagePanelSelected())
            return isNotBlank(coverImagePanel.getCurrentImageName());
        else
            return isPlayPanelSelected() && playerPanel.screenshot != null;
    }
}
