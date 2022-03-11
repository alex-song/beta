package alex.beta.portablecinema.tag;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.tag.xml.Glossary;
import alex.beta.portablecinema.tag.xml.Term;
import lombok.NonNull;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;

public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    private static final double THRESHOLD = 0.85;

    public static final int MINI_TERM_TEXT_LENGTH = 2;

    private static TagService instance;

    private final PortableCinemaConfig config;

    private JAXBContext jaxbContext;

    private File glossaryFile;

    private Glossary tmpG;

    /**
     * text:tags
     */
    private Map<String, Set<String>> glossaryMap;

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
    public static TagService getInstance(PortableCinemaConfig config) {
        return getInstance(config, null);
    }

    /**
     * Create the tag service using given glossary, ignore the glossary defined in config
     *
     * @param config
     * @param glossary
     * @return
     */
    public static synchronized TagService getInstance(PortableCinemaConfig config, Glossary glossary) {
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
        try {
            this.glossaryFile = new File(config.getRootFolderPath(), config.getGlossaryFileName());
            this.jaxbContext = JAXBContext.newInstance(Glossary.class);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            if (glossary == null) {
                if (glossaryFile.exists() && glossaryFile.isFile()) {
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    this.tmpG = (Glossary) jaxbUnmarshaller.unmarshal(glossaryFile);
                } else {
                    this.tmpG = new Glossary();
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    jaxbMarshaller.marshal(tmpG, glossaryFile);
                }
            } else {
                this.tmpG = glossary;
            }

            if (tmpG != null) {
                processTerms(tmpG.getActor());
                if (logger.isDebugEnabled())
                    logger.debug("There are {} actors", tmpG.getActor().size());

                processTerms(tmpG.getCategory());
                if (logger.isDebugEnabled())
                    logger.debug("There are {} categories", tmpG.getCategory().size());

                processTerms(tmpG.getProducer());
                if (logger.isDebugEnabled())
                    logger.debug("There are {} producers", tmpG.getProducer().size());

                processTerms(tmpG.getOther());
                if (logger.isDebugEnabled())
                    logger.debug("There are {} others", tmpG.getOther().size());

                if (logger.isInfoEnabled()) {
                    logger.info("There are {} terms in total", glossaryMap.size());
                }
            } else {
                logger.info("Glossary is empty");
            }
        } catch (Exception ex) {
            logger.warn("Failed to load Glossary.xml", ex);
            glossaryMap = Collections.synchronizedMap(new HashMap<>());
        }
    }

    private void processTerms(List<Term> terms) {
        for (Term term : terms) {
            String keyword = term.getKeyword();
            Set<String> tags = term.getTag();
            if (tags.isEmpty())
                tags.add(keyword);
            registerKeyword(keyword, tags);
            term.getAlias().forEach(a -> registerKeyword(a, tags));
        }
    }

    private void registerKeyword(String keyword, Set<String> tags) {
        if (glossaryMap.containsKey(keyword)) {
            tags.forEach(t -> glossaryMap.get(keyword).add(t));
        } else {
            glossaryMap.put(keyword, new HashSet<>(tags));
        }
    }

    /**
     * @param videoFile
     * @param currentFolder
     * @return
     * @throws IOException
     */
    public Set<String> detectTags(File videoFile, File currentFolder) throws IOException {
        Set<String> tags = new HashSet<>();

        // 1. By file
        detectByFile(tags, videoFile);

        // 2. By folder
        detectByFolder(tags, currentFolder);

        // 3. By other records, or content, source, web search
        // TODO - AI/ML to detect other tags, for instance OCR, possibility analytics

        return tags;
    }

    /**
     * @param tags
     * @param file
     */
    private void detectByFile(@NonNull Set<String> tags, @NonNull File file) {
        //remove the extension path
        String tag = file.getName();
        if (tag.lastIndexOf('.') >= 0) {
            tag = tag.substring(0, tag.lastIndexOf('.'));
        }
        if (isNotBlank(tag)) {
            //tags.add(tag);
            tags.addAll(suggest(tag));
        }
    }

    private void detectByFolder(Set<String> tags, @NonNull File folder) throws IOException {
        // 目录名
        // 上级目录名、上上级目录名...
        if (folder.getCanonicalPath().equalsIgnoreCase(config.getRootFolderPath())) {
            return;
        }
        proceedFolderName(tags, folder.getName());
        if (folder.getParent() != null) {
            detectByFolder(tags, folder.getParentFile());
        }
    }

    private void proceedFolderName(@NonNull Set<String> tags, @NonNull String folderName) {
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
            if (isNotBlank(laundaryStr) && !isNumeric(laundaryStr)) {
                String tmp = folderName.trim();
                //tags.add(tmp);
                tags.addAll(suggest(tmp));
            }
        }
    }

    /**
     * Suggest tags according to given text
     *
     * @param text
     * @return
     */
    public Set<String> suggest(final String text) {
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
            termTexts.sort((o1, o2) -> {
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

        return likes;
    }

    /**
     * @param tag
     * @return true, if given tag is defined in glossary tag list
     */
    public boolean hasTag(@NonNull String tag) {
        for (Set<String> tags : this.glossaryMap.values())
            if (tags.contains(tag)) return true;
        return false;
    }

    /**
     * Add actors into glossary
     *
     * @param actors
     * @return true, if it's new. Otherwise, false.
     */
    public boolean addActors(String... actors) {
        if (actors == null || actors.length == 0) return false;
        for (String actor : actors) {
            if (hasTag(actor)) return false;
            if (isBlank(actor)) continue;
            Term t = new Term(actor);
            this.tmpG.getActor().add(t);
        }
        try {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(tmpG, glossaryFile);
            // update glossary map
            for (String actor : actors) {
                if (isBlank(actor)) continue;
                Set<String> tags = new HashSet<>();
                tags.add(actor);
                this.glossaryMap.put(actor, tags);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to update glossary file, with new actors", ex);
            return false;
        }
    }

    /**
     * Add categories into glossary
     *
     * @param categories
     * @return true, if it's new. Otherwise, false.
     */
    public boolean addCategories(String... categories) {
        if (categories == null || categories.length == 0) return false;
        for (String category : categories) {
            if (hasTag(category)) return false;
            if (isBlank(category)) continue;
            Term t = new Term(category);
            this.tmpG.getCategory().add(t);
        }
        try {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(tmpG, glossaryFile);
            // update glossary map
            for (String category : categories) {
                if (isBlank(category)) continue;
                Set<String> tags = new HashSet<>();
                tags.add(category);
                this.glossaryMap.put(category, tags);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to update glossary file, with new categories", ex);
            return false;
        }
    }

    /**
     * Add producers into glossary
     *
     * @param producers
     * @return true, if it's new. Otherwise, false.
     */
    public boolean addProducers(String... producers) {
        if (producers == null || producers.length == 0) return false;
        for (String producer : producers) {
            if (hasTag(producer)) return false;
            if (isBlank(producer)) continue;
            Term t = new Term(producer);
            this.tmpG.getProducer().add(t);
        }
        try {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(tmpG, glossaryFile);
            // update glossary map
            for (String producer : producers) {
                if (isBlank(producer)) continue;
                Set<String> tags = new HashSet<>();
                tags.add(producer);
                this.glossaryMap.put(producer, tags);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to update glossary file, with new producers", ex);
            return false;
        }
    }

    /**
     * Add others into glossary
     *
     * @param others
     * @return true, if it's new. Otherwise, false.
     */
    public boolean addOthers(String... others) {
        if (others == null || others.length == 0) return false;
        for (String other : others) {
            if (hasTag(other)) return false;
            if (isBlank(other)) continue;
            Term t = new Term(other);
            this.tmpG.getOther().add(t);
        }
        try {
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(tmpG, glossaryFile);
            // update glossary map
            for (String other : others) {
                if (isBlank(other)) continue;
                Set<String> tags = new HashSet<>();
                tags.add(other);
                this.glossaryMap.put(other, tags);
            }
            return true;
        } catch (Exception ex) {
            logger.error("Failed to update glossary file, with new others", ex);
            return false;
        }
    }
}
