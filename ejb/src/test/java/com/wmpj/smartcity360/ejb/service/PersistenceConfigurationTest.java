package com.wmpj.smartcity360.ejb.service;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersistenceConfigurationTest {

    @Test
    void persistenceUnitUsesJtaDatasourceAndDisablesSchemaGeneration() throws Exception {
        try (InputStream xml = getClass().getResourceAsStream("/META-INF/persistence.xml")) {
            assertNotNull(xml, "META-INF/persistence.xml must be packaged in the EJB module");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(xml);
            Element persistenceUnit = (Element) document.getElementsByTagNameNS("*", "persistence-unit").item(0);

            assertEquals("SmartCity360PU", persistenceUnit.getAttribute("name"));
            assertEquals("JTA", persistenceUnit.getAttribute("transaction-type"));
            assertEquals("jdbc/SmartCity360DS",
                    document.getElementsByTagNameNS("*", "jta-data-source").item(0).getTextContent());
            assertEquals("org.eclipse.persistence.jpa.PersistenceProvider",
                    document.getElementsByTagNameNS("*", "provider").item(0).getTextContent());
            assertEquals("lib/smartcity360-core-1.0-SNAPSHOT.jar",
                    document.getElementsByTagNameNS("*", "jar-file").item(0).getTextContent());
            assertEquals("none", propertyValue(document,
                    "jakarta.persistence.schema-generation.database.action"));
        }
    }

    private String propertyValue(Document document, String name) {
        NodeList properties = document.getElementsByTagNameNS("*", "property");
        for (int index = 0; index < properties.getLength(); index++) {
            Element property = (Element) properties.item(index);
            if (name.equals(property.getAttribute("name"))) {
                return property.getAttribute("value");
            }
        }
        return null;
    }
}
