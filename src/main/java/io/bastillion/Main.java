package io.bastillion;

import io.bastillion.common.util.AppConfig;
import io.bastillion.manage.socket.SecureShellWS;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Entry point for running Bastillion as a self-contained jar (`java -jar`, or Google Cloud
 * Buildpacks on Cloud Run) instead of deploying a WAR into an external servlet container /
 * the jetty-maven-plugin dev server.
 *
 * TLS is on by default (self-signed, auto-generated - see configureTlsConnector), matching
 * how Bastillion has always been documented to run standalone on-prem. Behind a proxy that
 * already terminates TLS (Cloud Run, a load balancer), set TLS_ENABLED=false to bind plain
 * HTTP instead; see configurePlainConnector for why that's still safe for the Secure
 * session cookie.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // loophole.mvc.base.BaseKontroller scans this package (via classpath walking, on
        // first request) for @Kontrol-annotated controllers. Under the old jetty-maven-plugin
        // dev server this "happened" to work with the property unset, because scanning an
        // empty package name walked the exploded target/classes directory; that doesn't
        // work against a jar, so it needs to be set explicitly here.
        if (System.getProperty("MVC_CONTROLLER_PKGS") == null) {
            System.setProperty("MVC_CONTROLLER_PKGS", "io.bastillion.manage.control");
        }

        WebAppContext webApp = new WebAppContext();
        webApp.setContextPath("/");
        webApp.setResourceBase(resolveWebAppDir().toString());
        webApp.setParentLoaderPriority(true);
        // Without this, a failed context start (e.g. DBInitServlet rejecting a bad DB
        // config) just leaves the server up returning 503s instead of failing the process.
        webApp.setThrowUnavailableOnStartupException(true);
        // Lax (not Strict) so the session cookie still rides along on a top-level
        // navigation into the app from an external link.
        webApp.getSessionHandler().setSameSite(HttpCookie.SameSite.LAX);

        // Registers SecureShellWS (@ServerEndpoint) programmatically - annotation scanning
        // is off (no jetty-annotations dependency), so this is the one Jakarta WebSocket
        // endpoint that needs explicit wiring; everything else is in web.xml.
        JakartaWebSocketServletContainerInitializer.configure(webApp,
                (servletContext, wsContainer) -> wsContainer.addEndpoint(SecureShellWS.class));

        Server server = new Server();
        boolean tlsEnabled = !"false".equalsIgnoreCase(System.getenv("TLS_ENABLED"));
        if (tlsEnabled) {
            configureTlsConnector(server);
        } else {
            configurePlainConnector(server);
        }
        server.setHandler(webApp);
        server.start();
        server.join();
    }

    /**
     * Binds HTTPS with a self-signed certificate, generated on first startup (via keytool -
     * every JDK ships one, so no extra dependency for what's otherwise a non-trivial amount
     * of X.509 code) and reused after that. Matches how Bastillion has always run standalone
     * - browsers warn on the self-signed cert once, same as before this migration; anyone
     * wanting a trusted cert can point KEYSTORE_PATH/KEYSTORE_PASSWORD at a real one (e.g.
     * a Let's Encrypt cert converted to PKCS12). KEYSTORE_PATH defaults under AppConfig's
     * CONFIG_DIR, same as the H2 db, SSH host key pair, and bastillion.jceks - so pointing
     * CONFIG_DIR at one shared location (e.g. a Docker volume mount) is enough on its own.
     */
    private static void configureTlsConnector(Server server) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8443"));
        File keystoreFile = new File(System.getenv().getOrDefault("KEYSTORE_PATH",
                AppConfig.CONFIG_DIR + "keystore" + File.separator + "bastillion.p12"));
        String keystorePassword = System.getenv("KEYSTORE_PASSWORD");

        if (StringUtils.isEmpty(keystorePassword)) {
            // A keystore Bastillion generated itself has its password persisted via
            // AppConfig (see keystorePassword()) - only a keystore Bastillion never saw
            // before (a real cert dropped in by hand) requires KEYSTORE_PASSWORD explicitly,
            // since there's no other way to know it.
            if (keystoreFile.exists() && StringUtils.isEmpty(AppConfig.getProperty("keystorePassword"))) {
                throw new IllegalStateException("KEYSTORE_PATH is set to an existing file (" + keystoreFile
                        + ") but KEYSTORE_PASSWORD is not set");
            }
            keystorePassword = keystorePassword();
        }

        if (!keystoreFile.exists()) {
            log.info("No keystore at {} - generating a self-signed certificate (browsers will warn once; "
                    + "set KEYSTORE_PATH/KEYSTORE_PASSWORD to use a real certificate instead)", keystoreFile);
            generateSelfSignedKeystore(keystoreFile, keystorePassword);
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        sslContextFactory.setKeyStorePassword(keystorePassword);

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSendServerVersion(false);
        httpsConfig.addCustomizer(new SecureRequestCustomizer());
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(httpsConfig));
        connector.setPort(port);
        server.addConnector(connector);
    }

    /**
     * Reuses AppConfig's existing encrypted-property persistence (the same mechanism
     * DBInitServlet uses for a generated dbPassword) so a random keystore password survives
     * restarts instead of being regenerated - which would make the keystore file
     * unreadable, since the password wouldn't match anymore.
     */
    private static String keystorePassword() throws GeneralSecurityException, org.apache.commons.configuration2.ex.ConfigurationException {
        if (StringUtils.isNotEmpty(AppConfig.getProperty("keystorePassword"))) {
            return AppConfig.isPropertyEncrypted("keystorePassword")
                    ? AppConfig.decryptProperty("keystorePassword")
                    : AppConfig.getProperty("keystorePassword");
        }
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder generatedBuilder = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            generatedBuilder.append(alphabet.charAt(secureRandom.nextInt(alphabet.length())));
        }
        String generated = generatedBuilder.toString();
        AppConfig.encryptProperty("keystorePassword", generated);
        return generated;
    }

    private static void generateSelfSignedKeystore(File keystoreFile, String password) throws IOException, InterruptedException {
        File parent = keystoreFile.getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }
        String keytool = System.getProperty("java.home") + File.separator + "bin" + File.separator + "keytool";
        Process process = new ProcessBuilder(
                keytool, "-genkeypair",
                "-alias", "bastillion",
                "-keyalg", "RSA", "-keysize", "2048", "-sigalg", "SHA256withRSA",
                "-validity", "3650",
                "-keystore", keystoreFile.getAbsolutePath(),
                "-storetype", "PKCS12",
                "-storepass", password,
                "-dname", "CN=Bastillion",
                "-ext", "SAN=dns:localhost,ip:127.0.0.1")
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0) {
            throw new IllegalStateException("keytool failed to generate a self-signed keystore: " + output);
        }
    }

    /**
     * Plain HTTP, for behind a proxy that already terminates TLS (Cloud Run's edge, a load
     * balancer). Honors X-Forwarded-Proto/-For from that proxy: without this, redirects
     * build http:// URLs that strict clients refuse to follow from an https page, and
     * request.isSecure() misreports the visitor's protocol - which matters here since
     * web.xml marks the session cookie Secure (only sent back over what the browser
     * considers an HTTPS connection, which it does - the proxy is what's plain HTTP to this
     * process, not the browser's view of the connection).
     */
    private static void configurePlainConnector(Server server) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(port);
        server.addConnector(connector);
    }

    /**
     * Resolves the src/main/webapp content on the classpath (mapped to "webapp/" by
     * pom.xml). Jetty's resource layer can't reliably serve content straight out of a
     * jar:file: URL (eclipse/jetty.project#8549), so when running from the shaded jar,
     * the webapp tree is extracted to a real temp directory first; running unshaded
     * (mvn compile exec:java) it's already a plain directory and used as-is.
     */
    private static Path resolveWebAppDir() throws IOException, URISyntaxException {
        Enumeration<URL> rootsEnum = Main.class.getClassLoader().getResources("webapp");
        if (!rootsEnum.hasMoreElements()) {
            throw new IllegalStateException("No webapp/ resources found on the classpath");
        }
        URL root = Collections.list(rootsEnum).get(0);

        if ("file".equals(root.getProtocol())) {
            return Paths.get(root.toURI());
        }

        Path tempDir = Files.createTempDirectory("bastillion-webapp");
        copyJarWebApp(root, tempDir);
        return tempDir;
    }

    private static void copyJarWebApp(URL webAppRoot, Path targetDir) throws IOException {
        JarURLConnection conn = (JarURLConnection) webAppRoot.openConnection();
        conn.setUseCaches(false);
        Path normalizedTargetDir = targetDir.toAbsolutePath().normalize();
        try (JarFile jarFile = conn.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith("webapp/")) {
                    continue;
                }
                String relativeName = entry.getName().substring("webapp/".length());
                Path target = normalizedTargetDir.resolve(relativeName).normalize();
                if (!target.startsWith(normalizedTargetDir)) {
                    throw new IOException("Blocked suspicious jar entry path: " + entry.getName());
                }
                Files.createDirectories(target.getParent());
                try (InputStream in = jarFile.getInputStream(entry)) {
                    Files.copy(in, target);
                }
            }
        }
    }
}
