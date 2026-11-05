package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import org.junit.jupiter.api.Test; import java.lang.reflect.Field; import java.time.OffsetDateTime; import java.util.Arrays; import static org.junit.jupiter.api.Assertions.*;
class CityPolicyEntityMappingTest {
 @Test void mapsVerifiedPolicySchema()throws Exception{
  assertEquals("city_policies",CityPolicy.class.getAnnotation(Table.class).name()); Field id=CityPolicy.class.getDeclaredField("id"); assertEquals(GenerationType.IDENTITY,id.getAnnotation(GeneratedValue.class).strategy());
  Column code=CityPolicy.class.getDeclaredField("policyCode").getAnnotation(Column.class); assertEquals("policy_code",code.name()); assertEquals(60,code.length()); assertTrue(code.unique());
  assertRel("zone",Zone.class,"zone_id",true); assertRel("createdBy",User.class,"created_by",false); assertRel("updatedBy",User.class,"updated_by",true);
  assertEquals(String.class,CityPolicy.class.getDeclaredField("dslExpression").getType()); assertFalse(CityPolicy.class.getDeclaredField("dslExpression").isAnnotationPresent(Version.class));
  assertArrayEquals(new String[]{"PARKING","NOISE"},Arrays.stream(CityPolicyType.values()).map(Enum::name).toArray(String[]::new)); assertArrayEquals(new String[]{"DRAFT","ACTIVE","INACTIVE"},Arrays.stream(CityPolicyStatus.values()).map(Enum::name).toArray(String[]::new));
  assertEquals(CityPolicyStatus.DRAFT,new CityPolicy().getStatus()); assertEquals(OffsetDateTime.class,CityPolicy.class.getDeclaredField("createdAt").getType()); assertEquals(OffsetDateTime.class,CityPolicy.class.getDeclaredField("updatedAt").getType());
 }
 private static void assertRel(String n,Class<?> t,String c,boolean o)throws Exception{Field f=CityPolicy.class.getDeclaredField(n);assertEquals(t,f.getType());ManyToOne m=f.getAnnotation(ManyToOne.class);assertEquals(FetchType.LAZY,m.fetch());assertEquals(o,m.optional());assertArrayEquals(new CascadeType[0],m.cascade());assertEquals(c,f.getAnnotation(JoinColumn.class).name());}
}
