package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.ImageCache;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.filesystem.FileSystemUtils;
import alex.beta.portablecinema.pojo.FileInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.awt.Image.SCALE_SMOOTH;
import static org.apache.commons.lang3.StringUtils.*;

@SuppressWarnings({"squid:S1948", "squid:S3776"})
public class FileInfoEditPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(FileInfoEditPanel.class);

    private final PortableCinemaConfig config;
    private final ImageIcon imageSelectorBtnIcon;
    private final ImageIcon imageDeletionBtnIcon;
    private NumberComboBox widthField;
    private NumberComboBox heightField;
    private NumberComboBox hoursField;
    private NumberComboBox minsField;
    private NumberComboBox secondsField;
    private ImagePanel cover1Panel;
    private ImagePanel cover2Panel;
    private JTextArea tagsField;
    private JCheckBox manualOverrideCheck;

    private FileInfo fileInfo;

    public FileInfoEditPanel(PortableCinemaConfig config, FileInfo fileInfo) {
        super(new GridBagLayout());
        this.config = config;
        this.fileInfo = fileInfo;

        imageSelectorBtnIcon = new ImageIcon(ImageCache.getCache().getImage("images/Preview-icon_1.png", 20, 20, SCALE_SMOOTH));
        imageDeletionBtnIcon = new ImageIcon(ImageCache.getCache().getImage("images/Delete-icon.png", 20, 20, SCALE_SMOOTH));

        createUIComponents();
    }

    /**
     * @param owner
     * @param fileInfo
     * @return true, if there is any update on the file info
     */
    public static boolean showDialog(PortableCinemaConfig config, Component owner, FileInfo fileInfo) {
        FileInfoEditPanel panel = new FileInfoEditPanel(config, fileInfo);
        boolean isChanged = false;
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(owner,
                panel,
                fileInfo.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            isChanged = panel.populateFileInfo();
        }
        return isChanged;
    }

    private boolean populateFileInfo() {
        boolean isChanged = false;

        int newWidth = 0;
        int newHeight = 0;
        int newHours = 0;
        int newMins = 0;
        int newSeconds = 0;
        String newTagsText = StringUtils.trim(this.tagsField.getText());

        int iv = this.widthField.getValue();
        if (iv >= 0) {
            newWidth = iv;
        }

        iv = this.heightField.getValue();
        if (iv >= 0) {
            newHeight = iv;
        }

        //check and update resolution
        if ((newHeight != 0 && fileInfo.getResolution() == null) || (newHeight != fileInfo.getResolution().getHeight())) {
            isChanged = true;
            FileInfo.Resolution r = fileInfo.getResolution();
            if (r == null) {
                r = new FileInfo.Resolution();
            }
            r.setHeight(newHeight);
            fileInfo.setResolution(r);
        }

        if ((newWidth != 0 && fileInfo.getResolution() == null) || (newWidth != fileInfo.getResolution().getWidth())) {
            isChanged = true;
            FileInfo.Resolution r = fileInfo.getResolution();
            if (r == null) {
                r = new FileInfo.Resolution();
            }
            r.setWidth(newWidth);
            fileInfo.setResolution(r);
        }

        iv = this.hoursField.getValue();
        if (iv >= 0) {
            newHours = iv;
        }

        iv = this.minsField.getValue();
        if (iv >= 0) {
            newMins = iv;
        }

        iv = this.secondsField.getValue();
        if (iv >= 0) {
            newSeconds = iv;
        }

        //check and update duration
        long newDuration = FileInfo.toSeconds(newHours, newMins, newSeconds);
        if (newDuration != fileInfo.getDuration()) {
            isChanged = true;
            fileInfo.setDuration(newDuration);
        }

        //check and update manual override flag
        if (this.manualOverrideCheck.isSelected() != fileInfo.isManualOverride()) {
            fileInfo.setManualOverride(this.manualOverrideCheck.isSelected());
            isChanged = true;
        }

        // check and update cover images
        if (!StringUtils.equalsIgnoreCase(fileInfo.getCover1(), this.cover1Panel.getImageName())) {
            fileInfo.setCover1(this.cover1Panel.getImageName());
            isChanged = true;
        }
        if (!StringUtils.equalsIgnoreCase(fileInfo.getCover2(), this.cover2Panel.getImageName())) {
            fileInfo.setCover2(this.cover2Panel.getImageName());
            isChanged = true;
        }

        //check and update tags
        Set<String> newTags = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(newTagsText))) {
            String tagsLine;
            while ((tagsLine = reader.readLine()) != null)
                if (isNotBlank(tagsLine))
                    newTags.addAll(Arrays.stream(StringUtils.trim(tagsLine).split("[,，;；]")).map(StringUtils::trim).collect(Collectors.toSet()));
        } catch (IOException ex) {
            logger.error("Failed to read tags from UI", ex);
            newTags = null;
        }
        if (newTags != null) {
            //user input is valid
            boolean isTagsChanged = false;
            Set<String> tagsToPersist = new HashSet<>(newTags);
            Set<String> oldTags = fileInfo.getTags();
            if (oldTags == null) {
                oldTags = new HashSet<>();
            }
            if (newTags.size() != oldTags.size()) {
                isTagsChanged = true;
            }

            if (!isTagsChanged)
                for (String t : oldTags)
                    if (!newTags.contains(t)) {
                        isTagsChanged = true;
                        break;
                    }

            if (isTagsChanged) {
                fileInfo.setTags(tagsToPersist);
                isChanged = true;
            }
        }
        return isChanged;
    }

    private void createUIComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        add(new JLabel("影片分辨率（宽 * 高）："), gbc);

        JPanel resolutionPanel = new JPanel(new GridLayout(1, 2));
        widthField = new NumberComboBox(new Integer[]{1920, 1280, 960, 720, 640, 576, 512, 480, 0});
        widthField.setEditable(true);
        resolutionPanel.add(widthField);

        heightField = new NumberComboBox(new Integer[]{1080, 720, 540, 480, 432, 404, 396, 320, 0});
        heightField.setEditable(true);
        resolutionPanel.add(heightField);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(resolutionPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("影片长度（小时 : 分钟 : 秒）："), gbc);

        JPanel durationPanel = new JPanel(new GridLayout(1, 3));
        hoursField = new NumberComboBox(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
        durationPanel.add(hoursField);

        minsField = new NumberComboBox(0, 59, 1);
        durationPanel.add(minsField);

        secondsField = new NumberComboBox(0, 59, 1);
        durationPanel.add(secondsField);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(durationPanel, gbc);

        File folder = new File(isBlank(fileInfo.getPath()) ? "." : fileInfo.getPath());
        cover1Panel = new ImagePanel("封面图", fileInfo.getCover1(), folder);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 0);
        add(cover1Panel, gbc);

        cover2Panel = new ImagePanel("预览图", fileInfo.getCover2(), folder);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(cover2Panel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("标签（换行、逗号、分号分隔）："), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        tagsField = new JTextArea(10, 50);
        add(new JScrollPane(tagsField), gbc);

        manualOverrideCheck = new JCheckBox("手工编辑");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        add(manualOverrideCheck, gbc);

        initValues();
    }

    private void initValues() {
        if (fileInfo.getResolution() != null) {
            widthField.setSelectedItem(fileInfo.getResolution().getWidth());
            heightField.setSelectedItem(fileInfo.getResolution().getHeight());
        }
        hoursField.setSelectedItem((int) fileInfo.getDurationHoursPart());
        minsField.setSelectedItem(fileInfo.getDurationMinsPart());
        secondsField.setSelectedItem(fileInfo.getDurationSecondsPart());
        if (fileInfo.getTags() != null)
            tagsField.setText(StringUtils.join(fileInfo.getTags(), System.lineSeparator()));
        manualOverrideCheck.setSelected(fileInfo.isManualOverride());
    }

    private static class NumberComboBox extends JComboBox<Integer> {
        public NumberComboBox(Integer[] items) {
            super(items);
            ((JTextField) this.getEditor().getEditorComponent()).setHorizontalAlignment(SwingConstants.TRAILING);
            ((JTextField) this.getEditor().getEditorComponent()).setDocument(new PlainDocument() {
                @Override
                public void insertString(int offset, String text, AttributeSet attr) throws BadLocationException {
                    if (StringUtils.isNumeric(text))
                        super.insertString(offset, text, attr);
                }
            });
        }

        public NumberComboBox(int min, int max, int increment) {
            super();
            for (int i = min; i <= max; i += increment) {
                this.addItem(i);
            }
            ((JTextField) this.getEditor().getEditorComponent()).setHorizontalAlignment(SwingConstants.TRAILING);
            ((JTextField) this.getEditor().getEditorComponent()).setDocument(new PlainDocument() {
                @Override
                public void insertString(int offset, String text, AttributeSet attr) throws BadLocationException {
                    if (StringUtils.isNumeric(text)) {
                        int length = this.getLength();
                        if (offset > length) {
                            throw new BadLocationException("Invalid insert", length);
                        } else {
                            try {
                                String tmp = this.getText(0, offset) + text + this.getText(offset, length - offset);
                                int v = Integer.parseInt(tmp);
                                if (v >= min && v <= max)
                                    super.insertString(offset, text, attr);
                            } catch (Exception ex) {
                                logger.error("Failed to parse input value", ex);
                            }
                        }
                    }
                }
            });
        }

        public int getValue() {
            int value = -1;
            Object tmpO = this.getSelectedItem();
            if (tmpO != null && isNotBlank(tmpO.toString()) && isNumeric(tmpO.toString())) {
                value = Integer.parseInt(tmpO.toString().trim());
            }
            return value;
        }
    }

    private class ImagePanel extends JPanel {
        private String imageName;

        public ImagePanel(String text, String name, File folder) {
            super(new GridBagLayout());
            this.imageName = name;
            JLabel label = new JLabel(text + " ：");
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            this.add(label, gbc);

            JLabel imageLabel = new JLabel(toDisplay(this.imageName));
            imageLabel.setToolTipText(this.imageName);
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.add(imageLabel, gbc);

            JButton imageDeletionBtn = new JButton(imageDeletionBtnIcon);
            imageDeletionBtn.setToolTipText("重置" + text);
            imageDeletionBtn.addActionListener(e -> {
                this.imageName = null;
                imageLabel.setText(this.imageName);
                imageLabel.setToolTipText(this.imageName);
            });
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            this.add(imageDeletionBtn, gbc);

            JButton imageSelectorBtn = new JButton(imageSelectorBtnIcon);
            imageSelectorBtn.setToolTipText("设置" + text);
            if (isBlank(config.getImageFileExtensions())) {
                imageSelectorBtn.setEnabled(false);
            } else {
                imageSelectorBtn.addActionListener(e -> {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("设置" + text);
                    fileChooser.setMultiSelectionEnabled(false);

                    if (isNotBlank(config.getImageFileExtensions())
                            && !"*".equalsIgnoreCase(trimToEmpty(config.getImageFileExtensions()))) {
                        String[] imageExts = Arrays.stream(split(config.getImageFileExtensions(), "\\,"))
                                .filter(StringUtils::isNotBlank).map(String::trim).toArray(String[]::new);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("图片文件", imageExts));
                    }

                    if (isNotBlank(this.imageName)) {
                        fileChooser.setSelectedFile(new File(folder, this.imageName));
                    } else {
                        fileChooser.setCurrentDirectory(folder);
                    }

                    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                            && fileChooser.getSelectedFile() != null) {
                        File imageFile = fileChooser.getSelectedFile();
                        if (!FileSystemUtils.isImageFile(config, imageFile)) {
                            return;
                        }
                        this.imageName = imageFile.getName();
                        imageLabel.setText(toDisplay(this.imageName));
                        imageLabel.setToolTipText(this.imageName);
                    }
                });
            }
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.weightx = 0;
            this.add(imageSelectorBtn, gbc);
        }

        public String getImageName() {
            return this.imageName;
        }

        private String toDisplay(String text) {
            if (text == null || text.length() < 50) {
                return text;
            } else {
                return text.substring(0, 18) + "......" + text.substring(text.length() - 18);
            }
        }
    }
}
