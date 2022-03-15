package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.FolderVisitorFactory;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileDB;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class FileAggregate extends AbstractFolderVisitor {

    public FileAggregate() {
        this.actionType = FolderVisitorFactory.Action.AGGREGATE;
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param file
     */
    @Override
    public void doFile(final PortableCinemaConfig config, FileDB db, File currentFolder, File file) {
        // Nothing to do
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param files
     */
    @Override
    public void beforeAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) {
        // Nothing to do
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param files
     */
    @Override
    public void afterAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) {
        // Nothing to do
    }

    /**
     * Aggregate file information from in-folder DB file into database
     *
     * @param config
     * @param db
     * @param currentFolder
     * @param subFolders
     * @throws IOException
     * @throws DatabaseException
     */
    @Override
    public void beforeAllSubFolders(final PortableCinemaConfig config, @NonNull FileDB db, File currentFolder, File[] subFolders) throws IOException, DatabaseException {
        String path = currentFolder.getCanonicalPath();
        if (db.getFileInfos() != null && !db.getFileInfos().isEmpty()) {
            DatabaseAdapter databaseAdapter = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY);
            for (FileInfo fileInfo : db.getFileInfos()) {
                if (isBlank(fileInfo.getOtid()))
                    fileInfo.setOtid(FileInfo.randomOtid());
                if (isBlank(fileInfo.getPath()))
                    fileInfo.setPath(path);
                databaseAdapter.insert(fileInfo);
            }
            if (this.getMessageCallback() != null) {
                this.getMessageCallback().output("已导入 " + path);
            }
        }
        logger.info("Finish aggregating [{}], and populating database", path);
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param subFolders
     * @throws IOException
     * @throws DatabaseException
     */
    @Override
    public void afterAllSubFolders(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] subFolders) throws IOException, DatabaseException {
        // Nothing to do
    }
}
