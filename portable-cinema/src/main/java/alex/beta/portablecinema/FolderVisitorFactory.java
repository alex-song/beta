package alex.beta.portablecinema;

import alex.beta.portablecinema.filesystem.*;
import alex.beta.portablecinema.pojo.FolderInfo;
import lombok.NonNull;

import java.io.File;

public class FolderVisitorFactory {
    private FolderVisitorFactory() {
        //
    }

    public static FolderVisitor newFolderVisitor(@NonNull Action action) {
        switch (action) {
            case SCAN:
                return new FileScan();
            case AGGREGATE:
                return new FileAggregate();
            case RESET_FOLDER:
                return new FolderReset();
            case RESET_ALL:
                return new DeleteDBInFolder();
            default:
                throw new IllegalArgumentException(String.format("Cannot execute action [%s]. Action can be either SCAN, AGGREGATE, RESET_FOLDER, or RESET_ALL", action));
        }
    }

    public interface FolderVisitor {
        FolderInfo execute(PortableCinemaConfig config, File currentFolder);

        VisitorMessageCallback getMessageCallback();

        FolderVisitor messageCallback(VisitorMessageCallback messageCallback);
    }

    /**
     * SACN: Scan files and sub folders in the folder, and write into in-folder database
     * AGGREGATE: Aggregate file information from in-folder database
     * RESET_FOLDER: Reset timestamp of each folder
     * RESET_ALL: Remove all DB files in folders, and reset database
     */
    public enum Action {
        SCAN, AGGREGATE, RESET_FOLDER, RESET_ALL
    }
}
