package alex.beta.portablecinema.database;

import alex.beta.portablecinema.DatabaseAdapter;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.pojo.FileInfo.Resolution;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@NoArgsConstructor
public class H2Adapter extends DatabaseAdapter {

    private static final String TAGS_ARRAY_DATATYPE = "VARCHAR";

    private static final String DROP_FILEINFO_TABLE_DDL = "DROP TABLE IF EXISTS FILEINFO";

    private static final String CREATE_FILEINFO_TABLE_DDL = "CREATE TABLE IF NOT EXISTS FILEINFO (" +
            "PATH VARCHAR(2048), " +
            "NAME VARCHAR(255), " +
            "COVER1 VARCHAR(2048), " +
            "COVER2 VARCHAR(2048), " +
            "LASTMODIFIEDON TIMESTAMP, " +
            "WIDTH SMALLINT, " +
            "HEIGHT SMALLINT, " +
            "SIZE BIGINT, " +
            "DURATION BIGINT, " +
            "TAGS ARRAY[100], " +
            "OTID VARCHAR(255), " +
            "MANUALOVERRIDE BOOLEAN, " +
            "PRIMARY KEY(PATH, NAME))";

    private static final String INSERT_FILEINFO_DML = "INSERT INTO FILEINFO (" +
            "PATH, NAME, COVER1, COVER2, LASTMODIFIEDON, WIDTH, HEIGHT, SIZE, DURATION, TAGS, OTID, MANUALOVERRIDE) VALUES " +
            "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_FILEINFO_DML = "UPDATE FILEINFO SET " +
            "PATH = ?, " +
            "NAME = ?, " +
            "COVER1 = ?, " +
            "COVER2 = ?, " +
            "LASTMODIFIEDON = ?, " +
            "WIDTH = ?, " +
            "HEIGHT = ?, " +
            "SIZE = ?, " +
            "DURATION = ?, " +
            "TAGS = ?, " +
            "MANUALOVERRIDE = ? " +
            "WHERE OTID = ?";


    private static final String ORDER_BY = " ORDER BY PATH, NAME";

    private static final String FIND_BY_NAME_QUERY = "SELECT * FROM FILEINFO WHERE LOWER(NAME) LIKE ?" + ORDER_BY;

    private static final String FIND_BY_OTID_QUERY = "SELECT * FROM FILEINFO WHERE OTID = ?";

    private static final String FIND_BY_TAG_QUERY = "SELECT * FROM FILEINFO";

    private static final String COUNT_QUERY = "SELECT COUNT(1) FROM FILEINFO";

    private static final String FIND_BY_SAME_SIZE_QUERY = "SELECT SIZE, COUNT(1) FROM FILEINFO GROUP BY SIZE HAVING COUNT(1) > 1 ORDER BY COUNT(1) DESC";

    private static final String FIND_BY_SIZE_QUERY = "SELECT * FROM FILEINFO WHERE SIZE = ?" + ORDER_BY;

    private static final String ALL_TAGS_QUERY = "SELECT TAGS FROM FILEINFO";

    private JdbcConnectionPool pool;

    @Override
    public void initialize() throws DatabaseException {
        pool = JdbcConnectionPool.create("jdbc:h2:mem:portable_cinema", "sa", "");
        pool.setLoginTimeout(10);//in second
        pool.setMaxConnections(64);

        resetTables();
    }

    @Override
    public void resetTables() throws DatabaseException {
        new ConnectionWrapper<Integer>() {
            @Override
            public Integer run(Connection conn) throws DatabaseException {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(DROP_FILEINFO_TABLE_DDL);
                    stmt.executeUpdate(CREATE_FILEINFO_TABLE_DDL);
                    return 0;
                } catch (SQLException ex) {
                    throw new DatabaseException("Failed to initialize tables in jdbc:h2:mem:portable_cinema using sa", ex);
                }
            }
        }.execute();
    }

    /**
     * @param fileInfo
     * @return
     * @throws DatabaseException
     */
    @Override
    public int insert(@NonNull FileInfo fileInfo) throws DatabaseException {
        return new ConnectionWrapper<Integer>() {
            @Override
            public Integer run(Connection conn) throws DatabaseException {
                int result = 0;
                try (PreparedStatement infoStmt = conn.prepareStatement(INSERT_FILEINFO_DML)) {
                    infoStmt.setString(1, fileInfo.getPath());
                    infoStmt.setString(2, fileInfo.getName());
                    infoStmt.setString(3, fileInfo.getCover1());
                    infoStmt.setString(4, fileInfo.getCover2());
                    infoStmt.setTimestamp(5, fileInfo.getLastModifiedOn() == null ? null : new Timestamp(fileInfo.getLastModifiedOn().getTime()));
                    infoStmt.setInt(6, fileInfo.getResolution() == null ? 0 : fileInfo.getResolution().getWidth());
                    infoStmt.setInt(7, fileInfo.getResolution() == null ? 0 : fileInfo.getResolution().getHeight());
                    infoStmt.setLong(8, fileInfo.getSize());
                    infoStmt.setLong(9, fileInfo.getDuration());
                    if (fileInfo.getTags() != null && !fileInfo.getTags().isEmpty()) {
                        infoStmt.setArray(10, conn.createArrayOf(TAGS_ARRAY_DATATYPE, fileInfo.getTags().toArray()));
                    } else {
                        infoStmt.setArray(10, conn.createArrayOf(TAGS_ARRAY_DATATYPE, new String[0]));
                    }
                    infoStmt.setString(11, fileInfo.getOtid());
                    infoStmt.setBoolean(12, fileInfo.isManualOverride());
                    result = infoStmt.executeUpdate();
                    return result;
                } catch (SQLException ex) {
                    throw new DatabaseException(String.format("Failed to insert %s", fileInfo), ex);
                }
            }
        }.execute();
    }

    /**
     * @param fileInfo
     * @return
     * @throws DatabaseException
     */
    @Override
    public int update(@NonNull FileInfo fileInfo) throws DatabaseException {
        return new ConnectionWrapper<Integer>() {
            @Override
            public Integer run(Connection conn) throws DatabaseException {
                int result = 0;
                try (PreparedStatement infoStmt = conn.prepareStatement(UPDATE_FILEINFO_DML)) {
                    infoStmt.setString(1, fileInfo.getPath());
                    infoStmt.setString(2, fileInfo.getName());
                    infoStmt.setString(3, fileInfo.getCover1());
                    infoStmt.setString(4, fileInfo.getCover2());
                    infoStmt.setTimestamp(5, fileInfo.getLastModifiedOn() == null ? null : new Timestamp(fileInfo.getLastModifiedOn().getTime()));
                    infoStmt.setInt(6, fileInfo.getResolution() == null ? 0 : fileInfo.getResolution().getWidth());
                    infoStmt.setInt(7, fileInfo.getResolution() == null ? 0 : fileInfo.getResolution().getHeight());
                    infoStmt.setLong(8, fileInfo.getSize());
                    infoStmt.setLong(9, fileInfo.getDuration());
                    if (fileInfo.getTags() != null && !fileInfo.getTags().isEmpty()) {
                        infoStmt.setArray(10, conn.createArrayOf(TAGS_ARRAY_DATATYPE, fileInfo.getTags().toArray()));
                    } else {
                        infoStmt.setArray(10, conn.createArrayOf(TAGS_ARRAY_DATATYPE, new String[0]));
                    }
                    infoStmt.setBoolean(11, fileInfo.isManualOverride());
                    infoStmt.setString(12, fileInfo.getOtid());
                    result = infoStmt.executeUpdate();
                    return result;
                } catch (SQLException ex) {
                    throw new DatabaseException(String.format("Failed to update %s", fileInfo), ex);
                }
            }
        }.execute();
    }


    /**
     * @param name
     * @return
     * @throws DatabaseException
     */
    @Override
    public FileInfo[] findByName(@NonNull String name) throws DatabaseException {
        return new ConnectionWrapper<FileInfo[]>() {
            public FileInfo[] run(Connection connection) throws DatabaseException {
                try (PreparedStatement queryStmt = connection.prepareStatement(FIND_BY_NAME_QUERY)) {
                    queryStmt.setString(1, "%" + name.toLowerCase() + "%");
                    try (ResultSet resultSet = queryStmt.executeQuery()) {
                        List<FileInfo> infos = new ArrayList<>();
                        while (resultSet.next()) {
                            infos.add(populateFileInfo(resultSet));
                        }
                        return infos.toArray(new FileInfo[]{});
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, FIND_BY_NAME_QUERY);
                }
            }
        }.execute();
    }

    /**
     * @param otid
     * @return
     * @throws DatabaseException
     */
    @Override
    public FileInfo findByOtid(@NonNull String otid) throws DatabaseException {
        return new ConnectionWrapper<FileInfo>() {
            public FileInfo run(Connection connection) throws DatabaseException {
                try (PreparedStatement queryStmt = connection.prepareStatement(FIND_BY_OTID_QUERY)) {
                    queryStmt.setString(1, otid);
                    try (ResultSet resultSet = queryStmt.executeQuery()) {
                        FileInfo fi = null;
                        if (resultSet.next()) {
                            fi = populateFileInfo(resultSet);
                        }
                        return fi;
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, FIND_BY_OTID_QUERY);
                }
            }
        }.execute();
    }

    /**
     * @param tags
     * @return
     * @throws DatabaseException
     */
    @Override
    public FileInfo[] findByTags(@NonNull String... tags) throws DatabaseException {
        StringBuilder buffer = new StringBuilder(FIND_BY_TAG_QUERY);
        boolean isFirst = true;
        for (int i = tags.length; i > 0; i--) {
            if (isFirst) {
                buffer.append(" WHERE");
                isFirst = false;
            } else {
                buffer.append(" OR");
            }
            buffer.append(" ARRAY_CONTAINS (TAGS, ?)");
        }

        buffer.append(ORDER_BY);

        if (logger.isDebugEnabled())
            logger.debug("Query by tags: {}", buffer);

        return new ConnectionWrapper<FileInfo[]>() {
            public FileInfo[] run(Connection connection) throws DatabaseException {
                try (PreparedStatement queryStmt = connection.prepareStatement(buffer.toString())) {
                    for (int i = 0; i < tags.length; i++) {
                        queryStmt.setString(i + 1, tags[i]);
                    }
                    try (ResultSet resultSet = queryStmt.executeQuery()) {
                        List<FileInfo> infos = new ArrayList<>();
                        while (resultSet.next()) {
                            infos.add(populateFileInfo(resultSet));
                        }
                        return infos.toArray(new FileInfo[]{});
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, buffer.toString());
                }
            }
        }.execute();
    }

    /**
     * @param whereCause
     * @return
     */
    @Override
    public FileInfo[] findBy(@NonNull String whereCause) throws DatabaseException {
        return new ConnectionWrapper<FileInfo[]>() {
            public FileInfo[] run(Connection connection) throws DatabaseException {
                String query = FIND_BY_TAG_QUERY;
                if (StringUtils.isNotBlank(whereCause)) {
                    query += (" WHERE " + whereCause);
                }
                try (Statement queryStmt = connection.createStatement()) {
                    try (ResultSet resultSet = queryStmt.executeQuery(query)) {
                        List<FileInfo> infos = new ArrayList<>();
                        while (resultSet.next()) {
                            infos.add(populateFileInfo(resultSet));
                        }
                        return infos.toArray(new FileInfo[]{});
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, query);
                }
            }
        }.execute();
    }

    @Override
    public int count() throws DatabaseException {
        return new ConnectionWrapper<Integer>() {
            public Integer run(Connection connection) throws DatabaseException {
                String query = COUNT_QUERY;
                try (Statement queryStmt = connection.createStatement()) {
                    try (ResultSet resultSet = queryStmt.executeQuery(query)) {
                        int total = 0;
                        if (resultSet.next()) {
                            total = resultSet.getInt(1);
                        }
                        return total;
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, query);
                }
            }
        }.execute();
    }

    @Override
    public Long[] findBySameSize() throws DatabaseException {
        return new ConnectionWrapper<Long[]>() {
            public Long[] run(Connection connection) throws DatabaseException {
                String query = FIND_BY_SAME_SIZE_QUERY;
                try (Statement queryStmt = connection.createStatement()) {
                    try (ResultSet resultSet = queryStmt.executeQuery(query)) {
                        List<Long> ss = new ArrayList<>();
                        while (resultSet.next()) {
                            ss.add(resultSet.getLong(1));
                        }
                        return ss.toArray(new Long[]{});
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, query);
                }
            }
        }.execute();
    }

    @Override
    public FileInfo[] findBySize(long size) throws DatabaseException {
        return new ConnectionWrapper<FileInfo[]>() {
            public FileInfo[] run(Connection connection) throws DatabaseException {
                String query = FIND_BY_SIZE_QUERY;
                try (PreparedStatement queryStmt = connection.prepareStatement(query)) {
                    queryStmt.setLong(1, size);
                    try (ResultSet resultSet = queryStmt.executeQuery()) {
                        List<FileInfo> infos = new ArrayList<>();
                        while (resultSet.next()) {
                            infos.add(populateFileInfo(resultSet));
                        }
                        return infos.toArray(new FileInfo[]{});
                    }
                } catch (SQLException ex) {
                    throw queryDatabaseException(ex, query);
                }
            }
        }.execute();
    }

    @Override
    public Set<String> findAllTags() throws DatabaseException {
        return new ConnectionWrapper<Set<String>>() {
            public Set<String> run(Connection connection) throws DatabaseException {
                try (Statement queryStmt = connection.createStatement()) {
                    Set<String> allTags = new HashSet<>();
                    try (ResultSet resultSet = queryStmt.executeQuery(ALL_TAGS_QUERY)) {
                        while (resultSet.next()) {
                            Array tagsArray = resultSet.getArray(1);
                            if (tagsArray != null) {
                                Object[] tagsObjectArray = (Object[]) tagsArray.getArray();
                                if (tagsObjectArray.length > 0) {
                                    for (int i = 0; i < tagsObjectArray.length; i++) {
                                        allTags.add(String.valueOf(tagsObjectArray[i]));
                                    }
                                }
                            }
                        }
                    }
                    return allTags;
                } catch (SQLException ex) {
                    throw new DatabaseException(String.format("Failed to execute query [%s]", ALL_TAGS_QUERY), ex);
                }
            }
        }.execute();
    }

    //TODO - Convert these code into SQL
    @Override
    @SuppressWarnings({"squid:S3776"})
    public LinkedHashMap<String, Integer> listTagsAndOrderByCountDesc(int minOccurs, int top) throws DatabaseException {
        return new ConnectionWrapper<LinkedHashMap<String, Integer>>() {
            public LinkedHashMap<String, Integer> run(Connection connection) throws DatabaseException {
                try (Statement queryStmt = connection.createStatement()) {
                    LinkedHashMap<String, Integer> tags = new LinkedHashMap<>();
                    try (ResultSet resultSet = queryStmt.executeQuery(ALL_TAGS_QUERY)) {
                        while (resultSet.next()) {
                            Array tagsArray = resultSet.getArray(1);
                            if (tagsArray != null) {
                                Object[] tagsObjectArray = (Object[]) tagsArray.getArray();
                                //remove duplicated tag in one record, if there is any
                                Set<String> tagsStringArray = new HashSet<>();
                                if (tagsObjectArray.length > 0) {
                                    for (int i = 0; i < tagsObjectArray.length; i++) {
                                        tagsStringArray.add(String.valueOf(tagsObjectArray[i]));
                                    }
                                }

                                //count
                                if (!tagsStringArray.isEmpty()) {
                                    tagsStringArray.forEach(tag -> {
                                        if (tags.containsKey(tag)) {
                                            tags.put(tag, tags.get(tag) + 1);
                                        } else {
                                            tags.put(tag, 1);
                                        }
                                    });
                                }
                            }
                        }
                    }

                    //reverse order, and filter according to min occurrence
                    if (top > 0) {
                        return tags.entrySet().stream()
                                .sorted(Entry.comparingByValue(Comparator.reverseOrder())).filter(e -> e.getValue() > minOccurs).limit(top)
                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    } else {
                        return tags.entrySet().stream()
                                .sorted(Entry.comparingByValue(Comparator.reverseOrder())).filter(e -> e.getValue() > minOccurs)
                                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    }
                } catch (SQLException ex) {
                    throw new DatabaseException(String.format("Failed to execute query [%s] and minOccurs [%s]", ALL_TAGS_QUERY, minOccurs), ex);
                }
            }
        }.execute();
    }

    private FileInfo populateFileInfo(ResultSet resultSet) throws SQLException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setOtid(resultSet.getString("OTID"));
        fileInfo.setName(resultSet.getString("NAME"));
        fileInfo.setPath(resultSet.getString("PATH"));
        fileInfo.setCover1(resultSet.getString("COVER1"));
        fileInfo.setCover2(resultSet.getString("COVER2"));
        fileInfo.setResolution(new Resolution(resultSet.getInt("WIDTH"), resultSet.getInt("HEIGHT")));
        fileInfo.setSize(resultSet.getLong("SIZE"));
        fileInfo.setDuration(resultSet.getLong("DURATION"));
        fileInfo.setLastModifiedOn(resultSet.getTimestamp("LASTMODIFIEDON"));
        fileInfo.setManualOverride(resultSet.getBoolean("MANUALOVERRIDE"));
        Array tagsArray = resultSet.getArray("TAGS");
        if (tagsArray != null) {
            Object[] tagsObjectArray = (Object[]) tagsArray.getArray();
            if (tagsObjectArray.length > 0) {
                String[] tags = new String[tagsObjectArray.length];
                for (int i = 0; i < tagsObjectArray.length; i++) {
                    tags[i] = String.valueOf(tagsObjectArray[i]);
                }
                fileInfo.setTags(new HashSet<>(Arrays.asList(tags)));
            }
        }
        return fileInfo;
    }

    private DatabaseException queryDatabaseException(SQLException ex, String queryStatement) {
        return new DatabaseException(String.format("Failed to execute query [%s]", queryStatement), ex);
    }

    /**
     * A wrapper class to encapsulate the JDBC connection operations
     */
    @SuppressWarnings("squid:S1610")
    abstract class ConnectionWrapper<T> {
        public T execute() throws DatabaseException {
            try (Connection conn = pool.getConnection()) {
                if (conn != null) {
                    return run(conn);
                } else {
                    throw new DatabaseException("Failed to get connection from pool");
                }
            } catch (SQLException ex) {
                throw new DatabaseException("Failed to get connection from pool", ex);
            }
        }

        /**
         * @param connection
         * @return
         * @throws DatabaseException
         */
        public abstract T run(Connection connection) throws DatabaseException;
    }
}
