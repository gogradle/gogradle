/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * The skeleton of a {@code GolangDependency} with several default fields.
 */
public abstract class AbstractGolangDependency implements GolangDependency, Serializable {

    private String name;

    private boolean firstLevel;

    private GolangPackage golangPackage;

    private Set<String> subpackages = ImmutableSet.of(ALL_DESCENDANTS);

    public GolangPackage getPackage() {
        return golangPackage;
    }

    public void setPackage(GolangPackage golangPackage) {
        this.golangPackage = golangPackage;
    }

    @Override
    public boolean isFirstLevel() {
        return firstLevel;
    }

    public void setFirstLevel(boolean firstLevel) {
        this.firstLevel = firstLevel;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getSubpackages() {
        return subpackages;
    }

    protected boolean containsAllSubpackages() {
        return subpackages.contains(ALL_DESCENDANTS);
    }

    public void setSubpackages(Collection<String> subpackages) {
        this.subpackages = ImmutableSet.copyOf(subpackages);
    }

    public void setSubpackages(String subpackage) {
        this.subpackages = ImmutableSet.of(subpackage);
    }

    // these two methods exist in case of user's typo and should should not be called internally
    public void setSubpackage(Collection<String> subpackages) {
        this.setSubpackages(subpackages);
    }

    public void setSubpackage(String subpackage) {
        this.setSubpackages(subpackage);
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException("Unsupported method getGroup is invoked!");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Unsupported method getVersion is invoked!");
    }

    @Override
    public void because(@Nullable String reason) {
        throw new UnsupportedOperationException("Unsupported method because is invoked");
    }

    @Override
    public String getReason() {
        throw new UnsupportedOperationException("Unsupported method getReason is invoked");
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        throw new UnsupportedOperationException("Unsupported method contentEquals is invoked!");
    }

    @Override
    public Dependency copy() {
        throw new UnsupportedOperationException("Unsupported method copy is invoked!");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractGolangDependency that = (AbstractGolangDependency) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getSubpackages(), that.getSubpackages())
                && Objects.equals(isFirstLevel(), that.isFirstLevel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, firstLevel, subpackages);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return getName() + ":" + getVersion();
    }
}
