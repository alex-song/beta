package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TagCommand extends Command<FileInfo[]> {

    private String[] tags;

    /**
     * @param config
     * @return Array of file info that matches the given tag
     */
    @Override
    public FileInfo[] execute(PortableCinemaConfig config) {
        try {
            Set<String> inputs = new HashSet<>();
            if (tags != null) Collections.addAll(inputs, tags);
            Set<String> allTags = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).findAllTags();
            Set<String> filteredTags = allTags.stream()
                    .filter(t -> {
                        for (String s : inputs)
                            if (isNotBlank(s) && t.contains(s)) return true;
                        return false;
                    }).collect(Collectors.toSet());
            return DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).findByTags(filteredTags.toArray(new String[]{}));
        } catch (DatabaseException ex) {
            logger.error("Failed to query file info by tags [{}]", StringUtils.join(tags, ", "), ex);
            return new FileInfo[]{};
        }
    }

    /**
     * @param minOccurs
     * @param top
     * @return null, if there is any error when query database
     */
    public Set<String> topTags(int minOccurs, int top) {
        try {
            LinkedHashMap<String, Integer> tcs = DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).listTagsAndOrderByCountDesc(minOccurs, top);
            return tcs.keySet();
        } catch (DatabaseException ex) {
            logger.error("Failed to find top tags", ex);
            return Collections.emptySet();
        }
    }
}
