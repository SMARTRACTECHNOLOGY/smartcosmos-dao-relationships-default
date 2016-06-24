package net.smartcosmos.dao.relationships.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.UUID;

@Entity(name = "relationship")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "relationship") // , uniqueConstraints = @UniqueConstraint(columnNames = { "objectUrn", "accountUuid" }) )
public class RelationshipEntity implements Serializable {

    private static final int UUID_LENGTH = 16;
    private static final int SOURCE_TARGET_TYPE_LENGTH = 255;
    private static final int SOURCE_TARGET_URN_LENGTH = 255;
    private static final int RELATIONSHIP_URN_LENGTH = 255;
    private static final int RELATIONSHIP_TYPE_LENGTH = 255;

    /*
        Without setting an appropriate Hibernate naming strategy, the column names specified in the @Column annotations below will be converted
        from camel case to underscore, e.g.: systemUuid -> system_uuid

        To avoid that, select the "old" naming strategy org.hibernate.cfg.EJB3NamingStrategy in your configuration (smartcosmos-ext-objects-rdao.yml):
        jpa.hibernate.naming_strategy: org.hibernate.cfg.EJB3NamingStrategy
     */

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-binary")
    @Column(name = "systemUuid", length = UUID_LENGTH)
    private UUID id;

    @NotEmpty
    @Size(max = SOURCE_TARGET_TYPE_LENGTH)
    @Column(name="sourceType", length = SOURCE_TARGET_TYPE_LENGTH, nullable = false, updatable = false)
    private String sourceType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name="sourceId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID sourceId;

    @NotEmpty
    @Size(max = SOURCE_TARGET_TYPE_LENGTH)
    @Column(name="targetType", length = SOURCE_TARGET_TYPE_LENGTH, nullable = false, updatable = false)
    private String targetType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name="targetId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID targetId;

    @NotEmpty
    @Size(max = RELATIONSHIP_TYPE_LENGTH)
    @Column(name = "relationshipType", length = RELATIONSHIP_TYPE_LENGTH, nullable = false, updatable = false)
    private String relationshipType;

    @NotNull
    @Type(type = "uuid-binary")
    @Column(name = "tenantId", length = UUID_LENGTH, nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Column(name = "createdTimestamp", insertable = true, updatable = false)
    private Long created;

    @LastModifiedDate
    @Column(name = "lastModifiedTimestamp", nullable = false, insertable = true, updatable = true)
    private Long lastModified;

    /*
        Lombok's @Builder is not able to deal with field initialization default values. That's a known issue which won't get fixed:
        https://github.com/rzwitserloot/lombok/issues/663

        We therefore provide our own AllArgsConstructor that is used by the generated builder and takes care of field initialization.
     */
    @Builder
    @ConstructorProperties({"id", "sourceType", "sourceId", "relationshipType",
        "targetType", "targetId", "tenantId", "created", "lastModified", "moniker"})
    protected RelationshipEntity(
            UUID id,
            String sourceType,
            UUID sourceId,
            String relationshipType,
            String targetType,
            UUID targetId,
            UUID tenantId,
            Long created,
            Long lastModified,
            String moniker)
    {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.tenantId = tenantId;
        this.relationshipType = relationshipType;
        this.created = created;
        this.lastModified = lastModified;
    }
}
