package alex.beta.portablecinema.tag;

import alex.beta.portablecinema.PortableCinemaConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

@SuppressWarnings("squid:S3776")
public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    private static final double THRESHOLD = 0.85;

    private static final int MINI_TERM_TEXT_LENGTH = 2;

    private static TagService instance;

    private PortableCinemaConfig config;

    /**
     * text:tags
     */
    private Map<String, Set<String>> glossaryMap;

    private Set<String> actors;

    private TagService(PortableCinemaConfig config) {
        //private constructor
        this.config = config;
    }

    /**
     * Create the tag service using the glossary defined in config
     *
     * @param config
     * @return
     */
    public static TagService getInstance(@NonNull PortableCinemaConfig config) {
        return getInstance(config, null);
    }

    /**
     * Create the tag service using given glossary, ignore the glossary defined in config
     *
     * @param config
     * @param glossary
     * @return
     */
    public static synchronized TagService getInstance(@NonNull PortableCinemaConfig config, Glossary glossary) {
        if (instance == null) {
            instance = new TagService(config);
            instance.initialize(glossary);
        }
        return instance;
    }

    /**
     * Load Keywords
     * Load Actors
     * Load existing records for reference
     *
     * @param glossary
     */
    private void initialize(Glossary glossary) {
        glossaryMap = Collections.synchronizedMap(new HashMap<>());
        actors = Collections.synchronizedSet(new HashSet<>());
        try {
            Glossary tmpG = null;
            if (glossary == null) {
                File glossaryFile = new File(config.getRootFolderPath(), config.getGlossaryFileName());
                if (glossaryFile.exists() && glossaryFile.isFile()) {
                    tmpG = new GsonBuilder().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create().fromJson(new FileReader(glossaryFile), Glossary.class);
                } else if (!glossaryFile.exists()) {
                    tmpG = new Glossary();
                    FileWriter writer = new FileWriter(glossaryFile);
                    Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
                    gson.toJson(tmpG, writer);
                    writer.flush();
                }
            } else {
                tmpG = glossary;
            }

            if (tmpG != null) {
                if (tmpG.getKeywords() != null) {
                    for (Glossary.Term term : tmpG.getKeywords()) {
                        if (isNotBlank(term.getText()) && term.getTags() != null && !term.getTags().isEmpty()) {
                            //处理text，加入tags
                            if (glossaryMap.containsKey(term.getText())) {
                                glossaryMap.get(term.getText()).addAll(term.getTags());
                            } else {
                                Set<String> tmpS = new HashSet<>();
                                tmpS.addAll(term.getTags());
                                glossaryMap.put(term.getText(), tmpS);
                            }
                            //处理别名 - (加入text作为alias的tag?)
                            if (term.getAlias() != null && !term.getAlias().isEmpty()) {
                                for (String a : term.getAlias()) {
                                    if (glossaryMap.containsKey(a)) {
                                        glossaryMap.get(a).addAll(term.getTags());
                                    } else {
                                        Set<String> tmpS = new HashSet<>();
                                        tmpS.addAll(term.getTags());
                                        glossaryMap.put(a, tmpS);
                                    }
                                    //glossaryMap.get(a).add(term.getText());
                                }
                            }
                        }
                    }
                }
                logger.debug("There are {} keywords found in {}", glossaryMap.size(), config.getGlossaryFileName());

                if (isNotBlank(tmpG.getActors())) {
                    String[] as = split(tmpG.getActors(), "\\,");
                    for (String a : as) {
                        actors.add(a.trim());
                    }
                }
                logger.debug("There are {} actors found in {}", actors.size(), config.getGlossaryFileName());
            }
        } catch (Exception ex) {
            logger.warn("Failed to load Glossary.json", ex);
            glossaryMap = Collections.synchronizedMap(new HashMap<>());
            actors = Collections.synchronizedSet(new HashSet<>());
        }
    }

    /**
     * @param videoFile
     * @param currentFolder
     * @return
     * @throws IOException
     */
    public Set<String> detectTags(@NonNull File videoFile, @NonNull File currentFolder) throws IOException {
        Set<String> tags = new HashSet<>();

        // 1. By file
        detectByFile(tags, videoFile);

        // 2. By folder
        detectByFolder(tags, currentFolder);

        // 3. By other records, or content, source, web search
        // TODO - AI/ML to detect other tags

        return tags;
    }

    /**
     * @param tag
     * @return
     */
    public boolean hasTag(@NonNull String tag) {
        if (this.actors.contains(tag)) {
            return true;
        }
        for (Set<String> tags : this.glossaryMap.values()) {
            if (tags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private void detectByFile(@NonNull Set<String> tags, @NonNull File file) {
        //remove the extension path
        String tag = file.getName();
        if (tag.lastIndexOf('.') >= 0) {
            tag = tag.substring(0, tag.lastIndexOf('.'));
        }
        if (isNotBlank(tag)) {
            //tags.add(tag);
            tags.addAll(similarTags(tag));
        }
    }

    private void detectByFolder(Set<String> tags, @NonNull File folder) throws IOException {
        // 目录名
        // 上级目录名、上上级目录名...
        if (folder.getCanonicalPath().equalsIgnoreCase(config.getRootFolderPath())) {
            return;
        }
        validateFolderName(tags, folder.getName());
        if (folder.getParent() != null) {
            detectByFolder(tags, folder.getParentFile());
        }
    }

    private void validateFolderName(@NonNull Set<String> tags, @NonNull String folderName) {
        // 去除无效字符
        if (isNotEmpty(folderName) && !isNumeric(folderName)) {
            String laundaryStr = folderName.replace("\\.", "")
                    .replace(" ", "")
                    .replace("-", "")
                    .replace("_", "")
                    .replace(",", "")
                    .replace("=", "")
                    .replace("\\+", "")
                    .replace("\\[", "")
                    .replace("\\]", "")
                    .replace("!", "")
                    .replace("@", "")
                    .replace("#", "")
                    .replace("$", "")
                    .replace("%", "")
                    .replace("^", "")
                    .replace("&", "")
                    .replace("\\*", "")
                    .replace("\\(", "")
                    .replace("\\)", "");
            if (isNotEmpty(laundaryStr) && !isNumeric(laundaryStr)) {
                String tmp = folderName.trim();
                //tags.add(tmp);
                tags.addAll(similarTags(tmp));
            }
        }
    }

    public Set<String> similarTags(String text) {
        Set<String> likes = new HashSet<>();

        if (glossaryMap.containsKey(text)) {
            likes.addAll(glossaryMap.get(text));
        }

        if (glossaryMap.size() > 0) {
            List<String> termTexts = new ArrayList<>(glossaryMap.keySet());

            for (String t : termTexts) {
                //文字中含有Glossary中的词条（大小写不敏感）
                if (text.toLowerCase().contains(t.toLowerCase()) && t.length() >= MINI_TERM_TEXT_LENGTH) {
                    likes.addAll(glossaryMap.get(t));
                }
            }

            JaroWinklerSimilarity jwSimilarity = new JaroWinklerSimilarity();
            Collections.sort(termTexts, (o1, o2) -> {
                double similarity = jwSimilarity.apply(o2.toLowerCase(), text.toLowerCase()) - jwSimilarity.apply(o1.toLowerCase(), text.toLowerCase());
                if (similarity > 0) {
                    return 1;
                } else if (similarity < 0) {
                    return -1;
                } else {
                    return 0;
                }
            });

            double similarity = jwSimilarity.apply(termTexts.get(0).toLowerCase(), text.toLowerCase());
            logger.debug("The similarity (case insensitive) of [{}] and [{}] is {}", termTexts.get(0), text, similarity);
            if (similarity >= THRESHOLD) {
                likes.addAll(glossaryMap.get(termTexts.get(0)));
            }
        }

        for (String kw : actors) {
            if (text.toLowerCase().contains(kw.toLowerCase()) && kw.length() >= MINI_TERM_TEXT_LENGTH) {
                likes.add(kw);
            }
        }

        return likes;
    }
}
