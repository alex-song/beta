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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static alex.beta.portablecinema.command.EditCommand.resultText;

public class HyperlinkActionHandler extends MouseAdapter {

    private static Logger logger = LoggerFactory.getLogger(HyperlinkActionHandler.class);

    private PortableCinemaConfig config;

    private PortableCinemaFrame frame;

    public HyperlinkActionHandler(PortableCinemaConfig config) {
        this.config = config;
    }

    public void setFrame(PortableCinemaFrame frame) {
        this.frame = frame;
    }

    @Override
    @SuppressWarnings({"squid:S1135", "squid:S3776"})
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
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
                            PreviewPanel.showDialog(frame, fileInfo, config);
                        } else if (href.startsWith("edit://")) {
                            String otid = href.substring(7);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            if (FileInfoEditPanel.showDialog(frame, fileInfo)) {
                                int result = new EditCommand(fileInfo).execute(config);
                                logger.debug("Update file info [{}], result is [{}]", fileInfo, result);
                                JOptionPane.showMessageDialog(frame, resultText(result), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                            }
                        } else if (href.startsWith("fileinfo://")) {
                            String otid = href.substring(11);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            JOptionPane.showMessageDialog(frame, fileInfo.toPrettyString(), fileInfo.getName(), JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                        } else if (href.startsWith("otid://")) {
                            String otid = href.substring(7);
                            FileInfo fileInfo = new ViewCommand(otid).execute(config);
                            try {
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().open(new File(fileInfo.getPath()));
                                else
                                    JOptionPane.showMessageDialog(frame, fileInfo.getPath(), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE);
                            } catch (Exception ex) {
                                logger.warn("Cannot open video folder [{}]", fileInfo.getPath(), ex);
                            }
                        } else {
                            try {
                                if (Desktop.isDesktopSupported())
                                    Desktop.getDesktop().browse(new URI(href));
                                else
                                    JOptionPane.showMessageDialog(frame, href, href, JOptionPane.PLAIN_MESSAGE);
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
