package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
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

import static alex.beta.portablecinema.command.EditCommand.*;
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
        if ((event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1 && isNotBlank(currentImg))
                && (TagSuggestionPanel.showDialog(previewPanel, ocrClient, currentImg))) {
            FileInfo fileInfo = previewPanel.getFileInfo();
            int result = new EditCommand(fileInfo).execute(config);
            logger.debug("Update tags of file info [{}], result is [{}]", fileInfo, result);
            String msg = "标签编辑成功";
            if (result == UPDATE_SUCCESS) {
                //TODO - Refresh ui after tags edit
            } else if (result == DATABASE_UPDATE_ERROR) {
                msg = "数据库更新失败";
            } else if (result == DB_FILE_NOT_EXIST_ERROR) {
                msg = "数据文件不存在";
            } else if (result == DB_FILE_UPDATE_ERROR) {
                msg = "数据文件更新失败";
            } else {
                //NO_UPDATE
                msg = "标签编辑失败";
            }
            JOptionPane.showMessageDialog(previewPanel, msg, fileInfo.getName(), JOptionPane.PLAIN_MESSAGE);
        }
    }
}
