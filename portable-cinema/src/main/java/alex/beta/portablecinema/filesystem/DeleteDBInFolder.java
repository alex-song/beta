package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.FolderVisitorFactory;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileDB;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

public class DeleteDBInFolder extends AbstractFolderVisitor {
    public DeleteDBInFolder() {
        this.actionType = FolderVisitorFactory.Action.RESET_ALL;
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
        long lastModifiedTime = currentFolder.lastModified();
        File dbFile = new File(currentFolder, config.getDbFileName());
        try {
            Files.delete(dbFile.toPath());
            if (this.getMessageCallback() != null) {
                this.getMessageCallback().output("已删除 " + dbFile.getCanonicalPath());
            }
        } catch (NoSuchFileException ex) {
            //Ignore
        } catch (Exception ex) {
            logger.error("Failed to delete DB file [{}]", dbFile.getCanonicalPath(), ex);
        } finally {
            currentFolder.setLastModified(lastModifiedTime);
        }
    }
}
