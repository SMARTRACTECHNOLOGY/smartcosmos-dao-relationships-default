package net.smartcosmos.dao.relationships.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "relationship")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
@AllArgsConstructor
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "relationship", indexes = {
    @Index(columnList = "tenantId, sourceType, sourceId", name = "source_index"),
    @Index(columnList = "tenantId, targetType, targetId", name = "target_index")
})
public class RelationshipEntity implements Serializable {

    private static final int UUID_LENGTH = 16;
    private static final int SOURCE_TARGET_TYPE_LENGTH = 255;
    private static final int RELATIONSHIP_TYPE_LENGTH = 255;
    private static final int NAME_LENGTH = 255;
    private static final int DESCRIPTION_LENGTH = 1024;

    /*
        Without setting an appropriate Hibernate naming strategy, the column names specified in the @Column annotations below will be converted
        from camel case to underscore, e.g.: systemUuid -> system_uuid

        To avoid that, select the "old" naming strategy org.hibernate.cfg.EJB3NamingStrategy in your configuration (smartcosmos-ext-relationship-rdao
        .yml):
        jpa.hibernate.naming_strategy: org.hibernate.cfg.EJB3NamingStrategy
     */

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = "id", length = UUID_LENGTH)
    private UUID id;

    @NotEmpty
    @Size(max = SOURCE_TARGET_TYPE_LENGTH)
    @Column(name = "sourceType", length = SOURCE_TARGET_TYPE_LENGTH, nullable = false, updatable = false)
    private String sourceType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "sourceId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID sourceId;

    @NotEmpty
    @Size(max = RELATIONSHIP_TYPE_LENGTH)
    @Column(name = "relationshipType", length = RELATIONSHIP_TYPE_LENGTH, nullable = false, updatable = false)
    private String relationshipType;

    @NotEmpty
    @Size(max = SOURCE_TARGET_TYPE_LENGTH)
    @Column(name = "targetType", length = SOURCE_TARGET_TYPE_LENGTH, nullable = false, updatable = false)
    private String targetType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "targetId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID targetId;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "tenantId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", insertable = true, updatable = false)
    private Date created;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastModified", nullable = false, insertable = true, updatable = true)
    private Date lastModified;
}
