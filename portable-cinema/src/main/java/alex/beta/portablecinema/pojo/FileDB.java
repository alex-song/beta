package alex.beta.portablecinema.pojo;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class FileDB {

    @Expose
    private Set<FileInfo> fileInfos = new HashSet<>();

    public synchronized void addFileInfos(FileInfo... fis) {
        if (fileInfos == null) {
            fileInfos = new HashSet<>();
        }
        fileInfos.addAll(Arrays.asList(fis));
    }

    @Override
    public String toString() {
        return "FileDB{" +
                "fileInfos=[" + (fileInfos == null ? "" : StringUtils.join(fileInfos, ", " + System.lineSeparator())) +
                "]}";
    }

    /**
     * Find file info according to name, case insensitive, in the current file database in the folder.
     * Return null, if it's not found.
     *
     * @param name
     * @return
     */
    public FileInfo findByName(@NonNull String name) {
        if (this.getFileInfos() != null && !this.getFileInfos().isEmpty()) {
            for (FileInfo fileInfo : this.getFileInfos()) {
                if (fileInfo.getName().equalsIgnoreCase(name)) {
                    return fileInfo;
                }
            }
        }
        return null;
    }
}
