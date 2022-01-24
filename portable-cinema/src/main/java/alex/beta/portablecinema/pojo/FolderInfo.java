package alex.beta.portablecinema.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class FolderInfo {
    private String path;
    private int numOfVideos;
    private int level;
    private List<FolderInfo> subFolders;

    public String getShortName() {
        if (path.length() <= 100) {
            return path;
        } else {
            return path.substring(0, 96) + " ...";
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (level >= 1) {
            for (int i = 0; i < level - 1; i++) {
                buffer.append("| ");
            }
            buffer.append("+- ");
        }
        buffer.append(this.getShortName()).append(" [").append(this.getNumOfVideos()).append("]").append(System.lineSeparator());
        if (subFolders != null && !subFolders.isEmpty()) {
            for (FolderInfo subFolder : subFolders) {
                buffer.append(subFolder.toString());
            }
        }
        return buffer.toString();
    }
}
