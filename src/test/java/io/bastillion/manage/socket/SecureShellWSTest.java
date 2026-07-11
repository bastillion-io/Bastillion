/**
 * Copyright (C) 2013 Loophole, LLC
 *
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.socket;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;
import io.bastillion.manage.control.SecureShellKtrl;
import io.bastillion.manage.model.SchSession;
import io.bastillion.manage.model.UserSchSessions;
import io.bastillion.manage.util.SessionOutputUtil;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SecureShellWS is the websocket endpoint carrying every live SSH terminal - onMessage
 * dispatches keystrokes to a JSch PrintStream and onClose tears the SSH channel down. The
 * key security property under test: which terminal a message reaches is decided entirely by
 * this.sessionId (set once in onOpen from the *authenticated* HttpSession and never touched
 * again) - the "id" list in the client-supplied JSON only selects among that connection's own
 * already-registered terminals, never anyone else's. onOpen itself isn't exercised here: it
 * spawns a real background thread (SentOutputTask) and hits the DB via UserDB.getUser, neither
 * of which belongs in a fast unit test - so tests set the private session/sessionId/httpSession
 * fields directly via reflection, the same state onOpen would have produced.
 */
@ExtendWith(MockitoExtension.class)
class SecureShellWSTest {

    private static final Long SESSION_ID = 42L;

    private final SecureShellWS ws = new SecureShellWS();

    @BeforeEach
    void isolateSchSessionMap() throws Exception {
        SecureShellKtrl.setUserSchSessionMap(new ConcurrentHashMap<>());
        setField("sessionId", SESSION_ID);
        setField("httpSession", mock(HttpSession.class));
    }

    @AfterEach
    void resetSchSessionMap() {
        SecureShellKtrl.setUserSchSessionMap(new ConcurrentHashMap<>());
    }

    private void setField(String name, Object value) throws Exception {
        Field field = SecureShellWS.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(ws, value);
    }

    private jakarta.websocket.Session openWebSocketSession() throws Exception {
        jakarta.websocket.Session wsSession = mock(jakarta.websocket.Session.class);
        when(wsSession.isOpen()).thenReturn(true);
        setField("session", wsSession);
        return wsSession;
    }

    private PrintStream registerOwnTerminal(int instanceId) {
        PrintStream commander = mock(PrintStream.class);
        SchSession schSession = new SchSession();
        schSession.setCommander(commander);

        UserSchSessions sessions = new UserSchSessions();
        sessions.getSchSessionMap().put(instanceId, schSession);
        SecureShellKtrl.getUserSchSessionMap().put(SESSION_ID, sessions);
        return commander;
    }

    // ---- onMessage --------------------------------------------------------------------

    @Test
    void plainCommandIsPrintedVerbatimToTheMatchingTerminal() throws Exception {
        openWebSocketSession();
        PrintStream commander = registerOwnTerminal(1);

        ws.onMessage("{\"id\":[\"1\"],\"command\":\"ls -la; rm -rf /\"}");

        verify(commander).print("ls -la; rm -rf /");
    }

    @Test
    void keyCodeWritesTheMappedControlBytes() throws Exception {
        openWebSocketSession();
        PrintStream commander = registerOwnTerminal(1);

        // 67 -> CTRL-C in SecureShellWS.keyMap, i.e. byte 0x03 (ETX).
        ws.onMessage("{\"id\":[\"1\"],\"keyCode\":67}");

        verify(commander).write(new byte[]{0x03});
    }

    @Test
    void unmappedKeyCodeIsSilentlyIgnored() throws Exception {
        openWebSocketSession();
        PrintStream commander = registerOwnTerminal(1);

        ws.onMessage("{\"id\":[\"1\"],\"keyCode\":99999}");

        verify(commander, never()).write((byte[]) org.mockito.ArgumentMatchers.any());
        verify(commander, never()).print(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void heartbeatIsIgnoredAndDoesNotResetSessionTimeout() throws Exception {
        openWebSocketSession();
        HttpSession httpSession = mock(HttpSession.class);
        setField("httpSession", httpSession);

        ws.onMessage("heartbeat");

        verify(httpSession, never()).setAttribute(eq("timeout"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void malformedJsonIsLoggedNotThrown() throws Exception {
        openWebSocketSession();

        assertDoesNotThrow(() -> ws.onMessage("not valid json {{{"));
    }

    @Test
    void aClientCannotReachAnotherSessionsTerminalByGuessingItsId() throws Exception {
        openWebSocketSession();
        // Another (still-open) user's terminal, tracked under a different Bastillion
        // sessionId than this connection's.
        long otherSessionId = 999L;
        PrintStream otherCommander = mock(PrintStream.class);
        SchSession otherSchSession = new SchSession();
        otherSchSession.setCommander(otherCommander);
        UserSchSessions otherSessions = new UserSchSessions();
        otherSessions.getSchSessionMap().put(1, otherSchSession);
        SecureShellKtrl.getUserSchSessionMap().put(otherSessionId, otherSessions);
        // This connection (SESSION_ID) has no terminals registered at all.

        assertDoesNotThrow(() -> ws.onMessage("{\"id\":[\"1\"],\"command\":\"whoami\"}"));

        verify(otherCommander, never()).print(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void unregisteredInstanceIdWithinOwnSessionIsIgnoredNotCrashed() throws Exception {
        // References an instance id (2) that was never added to this connection's own
        // session - e.g. a closed/stale terminal tab. Must be skipped, not NPE.
        openWebSocketSession();
        PrintStream commander = registerOwnTerminal(1);

        assertDoesNotThrow(() -> ws.onMessage("{\"id\":[\"2\"],\"command\":\"whoami\"}"));

        verify(commander, never()).print(org.mockito.ArgumentMatchers.anyString());
    }

    // ---- onClose ------------------------------------------------------------------------

    @Test
    void onCloseDisconnectsEverySshChannelAndClearsSessionState() throws Exception {
        Channel channel1 = mock(Channel.class);
        Session jschSession1 = mock(Session.class);
        Channel channel2 = mock(Channel.class);
        Session jschSession2 = mock(Session.class);

        UserSchSessions sessions = new UserSchSessions();
        sessions.getSchSessionMap().put(1, sshSession(channel1, jschSession1));
        sessions.getSchSessionMap().put(2, sshSession(channel2, jschSession2));
        SecureShellKtrl.getUserSchSessionMap().put(SESSION_ID, sessions);

        ws.onClose();

        verify(channel1).disconnect();
        verify(jschSession1).disconnect();
        verify(channel2).disconnect();
        verify(jschSession2).disconnect();
        assertTrue(sessions.getSchSessionMap().isEmpty());
        assertFalse(SecureShellKtrl.getUserSchSessionMap().containsKey(SESSION_ID));
    }

    @Test
    void onCloseRemovesBufferedTerminalOutputForTheSession() throws Exception {
        SessionOutputUtil.addOutput(newBufferedOutput(SESSION_ID, 1));
        char[] text = "leftover output".toCharArray();
        SessionOutputUtil.addToOutput(SESSION_ID, 1, text, 0, text.length);

        UserSchSessions sessions = new UserSchSessions();
        sessions.getSchSessionMap().put(1, sshSession(mock(Channel.class), mock(Session.class)));
        SecureShellKtrl.getUserSchSessionMap().put(SESSION_ID, sessions);

        ws.onClose();

        assertTrue(SessionOutputUtil.getOutput(null, SESSION_ID, null).isEmpty());
    }

    @Test
    void onCloseWithNoRegisteredSessionsDoesNotThrow() {
        assertDoesNotThrow(ws::onClose);
    }

    private static SchSession sshSession(Channel channel, Session session) {
        SchSession schSession = new SchSession();
        schSession.setChannel(channel);
        schSession.setSession(session);
        return schSession;
    }

    private static io.bastillion.manage.model.SessionOutput newBufferedOutput(long sessionId, int instanceId) {
        io.bastillion.manage.model.SessionOutput sessionOutput = new io.bastillion.manage.model.SessionOutput();
        sessionOutput.setSessionId(sessionId);
        sessionOutput.setInstanceId(instanceId);
        return sessionOutput;
    }

}
