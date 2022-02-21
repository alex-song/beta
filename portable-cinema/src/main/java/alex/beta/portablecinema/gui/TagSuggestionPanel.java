package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.tag.TagService;
import alex.beta.simpleocr.OcrException;
import alex.beta.simpleocr.baidu.BaiduOcr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TagSuggestionPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(TagSuggestionPanel.class);
    private static final String ANALYSING_MSG = "图片解析中……";
    private static final String ERROR_MSG = "图片分析失败";
    private static final String NO_WORD_MSG = "未能识别有效字符";

    private JTextArea wordsArea;
    private DefaultListModel<String> wordsModel;
    private DefaultListModel<String> tagsModel;
    private JButton addButton;
    private JButton deleteButton;

    public TagSuggestionPanel(BaiduOcr ocrClient, FileInfo fileInfo, int width, int height, String currentImg) {
        super(new BorderLayout());
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));

        // initialize UI
        createUIComponents();

        // display initial content
        wordsModel.addElement(ANALYSING_MSG);
        if (fileInfo != null && fileInfo.getTags() != null) {
            fileInfo.getTags().forEach(tagsModel::addElement);
        }

        if (currentImg != null) {
            wordsArea.append(currentImg + System.lineSeparator());
            // call OCR
            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() {
                    try {
                        java.util.List<String> ows = ocrClient.analyse(new File(currentImg));
                        if (ows == null || ows.isEmpty()) {
                            publish(NO_WORD_MSG);
                        } else {
                            publish(ows.toArray(new String[]{}));
                        }
                    } catch (OcrException ex) {
                        logger.error("Failed to read/analyse file {}", currentImg, ex);
                        if (logger.isErrorEnabled() && isNotBlank(ex.getServerErrorMsg()))
                            logger.error(ex.getServerErrorMsg());
                        publish(ERROR_MSG);
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<String> chunks) {
                    if (chunks != null && !chunks.isEmpty()) {
                        // remove analysing message
                        if (wordsModel.getSize() == 1 && ANALYSING_MSG.equals(wordsModel.get(0)))
                            wordsModel.clear();
                        String firstMsg = chunks.get(0);
                        if (ERROR_MSG.equals(firstMsg) || NO_WORD_MSG.equals(firstMsg)) {
                            wordsModel.addElement(firstMsg);
                        } else {
                            // enable action buttons
                            addButton.setEnabled(true);
                            deleteButton.setEnabled(true);
                            // add result into textarea
                            chunks.forEach(w -> wordsArea.append(w + System.lineSeparator()));
                            // add eligible words into ui list
                            java.util.List<String> words = new ArrayList<>();
                            for (String ow : chunks)
                                if ((isNotBlank(ow) && ow.trim().length() >= TagService.MINI_TERM_TEXT_LENGTH)
                                        && (fileInfo == null || fileInfo.getTags() == null || !fileInfo.getTags().contains(ow.trim())))
                                    words.add(ow.trim());
                            if (!words.isEmpty())
                                words.forEach(wordsModel::addElement);
                        }
                    }
                }
            }.execute();
        }
    }

    private void createUIComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.8);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        JPanel tagSelectionPanel = new JPanel(new GridBagLayout());

        wordsModel = new DefaultListModel<>();
        JList<String> wordsList = new JList<>(wordsModel);
        wordsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 1;
        JScrollPane wordsScroll = new JScrollPane(wordsList);
        wordsScroll.setPreferredSize(new Dimension(200, 200));
        tagSelectionPanel.add(wordsScroll, c);

        JToolBar buttonBar = new JToolBar();
        buttonBar.setMargin(new Insets(0, 0, 0, 0));
        buttonBar.setOrientation(SwingConstants.VERTICAL);
        buttonBar.setFloatable(false);
        buttonBar.add(Box.createVerticalGlue());
        addButton = new JButton(" > ");
        addButton.setMargin(new Insets(3, 20, 3, 20));

        addButton.setEnabled(false);
        buttonBar.add(addButton);
        buttonBar.addSeparator();
        deleteButton = new JButton(" < ");
        deleteButton.setMargin(new Insets(3, 20, 3, 20));
        deleteButton.setEnabled(false);
        buttonBar.add(deleteButton);
        buttonBar.add(Box.createVerticalGlue());
        c.gridx = 1;
        c.weightx = 0.0;
        tagSelectionPanel.add(buttonBar, c);

        tagsModel = new DefaultListModel<>();
        JList<String> tagsList = new JList<>(tagsModel);
        tagsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        c.gridx = 2;
        c.weightx = 0.5;
        JScrollPane tagsScroll = new JScrollPane(tagsList);
        tagsScroll.setPreferredSize(new Dimension(200, 200));
        tagSelectionPanel.add(tagsScroll, c);

        splitPane.add(tagSelectionPanel);

        wordsArea = new JTextArea();
        wordsArea.setLineWrap(true);
        wordsArea.setWrapStyleWord(true);
        splitPane.add(new JScrollPane(wordsArea));

        add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> moveSelectedTo(wordsList, tagsList));
        deleteButton.addActionListener(e -> moveSelectedTo(tagsList, wordsList));
    }

    private Set<String> getSuggestedTags() {
        Set<String> suggestedTags = new HashSet<>();
        for (int i = 0; i < tagsModel.getSize(); i++) {
            String tag = tagsModel.elementAt(i);
            if (isNotBlank(tag) && tag.trim().length() >= TagService.MINI_TERM_TEXT_LENGTH) {
                suggestedTags.add(tag.trim());
            }
        }
        return suggestedTags;
    }

    /**
     * https://stackoverflow.com/questions/19223366/moving-selected-data-between-two-jlist
     * Due to a weird compile error in the IDE, cannot use addAll.
     *
     * @param from {@link JList} to obtain the entities from.
     * @param to   {@link  JList} to add the obtained entities.
     * @param <E>  Class of the elements loaded in the {@link  JList}.
     * @throws IllegalArgumentException - if any the provided parameters are null.
     */
    private <E> void moveSelectedTo(JList<E> from, JList<E> to) {
        java.util.List<E> entitiesToTransition = from.getSelectedValuesList();
        if (entitiesToTransition == null || entitiesToTransition.isEmpty()) return;

        DefaultListModel<E> fromModel = (DefaultListModel<E>) from.getModel();
        for (E entity : entitiesToTransition) fromModel.removeElement(entity);

        java.util.List<E> previouslyLoadedEntities = new java.util.ArrayList<>();
        ListModel<E> model = (to.getModel());
        for (int i = 0; i < model.getSize(); i++) previouslyLoadedEntities.add(model.getElementAt(i));
//        DefaultListModel<E> dlm = new DefaultListModel<E>() {{
//            addAll(previouslyLoadedEntities);
//            addAll(entitiesToTransition);
//        }};
        DefaultListModel<E> dlm = (DefaultListModel<E>) to.getModel();
        dlm.clear();
        previouslyLoadedEntities.forEach(dlm::addElement);
        entitiesToTransition.forEach(dlm::addElement);
    }

    /**
     * @param previewPanel
     * @param ocrClient
     * @param currentImg
     * @return true, if tags are changed
     */
    public static boolean showDialog(PreviewPanel previewPanel, BaiduOcr ocrClient, String currentImg) {
        FileInfo fileInfo = previewPanel.getFileInfo();
        TagSuggestionPanel tsp = new TagSuggestionPanel(ocrClient, fileInfo, 800, 700, currentImg);
        Object[] choices = {"取消", "确认"};
        Object defaultChoice = choices[0];
        int option = JOptionPane.showOptionDialog(previewPanel, tsp, fileInfo.getName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices, defaultChoice);

        if (option == 1) {
            boolean isTagsChanged = false;
            Set<String> newTags = tsp.getSuggestedTags();
            Set<String> oldTags = fileInfo.getTags();
            Set<String> tagsToPersist = new HashSet<>(newTags);
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
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }
}
