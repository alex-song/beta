package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
import alex.beta.portablecinema.command.ViewCommand;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.simpleocr.baidu.BaiduOcr;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import static alex.beta.portablecinema.command.EditCommand.resultText;
import static alex.beta.portablecinema.gui.TagSuggestionPanel.*;
import static alex.beta.simpleocr.OcrFactory.PROXY_HOST;
import static alex.beta.simpleocr.OcrFactory.PROXY_PORT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@NoArgsConstructor
public class ImageOcrActionHandler extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ImageOcrActionHandler.class);

    private PortableCinemaConfig config;
    private PreviewPanel previewPanel;
    private BaiduOcr ocrClient;

    public ImageOcrActionHandler(PortableCinemaConfig config, PreviewPanel previewPanel) {
        this.config = config;
        this.previewPanel = previewPanel;
        PortableCinemaConfig.BaiduOCR ocrConfig = config.getBaiduOCR();
        if (ocrConfig != null && isNotBlank(ocrConfig.getAppId())) {
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
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        final String currentImg = previewPanel.getCurrentImg();
        if ((event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event) && isNotBlank(currentImg))) {
            int option = TagSuggestionPanel.showDialog(previewPanel, ocrClient, currentImg);
            FileInfo fileInfo = previewPanel.getFileInfo();
            if (option == SAVE_CHANGES_OPTION || option == SAVE_CHANGES_OPEN_EDITOR_OPTION) {
                int result = new EditCommand(fileInfo).execute(config);
                logger.debug("Update tags of file info [{}], result is [{}]", fileInfo, result);
                if (option == SAVE_CHANGES_OPTION)
                    JOptionPane.showMessageDialog(previewPanel, resultText(result), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE);
                else {
                    //open editor
                    showEditorDialog(fileInfo.getOtid());
                }
            } else if (option == DISCARD_CHANGES_OPEN_EDITOR_OPTION || option == NO_CHANGE_OPEN_EDITOR_OPTION) {
                //open editor
                showEditorDialog(fileInfo.getOtid());
            }
        }
    }

    private void showEditorDialog(String otid) {
        FileInfo fileInfo = new ViewCommand(otid).execute(config);
        if (FileInfoEditPanel.showDialog(previewPanel, fileInfo)) {
            int result = new EditCommand(fileInfo).execute(config);
            logger.debug("Update file info [{}], result is [{}]", fileInfo, result);
            JOptionPane.showMessageDialog(previewPanel, resultText(result), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE);
        }
    }
}
