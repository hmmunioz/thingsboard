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
package org.thingsboard.server.dao.sql.entitygroup;

import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.EntityGroup;
import org.thingsboard.server.common.data.id.EntityGroupId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.page.PageLink;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.entitygroup.EntityGroupDao;
import org.thingsboard.server.dao.model.sql.EntityGroupEntity;

import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dao.util.SqlDao;

import java.util.UUID;

/**
 * Created by Victor Basanets on 8/31/2017.
 */
@Component
@Slf4j
@SqlDao
public class JpaEntityGroupDao extends JpaAbstractSearchTextDao<EntityGroupEntity, EntityGroup>
        implements EntityGroupDao {

    @Autowired
    private EntityGroupRepository entityGroupRepository;

    @Override
    protected Class<EntityGroupEntity> getEntityClass() {
        return EntityGroupEntity.class;
    }

    @Override
    protected JpaRepository<EntityGroupEntity, UUID> getRepository() {
        return entityGroupRepository;
    }

    @Override
    public EntityGroup findEntityGroupById(UUID entityGroupId) {
        return DaoUtil.getData(entityGroupRepository.findEntityGroupById(entityGroupId));
    }

    @Override
    public EntityGroup findByTenantIdAndExternalId(UUID tenantId, UUID externalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PageData<EntityGroup> findByTenantId(UUID tenantId, PageLink pageLink) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityGroupId getExternalIdByInternal(EntityGroupId internalId) {
        // TODO Auto-generated method stub
        return null;
    }

}
