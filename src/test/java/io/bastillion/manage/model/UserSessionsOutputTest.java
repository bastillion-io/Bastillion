/**
 * Copyright (C) 2015 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * getSessionOutputMap() must keep returning the live, mutable map. A prior "fix" wrapped it
 * in an unmodifiable view to avoid "exposing internal representation" - which silently broke
 * every terminal session (SessionOutputUtil.addToOutput/getOutput mutate through this
 * reference from the websocket and SSH output-reader threads) and had to be reverted. This
 * test exists so that fix can't be silently reapplied.
 */
class UserSessionsOutputTest {

    @Test
    void getSessionOutputMapReturnsTheSameLiveMutableMapEveryTime() {
        UserSessionsOutput sessions = new UserSessionsOutput();

        Map<Integer, SessionOutput> map = sessions.getSessionOutputMap();
        assertSame(map, sessions.getSessionOutputMap());

        assertDoesNotThrow(() -> sessions.getSessionOutputMap().put(1, new SessionOutput()));
        assertDoesNotThrow(() -> sessions.getSessionOutputMap().remove(1));
    }
}
