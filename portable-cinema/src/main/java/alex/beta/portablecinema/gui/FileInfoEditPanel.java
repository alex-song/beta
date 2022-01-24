package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.pojo.FileInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"squid:S1948", "squid:S3776"})
public class FileInfoEditPanel extends JPanel {

    private static Logger logger = LoggerFactory.getLogger(FileInfoEditPanel.class);

    private JTextField widthField;
    private JTextField heightField;
    private JTextField hoursField;
    private JTextField minsField;
    private JTextField secondsField;
    private JTextArea tagsField;

    private FileInfo fileInfo;

    public FileInfoEditPanel(FileInfo fileInfo) {
        super(new GridBagLayout());
        this.fileInfo = fileInfo;
        createUIComponents();
        initValues();
    }

    /**
     * @param owner
     * @param fileInfo
     * @return true, if there is any update on the file info
     */
    public static boolean showDialog(Frame owner, FileInfo fileInfo) {
        FileInfoEditPanel panel = new FileInfoEditPanel(fileInfo);
        boolean isChanged = false;
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(owner,
                panel,
                fileInfo.getName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            int newWidth = 0;
            int newHeight = 0;
            int newHours = 0;
            int newMins = 0;
            int newSeconds = 0;
            String newTagsText = StringUtils.trim(panel.tagsField.getText());

            String tmp = StringUtils.trim(panel.widthField.getText());
            if (StringUtils.isNumeric(tmp)) {
                newWidth = Integer.parseInt(tmp);
            }

            tmp = StringUtils.trim(panel.heightField.getText());
            if (StringUtils.isNumeric(tmp)) {
                newHeight = Integer.parseInt(tmp);
            }

            tmp = StringUtils.trim(panel.hoursField.getText());
            if (StringUtils.isNumeric(tmp)) {
                newHours = Integer.parseInt(tmp);
            }

            tmp = StringUtils.trim(panel.minsField.getText());
            if (StringUtils.isNumeric(tmp)) {
                newMins = Integer.parseInt(tmp);
            }

            tmp = StringUtils.trim(panel.secondsField.getText());
            if (StringUtils.isNumeric(tmp)) {
                newSeconds = Integer.parseInt(tmp);
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

            //check and update duration
            long newDuration = FileInfo.toSeconds(newHours, newMins, newSeconds);
            if (newDuration != fileInfo.getDuration()) {
                isChanged = true;
                fileInfo.setDuration(newDuration);
            }

            //check and update tags
            Set<String> newTags = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new StringReader(newTagsText))) {
                String tagsLine = null;
                while ((tagsLine = reader.readLine()) != null)
                    if (StringUtils.isNotBlank(tagsLine))
                        newTags.addAll(Arrays.stream(StringUtils.trim(tagsLine).split("[,，;；]")).map(StringUtils::trim).collect(Collectors.toSet()));
            } catch (IOException ex) {
                logger.error("Failed to read tags from UI", ex);
                newTags = null;
            }
            if (newTags != null) {
                //user input is valid
                boolean isTagsChanged = false;
                Set<String> tagsToPersist = new HashSet<>();
                Set<String> oldTags = fileInfo.getTags();
                if (oldTags == null) {
                    oldTags = new HashSet<>();
                }
                if (newTags.size() != oldTags.size()) {
                    isTagsChanged = true;
                }

                tagsToPersist.addAll(newTags);
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
        }
        return isChanged;
    }

    private void createUIComponents() {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("影片分辨率（宽 * 高）："), gbc);

        JPanel resolutionPanel = new JPanel(new GridLayout(1, 2));
        widthField = new JTextField();
        resolutionPanel.add(widthField);
        heightField = new JTextField();
        resolutionPanel.add(heightField);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(resolutionPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("影片长度（小时 : 分钟 : 秒）："), gbc);

        JPanel durationPanel = new JPanel(new GridLayout(1, 3));
        hoursField = new JTextField();
        durationPanel.add(hoursField);
        minsField = new JTextField();
        durationPanel.add(minsField);
        secondsField = new JTextField();
        durationPanel.add(secondsField);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(durationPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("标签（换行、逗号、分号分隔）："), gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        tagsField = new JTextArea(10, 40);
        add(new JScrollPane(tagsField), gbc);
    }

    private void initValues() {
        if (fileInfo.getResolution() != null) {
            widthField.setText(String.valueOf(fileInfo.getResolution().getWidth()));
            heightField.setText(String.valueOf(fileInfo.getResolution().getHeight()));
        }
        hoursField.setText(String.valueOf(fileInfo.getDurationHoursPart()));
        minsField.setText(String.valueOf(fileInfo.getDurationMinsPart()));
        secondsField.setText(String.valueOf(fileInfo.getDurationSecondsPart()));
        if (fileInfo.getTags() != null)
            tagsField.setText(StringUtils.join(fileInfo.getTags(), System.lineSeparator()));
    }
}
