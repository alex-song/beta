package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
import alex.beta.portablecinema.command.ViewCommand;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.tag.TagService;
import alex.beta.simpleocr.baidu.BaiduOcr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static alex.beta.portablecinema.command.EditCommand.resultText;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class HyperlinkActionHandler extends MouseAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HyperlinkActionHandler.class);

    private final PortableCinemaConfig config;

    private PortableCinemaFrame frame;

    public HyperlinkActionHandler(PortableCinemaConfig config) {
        this.config = config;
    }

    public void setFrame(PortableCinemaFrame frame) {
        this.frame = frame;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Element h = getHyperlinkElement(e);
        if (h != null) {
            Object attribute = h.getAttributes().getAttribute(HTML.Tag.A);
            if (attribute instanceof AttributeSet) {
                AttributeSet set = (AttributeSet) attribute;
                String title = (String) set.getAttribute(HTML.Attribute.TITLE);
                if (isEmpty(title)) {
                    title = (String) set.getAttribute(HTML.Attribute.ALT);
                }
                if (isNotEmpty(title)) {
                    frame.getResultPane().setToolTipText(title);
                } else {
                    frame.getResultPane().setToolTipText(null);
                }
            }
        } else {
            frame.getResultPane().setToolTipText(null);
        }
    }

    @Override
    @SuppressWarnings({"squid:S1135", "squid:S3776"})
    public void mouseClicked(MouseEvent e) {
        long priorTimestamp = System.currentTimeMillis();
        if (config.isEnablePerformanceLog()) logger.info("Mouse clicked on {}", priorTimestamp);
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (config.isEnablePerformanceLog()) {
                logger.info("It takes {}ms to identify the button", System.currentTimeMillis() - priorTimestamp);
                priorTimestamp = System.currentTimeMillis();
            }
            Element h = getHyperlinkElement(e);
            if (config.isEnablePerformanceLog()) {
                logger.info("It takes {}ms to identify the HTML elment", System.currentTimeMillis() - priorTimestamp);
                priorTimestamp = System.currentTimeMillis();
            }
            if (h != null) {
                Object attribute = h.getAttributes().getAttribute(HTML.Tag.A);
                if (config.isEnablePerformanceLog()) {
                    logger.info("It takes {}ms to identify the hyperlink tag", System.currentTimeMillis() - priorTimestamp);
                    priorTimestamp = System.currentTimeMillis();
                }
                if (attribute instanceof AttributeSet) {
                    AttributeSet set = (AttributeSet) attribute;
                    String href = (String) set.getAttribute(HTML.Attribute.HREF);
                    if (href != null) {
                        if (config.isEnablePerformanceLog()) {
                            logger.info("It takes {}ms to extract href content", System.currentTimeMillis() - priorTimestamp);
//                            priorTimestamp = System.currentTimeMillis();
                        }
                        logger.debug("hyper link::{}", href);
                        FileInfo fileInfo;
                        if (href.startsWith("tags://add/")) {
                            String tag = new String(Base64.getDecoder().decode(href.substring(11)), StandardCharsets.UTF_8);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Add {} into glossary", tag);
                            }
                            Object[] options = new Object[]{"取消", "演员", "分类", "出品人", "其他"};
                            boolean result;
                            switch (JOptionPane.showOptionDialog(frame, "添加 " + tag + " 到？", "更新关键字库",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, frame.logo50Icon, options, options[0])) {
                                case 1:
                                    result = TagService.getInstance(config).addActors(tag);
                                    break;
                                case 2:
                                    result = TagService.getInstance(config).addCategories(tag);
                                    break;
                                case 3:
                                    result = TagService.getInstance(config).addProducers(tag);
                                    break;
                                case 4:
                                    result = TagService.getInstance(config).addOthers(tag);
                                    break;
                                default:
                                    return;
                            }
                            JOptionPane.showMessageDialog(frame, tag + (result ? "添加成功" : "已存在，或添加失败"), "更新关键字库", JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                        } else if (href.startsWith("preview://")) {
                            fileInfo = new ViewCommand(href.substring(10)).execute(config);
                            PreviewPanel.showDialog(config, frame, fileInfo,
                                    config.getBaiduOCR() == null ? null : new BaiduOcr(config.getBaiduOCR().toProperties()));
                        } else if (href.startsWith("edit://")) {
                            fileInfo = new ViewCommand(href.substring(7)).execute(config);
                            if (FileInfoEditPanel.showDialog(config, frame, fileInfo)) {
                                int result = new EditCommand(fileInfo).execute(config);
                                if (logger.isDebugEnabled())
                                    logger.debug("Update file info [{}], result is [{}]", fileInfo, result);
                                JOptionPane.showMessageDialog(frame, resultText(result), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                            }
                        } else if (href.startsWith("fileinfo://")) {
                            fileInfo = new ViewCommand(href.substring(11)).execute(config);
                            JOptionPane.showMessageDialog(frame, fileInfo.toPrettyString(), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                        } else if (href.startsWith("folder://")) {
                            fileInfo = new ViewCommand(href.substring(9)).execute(config);
                            try {
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(new File(fileInfo.getPath()));
                                else
                                    JOptionPane.showMessageDialog(frame, fileInfo.getPath(), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, frame.logo50Icon);
                            } catch (Exception ex) {
                                logger.warn("Cannot open folder [{}]", fileInfo.getPath(), ex);
                            }
                        } else if (href.startsWith("otid://")) {
                            fileInfo = new ViewCommand(href.substring(7)).execute(config);
                            try {
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(new File(fileInfo.getPath(), fileInfo.getName()));
                                else
                                    JOptionPane.showMessageDialog(frame, fileInfo.getPath(), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, frame.logo50Icon);
                            } catch (Exception ex) {
                                logger.warn("Cannot open video [{}/{}]", fileInfo.getPath(), fileInfo.getName(), ex);
                            }
                        } else {
                            try {
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().browse(new URI(href));
                                else
                                    JOptionPane.showMessageDialog(frame, href, href, JOptionPane.PLAIN_MESSAGE, frame.logo50Icon);
                            } catch (IOException | URISyntaxException ex) {
                                logger.warn("Cannot open URI [{}]", href, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "squid:S1874"}) //TO be compatible with Java 8
    private Element getHyperlinkElement(MouseEvent event) {
        JEditorPane editor = frame.getResultPane();
        int pos = editor.getUI().viewToModel(editor, event.getPoint());
        if (pos >= 0 && editor.getDocument() instanceof HTMLDocument) {
            HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
            Element elem = hdoc.getCharacterElement(pos);
            if (elem.getAttributes().getAttribute(HTML.Tag.A) != null) {
                return elem;
            }
        }
        return null;
    }
}
