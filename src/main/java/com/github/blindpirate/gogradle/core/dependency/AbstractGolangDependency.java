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
import org.gradle.api.artifacts.Dependency;

import java.io.Serializable;

public abstract class AbstractGolangDependency implements GolangDependency, Serializable {
    private String name;
    private boolean firstLevel;

    private GolangPackage golangPackage;

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
    public String getGroup() {
        throw new UnsupportedOperationException("Unsupported method getGroup is invoked!");
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Unsupported method getVersion is invoked!");
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
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return getName() + ":" + getVersion();
    }
}
