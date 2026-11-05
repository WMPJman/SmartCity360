package com.wmpj.smartcity360.core.entity;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class StreetlightEntityMappingTest {
    @Test void mapsVerifiedStreetlightSchemaAndDefaults() throws Exception {
        assertEquals("streetlights",Streetlight.class.getAnnotation(Table.class).name());
        Field id=Streetlight.class.getDeclaredField("id"); assertEquals(Long.class,id.getType()); assertEquals(GenerationType.IDENTITY,id.getAnnotation(GeneratedValue.class).strategy());
        Column code=Streetlight.class.getDeclaredField("lightCode").getAnnotation(Column.class); assertEquals("light_code",code.name()); assertEquals(60,code.length()); assertTrue(code.unique());
        assertRelationship("profile",StreetlightProfile.class,"profile_id"); assertRelationship("zone",Zone.class,"zone_id");
        for(String field:new String[]{"latitude","longitude"}){Field f=Streetlight.class.getDeclaredField(field); assertEquals(BigDecimal.class,f.getType()); assertEquals(9,f.getAnnotation(Column.class).precision()); assertEquals(6,f.getAnnotation(Column.class).scale());}
        assertEquals(LocalDate.class,Streetlight.class.getDeclaredField("installedAt").getType()); assertEquals(OffsetDateTime.class,Streetlight.class.getDeclaredField("updatedAt").getType());
        assertArrayEquals(new String[]{"ACTIVE","FAULTY","MAINTENANCE"},Arrays.stream(StreetlightOperationalStatus.values()).map(Enum::name).toArray(String[]::new));
        Streetlight light=new Streetlight(); assertFalse(light.isOn()); assertEquals((short)100,light.getBrightness()); assertEquals(StreetlightOperationalStatus.ACTIVE,light.getOperationalStatus());
    }
    private static void assertRelationship(String field,Class<?> type,String column)throws Exception{
        Field f=Streetlight.class.getDeclaredField(field); assertEquals(type,f.getType()); ManyToOne m=f.getAnnotation(ManyToOne.class); assertEquals(FetchType.LAZY,m.fetch()); assertFalse(m.optional()); assertArrayEquals(new CascadeType[0],m.cascade()); JoinColumn j=f.getAnnotation(JoinColumn.class); assertEquals(column,j.name()); assertFalse(j.nullable());
    }
}
