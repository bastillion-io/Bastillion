/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.SessionOutput;
import io.bastillion.manage.model.User;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The audit session viewer streams terminal output to the browser instead of loading the
 * whole session into memory (sessions with large amounts of output used to hang both the
 * JVM and the browser). These tests pin down the streaming contract: rows are concatenated
 * in log_tm order, output is emitted as newline-terminated lines, and the same cleanup the
 * old whole-session regex did (control sequences stripped, backspaces applied) happens in
 * a single pass - the old while(replaceFirst(".\b")) loop was quadratic and spun forever
 * when a backspace followed a newline.
 */
class SessionAuditDBTest {

    private static final int INSTANCE_ID = 1;

    private static Long newSession(Connection con) throws Exception {
        User user = new User();
        user.setUsername("alice");
        user.setFirstNm("Alice");
        user.setLastNm("Auditor");
        user.setIpAddress("10.0.0.1");
        return SessionAuditDB.createSessionLog(con, user);
    }

    /**
     * Inserts a terminal_log row with an explicit log_tm so ordering is deterministic -
     * rows inserted in the same millisecond could otherwise stream in either order.
     */
    private static void insertRow(Connection con, Long sessionId, String output, long tm) throws Exception {
        PreparedStatement stmt = con.prepareStatement(
                "insert into terminal_log (session_id, instance_id, output, log_tm, display_nm, username, host, port) values (?,?,?,?,?,?,?,?)");
        stmt.setLong(1, sessionId);
        stmt.setInt(2, INSTANCE_ID);
        stmt.setString(3, output);
        stmt.setTimestamp(4, new Timestamp(tm));
        stmt.setString(5, "test-system");
        stmt.setString(6, "alice");
        stmt.setString(7, "localhost");
        stmt.setInt(8, 22);
        stmt.execute();
        stmt.close();
    }

    private static String stream(Connection con, Long sessionId) throws Exception {
        StringWriter writer = new StringWriter();
        SessionAuditDB.streamTerminalLogsForSession(con, sessionId, INSTANCE_ID, writer);
        return writer.toString();
    }

    @Test
    void streamsRowsInLogOrderAndTerminatesEveryLine() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);
            long t = 1700000000000L;
            insertRow(con, sessionId, "$ ls\r\nfile-a  fi", t);
            insertRow(con, sessionId, "le-b\r\n$ exit", t + 1000);

            assertEquals("$ ls\nfile-a  file-b\n$ exit\n", stream(con, sessionId));
        }
    }

    @Test
    void onlyStreamsRowsForTheRequestedInstance() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);
            insertRow(con, sessionId, "instance one\r\n", 1700000000000L);

            PreparedStatement stmt = con.prepareStatement(
                    "insert into terminal_log (session_id, instance_id, output, display_nm, username, host, port) values (?,?,?,?,?,?,?)");
            stmt.setLong(1, sessionId);
            stmt.setInt(2, INSTANCE_ID + 1);
            stmt.setString(3, "instance two\r\n");
            stmt.setString(4, "other-system");
            stmt.setString(5, "alice");
            stmt.setString(6, "localhost");
            stmt.setInt(7, 22);
            stmt.execute();
            stmt.close();

            assertEquals("instance one\n", stream(con, sessionId));
        }
    }

    @Test
    void cleansControlSequencesEvenWhenSplitAcrossRows() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);
            long t = 1700000000000L;
            // the ESC[K erase sequence is split across two output chunks; the bell is stripped too
            insertRow(con, sessionId, "hel\u001B", t);
            insertRow(con, sessionId, "[Klo\u0007 world\r\n", t + 1000);

            assertEquals("hello world\n", stream(con, sessionId));
        }
    }

    @Test
    void appliesBackspacesIncludingOnesThatStartALine() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);
            // a backspace as the first character of a line made the old
            // while(replaceFirst(".\b")) loop spin forever
            insertRow(con, sessionId, "$ exti\b\bit\r\n\bnext\r\n", 1700000000000L);

            assertEquals("$ exit\nnext\n", stream(con, sessionId));
        }
    }

    @Test
    void flushesATrailingLineWithNoNewline() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);
            insertRow(con, sessionId, "no newline at end", 1700000000000L);

            assertEquals("no newline at end\n", stream(con, sessionId));
        }
    }

    @Test
    void streamsOutputInsertedThroughTheDaoRoundTrip() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            Long sessionId = newSession(con);

            SessionOutput sessionOutput = new SessionOutput();
            sessionOutput.setSessionId(sessionId);
            sessionOutput.setInstanceId(INSTANCE_ID);
            sessionOutput.setDisplayNm("test-system");
            sessionOutput.setUser("alice");
            sessionOutput.setHost("localhost");
            sessionOutput.setPort(22);
            sessionOutput.getOutput().append("$ whoami\r\nalice\r\n");
            SessionAuditDB.insertTerminalLog(con, sessionOutput);

            assertEquals("$ whoami\nalice\n", stream(con, sessionId));
        }
    }

    @Test
    void cleanLineStripsColorCodesAndAppliesBackspaces() {
        assertEquals("ax", SessionAuditDB.cleanLine("\babc\b\bx"));
        assertEquals("dir listing", SessionAuditDB.cleanLine("[01;34mdir[0m listing"));
        assertEquals("plain text", SessionAuditDB.cleanLine("plain text"));
        assertEquals("", SessionAuditDB.cleanLine("\b\b\b"));
    }

    @Test
    void cleanLineStripsFullAnsiControlSequences() {
        // bracketed paste / alt screen modes emitted by zsh and top
        assertEquals("bash-3.2$ ", SessionAuditDB.cleanLine("\u001B[?2004hbash-3.2$ \u001B[?2004l"));
        assertEquals("Processes: 456 total", SessionAuditDB.cleanLine("\u001B[?1049h\u001B[1;24r\u001B[2JProcesses: 456 total"));
        // window title (OSC), charset selection, cursor save/restore
        assertEquals("prompt", SessionAuditDB.cleanLine("\u001B]0;kavanagh@host: ~\u0007prompt"));
        assertEquals("text", SessionAuditDB.cleanLine("\u001B(Btext\u001B7"));
        // an unterminated title sequence at the end of a line is still removed
        assertEquals("before", SessionAuditDB.cleanLine("before\u001B]0;partial title"));
        // a stray escape char never lingers in the output
        assertEquals("ab", SessionAuditDB.cleanLine("a\u001Bb"));
    }
}
