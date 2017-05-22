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

import com.github.blindpirate.gogradle.util.Assert;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class GogradleRootProject extends LocalDirectoryDependency {
    @Inject
    public GogradleRootProject(Project project) {
        super();
        super.setDir(project.getRootDir());
    }

    public void setName(String name) {
        Assert.isTrue(getName() == null, "Root project's name can be set only once!");
        super.setName(name);
    }

    @Override
    public void setDir(String dir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResolvedDependency doResolve(ResolveContext context) {
        return this;
    }

    @Override
    public long getUpdateTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String formatVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
