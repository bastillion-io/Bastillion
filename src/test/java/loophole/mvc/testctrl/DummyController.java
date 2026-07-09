/**
 * Copyright (C) 2018 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package loophole.mvc.testctrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.annotation.Model;
import loophole.mvc.annotation.Validate;
import loophole.mvc.base.BaseKontroller;

/**
 * Controller scanned by {@link BaseKontroller} during tests via the
 * {@code MVC_CONTROLLER_PKGS} system property set in the surefire plugin configuration.
 */
public class DummyController extends BaseKontroller {

    public static boolean helloExecuted;
    public static boolean echoExecuted;
    public static boolean submitExecuted;
    public static boolean validateSubmitExecuted;

    @Model(name = "name")
    private String name;

    @Model(name = "age")
    private Integer age;

    @Model(name = "greeting")
    private String greeting;

    @Model(name = "tags")
    private java.util.Map<String, String> tags = new java.util.HashMap<>();

    public static boolean tagsExecuted;

    public DummyController(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public static void reset() {
        helloExecuted = false;
        echoExecuted = false;
        submitExecuted = false;
        validateSubmitExecuted = false;
        tagsExecuted = false;
    }

    @Kontrol(path = "/hello", method = MethodType.GET)
    public String hello() {
        helloExecuted = true;
        return "forward:/hello.html";
    }

    @Kontrol(path = "/redirect", method = MethodType.GET)
    public String doRedirect() {
        return "redirect:/target";
    }

    @Kontrol(path = "/echo", method = MethodType.POST)
    public String echo() {
        echoExecuted = true;
        greeting = "hello " + name + " (" + age + ")";
        return "forward:/echo.html";
    }

    @Kontrol(path = "/tags", method = MethodType.POST)
    public String echoTags() {
        tagsExecuted = true;
        return "forward:/tags.html";
    }

    @Kontrol(path = "/validate", method = MethodType.POST)
    public String submit() {
        submitExecuted = true;
        return "forward:/submit.html";
    }

    @Validate(input = "forward:/error.html")
    public void validateSubmit() {
        validateSubmitExecuted = true;
        addFieldError("name", "name is required");
    }
}
