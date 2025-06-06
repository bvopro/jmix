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

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.Set;

@JmixEntity
@Entity(name = "testimportexport_Plant")
@Table(name = "TESTIMPORTEXPORT_PLANT")
public class Plant extends StandardEntity {

    @Column(name = "NAME")
    protected String name;

    @ManyToMany
    @JoinTable(name = "TESTIMPORTEXPORT_PLANT_MODEL_LINK",
        joinColumns = @JoinColumn(name = "PLANT_ID"),
        inverseJoinColumns = @JoinColumn(name = "MODEL_ID"))
    protected Set<Model> models;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Model> getModels() {
        return models;
    }

    public void setModels(Set<Model> models) {
        this.models = models;
    }
}
