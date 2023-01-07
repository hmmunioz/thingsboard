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
package org.thingsboard.server.common.data;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.OwnerId;
import org.thingsboard.server.common.data.id.EntityGroupId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.objects.TelemetryEntityView;
import org.thingsboard.server.common.data.validation.Length;
import org.thingsboard.server.common.data.validation.NoXss;

/**
 * Created by Victor Basanets on 8/27/2017.
 */

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EntityGroup extends SearchTextBasedWithAdditionalInfo<EntityGroupId>
        implements HasName, HasOwnerId, ExportableEntity<EntityGroupId> {

    private static final long serialVersionUID = 5582010124562018986L;
    private OwnerId ownerId;
    @NoXss
    @Length(fieldName = "name")
    @ApiModelProperty(position = 4, required = true, value = "Entity Group name", example = "A4B72CCDFF33")
    private String name;
    @NoXss
    @Length(fieldName = "type")
    @ApiModelProperty(position = 5, example = "Temperature Sensor")
    private String type;
    @ApiModelProperty(position = 8, example = "Temperature Sensor")
    private boolean groupAll;
    @ApiModelProperty(position = 9, example = "Temperature Sensor")
    private boolean edgeGroupAll;

    public EntityGroup() {
        super();
    }

    public EntityGroup(EntityGroupId id) {
        super(id);
    }

    public EntityGroup(EntityGroup entityGroup) {
        super(entityGroup);
        this.id = entityGroup.getId();
        this.ownerId = entityGroup.getOwnerId();
        this.name = entityGroup.getName();
        this.type = entityGroup.getType();
        this.createdTime = entityGroup.getCreatedTime();
    }

    @Override
    public String getSearchText() {
        return getName() /* What the ... */;
    }

    @Override
    public String getName() {
        return name;
    }

    @ApiModelProperty(position = 3, value = "JSON object with Tenant Id.", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Override
    public OwnerId getOwnerId() {
        return ownerId;
    }

    @ApiModelProperty(position = 1, value = "JSON object with the Entity View Id. " +
            "Specify this field to update the Entity View. " +
            "Referencing non-existing Entity View Id will cause error. " +
            "Omit this field to create new Entity View.")
    @Override
    public EntityGroupId getId() {
        return super.getId();
    }

    @ApiModelProperty(position = 2, value = "Timestamp of the Entity View creation, in milliseconds", example = "1609459200000", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    @Override
    public long getCreatedTime() {
        return super.getCreatedTime();
    }

    @ApiModelProperty(position = 6, value = "Additional parameters of the device", dataType = "com.fasterxml.jackson.databind.JsonNode")
    @Override
    public JsonNode getAdditionalInfo() {
        return super.getAdditionalInfo();
    }

    @Override
    public void setId(EntityGroupId id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTenantId(TenantId tenantId) {
        // TODO Auto-generated method stub

    }

    @Override
    public EntityGroupId getExternalId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setExternalId(EntityGroupId externalId) {
        // TODO Auto-generated method stub

    }

}
