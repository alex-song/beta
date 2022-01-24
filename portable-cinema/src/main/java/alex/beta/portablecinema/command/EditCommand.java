package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileDB;
import alex.beta.portablecinema.pojo.FileInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditCommand extends Command<Integer> {
    public static final int DATABASE_UPDATE_ERROR = -1;
    public static final int DB_FILE_UPDATE_ERROR = -2;
    public static final int DB_FILE_NOT_EXIST_ERROR = -3;
    public static final int NO_UPDATE = 0;
    public static final int UPDATE_SUCCESS = 1;

    private FileInfo fileInfo;

    /**
     * @param config
     * @return
     */
    @Override
    public Integer execute(PortableCinemaConfig config) {
        try {
            //Update database
            if (DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).update(fileInfo) == UPDATE_SUCCESS) {
                //Update DB file
                File currentFolder = new File(fileInfo.getPath());
                if (!currentFolder.isDirectory()) {
                    return DB_FILE_NOT_EXIST_ERROR;
                } else {
                    File dbFile = new File(currentFolder, config.getDbFileName());
                    if (!dbFile.exists() || !dbFile.isFile()) {
                        return DB_FILE_NOT_EXIST_ERROR;
                    } else {
                        final long lastModifiedTime = currentFolder.lastModified();
                        FileDB db = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create().fromJson(new FileReader(dbFile), FileDB.class);
                        FileInfo oldFileInfo = db.findByName(fileInfo.getName());
                        if (oldFileInfo == null) {
                            db.getFileInfos().add(fileInfo);
                        } else {
                            //We just enable 3 fields on the UI
                            oldFileInfo.setDuration(fileInfo.getDuration());
                            oldFileInfo.setResolution(fileInfo.getResolution());
                            oldFileInfo.setTags(fileInfo.getTags());
                        }
                        try (FileWriter writer = new FileWriter(dbFile)) {
                            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
                            gson.toJson(db, writer);
                            writer.flush();
                            //reset timestamp of current folder
                            return UPDATE_SUCCESS;
                        } finally {
                            if (currentFolder.setLastModified(lastModifiedTime)) {
                                logger.debug("Reset lastModifiedTime of [{}]", fileInfo.getPath());
                            }
                        }
                    }
                }
            } else {
                return NO_UPDATE;
            }
        } catch (DatabaseException ex) {
            logger.error("Failed to update database [{}]", fileInfo, ex);
            return DATABASE_UPDATE_ERROR;
        } catch (IOException ex) {
            logger.error("Failed to update DB file in [{}] with [{}]", fileInfo.getPath(), fileInfo, ex);
            return DB_FILE_UPDATE_ERROR;
        }
    }
}
