# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bastillion is a web-based SSH console that centrally manages administrative access to systems. It combines web-based administration with SSH key management and distribution. The application is built with Java and uses a web interface for SSH terminal access.

## Build Commands

```bash
# Build and package the application
mvn clean package

# Build and run with embedded Jetty server
mvn package jetty:run

# Build frontend assets only
npm install
grunt

# Run the application (after building)
./startBastillion.sh  # Linux/Unix/OSX
startBastillion.bat   # Windows
```

## Development Commands

```bash
# Run application in development mode with Jetty
mvn jetty:run

# Clean build (WARNING: This deletes the H2 database)
mvn clean

# Install dependencies
mvn install
```

## Architecture Overview

### Technology Stack
- **Backend**: Java 9+, Maven build system
- **Web Framework**: Custom MVC framework (lmvc) with Kontrol/Model annotations
- **Database**: H2 embedded database (can be configured for remote)
- **SSH Library**: JSch for SSH connections
- **WebSocket**: Jetty WebSocket for real-time terminal communication
- **Frontend**: jQuery, Bootstrap 5, xterm.js for terminal emulation
- **Build Tools**: Maven for Java, Grunt for frontend asset management

### Core Components

1. **Authentication & Authorization** (`io.bastillion.common`)
   - `AuthFilter.java`: Main authentication filter
   - `AuthUtil.java`: Authentication utilities
   - Two-factor authentication support (Google Authenticator/Authy)
   - LDAP/external authentication via JAAS

2. **SSH Management** (`io.bastillion.manage`)
   - `SecureShellKtrl.java`: Main controller for SSH terminal sessions
   - `SecureShellWS.java`: WebSocket handler for terminal I/O
   - `SSHUtil.java`: SSH connection utilities
   - Session management and audit logging

3. **Key Management**
   - Public/private key generation and distribution
   - Authorized_keys file management
   - Key rotation and refresh mechanisms

4. **Database Layer** (`io.bastillion.manage.db`)
   - DAO pattern for data access
   - Connection pooling via Apache DBCP2
   - Encrypted database support

### Configuration

Main configuration file: `src/main/resources/BastillionConfig.properties`
- SSH key settings
- Authentication options
- Database configuration
- Session timeout settings
- Audit logging options

### Security Features

- TLS/SSL layered on top of SSH
- No SSH tunneling/port forwarding allowed
- Encrypted database storage
- Session audit logging
- Two-factor authentication
- SSH key management with passphrase enforcement

## Development Notes

### Testing the Application
```bash
# Run the application in development mode
mvn clean package jetty:run
```

### Code Style Guidelines
- Java 9+ features where appropriate
- Consistent indentation and formatting
- Follow existing patterns in the codebase

## Important Notes

- Default credentials: admin/changeme (change immediately)
- Default HTTPS port: 8443
- Database is encrypted by default
- All SSH sessions are audited when audit is enabled
- The application acts as a bastion host, preventing direct SSH exposure