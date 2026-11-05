package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import org.junit.jupiter.api.Test; import java.lang.reflect.Field; import java.time.OffsetDateTime; import static org.junit.jupiter.api.Assertions.*;
class AuditLogEntityMappingTest { @Test void mapsVerifiedAuditSchemaWithoutDestructiveCascade()throws Exception{
 assertEquals("audit_logs",AuditLog.class.getAnnotation(Table.class).name()); assertEquals(GenerationType.IDENTITY,AuditLog.class.getDeclaredField("id").getAnnotation(GeneratedValue.class).strategy());
 Field actor=AuditLog.class.getDeclaredField("actor"); ManyToOne m=actor.getAnnotation(ManyToOne.class); assertEquals(FetchType.LAZY,m.fetch()); assertTrue(m.optional()); assertArrayEquals(new CascadeType[0],m.cascade()); assertEquals("actor_id",actor.getAnnotation(JoinColumn.class).name());
 assertEquals(80,AuditLog.class.getDeclaredField("action").getAnnotation(Column.class).length()); assertEquals(80,AuditLog.class.getDeclaredField("entityType").getAnnotation(Column.class).length()); assertEquals(Long.class,AuditLog.class.getDeclaredField("entityId").getType());
 Field details=AuditLog.class.getDeclaredField("details"); assertEquals(String.class,details.getType()); assertEquals("jsonb",details.getAnnotation(Column.class).columnDefinition()); assertTrue(details.getAnnotation(Column.class).nullable()); assertEquals(OffsetDateTime.class,AuditLog.class.getDeclaredField("createdAt").getType()); }
}
