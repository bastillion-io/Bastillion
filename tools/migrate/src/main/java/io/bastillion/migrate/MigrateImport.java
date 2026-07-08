package io.bastillion.migrate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.util.EncryptionUtil;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads the JSON produced by MigrateExport into a NEW Bastillion instance's H2 database,
 * re-encrypting the app-level-encrypted columns (users.otp_secret, application_key.private_key,
 * application_key.passphrase) with the NEW instance's own keystore.
 *
 * This is a full replace, not a merge: it deletes all rows from all 12 Bastillion tables in
 * the target database before inserting the migrated rows (so the freshly-bootstrapped default
 * "admin"/"changeme" user is removed). To confirm that's intended, the caller must pass the
 * literal flag --yes-replace-all-data.
 *
 * The target database must already exist and have its schema created (i.e. the new Bastillion
 * has been started at least once against -DCONFIG_DIR=<new-config-dir>, so bastillion.jceks and
 * keydb/bastillion.mv.db are already there) - this tool does not create schema from scratch, to
 * avoid drifting out of sync with DBInitServlet's DDL.
 *
 * Must run with -DCONFIG_DIR pointing at the new instance's config dir (see migrate.sh).
 */
public class MigrateImport {

    public static void main(String[] args) throws Exception {
        if (args.length != 3 || !"--yes-replace-all-data".equals(args[2])) {
            System.err.println("Usage: import <new-config-dir> <input-json-file> --yes-replace-all-data");
            System.err.println();
            System.err.println("This deletes ALL existing rows in the target Bastillion database before");
            System.err.println("importing the migrated data. Pass --yes-replace-all-data to confirm.");
            System.exit(1);
        }
        String configDir = args[0];
        String inputPath = args[1];
        MigrateCommon.requireConfigDirMatch(configDir);

        String dbUser = AppConfig.getProperty("dbUser");
        String dbPassword = AppConfig.decryptProperty("dbPassword");

        Map<String, Object> export;
        Gson gson = new Gson();
        try (FileReader r = new FileReader(inputPath)) {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            export = gson.fromJson(r, type);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> tablesRaw = (Map<String, Object>) export.get("tables");

        try (Connection conn = MigrateCommon.connect(configDir, dbUser, dbPassword)) {
            conn.setAutoCommit(false);
            try {
                System.out.println("Current row counts in " + configDir + " (about to be replaced):");
                for (String table : MigrateCommon.TABLES) {
                    try (var stmt = conn.prepareStatement("select count(*) from " + table);
                         var rs = stmt.executeQuery()) {
                        rs.next();
                        System.out.println("  " + table + ": " + rs.getLong(1) + " row(s)");
                    }
                }

                deleteAll(conn);

                for (String table : MigrateCommon.TABLES) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rows = (List<Map<String, Object>>) tablesRaw.getOrDefault(table, new ArrayList<>());
                    if (table.equals("users")) {
                        reEncryptUserSecrets(rows);
                    } else if (table.equals("application_key")) {
                        reEncryptAppKeySecrets(rows);
                    }
                    insertRows(conn, table, rows);
                    fixIdentitySequence(conn, table, rows);
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        }

        System.out.println();
        System.out.println("Import complete into " + configDir);
        for (String table : MigrateCommon.TABLES) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) tablesRaw.getOrDefault(table, new ArrayList<>());
            System.out.println("  " + table + ": " + rows.size() + " row(s)");
        }
    }

    private static void deleteAll(Connection conn) throws SQLException {
        for (int i = MigrateCommon.TABLES.length - 1; i >= 0; i--) {
            try (PreparedStatement stmt = conn.prepareStatement("delete from " + MigrateCommon.TABLES[i])) {
                stmt.executeUpdate();
            }
        }
    }

    private static void reEncryptUserSecrets(List<Map<String, Object>> rows) throws Exception {
        for (Map<String, Object> row : rows) {
            Object otp = row.get("otp_secret");
            if (otp != null) {
                row.put("otp_secret", EncryptionUtil.encrypt((String) otp));
            }
        }
    }

    private static void reEncryptAppKeySecrets(List<Map<String, Object>> rows) throws Exception {
        for (Map<String, Object> row : rows) {
            Object pk = row.get("private_key");
            if (pk != null) {
                row.put("private_key", EncryptionUtil.encrypt((String) pk));
            }
            Object pass = row.get("passphrase");
            if (pass != null) {
                row.put("passphrase", EncryptionUtil.encrypt((String) pass));
            }
        }
    }

    private static void insertRows(Connection conn, String table, List<Map<String, Object>> rows) throws SQLException {
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Integer> types = MigrateCommon.columnTypes(conn, table);
        List<String> columns = new ArrayList<>(rows.get(0).keySet());

        StringBuilder sql = new StringBuilder("insert into ").append(table).append(" (");
        sql.append(String.join(", ", columns)).append(") values (");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < columns.size(); i++) {
                    String col = columns.get(i);
                    MigrateCommon.bindParam(stmt, i + 1, types.get(col), row.get(col));
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void fixIdentitySequence(Connection conn, String table, List<Map<String, Object>> rows) throws SQLException {
        String idColumn = MigrateCommon.IDENTITY_COLUMNS.get(table);
        if (idColumn == null || rows.isEmpty()) {
            return;
        }
        long maxId = 0;
        for (Map<String, Object> row : rows) {
            Object val = row.get(idColumn);
            if (val instanceof Number) {
                maxId = Math.max(maxId, ((Number) val).longValue());
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "alter table " + table + " alter column " + idColumn + " restart with " + (maxId + 1))) {
            stmt.executeUpdate();
        }
    }
}
