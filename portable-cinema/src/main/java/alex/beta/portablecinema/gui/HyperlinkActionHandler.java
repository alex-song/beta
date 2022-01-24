package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
import alex.beta.portablecinema.command.ViewCommand;
import alex.beta.portablecinema.pojo.FileInfo;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static alex.beta.portablecinema.command.EditCommand.*;

public class HyperlinkActionHandler extends MouseAdapter {

    private static Logger logger = LoggerFactory.getLogger(HyperlinkActionHandler.class);

    private PortableCinemaConfig config;

    public HyperlinkActionHandler(PortableCinemaConfig config) {
        this.config = config;
    }

    @Override
    @SuppressWarnings({"squid:S1135", "squid:S3776"})
    public void mouseClicked(MouseEvent e) {
        PortableCinemaFrame frame = (PortableCinemaFrame) ((JTextPane) e.getSource()).getRootPane().getParent();

        if (e.getButton() == MouseEvent.BUTTON1) {
            Element h = getHyperlinkElement(e);
            if (h != null) {
                Object attribute = h.getAttributes().getAttribute(HTML.Tag.A);
                if (attribute instanceof AttributeSet) {
                    AttributeSet set = (AttributeSet) attribute;
                    String href = (String) set.getAttribute(HTML.Attribute.HREF);
                    if (href != null) {
                        logger.debug("hyper link::{}", href);
                        if (href.startsWith("preview://")) {
                            String otid = href.substring(10);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            PreviewPanel.showDialog(frame, fileInfo, 800, 700);
                        } else if (href.startsWith("edit://")) {
                            String otid = href.substring(7);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            if (FileInfoEditPanel.showDialog(frame, fileInfo)) {
                                int result = new EditCommand(fileInfo).execute(config);
                                logger.debug("Update file info [{}], result is [{}]", fileInfo, result);
                                String msg = "编辑成功";
                                if (result == UPDATE_SUCCESS) {
                                    //TODO - Refresh ui after file info edit
                                } else if (result == DATABASE_UPDATE_ERROR) {
                                    msg = "数据库更新失败";
                                } else if (result == DB_FILE_NOT_EXIST_ERROR) {
                                    msg = "数据文件不存在";
                                } else if (result == DB_FILE_UPDATE_ERROR) {
                                    msg = "数据文件更新失败";
                                } else {
                                    //NO_UPDATE
                                    msg = "编辑失败";
                                }
                                JOptionPane.showMessageDialog(frame, msg, fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else if (href.startsWith("fileinfo://")) {
                            String otid = href.substring(11);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            JOptionPane.showMessageDialog(frame, fileInfo.toPrettyString(), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                        } else {
                            try {
                                Desktop.getDesktop().browse(new URI(href));
                            } catch (IOException | URISyntaxException ex) {
                                logger.warn("Cannot open video folder [{}]", href, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"deprecation", "squid:S1874"}) //TO be compatible with Java 8
    private Element getHyperlinkElement(MouseEvent event) {
        JEditorPane editor = (JEditorPane) event.getSource();
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
