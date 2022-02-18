package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.*;
import alex.beta.portablecinema.filesystem.VisitorMessageCallback;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.pojo.FolderInfo;
import com.google.common.io.Resources;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static alex.beta.portablecinema.FolderVisitorFactory.Action.*;
import static alex.beta.portablecinema.FolderVisitorFactory.newFolderVisitor;
import static alex.beta.portablecinema.gui.PortableCinemaFrame.*;
import static org.apache.commons.lang3.StringUtils.*;

public class ButtonActionHandler implements ActionListener {
    private static final Logger logger = LoggerFactory.getLogger(ButtonActionHandler.class);

    private static final String HTML_SPACE = "&nbsp;";

    private static final String HTML_LINE = "<hr/>";

    private static final String PRE_HTML_TAG = "<pre>%s</pre>";

    private String FILEINFO_TABLE_TEMPLATE;
    private String FILEINFO_TABLE_TR_TEMPLATE;
    private String FILEINFO_TABLE_TR_A_TEMPLATE;

    private String RESOLUTION_HD_IMG_TEMPLATE;
    private String GALLERY_IMG_TEMPLATE;
    private String FILEINFO_EDIT_IMG_TEMPLATE;
    private String FILEINFO_DETAIL_IMG_TEMPLATE;

    private PortableCinemaConfig config;
    private File rootFolder;
    private File confFile;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public ButtonActionHandler(File confFile, PortableCinemaConfig config) {
        this.config = config;
        this.confFile = confFile;
    }

    public void loadTemplates() throws IOException {
        //read resource templates
        FILEINFO_TABLE_TEMPLATE = readTemplate("templates/FileInfo-table.tpl");
        FILEINFO_TABLE_TR_TEMPLATE = readTemplate("templates/FileInfo-table-tr.tpl");
        FILEINFO_TABLE_TR_A_TEMPLATE = readTemplate("templates/FileInfo-table-tr-a.tpl");
        RESOLUTION_HD_IMG_TEMPLATE = readTemplate("templates/FileInfo-HD-img.tpl");
        GALLERY_IMG_TEMPLATE = readTemplate("templates/FileInfo-gallery-img.tpl");
        FILEINFO_EDIT_IMG_TEMPLATE = readTemplate("templates/FileInfo-edit-img.tpl");
        FILEINFO_DETAIL_IMG_TEMPLATE = readTemplate("templates/FileInfo-detail-img.tpl");
    }

    private String readTemplate(String resourcePath) throws IOException {
        return Resources.asCharSource(Resources.getResource(resourcePath), StandardCharsets.UTF_8).read();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Locate PortableCinemaFrame
        PortableCinemaFrame frame = (PortableCinemaFrame) ((JButton) e.getSource()).getRootPane().getParent();
        if (isRunning.compareAndSet(false, true)) {
            String action = e.getActionCommand();
            frame.appendResultText("[" + action.toUpperCase() + "]" + HTML_LINE);
            //Dispatch commands
            if (ROOT_FOLDER_CHOOSE_ACTION.equalsIgnoreCase(action)) {
                performRoot(frame);
            } else {
                if (rootFolder == null) {
                    JOptionPane.showMessageDialog(frame, "请选择影库目录", TITLE, JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
                    frame.appendResultText(HTML_LINE);
                    isRunning.set(false);
                } else {
                    if (SCAN_ACTION.equalsIgnoreCase(action)) {
                        performScan(frame);
                    } else if (EXPORT_ACTION.equalsIgnoreCase(action)) {
                        performExport(frame);
                    } else if (NAME_ACTION.equalsIgnoreCase(action)) {
                        performQuery(frame, NAME_ACTION, "片名关键字（大小写不敏感）：");
                    } else if (TAG_ACTION.equalsIgnoreCase(action)) {
                        Set<String> topTags = new TagCommand().topTags(1, 10);
                        performQuery(frame, TAG_ACTION, "影片标签(\",\"分隔)：" + System.lineSeparator() + join(topTags, ", "));
                    } else if (WHERE_ACTION.equalsIgnoreCase(action)) {
                        performQuery(frame, WHERE_ACTION, "查询条件：");
                    } else if (RESET_ACTION.equalsIgnoreCase(action)) {
                        performReset(frame);
                    } else {
                        new ButtonWorker(frame, action).execute();
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "当前指令正在执行，请稍后重试", TITLE, JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
        }
    }

    private void performRoot(PortableCinemaFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("影库目录");
        fileChooser.setMultiSelectionEnabled(false);
        if (isNotBlank(config.getRootFolderPath()))
            fileChooser.setSelectedFile(new File(config.getRootFolderPath()));
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
            rootFolder = fileChooser.getSelectedFile();
            new FileButtonWorker(frame, ROOT_FOLDER_CHOOSE_ACTION, rootFolder).execute();
        } else {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }
    }

    private void performScan(PortableCinemaFrame frame) {
        if (JOptionPane.showConfirmDialog(frame,
                "影库目录是" + config.getRootFolderPath() + "，索引当前目录？",
                "索引影库",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                frame.logo50Icon) == JOptionPane.YES_OPTION) {
            new ButtonWorker(frame, SCAN_ACTION).execute();
        } else {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }
    }

    private void performExport(PortableCinemaFrame frame) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel工作簿", "xls", "xlsx"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON数据文件", "json"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle("导出影库");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        if (isNotBlank(config.getRootFolderPath()))
            fileChooser.setSelectedFile(new File(config.getRootFolderPath()));
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null) {
            new FileButtonWorker(frame, EXPORT_ACTION, fileChooser.getSelectedFile()).execute();
        } else {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }
    }

    private void performQuery(PortableCinemaFrame frame, String action, String inputMessage) {
        Object inputValue = JOptionPane.showInputDialog(frame,
                inputMessage,
                TITLE,
                JOptionPane.QUESTION_MESSAGE,
                frame.logo50Icon, null, null);
        if (NAME_ACTION.equalsIgnoreCase(action) && inputValue != null && isNotBlank(String.valueOf(inputValue))) {
            new QueryButtonWorker(frame, NAME_ACTION, String.valueOf(inputValue)).execute();
        } else if (TAG_ACTION.equalsIgnoreCase(action) && inputValue != null) {
            new QueryButtonWorker(frame, TAG_ACTION, String.valueOf(inputValue)).execute();
        } else if (WHERE_ACTION.equalsIgnoreCase(action) && inputValue != null && isNotBlank(String.valueOf(inputValue))) {
            new QueryButtonWorker(frame, WHERE_ACTION, String.valueOf(inputValue)).execute();
        } else {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }
    }

    private void performReset(PortableCinemaFrame frame) {
        Object[] choices = {"取消", "重置目录时间", "完全重置"};
        Object defaultChoice = choices[0];
        int option = JOptionPane.showOptionDialog(frame,
                "重置影库，请谨慎选择要进行的选择操作",
                "重置影库",
                JOptionPane.CLOSED_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                frame.logo50Icon,
                choices,
                defaultChoice);
        if (option == 2 && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
                "删除所有数据文件并重置数据库，是吗？",
                "重置影库",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                frame.logo50Icon)) {
            new ResetButtonWorker(frame, 2).execute();
        } else if (option == 1 && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(frame,
                "重置所有目录时间，是吗？",
                "重置影库",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                frame.logo50Icon)) {
            new ResetButtonWorker(frame, 1).execute();
        } else {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }
    }

    class FileButtonWorker extends ButtonWorker {
        private File selectedFile;

        public FileButtonWorker(PortableCinemaFrame frame, String action, File selectedFile) {
            super(frame, action);
            this.selectedFile = selectedFile;
        }

        @Override
        protected Void doInBackground() {
            switch (action.toUpperCase()) {
                case EXPORT_ACTION:
                    doExport();
                    break;
                case ROOT_FOLDER_CHOOSE_ACTION:
                    doRoot();
                    break;
                default:
                    super.doInBackground();
            }
            return null;
        }

        private void doExport() {
            try {
                String filePath = new ExportCommand(selectedFile.getCanonicalPath()).execute(config);
                output("影库导出成功，" + filePath);
                JOptionPane.showMessageDialog(frame, "影库导出成功", "导出影库", JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
            } catch (IOException ex) {
                logger.error("Error when exporting database", ex);
                output("影库导出失败（" + ex.getMessage() + "）");
            }
        }

        private void doRoot() {
            try (FileWriter writer = new FileWriter(confFile)) {
                String rootFolderPath = rootFolder.getCanonicalPath();
                logger.debug("rootChooserButton::root folder is selected as {}", rootFolderPath);

                //update configuration file
                Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
                gson.toJson(config.rootFolderPath(rootFolderPath), writer);
                writer.flush();
                output("配置文件更新成功");
                if (logger.isInfoEnabled())
                    logger.info("Update configuration, {}", config);
                //reset database, clear legacy data
                new ResetDatabaseCommand().execute(config);
                output("数据库重置成功");
                //aggregate
                FolderInfo folderInfo = newFolderVisitor(AGGREGATE).execute(config, rootFolder);
                output("影库更新成功");
                //output aggregation result
                frame.setStatusText("影库目录: " + rootFolderPath);
                output(folderInfo.toString());
            } catch (IOException ex) {
                logger.error("Error when selecting root folder", ex);
                frame.setErrorStatusText("影库目录读取失败（" + ex.getMessage() + "）");
                rootFolder = null;
            }
        }
    }

    class QueryButtonWorker extends ButtonWorker {
        private String inputValue;

        public QueryButtonWorker(PortableCinemaFrame frame, String action, String inputValue) {
            super(frame, action);
            this.inputValue = inputValue;
        }

        @Override
        protected Void doInBackground() {
            FileInfo[] fileInfos = null;
            switch (action.toUpperCase()) {
                case NAME_ACTION:
                    logger.debug("findByNameButton::user input is {}", inputValue);
                    fileInfos = new NameCommand(String.valueOf(inputValue)).execute(config);
                    break;
                case TAG_ACTION:
                    logger.debug("findByTagButton::user input is {}", inputValue);
                    fileInfos = new TagCommand(split(String.valueOf(inputValue), ",")).execute(config);
                    break;
                case WHERE_ACTION:
                    logger.debug("findByWhereButton::user input is {}", inputValue);
                    fileInfos = new WhereCommand(String.valueOf(inputValue)).execute(config);
                    break;
                default:
                    super.doInBackground();
            }
            output(fileInfo2HTML(fileInfos));
            return null;
        }
    }

    class ResetButtonWorker extends ButtonWorker {
        /**
         * Reset option
         * 2 - Reset all
         * 1 - Reset folder last modified time
         */
        private int option;

        public ResetButtonWorker(PortableCinemaFrame frame, int option) {
            super(frame, RESET_ACTION);
            this.option = option;
        }

        @Override
        protected Void doInBackground() {
            if (option == 2) {
                newFolderVisitor(RESET_ALL).messageCallback(this).execute(config, rootFolder);
                new ResetDatabaseCommand().execute(config);
                output("影库重置成功");
            } else if (option == 1) {
                newFolderVisitor(RESET_FOLDER).messageCallback(this).execute(config, rootFolder);
                output("目录访问时间重置成功");
            } else {
                super.doInBackground();
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
            JOptionPane.showMessageDialog(frame, "影库重置成功", "重置影库", JOptionPane.INFORMATION_MESSAGE, frame.logo50Icon);
        }
    }

    @AllArgsConstructor
    class ButtonWorker extends SwingWorker<Void, String> implements VisitorMessageCallback {

        /**
         * PortableCinemaFrame that display result
         */
        protected PortableCinemaFrame frame;

        /**
         * Action type
         */
        protected String action;

        @Override
        protected Void doInBackground() {
            switch (action.toUpperCase()) {
                case SCAN_ACTION:
                    doScan(this);
                    break;
                case ANALYZE_ACTION:
                    doAnalyze();
                    break;
                default:
                    logger.warn("Unsupported action [{}]", action);
            }
            return null;
        }

        @Override
        protected void done() {
            frame.appendResultText(HTML_LINE);
            isRunning.set(false);
        }

        @Override
        protected void process(List<String> chunks) {
            chunks.stream().forEach(msg -> frame.appendResultText(String.format(PRE_HTML_TAG, msg)));
        }

        @Override
        public void output(String... messages) {
            if (messages != null)
                publish(messages);
        }

        private void doScan(@NonNull VisitorMessageCallback messageCallback) {
            logger.debug("scanButton::root folder is {}", config.getRootFolderPath());
            newFolderVisitor(SCAN).messageCallback(messageCallback).execute(config, rootFolder);
            output("影片索引完成");
            new ResetDatabaseCommand().execute(config);
            FolderInfo folderInfo = newFolderVisitor(AGGREGATE).execute(config, rootFolder);
            output("影库更新成功", folderInfo.toString());
        }

        private void doAnalyze() {
            logger.debug("analyzeButton::root folder is {}", config.getRootFolderPath());
            AnalyzeCommand.AnalyzeResult result = new AnalyzeCommand().execute(config);
            output("影片数量：" + result.getTotalVideos() + " 部影片");
            if (result.getExtraTags() != null && !result.getExtraTags().isEmpty())
                output("新标签：" + join(result.getExtraTags(), ", "));
            if (result.getTotalVideos() > 0) {
                if (!result.getTagsInUse().isEmpty()) {
                    output("常用标签：" + join(result.getTagsInUse().keySet(), ", "));
                } else {
                    output("无常用标签");
                }
                if (result.getSimilarVideos().isEmpty()) {
                    output("无重复的影片");
                } else {
                    for (int i = 0; i < result.getSimilarVideos().size(); i++) {
                        output("下列影片大小一致，都是：" + result.getSimilarSizes().get(i),
                                fileInfo2HTML(result.getSimilarVideos().get(i)));
                    }
                }
            }
        }

        /**
         * Return null, if input null
         *
         * @param fileInfos
         * @return
         */
        protected String fileInfo2HTML(FileInfo... fileInfos) {
            if (fileInfos != null) {
                StringBuilder tbodyBuffer = new StringBuilder();
                for (int i = 0; i < fileInfos.length; i++) {
                    FileInfo fi = fileInfos[i];
                    String fileLinkText = HTML_SPACE;
                    if (!isBlank(fi.getPath())) {
                        String timestampText = (fi.getLastModifiedOn() == null ? "" : DateFormatUtils.format(fi.getLastModifiedOn(), PortableCinemaConfig.DATE_FORMATTER));
                        fileLinkText = String.format(FILEINFO_TABLE_TR_A_TEMPLATE,
                                "file://" + UrlEscapers.urlFragmentEscaper().escape(fi.getPath()),
                                timestampText,
                                StringEscapeUtils.escapeHtml4(fi.getName()));
                    }

                    String editLinkText = String.format(FILEINFO_EDIT_IMG_TEMPLATE, fi.getOtid());

                    String tagText = HTML_SPACE;
                    if (fi.getTags() != null && !fi.getTags().isEmpty()) {
                        tagText = join(fi.getTags(), ", ");
                    }
                    String resolutionText = HTML_SPACE;
                    String hdImg = HTML_SPACE;
                    if (fi.getResolution() != null) {
                        resolutionText = fi.getResolution().toString();
                        if (fi.getResolution().isHD()) {
                            hdImg = String.format(RESOLUTION_HD_IMG_TEMPLATE, fi.getResolution().toString());
                        }
                    }
                    String galleryLinkText = HTML_SPACE;
                    if (!isBlank(fi.getCover1()) || !isBlank(fi.getCover2())) {
                        galleryLinkText = String.format(GALLERY_IMG_TEMPLATE, fi.getOtid());
                    }

                    String detailLinkText = String.format(FILEINFO_DETAIL_IMG_TEMPLATE, fi.getOtid());

                    String bgcolor = (i % 2 == 0 ? "white" : "silver");
                    tbodyBuffer.append(String.format(FILEINFO_TABLE_TR_TEMPLATE, bgcolor,
                            i + 1,
                            fileLinkText, galleryLinkText, editLinkText, detailLinkText,
                            fi.getFormattedDuration(),
                            tagText,
                            resolutionText, hdImg)).append(System.lineSeparator());
                }
                return String.format(FILEINFO_TABLE_TEMPLATE, tbodyBuffer.toString());
            } else {
                return null;
            }
        }
    }
}