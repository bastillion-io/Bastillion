# Bastillion v4 -> v5 H2 database migration tool

Migrates all data (users, systems, profiles, scripts, public keys, audit logs, and the
Bastillion SSH application key/identity) from an old Bastillion H2 database into a new
Bastillion v5 instance's H2 database.

This is a **standalone Maven project**: it has its own `pom.xml` and isn't a module of
Bastillion's own build, and it doesn't depend on Bastillion's build artifact at all. It
reuses Bastillion's own `AppConfig`/`EncryptionUtil`/`KeyStoreUtil` classes (and the bundled
`BastillionConfig.properties` defaults) via **symlinks** straight into
`src/main/java`/`src/main/resources` (see Files below), compiled directly into this tool's
own jar. That guarantees the crypto here matches the real app exactly - nothing is
reimplemented - without ever needing `mvn install` in the main repo first, and without
pulling in everything else Bastillion depends on (Jetty, WebSocket, JAAS - none of it is
needed here, so the resulting jar is a fraction of the size of Bastillion's own).

It works in two phases so no secrets ever need to be decrypted and re-encrypted in the same
process/keystore context:

1. **export** - reads every table from the old database and writes it to a JSON file. The
   three app-level-encrypted columns (`users.otp_secret`, `application_key.private_key`,
   `application_key.passphrase`) are decrypted to plaintext using the OLD instance's
   `bastillion.jceks` keystore, via the symlinked `EncryptionUtil`/`AppConfig`/`KeyStoreUtil`.
2. **import** - deletes all rows from the new instance's 12 Bastillion tables (a full
   replace, not a merge) and inserts the migrated rows, re-encrypting those same three
   columns with the NEW instance's own keystore. Auto-increment/identity sequences are
   fixed up afterward so the app can keep inserting normally.

## Prerequisites

- Nothing needs to be built by hand - `migrate.sh` runs `mvn package` here the first time,
  automatically, whenever the jar is missing.
- The **new** instance's database must already exist - i.e. start this Bastillion once
  against the config dir you intend to migrate into (e.g. `mvn -DCONFIG_DIR=/path/ compile
  exec:java`, or `java -DCONFIG_DIR=/path/ -jar target/bastillion-5.0.0-SNAPSHOT.jar`), let it
  finish booting (creates schema, `bastillion.jceks`, and a default `admin`/`changeme` user),
  then stop it. The import tool intentionally does not create schema itself, to avoid
  drifting out of sync with `DBInitServlet`'s DDL - it only replaces data in an
  already-initialized DB.

## Usage

```bash
cd migrate

# 1. Export the old database (plaintext secrets end up in the output file - see Security below)
./migrate.sh export /path/to/old-config-dir /path/to/export.json

# 2. Start this Bastillion once against its real config dir, then stop it (Ctrl+C) once
#    it has finished booting - this creates the schema + jceks + default admin.

# 3. Import into the new database (full replace of all 12 tables)
./migrate.sh import /path/to/new-config-dir /path/to/export.json --yes-replace-all-data

# 4. Delete the export file - it contains decrypted secrets.
rm /path/to/export.json
```

`<config-dir>` is whatever directory Bastillion's `BastillionConfig.properties` and
`bastillion.jceks` live in for that instance (same thing you'd pass as `-DCONFIG_DIR`). The H2
file itself may be at `<config-dir>/keydb/bastillion.mv.db` (the real app's normal layout) or
flatly at `<config-dir>/bastillion.mv.db` - both are auto-detected.

## What gets migrated

All 12 tables: `users, user_theme, system, profiles, system_map, user_map, application_key,
status, scripts, public_keys, session_log, terminal_log`. This includes the Bastillion
application SSH key pair (`application_key`), which is migrated rather than regenerated so
the remote hosts' `authorized_keys` entries (authorized against the old public key) keep
working without any changes on those hosts.

Password hashes and salts are copied as-is (not re-hashed) - Bastillion v5's `verifyHash()`
already supports both the legacy single-round SHA-256 format and the newer PBKDF2 format, so
old users can log in immediately with their existing passwords.

## Security notes

- The export JSON file contains **decrypted** SSH private key material, the application key
  passphrase, and any users' OTP/2FA seeds. It's written with owner-only file permissions
  (`chmod 600`-equivalent), but treat it as sensitive: keep it off shared/network storage and
  delete it as soon as the import succeeds.
- `import` is destructive to the *target* database: it deletes all existing rows in all 12
  tables before inserting. That's why it requires the explicit `--yes-replace-all-data` flag,
  and prints the row counts it's about to delete first.

## Files

- `pom.xml` - standalone Maven project; declares the specific transitive dependencies
  `AppConfig`/`EncryptionUtil`/`KeyStoreUtil` need by hand (kept in sync with `../../pom.xml`
  - see the comment in `pom.xml` if a `NoClassDefFoundError` ever shows up after a Bastillion
  dependency bump), and shades everything into one runnable jar via
  `mainClass=io.bastillion.migrate.Main`.
- `src/main/java/io/bastillion/common/util/AppConfig.java`,
  `src/main/java/io/bastillion/manage/util/EncryptionUtil.java`,
  `src/main/java/io/bastillion/manage/util/KeyStoreUtil.java`,
  `src/main/resources/BastillionConfig.properties` - **symlinks** into the real Bastillion
  source/resources (`../../../../src/main/...`), not copies. `git ls-files -s` shows them as
  mode `120000`; git tracks and recreates the actual symlink on checkout.
- `src/main/java/io/bastillion/migrate/MigrateCommon.java` - shared table list/order, JDBC
  connection helper, generic row dump/bind.
- `src/main/java/io/bastillion/migrate/MigrateExport.java` - phase 1.
- `src/main/java/io/bastillion/migrate/MigrateImport.java` - phase 2.
- `src/main/java/io/bastillion/migrate/Main.java` - dispatches `export`/`import` to the
  classes above; the jar's actual entry point.
- `migrate.sh` - builds the jar on demand (if missing) and runs the tool.
