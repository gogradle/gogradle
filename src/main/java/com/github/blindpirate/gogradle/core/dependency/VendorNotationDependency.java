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
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;

import java.util.Objects;
import java.util.Optional;

public class VendorNotationDependency extends AbstractNotationDependency {

    private NotationDependency hostNotationDependency;

    private String vendorPath;

    public String getVendorPath() {
        return vendorPath;
    }

    public NotationDependency getHostNotationDependency() {
        return hostNotationDependency;
    }

    public void setHostNotationDependency(NotationDependency hostNotationDependency) {
        this.hostNotationDependency = hostNotationDependency;
    }

    public void setVendorPath(String vendorPath) {
        this.vendorPath = vendorPath;
    }


    @Override
    public ResolvedDependency doResolve(ResolveContext context) {
        // Here we create a new context to avoid incorrect exclusion
        ResolveContext rootContext = ResolveContext.root(hostNotationDependency, context.getConfiguration());

        ResolvedDependency hostResolvedDependency = hostNotationDependency.resolve(rootContext);
        Optional<VendorResolvedDependency> result = hostResolvedDependency.getDependencies().flatten()
                .stream()
                .filter(d -> d instanceof VendorResolvedDependency)
                .map(d -> (VendorResolvedDependency) d)
                .filter(d -> d.getRelativePathToHost().equals(vendorPath))
                .findFirst();

        if (result.isPresent()) {
            return result.get();
        } else {
            throw DependencyResolutionException.vendorNotExist(this, hostResolvedDependency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        VendorNotationDependency that = (VendorNotationDependency) o;
        return Objects.equals(hostNotationDependency, that.hostNotationDependency)
                && Objects.equals(vendorPath, that.vendorPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hostNotationDependency, vendorPath);
    }

    @Override
    public String getVersion() {
        return hostNotationDependency + "/" + vendorPath;
    }

    @Override
    public CacheScope getCacheScope() {
        return hostNotationDependency.getCacheScope();
    }
}
