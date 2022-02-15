package alex.beta.portablecinema;

import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.gui.ButtonActionHandler;
import alex.beta.portablecinema.gui.HyperlinkActionHandler;
import alex.beta.portablecinema.gui.PortableCinemaFrame;
import alex.beta.portablecinema.gui.classpath.Handler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PortableCinemaGUI {
    private static Logger logger = LoggerFactory.getLogger(PortableCinemaGUI.class);

    public static void main(String[] args) throws Exception {
        logger.info("### Portable Cinema GUI ###");
        //init inline image handler
        Handler.install();
        //init config
        String confFileName = PortableCinemaConfig.DEFAULT_CONFIGURATION_FILE_NAME;
        if (System.getProperty(PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME) != null) {
            confFileName = System.getProperty(PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME);
        }

        File confFile = new File(confFileName);
        if (!confFile.exists()) {
            FileWriter writer = new FileWriter(confFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
            gson.toJson(PortableCinemaConfig.getDefault(), writer);
            writer.flush();
        } else if (!confFile.isFile()) {
            throw new IllegalArgumentException(String.format("Configuration file [%s] is not a valid file", confFileName));
        }
        PortableCinemaConfig config = new GsonBuilder().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create().fromJson(new FileReader(confFile), PortableCinemaConfig.class);
        logger.info("Configuration file: [{}]", confFile.getCanonicalPath());
        if (logger.isInfoEnabled())
            logger.info(config.toString());

        //init UI
        ButtonActionHandler buttonActionHandler = new ButtonActionHandler(confFile, config);
        HyperlinkActionHandler hyperlinkActionHandler = new HyperlinkActionHandler(config);

        PortableCinemaFrame frame = new PortableCinemaFrame();
        frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //init DB, HTML templates and display banner
        SwingUtilities.invokeLater(() -> {
            try {
                if (DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY) != null) {
                    logger.info("Initializing database is done");
                }
                buttonActionHandler.loadTemplates();
                frame.enableUIActions(buttonActionHandler, hyperlinkActionHandler);
                frame.appendResultText("<pre>" + System.lineSeparator() + Banner.read().forGUI(confFile.getCanonicalPath(), config) + "</pre>" + System.lineSeparator());
            } catch (DatabaseException ex) {
                logger.error("Database initialization failed", ex);
                frame.setErrorStatusText("数据库初始化失败");
            } catch (IOException ex) {
                logger.error("Failed to load templates", ex);
                frame.setErrorStatusText("读取资源文件失败");
            }

        });
    }
}
