![Build](https://github.com/bastillion-io/lmvc/actions/workflows/github-build.yml/badge.svg)
![CodeQL](https://github.com/bastillion-io/lmvc/actions/workflows/codeql-analysis.yml/badge.svg)

# lmvc

**Lightweight MVC** is a lightweight, annotation-driven Model-View-Controller framework for Java servlet applications. It maps HTTP requests to plain Java controller methods, binds request parameters to typed fields, and renders views with [Thymeleaf](https://www.thymeleaf.org/) — without pulling in a full application framework.

## Features

- **Annotation-based routing** — map a controller method to a path and HTTP verb with `@Kontrol(path = "...", method = MethodType.GET)`.
- **Automatic model binding** — annotate a field with `@Model(name = "...")` to have request parameters (including nested and indexed properties) bound to it automatically, and to pass it through to the view.
- **Validation hooks** — annotate a method with `@Validate(input = "...")` to run validation before the matching `@Kontrol` method executes, and forward back to an input view on error.
- **Thymeleaf view resolution** — any `*.html` request is resolved and rendered through Thymeleaf's servlet integration.
- **Built-in security** — CSRF token generation/verification (`CSRFFilter`) and standard hardening headers for clickjacking, MIME sniffing, XSS, and transport security (`SecurityFilter`).

## Prerequisites

- **Java 21+** (OpenJDK or Oracle JDK) — [downloads](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3+** — [downloads](https://maven.apache.org/download.cgi)

On Debian/Ubuntu:

```bash
apt-get install openjdk-21-jdk maven
```

## Getting Started

### Add the dependency

lmvc is published to Maven Central:

```xml
<dependency>
    <groupId>io.bastillion</groupId>
    <artifactId>lmvc</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Configure controller scanning

Set the `MVC_CONTROLLER_PKGS` system property (comma-separated for multiple packages) to the base package(s) lmvc should scan for controllers, e.g. via a JVM argument:

```bash
-DMVC_CONTROLLER_PKGS=com.example.app.controller
```

### Write a controller

```java
public class HelloKontroller extends BaseKontroller {

    @Model(name = "message")
    private String message;

    public HelloKontroller(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Kontrol(path = "/hello", method = MethodType.GET)
    public String hello() {
        message = "Hello, World!";
        return "forward:/hello.html";
    }
}
```

A request to `/hello.ktrl` invokes `hello()`, which sets `message` and forwards to `/hello.html`, where the bound `message` field is available to the Thymeleaf template.

## Build from Source

Export environment variables:

```bash
export JAVA_HOME=/path/to/jdk
export PATH=$JAVA_HOME/bin:$PATH
```

In the directory that contains `pom.xml`, run:

```bash
mvn clean package install
```

## License

Released under [The Prosperity Public License 3.0.0](LICENSE.md). Third-party licenses are listed in [3rdPartyLicenses.md](3rdPartyLicenses.md).
