/**
 * Copyright © 2016-2022 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.dao.model.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.EntityGroup;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityIdFactory;
import org.thingsboard.server.common.data.id.OwnerId;
import org.thingsboard.server.common.data.id.EntityGroupId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.BaseSqlEntity;
import org.thingsboard.server.dao.model.ModelConstants;
import org.thingsboard.server.dao.model.SearchTextEntity;
import org.thingsboard.server.dao.util.mapping.JsonStringType;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.io.IOException;
import java.util.UUID;

import static org.thingsboard.server.dao.model.ModelConstants.ENTITY_TYPE_PROPERTY;

/**
 * Created by Victor Basanets on 8/30/2017.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TypeDef(name = "json", typeClass = JsonStringType.class)
@MappedSuperclass
@Slf4j
public abstract class AbstractEntityGroupEntity<T extends EntityGroup> extends BaseSqlEntity<T>
        implements SearchTextEntity<T> {

    @Column(name = "id")
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = ModelConstants.DEVICE_TYPE_PROPERTY)
    private String type;

    @Column(name = ModelConstants.ENTITY_VIEW_NAME_PROPERTY)
    private String name;

    @Type(type = "json")
    @Column(name = ModelConstants.ENTITY_VIEW_ADDITIONAL_INFO_PROPERTY)
    private JsonNode additionalInfo;

    public AbstractEntityGroupEntity() {
        super();
    }

    public AbstractEntityGroupEntity(EntityGroup entityGroup) {
        if (entityGroup.getId() != null) {
            this.setUuid(entityGroup.getId().getId());
        }
        this.setCreatedTime(entityGroup.getCreatedTime());
        if (entityGroup.getId() != null) {
            this.id = entityGroup.getId().getId();
        }
        if (entityGroup.getOwnerId() != null) {
            this.ownerId = entityGroup.getOwnerId().getId();
        }
        this.type = entityGroup.getType();
        this.name = entityGroup.getName();
        this.additionalInfo = entityGroup.getAdditionalInfo();
    }

    public AbstractEntityGroupEntity(EntityGroupEntity entityGroupEntity) {
        this.setId(entityGroupEntity.getId());
        this.setCreatedTime(entityGroupEntity.getCreatedTime());
        this.id = entityGroupEntity.getUuid();
        this.ownerId = entityGroupEntity.getOwnerId();
        this.type = entityGroupEntity.getType();
        this.name = entityGroupEntity.getName();
        this.additionalInfo = entityGroupEntity.getAdditionalInfo();
    }

    @Override
    public String getSearchTextSource() {
        return name;
    }

    protected EntityGroup toEntityGroup() {
        EntityGroup entityGroup = new EntityGroup(new EntityGroupId(getUuid()));
        entityGroup.setCreatedTime(createdTime);

        if (ownerId != null) {
            entityGroup.setOwnerId(OwnerId.fromUUID(ownerId));
        }
        entityGroup.setType(type);
        entityGroup.setName(name);

        entityGroup.setAdditionalInfo(additionalInfo);
        System.out.println("SEEE IMPRIMIOOO EL ENTITY GROUP sql");
        System.out.println(name);
        System.out.println(type);
        System.out.println(entityGroup.getUuidId());
        return entityGroup;
    }
}
