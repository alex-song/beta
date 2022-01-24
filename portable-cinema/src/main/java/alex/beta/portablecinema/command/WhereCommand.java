package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WhereCommand extends Command<FileInfo[]> {

    private String searchCriteria;

    @Override
    public FileInfo[] execute(PortableCinemaConfig config) {
        try {
            return DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).findBy(searchCriteria);
        } catch (DatabaseException ex) {
            logger.error("Failed to query file info according to given where cause [{}]", searchCriteria, ex);
            return new FileInfo[]{};
        }
    }
}
