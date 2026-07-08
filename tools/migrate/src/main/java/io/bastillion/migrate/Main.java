package io.bastillion.migrate;

import java.util.Arrays;

/**
 * Entry point for the runnable jar (see migrate.sh): dispatches to MigrateExport/
 * MigrateImport based on the first argument, so `java -jar ...` has one subcommand-style
 * entry point instead of two separate main classes.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "export":
                MigrateExport.main(rest);
                break;
            case "import":
                MigrateImport.main(rest);
                break;
            default:
                printUsage();
                System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: export <old-config-dir> <output-json-file>");
        System.err.println("       import <new-config-dir> <input-json-file> --yes-replace-all-data");
    }
}
