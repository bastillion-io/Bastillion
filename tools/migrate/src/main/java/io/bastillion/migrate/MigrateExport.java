package io.bastillion.migrate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.util.EncryptionUtil;

import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads every table out of an old Bastillion H2 database and writes it to a single JSON
 * file, with the app-level-encrypted columns (users.otp_secret, application_key.private_key,
 * application_key.passphrase) decrypted to plaintext using the OLD instance's keystore.
 *
 * Must run with -DCONFIG_DIR pointing at the directory containing the old instance's
 * BastillionConfig.properties + bastillion.jceks (see migrate.sh).
 *
 * The resulting file contains plaintext secrets (SSH private key, passphrase, OTP seeds).
 * Treat it as sensitive; delete it once MigrateImport has run successfully.
 */
public class MigrateExport {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: export <old-config-dir> <output-json-file>");
            System.exit(1);
        }
        String configDir = args[0];
        String outputPath = args[1];
        MigrateCommon.requireConfigDirMatch(configDir);

        String dbUser = AppConfig.getProperty("dbUser");
        String dbPassword = AppConfig.decryptProperty("dbPassword");

        Map<String, Object> export = new LinkedHashMap<>();
        export.put("warning", "Contains DECRYPTED secrets (SSH private key, passphrase, OTP seeds). "
                + "Treat as highly sensitive. Delete after a successful import.");
        Map<String, List<Map<String, Object>>> tables = new LinkedHashMap<>();
        export.put("tables", tables);

        try (Connection conn = MigrateCommon.connect(configDir, dbUser, dbPassword)) {
            for (String table : MigrateCommon.TABLES) {
                tables.put(table, MigrateCommon.dumpTable(conn, table));
            }
        }

        decryptSecrets(tables);

        // serializeNulls() is required: MigrateImport derives each table's column list from
        // the first row's key set, so every row must carry every column key, null or not.
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        java.io.File parent = new java.io.File(outputPath).getAbsoluteFile().getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (Writer w = new FileWriter(outputPath)) {
            gson.toJson(export, w);
        }
        java.io.File f = new java.io.File(outputPath);
        f.setReadable(false, false);
        f.setReadable(true, true);
        f.setWritable(false, false);
        f.setWritable(true, true);

        System.out.println("Exported tables from " + configDir + " to " + outputPath + ":");
        for (String table : MigrateCommon.TABLES) {
            System.out.println("  " + table + ": " + tables.get(table).size() + " row(s)");
        }
        System.out.println();
        System.out.println("*** " + outputPath + " contains PLAINTEXT secrets. Delete it after import. ***");
    }

    private static void decryptSecrets(Map<String, List<Map<String, Object>>> tables) throws Exception {
        for (Map<String, Object> row : tables.get("users")) {
            Object otp = row.get("otp_secret");
            if (otp != null) {
                row.put("otp_secret", EncryptionUtil.decrypt((String) otp));
            }
        }
        for (Map<String, Object> row : tables.get("application_key")) {
            Object pk = row.get("private_key");
            if (pk != null) {
                row.put("private_key", EncryptionUtil.decrypt((String) pk));
            }
            Object pass = row.get("passphrase");
            if (pass != null) {
                row.put("passphrase", EncryptionUtil.decrypt((String) pass));
            }
        }
    }
}
