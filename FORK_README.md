# Bastillion with Ed25519 SSH Key Support

This fork of [Bastillion](https://github.com/bastillion-io/Bastillion) adds full support for Ed25519 SSH keys, which offer stronger security with shorter key lengths compared to RSA and DSA.

## Implementation Details

We've discovered a limitation in the JSch library that prevents full Ed25519 key generation. The `getPrivateKey()` method in the `KeyPairEdDSA` class is not implemented, which causes an `UnsupportedOperationException` when Bastillion tries to generate Ed25519 keys using JSch directly:

```
java.lang.UnsupportedOperationException
    at com.jcraft.jsch.KeyPairEdDSA.getPrivateKey (KeyPairEdDSA.java:75)
```

To work around this limitation, we've implemented a solution that uses the system's `ssh-keygen` command to generate Ed25519 keys when that key type is selected. This allows Bastillion to use Ed25519 keys for both:

1. **Application Keys**: Bastillion's own key pair (the one generated at startup)
2. **User Authentication**: Keys generated for user authentication

## Changes Made

1. Updated `BastillionConfig.properties` to include Ed25519 as a valid option for SSH key type:
   ```properties
   #SSH key type 'rsa', 'ecdsa', 'ed25519', 'ed448', (deprecated 'dsa') for generated keys
   sshKeyType=ed25519
   #SSH key length for generated keys. 4096 => 'rsa', 521 => 'ecdsa', 256 => 'ed25519', (deprecated 2048 => 'dsa')
   sshKeyLength=256
   ```

2. Updated `AuthKeysKtrl.java` to handle Ed25519 and Ed448 key types when generating user keys:
   ```java
   //set key type
   int type = KeyPair.RSA;
   if ("dsa".equals(SSHUtil.KEY_TYPE)) {
       type = KeyPair.DSA;
   } else if ("ecdsa".equals(SSHUtil.KEY_TYPE)) {
       type = KeyPair.ECDSA;
   } else if ("ed25519".equals(SSHUtil.KEY_TYPE)) {
       type = KeyPair.ED25519;
   } else if ("ed448".equals(SSHUtil.KEY_TYPE)) {
       type = KeyPair.ED448;
   }
   ```

3. Updated `SSHUtil.java` to use `ssh-keygen` for Ed25519 and Ed448 key generation:
   ```java
   // For Ed25519 and Ed448 keys, use ssh-keygen command to generate keys
   // This is a workaround for JSch limitations with Ed25519/Ed448 key generation
   if ("ed25519".equals(SSHUtil.KEY_TYPE) || "ed448".equals(SSHUtil.KEY_TYPE)) {
       try {
           // Create a temporary file for the passphrase
           File passphraseFile = File.createTempFile("passphrase", ".tmp");
           FileUtils.writeStringToFile(passphraseFile, passphrase, "UTF-8");
           
           // Build the ssh-keygen command
           String keyTypeArg = "ed25519".equals(SSHUtil.KEY_TYPE) ? "ed25519" : "ed448";
           String[] cmd = {
               "ssh-keygen", 
               "-t", keyTypeArg,
               "-f", PVT_KEY,
               "-N", passphrase,
               "-C", comment
           };
           
           // Execute the command
           Process process = Runtime.getRuntime().exec(cmd);
           int exitCode = process.waitFor();
           
           // Check if the command was successful
           if (exitCode != 0) {
               throw new IOException("Failed to generate " + keyTypeArg + " key pair. Exit code: " + exitCode);
           }
           
           // Delete the temporary passphrase file
           passphraseFile.delete();
           
           System.out.println("Generated " + keyTypeArg + " key pair using ssh-keygen");
       } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
           throw new IOException("Key generation was interrupted", e);
       }
   } else {
       // For other key types, use JSch's built-in key generation
       // ...
   }
   ```

4. Updated the JSch library to version 0.2.23:
   ```xml
   <dependency>
       <groupId>com.github.mwiede</groupId>
       <artifactId>jsch</artifactId>
       <version>0.2.23</version>
   </dependency>
   ```

## Using Ed25519 Keys

To use Ed25519 keys with Bastillion:

1. Edit the `BastillionConfig.properties` file (located in `src/main/resources` if building from source, or in `jetty/bastillion/WEB-INF/classes` if using the bundled version)
2. Change the `sshKeyType` property to `ed25519`:
   ```properties
   sshKeyType=ed25519
   ```
3. Restart Bastillion

## Benefits of Ed25519

Ed25519 keys offer several advantages over RSA and DSA:
- Stronger security with shorter key lengths
- Faster operations
- More resistant to side-channel attacks
- Becoming the industry standard for SSH authentication

## Requirements

This fork requires the mwiede JSch fork version 0.2.16 or higher, which is already included in the dependencies.

## Original Bastillion Documentation

Please refer to the [original README.md](README.md) for general Bastillion documentation.
