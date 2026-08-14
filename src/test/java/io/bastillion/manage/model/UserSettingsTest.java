package io.bastillion.manage.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSettingsTest {

    @Test
    void interfaceThemeDefaultsToDarkAndOnlyAcceptsLightExplicitly() {
        UserSettings settings = new UserSettings();
        assertEquals("dark", settings.getUiTheme());

        settings.setUiTheme("light");
        assertEquals("light", settings.getUiTheme());

        settings.setUiTheme("unexpected");
        assertEquals("dark", settings.getUiTheme());
    }
}
