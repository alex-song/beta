package alex.beta.portablecinema.command;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;

public class ResetDatabaseCommand extends Command<Void> {
    @Override
    public Void execute(PortableCinemaConfig config) {
        try {
            DatabaseAdapter.getAdapter(DatabaseAdapter.Type.H2_IN_MEMORY).resetTables();
        } catch (DatabaseException ex) {
            logger.error("Failed to reset database", ex);
        }
        return null;
    }
}
