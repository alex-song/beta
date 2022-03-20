package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.FolderVisitorFactory;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileDB;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static alex.beta.portablecinema.filesystem.FileSystemUtils.doSkip;

public class FolderReset extends AbstractFolderVisitor {

    public FolderReset() {
        this.actionType = FolderVisitorFactory.Action.RESET_FOLDER;
    }

    @Override
    public void doFile(final PortableCinemaConfig config, FileDB db, File currentFolder, File file) {
        // Nothing to do
    }

    @Override
    public void beforeAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) {
        // Nothing to do
    }

    @Override
    public void afterAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) {
        // Nothing to do
    }

    @Override
    public void beforeAllSubFolders(final PortableCinemaConfig config, @NonNull FileDB db, File currentFolder, File[] subFolders) throws IOException, DatabaseException {
        //Nothing to do
    }

    @Override
    public void afterAllSubFolders(final PortableCinemaConfig config, FileDB db, @NonNull File currentFolder, File[] subFolders) throws IOException, DatabaseException {
        long tmpD = 0;
        String currentFolderPath = currentFolder.getCanonicalPath();
        File[] files = currentFolder.listFiles();
        if (files != null && files.length > 0)
            for (File f : files) {
                if (!f.getName().equalsIgnoreCase(config.getDbFileName())
                        && !f.getName().equalsIgnoreCase(config.getGlossaryFileName())
                        && !doSkip(config, f)
                        && f.lastModified() > tmpD) {
                    tmpD = f.lastModified();
                }
            }
        if (!currentFolderPath.equalsIgnoreCase(config.getRootFolderPath())
                && tmpD > 0
                && currentFolder.lastModified() >= tmpD - 999
                && currentFolder.setLastModified(tmpD)) {
            logger.info("Changed [{}] last modified time from [{}] to [{}]", currentFolderPath, new Date(currentFolder.lastModified()), new Date(tmpD));
        }
    }
}
