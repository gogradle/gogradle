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

import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyManager;
import com.github.blindpirate.gogradle.util.Assert;

import java.io.File;

/**
 * Represents some code at a specific version.
 */
public abstract class AbstractResolvedDependency extends AbstractGolangDependency implements ResolvedDependency {
    private String version;
    private long updateTime;

    private GolangDependencySet dependencies = GolangDependencySet.empty();

    protected AbstractResolvedDependency(String name, String version, long updateTime) {
        setName(name);

        this.version = version;
        this.updateTime = updateTime;
    }

    public void setDependencies(GolangDependencySet dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public ResolvedDependency resolve(ResolveContext context) {
        return this;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    @Override
    public void installTo(File targetDirectory) {
        getInstaller().install(this, targetDirectory);
    }

    protected abstract DependencyManager getInstaller();

    @Override
    public String toString() {
        return getName() + ":" + formatVersion();
    }

    @Override
    public CacheScope getCacheScope() {
        return CacheScope.PERSISTENCE;
    }

    @Override
    public Object clone() {
        AbstractResolvedDependency ret = (AbstractResolvedDependency) super.clone();
        Assert.isTrue(onlyVendorDependenciesCanHaveDescendants());
        ret.dependencies = this.dependencies.clone();
        if (getClass() != VendorResolvedDependency.class) {
            ret.dependencies.flatten().forEach(dependency -> resetVendorHostIfNecessary(dependency, ret));
        }
        return ret;
    }

    private boolean onlyVendorDependenciesCanHaveDescendants() {
        return this.dependencies.stream().
                filter(d -> (d instanceof ResolvedDependency) && !(d instanceof VendorResolvedDependency))
                .map(d -> (ResolvedDependency) d)
                .allMatch(d -> d.getDependencies().isEmpty());
    }

    private void resetVendorHostIfNecessary(GolangDependency dependency, ResolvedDependency clone) {
        if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency.class.cast(dependency).setHostDependency(clone);
        }
    }
}
