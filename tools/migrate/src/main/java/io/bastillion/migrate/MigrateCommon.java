package io.bastillion.migrate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared helpers for MigrateExport / MigrateImport: locating the H2 file behind a
 * Bastillion CONFIG_DIR, opening a plain JDBC connection to it (reusing AppConfig's
 * decrypted dbUser/dbPassword), and generic row <-> JSON-friendly-map conversion.
 *
 * Table list/order matches DBInitServlet's DDL and already respects FK dependencies
 * (each table only references tables earlier in the list), so it is used as-is for
 * export order, import (insert) order, and reversed for delete order.
 */
public class MigrateCommon {

    public static final String[] TABLES = {
            "users", "user_theme", "system", "profiles", "system_map",
            "user_map", "application_key", "status", "scripts",
            "public_keys", "session_log", "terminal_log"
    };

    // table -> identity/auto-increment PK column, used to fix the sequence after a raw
    // insert of explicit id values. Tables not listed have no identity column.
    public static final Map<String, String> IDENTITY_COLUMNS = new LinkedHashMap<>();

    static {
        IDENTITY_COLUMNS.put("users", "id");
        IDENTITY_COLUMNS.put("system", "id");
        IDENTITY_COLUMNS.put("profiles", "id");
        IDENTITY_COLUMNS.put("application_key", "id");
        IDENTITY_COLUMNS.put("scripts", "id");
        IDENTITY_COLUMNS.put("public_keys", "id");
        IDENTITY_COLUMNS.put("session_log", "id");
    }

    private MigrateCommon() {
    }

    /**
     * Resolves the H2 database file base path (without .mv.db) for a Bastillion
     * CONFIG_DIR. A real running Bastillion instance always stores it under
     * "<CONFIG_DIR>/keydb/bastillion.mv.db" (default dbConnectionURL). We also accept
     * a flat "<CONFIG_DIR>/bastillion.mv.db" layout since that's how the old database
     * was handed over for this migration.
     */
    public static String resolveDbBase(String configDir) {
        File nested = new File(configDir, "keydb/bastillion.mv.db");
        if (nested.isFile()) {
            return new File(configDir, "keydb/bastillion").getPath();
        }
        File flat = new File(configDir, "bastillion.mv.db");
        if (flat.isFile()) {
            return new File(configDir, "bastillion").getPath();
        }
        throw new IllegalStateException("No H2 database found under " + configDir
                + " (looked for keydb/bastillion.mv.db and bastillion.mv.db)");
    }

    public static Connection connect(String configDir, String dbUser, String dbPassword) throws SQLException {
        String dbBase = resolveDbBase(configDir);
        String url = "jdbc:h2:file:" + dbBase + ";CIPHER=AES";
        return DriverManager.getConnection(url, dbUser, "filepwd " + dbPassword);
    }

    /** Dumps every row of a table into a list of column-name -> value maps, JSON-friendly. */
    public static List<Map<String, Object>> dumpTable(Connection conn, String table) throws SQLException {
        List<Map<String, Object>> rows = new java.util.ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement("select * from " + table);
             ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= cols; i++) {
                    String col = md.getColumnLabel(i).toLowerCase();
                    Object val = rs.getObject(i);
                    if (val instanceof Timestamp) {
                        val = val.toString();
                    }
                    row.put(col, val);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /** column name (lowercase) -> java.sql.Types constant, read from an empty result set. */
    public static Map<String, Integer> columnTypes(Connection conn, String table) throws SQLException {
        Map<String, Integer> types = new LinkedHashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement("select * from " + table + " where 1 = 0");
             ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                types.put(md.getColumnLabel(i).toLowerCase(), md.getColumnType(i));
            }
        }
        return types;
    }

    /** Binds a JSON-parsed value onto a PreparedStatement parameter per the target column's SQL type. */
    public static void bindParam(PreparedStatement stmt, int index, int sqlType, Object value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, sqlType);
            return;
        }
        switch (sqlType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                stmt.setInt(index, ((Number) value).intValue());
                break;
            case Types.BIGINT:
                stmt.setLong(index, ((Number) value).longValue());
                break;
            case Types.BOOLEAN:
            case Types.BIT:
                stmt.setBoolean(index, (Boolean) value);
                break;
            case Types.TIMESTAMP:
                stmt.setTimestamp(index, Timestamp.valueOf(value.toString()));
                break;
            default:
                stmt.setString(index, value.toString());
        }
    }

    public static String requireConfigDirMatch(String argDir) {
        String actual = System.getProperty("CONFIG_DIR");
        if (actual == null || !actual.equals(argDir)) {
            throw new IllegalStateException("Must be launched with -DCONFIG_DIR=" + argDir
                    + " (was: " + actual + "). Use migrate.sh, which sets this correctly.");
        }
        return actual;
    }
}
