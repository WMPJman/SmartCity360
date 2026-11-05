package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import org.junit.jupiter.api.Test; import java.lang.reflect.Field; import java.time.OffsetDateTime; import java.util.Arrays; import static org.junit.jupiter.api.Assertions.*;
class UserZoneAssignmentEntityMappingTest { @Test void mapsVerifiedAssignmentSchema()throws Exception{
 Table t=UserZoneAssignment.class.getAnnotation(Table.class); assertEquals("user_zone_assignments",t.name()); assertArrayEquals(new String[]{"user_id","zone_id"},Arrays.stream(t.uniqueConstraints()).filter(u->"uq_user_zone".equals(u.name())).findFirst().orElseThrow().columnNames());
 assertEquals(GenerationType.IDENTITY,UserZoneAssignment.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy()); rel("user",User.class,"user_id",false); rel("zone",Zone.class,"zone_id",false); rel("assignedBy",User.class,"assigned_by",true); assertEquals(OffsetDateTime.class,UserZoneAssignment.class.getDeclaredField("assignedAt").getType()); }
 private static void rel(String n,Class<?> t,String c,boolean o)throws Exception{Field f=UserZoneAssignment.class.getDeclaredField(n);assertEquals(t,f.getType());ManyToOne m=f.getAnnotation(ManyToOne.class);assertEquals(FetchType.LAZY,m.fetch());assertEquals(o,m.optional());assertArrayEquals(new CascadeType[0],m.cascade());assertEquals(c,f.getAnnotation(JoinColumn.class).name());}
}
