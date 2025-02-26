# Bastillion with Ed25519 SSH Key Support

This fork of [Bastillion](https://github.com/bastillion-io/Bastillion) adds full support for Ed25519 SSH keys, which offer stronger security with shorter key lengths compared to RSA and DSA.

## Changes Made

1. Updated `BastillionConfig.properties` to include Ed25519 as a valid option for SSH key type:
   ```properties
   #SSH key type 'rsa', 'ecdsa', 'ed25519', 'ed448', (deprecated 'dsa') for generated keys
   sshKeyType=rsa
   #SSH key length for generated keys. 4096 => 'rsa', 521 => 'ecdsa', 256 => 'ed25519', (deprecated 2048 => 'dsa')
   ```

2. Updated `AuthKeysKtrl.java` to handle Ed25519 and Ed448 key types when generating keys:
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
