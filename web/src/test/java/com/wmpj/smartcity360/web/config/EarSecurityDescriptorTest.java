package com.wmpj.smartcity360.web.config;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EarSecurityDescriptorTest {

    private static final Set<String> EXPECTED_ROLES = Set.of(
            "ADMIN",
            "CITY_MANAGER",
            "SUPERVISOR",
            "LOCAL_STAFF",
            "RESIDENT"
    );

    @Test
    void applicationDeclaresTheFiveExistingDatabaseRoles() throws Exception {
        Document document = parse("../ear/src/main/application/META-INF/application.xml");
        NodeList roleNames = document.getElementsByTagName("role-name");
        Set<String> declaredRoles = new HashSet<>();

        for (int index = 0; index < roleNames.getLength(); index++) {
            declaredRoles.add(roleNames.item(index).getTextContent().trim());
        }

        assertEquals(EXPECTED_ROLES, declaredRoles);
    }

    @Test
    void payaraMapsEveryApplicationRoleToTheSameNamedTrustedGroup() throws Exception {
        Document document = parse(
                "../ear/src/main/application/META-INF/glassfish-application.xml"
        );
        NodeList mappings = document.getElementsByTagName("security-role-mapping");
        Map<String, String> roleGroups = new HashMap<>();

        for (int index = 0; index < mappings.getLength(); index++) {
            Element mapping = (Element) mappings.item(index);
            String roleName = mapping.getElementsByTagName("role-name")
                    .item(0)
                    .getTextContent()
                    .trim();
            String groupName = mapping.getElementsByTagName("group-name")
                    .item(0)
                    .getTextContent()
                    .trim();
            roleGroups.put(roleName, groupName);
        }

        assertEquals(EXPECTED_ROLES, roleGroups.keySet());
        assertEquals(EXPECTED_ROLES, Set.copyOf(roleGroups.values()));
    }

    private static Document parse(String path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(Path.of(path).toFile());
    }
}
