/*
 * Copyright 2024 Haulmont.
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
package test_support.entity.importexport;


import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import java.util.Set;

@JmixEntity
@Entity(name = "testimportexport_Role")
@Table(name = "TESTIMPORTEXPORT_ROLE")
public class Role extends StandardEntity {

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "LOC_NAME")
    private String locName;

    @Column(name = "DESCRIPTION", length = 1000)
    private String description;

    @Column(name = "ROLE_TYPE")
    private Integer type;

    @Column(name = "IS_DEFAULT_ROLE")
    private Boolean defaultRole;

    @SystemLevel
    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Column(name = "SECURITY_SCOPE")
    private String securityScope;

    @OneToMany(mappedBy = "role")
    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    private Set<Permission> permissions;

    @Transient
    private boolean isPredefined = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleType getType() {
        return RoleType.fromId(type);
    }

    public void setType(RoleType type) {
        this.type = type == null ? null : type.getId();
    }

    public String getSecurityScope() {
        return securityScope;
    }

    public void setSecurityScope(String securityScope) {
        this.securityScope = securityScope;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(Boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    public boolean isPredefined() {
        return isPredefined;
    }

    public void setPredefined(boolean predefined) {
        isPredefined = predefined;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }
}