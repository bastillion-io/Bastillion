/**
 * Vendor-side license tooling for Bastillion. NOT part of the deployed application —
 * run this yourself, offline, to generate your signing keypair and issue license keys
 * to customers. The private key this produces must never be committed to the repo or
 * shipped with the product; only the public key (printed by `gen-keys`) belongs in
 * io.bastillion.manage.util.LicenseUtil.
 *
 * Requires no dependencies beyond the JDK. Run directly with the single-file launcher:
 *
 *   java licensing/LicenseGenerator.java gen-keys
 *   java licensing/LicenseGenerator.java issue --licensee "Acme Corp" --max-systems 50 \
 *       --expires 2027-07-07 --private-key licensing/license_private.b64 --out acme.lic
 *
 * License blob format (deliberately not JSON, so this file has zero dependencies):
 *   base64(licenseId|licensee|maxSystems|issuedDate|expiryDate) + "." + base64(Ed25519 signature)
 * expiryDate is "none" for a perpetual license.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LicenseGenerator {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        switch (args[0]) {
            case "gen-keys":
                genKeys(args.length > 1 ? Path.of(args[1]) : Path.of("licensing"));
                break;
            case "issue":
                issue(parseFlags(args));
                break;
            default:
                printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java LicenseGenerator.java gen-keys [outputDir]");
        System.out.println("  java LicenseGenerator.java issue --licensee NAME --max-systems N " +
                "--expires YYYY-MM-DD|none --private-key FILE [--out FILE]");
    }

    private static void genKeys(Path outDir) throws GeneralSecurityException, IOException {
        Files.createDirectories(outDir);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        String privB64 = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
        String pubB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

        Path privFile = outDir.resolve("license_private.b64");
        Files.writeString(privFile, privB64);

        System.out.println("Private key written to: " + privFile.toAbsolutePath());
        System.out.println("Keep this file secret. Do not commit it. Back it up somewhere safe —");
        System.out.println("losing it means you can never issue a license that validates again.");
        System.out.println();
        System.out.println("Public key (paste into LicenseUtil.PUBLIC_KEY_B64):");
        System.out.println(pubB64);
    }

    private static void issue(Map<String, String> flags) throws GeneralSecurityException, IOException {
        String licensee = require(flags, "licensee");
        int maxSystems = Integer.parseInt(require(flags, "max-systems"));
        String expires = require(flags, "expires");
        Path privateKeyFile = Path.of(require(flags, "private-key"));

        String licenseId = UUID.randomUUID().toString();
        String issuedDate = LocalDate.now().toString();
        if (!"none".equalsIgnoreCase(expires)) {
            LocalDate.parse(expires); // validate format early
        }
        if (licensee.contains("|")) {
            throw new IllegalArgumentException("licensee must not contain '|'");
        }

        String payload = String.join("|", licenseId, licensee, String.valueOf(maxSystems), issuedDate, expires);
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        PrivateKey privateKey = loadPrivateKey(privateKeyFile);
        Signature signer = Signature.getInstance("Ed25519");
        signer.initSign(privateKey);
        signer.update(payloadBytes);
        byte[] sig = signer.sign();

        String blob = Base64.getEncoder().encodeToString(payloadBytes) + "." + Base64.getEncoder().encodeToString(sig);

        String out = flags.get("out");
        if (out != null) {
            Path outFile = Path.of(out);
            if (outFile.getParent() != null) {
                Files.createDirectories(outFile.getParent());
            }
            Files.writeString(outFile, blob);
            System.out.println("License written to: " + out);
        } else {
            System.out.println(blob);
        }
        System.out.println();
        System.out.println("licenseId=" + licenseId + " licensee=" + licensee +
                " maxSystems=" + maxSystems + " issued=" + issuedDate + " expires=" + expires);
    }

    private static PrivateKey loadPrivateKey(Path file) throws GeneralSecurityException, IOException {
        byte[] keyBytes = Base64.getDecoder().decode(Files.readString(file).trim());
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static Map<String, String> parseFlags(String[] args) {
        Map<String, String> flags = new HashMap<>();
        for (int i = 1; i < args.length - 1; i++) {
            if (args[i].startsWith("--")) {
                flags.put(args[i].substring(2), args[i + 1]);
            }
        }
        return flags;
    }

    private static String require(Map<String, String> flags, String key) {
        String value = flags.get(key);
        if (value == null) {
            throw new IllegalArgumentException("missing required --" + key);
        }
        return value;
    }
}
