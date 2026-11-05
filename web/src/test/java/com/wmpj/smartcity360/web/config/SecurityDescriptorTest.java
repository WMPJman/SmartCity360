package com.wmpj.smartcity360.web.config;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityDescriptorTest {

    @Test
    void webDescriptorConfiguresAThirtyMinuteCookieOnlySession() throws Exception {
        Document document = parse("src/main/webapp/WEB-INF/web.xml");

        assertEquals("30", text(document, "session-timeout"));
        assertEquals("COOKIE", text(document, "tracking-mode"));
    }

    @Test
    void payaraDescriptorUsesContextScopedSecureHttpOnlyStrictCookies() throws Exception {
        Document document = parse("src/main/webapp/WEB-INF/payara-web.xml");
        NodeList properties = document.getElementsByTagName("property");
        boolean secure = false;
        boolean httpOnly = false;
        boolean sameSite = false;
        boolean path = false;

        for (int index = 0; index < properties.getLength(); index++) {
            Element property = (Element) properties.item(index);
            String name = property.getAttribute("name");
            String value = property.getAttribute("value");

            secure |= name.equals("cookieSecure") && value.equals("dynamic");
            httpOnly |= name.equals("cookieHttpOnly") && value.equals("true");
            sameSite |= name.equals("cookieSameSite") && value.equals("Strict");
            path |= name.equals("cookiePath") && value.equals("/smartcity360");
        }

        assertTrue(secure);
        assertTrue(httpOnly);
        assertTrue(sameSite);
        assertTrue(path);
    }

    private static Document parse(String path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(Path.of(path).toFile());
    }

    private static String text(Document document, String elementName) {
        return document.getElementsByTagName(elementName).item(0).getTextContent().trim();
    }
}
