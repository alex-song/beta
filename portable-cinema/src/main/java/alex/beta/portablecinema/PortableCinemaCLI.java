package alex.beta.portablecinema;

import alex.beta.portablecinema.command.*;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.pojo.FolderInfo;
import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@SuppressWarnings({"squid:S106", "squid:S3776"})
public class PortableCinemaCLI {
    static final String OUTPUT_PREFIX = ">>>";
    private static final Logger logger = LoggerFactory.getLogger(PortableCinemaCLI.class);

    public static void main(String[] args) throws IOException, DatabaseException {
        logger.info("### Portable Cinema Command Line Interface ###");
        String confFileName = PortableCinemaConfig.DEFAULT_CONFIGURATION_FILE_NAME;
        if (System.getProperty(PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME) != null) {
            confFileName = System.getProperty(PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME);
        }
        //TODO - tesing only
        String rootFolderName = ".";
        //String rootFolderName = "/Users/alexsong/Development/my_workspace/beta/portable-cinema/sample";

        if (args.length == 2) {
            rootFolderName = args[1];
        }
        FolderVisitorFactory.Action action = FolderVisitorFactory.Action.AGGREGATE;
        if (args.length >= 1) {
            action = FolderVisitorFactory.Action.valueOf(args[0].toUpperCase());
        }

        File rootFolder = new File(rootFolderName);
        if (!rootFolder.exists() || rootFolder.isFile()) {
            throw new IllegalArgumentException(String.format("Root folder [%s] does not exist, or not valid", rootFolderName));
        }
        File confFile = new File(confFileName);
        if (!confFile.exists()) {
            FileWriter writer = new FileWriter(confFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
            gson.toJson(PortableCinemaConfig.getDefault().rootFolderPath(rootFolder.getCanonicalPath()), writer);
            writer.flush();
        } else if (!confFile.isFile()) {
            throw new IllegalArgumentException(String.format("Configuration file [%s] is not a valid file", confFileName));
        }
        logger.info("Root folder: [{}]", rootFolder.getCanonicalPath());
        logger.info("Configuration file: [{}]", confFile.getCanonicalPath());

        PortableCinemaConfig config = new GsonBuilder().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create().fromJson(new FileReader(confFile), PortableCinemaConfig.class);
        System.out.println(Banner.getInstance().read4CLI(confFile.getCanonicalPath(), config) + System.lineSeparator());

        if (logger.isInfoEnabled())
            logger.info(config.toString());

        if (DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY) != null) {
            logger.info("Initializing database is done");
        }
        System.out.println("==============================================================================");

        System.out.println(action + ":");
        FolderVisitorFactory.FolderVisitor fv = FolderVisitorFactory.newFolderVisitor(action);

        if (FolderVisitorFactory.Action.SCAN == action) {
            fv = fv.messageCallback(PortableCinemaCLI::output);
        }
        FolderInfo fi = fv.execute(config, rootFolder);
        if (FolderVisitorFactory.Action.AGGREGATE == action) {
            System.out.println(fi);
        }

        System.out.println("==============================================================================");

        if (action == FolderVisitorFactory.Action.AGGREGATE) {
            boolean quit = false;
            do {
                String commandStr = readDataFromConsole("Command");
                logger.debug("User input command is [{}]", commandStr);
                if (isBlank(commandStr)) {
                    continue;
                } else if ("Quit".equalsIgnoreCase(commandStr.trim()) || "Exit".equalsIgnoreCase(commandStr.trim()) || "Bye".equalsIgnoreCase(commandStr.trim())) {
                    System.out.println("Bye!");
                    quit = true;
                } else {
                    try {
                        output(getCommand(commandStr.trim()).execute(config));
                    } catch (IllegalArgumentException ex) {
                        logger.debug("Command [{}] is not supported", commandStr, ex);
                        System.out.println(ConsoleColors.RED_BOLD + String.format("Command [%s] is not supported. %s", commandStr, ex.getMessage()) + ConsoleColors.RESET);
                    }
                }
            } while (!quit);
        }
    }

    /**
     * Instantiate a command according to given input.
     *
     * @param inputStr Tag, Name, Where, Export, Analyze
     * @return
     */
    private static Command<?> getCommand(@NonNull String inputStr) {
        int seperatorIndex = inputStr.indexOf(' ');
        String commandStr = seperatorIndex == -1 ? inputStr : inputStr.substring(0, seperatorIndex);
        String paramStr = seperatorIndex == -1 ? "" : inputStr.substring(seperatorIndex + 1).trim();

        if ("Tag".equalsIgnoreCase(commandStr)) {
            List<String> tagList = Splitter.on(",")
                    .trimResults()
                    .omitEmptyStrings()//可以 选择是否对 空字符串 做处理
                    .splitToList(paramStr);
            return new TagCommand(tagList.toArray(new String[]{}));
        } else if ("Name".equalsIgnoreCase(commandStr)) {
            if (isBlank(paramStr)) {
                throw new IllegalArgumentException("Need to specify file path");
            }
            return new NameCommand(paramStr);
        } else if ("Where".equalsIgnoreCase(commandStr)) {
            if (isBlank(paramStr)) {
                throw new IllegalArgumentException("Need to specify search criteria");
            }
            return new WhereCommand(paramStr);
        } else if ("Analyze".equalsIgnoreCase(commandStr)) {
            return new AnalyzeCommand();
        } else if ("Export".equalsIgnoreCase(commandStr)) {
            if (isBlank(paramStr)) {
                throw new IllegalArgumentException("Need to specify export file path");
            }
            return new ExportCommand(paramStr);
        } else {
            throw new IllegalArgumentException("Use either Tag, Where, Name, Export, or Analyze");
        }
    }

    private static String readDataFromConsole(String prompt) throws IOException {
        System.out.print("[" + ConsoleColors.GREEN_BOLD + prompt + ConsoleColors.RESET + "] ");
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    private static void output(Object content) {
        output(content, true);
    }

    private static void output(Object content, boolean prefix) {
        if (content != null) {
            if (content instanceof Collection) {
                ((Collection<?>) content).forEach(o -> outputInternal(o, prefix));
            } else if (content.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(content); i++) {
                    outputInternal(Array.get(content, i), prefix);
                }
            } else if (content instanceof AnalyzeCommand.AnalyzeResult) {
                AnalyzeCommand.AnalyzeResult result = (AnalyzeCommand.AnalyzeResult) content;
                outputInternal("影片数量：" + result.getTotalVideos() + "部影片", prefix);

                if (!result.getTagsInUse().isEmpty()) {
                    StringBuilder buffer = new StringBuilder("常用标签：").append(System.lineSeparator());
                    String[] tagsInUse = result.getTagsInUse().keySet().toArray(new String[]{});
                    for (int i = 0; i < 10 && i < tagsInUse.length; i++) {
                        buffer.append(tagsInUse[i]).append(" : ").append(result.getTagsInUse().get(tagsInUse[i]))
                                .append("部影片").append(System.lineSeparator());
                    }
                    outputInternal(buffer.toString(), prefix);
                }

                if (result.getSimilarVideos().isEmpty()) {
                    outputInternal("没有发现重复的影片", prefix);
                } else {
                    for (int i = 0; i < result.getSimilarVideos().size(); i++) {
                        StringBuilder buffer = new StringBuilder("下列影片大小一致，都是：")
                                .append(result.getSimilarSizes().get(i)).append(System.lineSeparator());
                        FileInfo[] fis = result.getSimilarVideos().get(i);
                        for (FileInfo fi : fis) {
                            buffer.append(fi).append(System.lineSeparator());
                        }
                        outputInternal(buffer.toString(), prefix);
                    }
                }
            } else {
                outputInternal(content, prefix);
            }
        } else {
            outputInternal(content, prefix);
        }
    }

    private static void outputInternal(Object o, boolean prefix) {
        if (o == null) {
            if (prefix) {
                System.out.println(ConsoleColors.BLUE_BOLD + OUTPUT_PREFIX + ConsoleColors.RESET + " ");
            } else {
                System.out.println();
            }
        } else {
            if (prefix) {
                System.out.println(ConsoleColors.BLUE_BOLD + OUTPUT_PREFIX + ConsoleColors.RESET + " " + o.toString());
            } else {
                System.out.println(o.toString());
            }
        }
    }
}
