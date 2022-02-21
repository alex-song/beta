package alex.beta.portablecinema;

import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.database.H2Adapter;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.NonNull;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Set;

public abstract class DatabaseAdapter {
    protected static final Logger logger = LoggerFactory.getLogger(DatabaseAdapter.class);

    private static H2Adapter h2Adapter;

    /**
     * To initialize and get a database adapter
     *
     * @param type
     * @return
     */
    @Synchronized
    public static DatabaseAdapter getAdapter(@NonNull Type type) throws DatabaseException {
        switch (type) {
            case H2_IN_MEMORY:
                if (h2Adapter == null) {
                    h2Adapter = new H2Adapter();
                    h2Adapter.initialize();
                }
                return h2Adapter;
            default:
                throw new IllegalArgumentException(String.format("[%s] is not supported, please use H2_IN_MEMORY", type));
        }

    }

    /**
     * @throws DatabaseException
     */
    public abstract void initialize() throws DatabaseException;

    /**
     * Drop and recreate tables
     *
     * @throws DatabaseException
     */
    public abstract void resetTables() throws DatabaseException;

    /**
     * @param fileInfo
     * @return
     * @throws DatabaseException
     */
    public abstract int insert(FileInfo fileInfo) throws DatabaseException;

    /**
     * @param fileInfo
     * @return
     * @throws DatabaseException
     */
    public abstract int update(FileInfo fileInfo) throws DatabaseException;

    /**
     * Wild cast, like %path%, case insensitive
     *
     * @param name
     * @return
     * @throws DatabaseException
     */
    public abstract FileInfo[] findByName(String name) throws DatabaseException;

    /**
     * @param otid
     * @return
     * @throws DatabaseException
     */
    public abstract FileInfo findByOtid(String otid) throws DatabaseException;

    /**
     * Find file info according to specified tags (match all)
     * Return all records, if no tag is specified
     *
     * @param tags
     * @return
     * @throws DatabaseException
     */
    public abstract FileInfo[] findByTags(String... tags) throws DatabaseException;

    /**
     * Find by custom where cause
     *
     * @param whereCause
     * @return
     * @throws DatabaseException
     */
    public abstract FileInfo[] findBy(String whereCause) throws DatabaseException;

    /**
     * @return
     * @throws DatabaseException
     */
    public abstract int count() throws DatabaseException;

    /**
     * @return
     * @throws DatabaseException
     */
    public abstract Long[] findBySameSize() throws DatabaseException;


    /**
     * @param minOccurs count > minOccurs
     * @param top       maxsize
     * @return key - tag, value - count. Ordered by count desc.
     * @throws DatabaseException
     */
    @SuppressWarnings({"squid:S1319"})
    public abstract LinkedHashMap<String, Integer> listTagsAndOrderByCountDesc(int minOccurs, int top) throws DatabaseException;

    /**
     * @param size
     * @return
     * @throws DatabaseException
     */
    public abstract FileInfo[] findBySize(long size) throws DatabaseException;

    /**
     * @return
     * @throws DatabaseException
     */
    public abstract Set<String> findAllTags() throws DatabaseException;

    public enum Type {
        H2_IN_MEMORY
    }
}
