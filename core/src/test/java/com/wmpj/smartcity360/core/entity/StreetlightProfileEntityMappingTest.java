package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class StreetlightProfileEntityMappingTest {
    @Test void mapsVerifiedProfileSchema() throws Exception {
        assertEquals("streetlight_profiles", StreetlightProfile.class.getAnnotation(Table.class).name());
        Field id=StreetlightProfile.class.getDeclaredField("id");
        assertEquals(Long.class,id.getType()); assertNotNull(id.getAnnotation(Id.class));
        assertEquals(GenerationType.IDENTITY,id.getAnnotation(GeneratedValue.class).strategy());
        assertColumn("profileCode","profile_code",false,50); assertTrue(StreetlightProfile.class.getDeclaredField("profileCode").getAnnotation(Column.class).unique());
        assertColumn("name","name",false,100); assertColumn("manufacturer","manufacturer",true,100);
        assertColumn("model","model",true,100); assertColumn("firmwareVersion","firmware_version",true,50);
        Field wattage=StreetlightProfile.class.getDeclaredField("wattage"); assertEquals(BigDecimal.class,wattage.getType());
        assertEquals(8,wattage.getAnnotation(Column.class).precision()); assertEquals(2,wattage.getAnnotation(Column.class).scale());
        assertEquals(OffsetDateTime.class,StreetlightProfile.class.getDeclaredField("createdAt").getType());
        assertArrayEquals(new String[]{"LED","SOLAR_LED","OTHER"},Arrays.stream(StreetlightType.values()).map(Enum::name).toArray(String[]::new));
        assertEquals(EnumType.STRING,StreetlightProfile.class.getDeclaredField("lightType").getAnnotation(Enumerated.class).value());
        assertEquals((short)100,new StreetlightProfile().getDefaultBrightness());
    }
    private static void assertColumn(String field,String name,boolean nullable,int length)throws Exception{
        Column c=StreetlightProfile.class.getDeclaredField(field).getAnnotation(Column.class); assertEquals(name,c.name()); assertEquals(nullable,c.nullable()); assertEquals(length,c.length());
    }
}
