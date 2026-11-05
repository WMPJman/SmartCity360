package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import org.junit.jupiter.api.Test; import java.lang.reflect.Field; import java.time.OffsetDateTime; import java.util.Arrays; import static org.junit.jupiter.api.Assertions.*;
class PolicyEvaluationEntityMappingTest {
 @Test void mapsVerifiedEvaluationSchema()throws Exception{
  assertEquals("policy_evaluations",PolicyEvaluation.class.getAnnotation(Table.class).name()); assertEquals(GenerationType.IDENTITY,PolicyEvaluation.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy());
  assertRel("policy",CityPolicy.class,"policy_id",false); assertRel("evaluatedBy",User.class,"evaluated_by",true);
  Field json=PolicyEvaluation.class.getDeclaredField("inputContext"); assertEquals(String.class,json.getType()); assertEquals("jsonb",json.getAnnotation(Column.class).columnDefinition()); assertFalse(json.getAnnotation(Column.class).nullable());
  assertEquals(String.class,PolicyEvaluation.class.getDeclaredField("expressionSnapshot").getType()); assertEquals(OffsetDateTime.class,PolicyEvaluation.class.getDeclaredField("evaluatedAt").getType());
  assertArrayEquals(new String[]{"MATCHED","NOT_MATCHED","ERROR"},Arrays.stream(PolicyEvaluationResult.values()).map(Enum::name).toArray(String[]::new)); assertEquals(EnumType.STRING,PolicyEvaluation.class.getDeclaredField("result").getAnnotation(Enumerated.class).value());
 }
 private static void assertRel(String n,Class<?> t,String c,boolean o)throws Exception{Field f=PolicyEvaluation.class.getDeclaredField(n);assertEquals(t,f.getType());ManyToOne m=f.getAnnotation(ManyToOne.class);assertEquals(FetchType.LAZY,m.fetch());assertEquals(o,m.optional());assertArrayEquals(new CascadeType[0],m.cascade());assertEquals(c,f.getAnnotation(JoinColumn.class).name());}
}
