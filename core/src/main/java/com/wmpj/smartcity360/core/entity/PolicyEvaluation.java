package com.wmpj.smartcity360.core.entity;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="policy_evaluations") public class PolicyEvaluation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id",nullable=false,updatable=false) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="policy_id",nullable=false) private CityPolicy policy;
 @ManyToOne(fetch=FetchType.LAZY,optional=true) @JoinColumn(name="evaluated_by") private User evaluatedBy;
 @Column(name="input_context",nullable=false,columnDefinition="jsonb") private String inputContext;
 @Column(name="expression_snapshot",nullable=false) private String expressionSnapshot;
 @Enumerated(EnumType.STRING) @Column(name="result",nullable=false,length=20) private PolicyEvaluationResult result;
 @Column(name="error_message") private String errorMessage; @Column(name="evaluated_at",nullable=false) private OffsetDateTime evaluatedAt;
 protected PolicyEvaluation(){} public Long getId(){return id;} public CityPolicy getPolicy(){return policy;} public void setPolicy(CityPolicy v){policy=v;} public User getEvaluatedBy(){return evaluatedBy;} public void setEvaluatedBy(User v){evaluatedBy=v;} public String getInputContext(){return inputContext;} public void setInputContext(String v){inputContext=v;} public String getExpressionSnapshot(){return expressionSnapshot;} public void setExpressionSnapshot(String v){expressionSnapshot=v;} public PolicyEvaluationResult getResult(){return result;} public void setResult(PolicyEvaluationResult v){result=v;} public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){errorMessage=v;} public OffsetDateTime getEvaluatedAt(){return evaluatedAt;} public void setEvaluatedAt(OffsetDateTime v){evaluatedAt=v;}
}
