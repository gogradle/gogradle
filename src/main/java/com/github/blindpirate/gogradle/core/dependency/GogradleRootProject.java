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
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;

@Singleton
public class GogradleRootProject extends LocalDirectoryDependency {
    public static final String GOGRADLE_ROOT = "GOGRADLE_ROOT";

    public void initSingleton(String name, File rootDir) {
        Assert.isTrue(getName() == null, "Gogradle root project can be initialized only once!");
        setName(name);
        setDir(rootDir);
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
        return MapUtils.asMap(NAME_KEY, GOGRADLE_ROOT);
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
        return GOGRADLE_ROOT;
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
