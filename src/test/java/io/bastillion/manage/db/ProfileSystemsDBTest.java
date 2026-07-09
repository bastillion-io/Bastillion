/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.manage.db;

import io.bastillion.manage.model.HostSystem;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * getSystemsByProfile is what RefreshAuthKeyUtil/SSHUtil.distributePubKeysToProfile walks
 * to decide which hosts get an application-key push when a profile's key set changes - a
 * system missing from this result never receives the push.
 */
class ProfileSystemsDBTest {

    private long insertSystem(Connection con, String displayNm, String host) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "insert into system(display_nm, username, host, port, authorized_keys) values (?, 'root', ?, 22, '~/.ssh/authorized_keys')",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, displayNm);
            stmt.setString(2, host);
            stmt.execute();
            var rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        }
    }

    private long insertProfile(Connection con, String nm) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "insert into profiles(nm, desc) values (?, '')", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nm);
            stmt.execute();
            var rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        }
    }

    private void mapSystemToProfile(Connection con, long profileId, long systemId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "insert into system_map(profile_id, system_id) values (?, ?)")) {
            stmt.setLong(1, profileId);
            stmt.setLong(2, systemId);
            stmt.execute();
        }
    }

    @Test
    void returnsOnlySystemsMappedToTheGivenProfile() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            long profileId = insertProfile(con, "prod");
            long otherProfileId = insertProfile(con, "staging");
            long inProfile = insertSystem(con, "prod-1", "prod1.example.com");
            long inOtherProfile = insertSystem(con, "staging-1", "staging1.example.com");
            mapSystemToProfile(con, profileId, inProfile);
            mapSystemToProfile(con, otherProfileId, inOtherProfile);

            List<HostSystem> systems = ProfileSystemsDB.getSystemsByProfile(con, profileId);

            assertEquals(1, systems.size());
            assertEquals("prod-1", systems.get(0).getDisplayNm());
            assertEquals(inProfile, systems.get(0).getId());
        }
    }

    @Test
    void systemInMultipleProfilesIsReturnedForEachOne() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            long profileA = insertProfile(con, "a");
            long profileB = insertProfile(con, "b");
            long systemId = insertSystem(con, "shared-host", "shared.example.com");
            mapSystemToProfile(con, profileA, systemId);
            mapSystemToProfile(con, profileB, systemId);

            assertEquals(1, ProfileSystemsDB.getSystemsByProfile(con, profileA).size());
            assertEquals(1, ProfileSystemsDB.getSystemsByProfile(con, profileB).size());
        }
    }

    @Test
    void profileWithNoSystemsReturnsEmptyList() throws Exception {
        try (Connection con = DbTestSupport.newConnection()) {
            long profileId = insertProfile(con, "empty-profile");

            assertTrue(ProfileSystemsDB.getSystemsByProfile(con, profileId).isEmpty());
        }
    }
}
