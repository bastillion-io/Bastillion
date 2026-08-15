/**
 * Copyright (C) 2013 Loophole, LLC
 * <p>
 * Licensed under The Prosperity Public License 3.0.0
 */
package io.bastillion.common.saml;

import com.onelogin.saml2.settings.Metadata;
import com.onelogin.saml2.settings.Saml2Settings;
import io.bastillion.manage.util.SamlAuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves Bastillion's own SAML SP metadata XML (entity ID, ACS URL, and its signing/
 * encryption certificate) - hand this URL to an IdP admin instead of typing the equivalent
 * fields in by hand. A plain servlet, not a @Kontrol controller, for the same reason as
 * SamlAcsServlet: this document is meant to be publicly fetchable (by IdP tooling with no
 * Bastillion session at all), and CSRFFilter would invalidate any request that arrives with
 * an existing Bastillion session but no matching _csrf param - the wrong behavior for a
 * public, read-only document with no session semantics of its own.
 */
@WebServlet(name = "SamlMetadataServlet", urlPatterns = {SamlSettingsUtil.METADATA_PATH})
public class SamlMetadataServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SamlMetadataServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!SamlAuthUtil.samlAuthEnabled) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            Saml2Settings settings = SamlSettingsUtil.getSettings();
            String metadata = new Metadata(settings).getMetadataString();
            response.setContentType("application/samlmetadata+xml;charset=UTF-8");
            response.getWriter().write(metadata);
        } catch (Exception ex) {
            log.error(ex.toString(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
