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

import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import org.gradle.api.logging.Logging;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import static java.util.Comparator.comparing;

public class DefaultDependencyRegistry implements DependencyRegistry {
    private static final Logger LOGGER = Logging.getLogger(DefaultDependencyRegistry.class);
    private final PackagePathResolver packagePathResolver;
    private final Map<String, SameResolvedDependencyInAllVersions> packages = new HashMap<>();

    public DefaultDependencyRegistry(PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public synchronized boolean register(ResolvedDependency dependencyToResolve) {
        SameResolvedDependencyInAllVersions allVersions = getAllVersions(dependencyToResolve.getName());

        if (allVersions.isEmpty()) {
            LOGGER.debug("{} doesn't exit in registry, add it.", dependencyToResolve);
            allVersions.add(dependencyToResolve);
            return true;
        }

        ResolvedDependencyWithReferenceCount head = allVersions.head();
        int compareResult = PackageComparator.INSTANCE.compare(dependencyToResolve, head.resolvedDependency);
        if (currentDependencyEqualsLatestOne(compareResult)) {
            LOGGER.debug("Same version of {} already exited in registry, increase reference count to {}",
                    dependencyToResolve, head.referenceCount);
            head.referenceCount++;
            return false;
        } else if (currentDependencyShouldReplaceExistedOnes(compareResult)) {
            LOGGER.debug("{} is newer, replace the old version {} in registry",
                    dependencyToResolve, head.resolvedDependency);
            allVersions.decreaseAllVersionsReferenceCount();
            allVersions.add(dependencyToResolve);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized Optional<ResolvedDependency> retrieve(String name) {
        return Optional.ofNullable(getAllVersions(name).head())
                .map(ResolvedDependencyWithReferenceCount::getResolvedDependency);
    }

    private class SameResolvedDependencyInAllVersions {
        private PriorityQueue<ResolvedDependencyWithReferenceCount> allVersions
                = new PriorityQueue<>(ResolvedDependencyWithReferenceCount.COMPARATOR);

        private void add(ResolvedDependency pkg) {
            allVersions.add(new ResolvedDependencyWithReferenceCount(pkg));
        }

        private Optional<ResolvedDependencyWithReferenceCount> find(ResolvedDependency resolvedDependency) {
            return allVersions
                    .stream()
                    .filter(pkgWithRC -> pkgWithRC.resolvedDependency.equals(resolvedDependency))
                    .findFirst();
        }

        private void decreaseReferenceCount(ResolvedDependency resolvedDependency) {
            Optional<ResolvedDependencyWithReferenceCount> optionalTarget = find(resolvedDependency);
            if (optionalTarget.isPresent()) {
                ResolvedDependencyWithReferenceCount target = optionalTarget.get();
                target.referenceCount--;
                LOGGER.debug("Decrease reference count of {} to {}", target.resolvedDependency, target.referenceCount);
                if (target.referenceCount == 0) {
                    allVersions.remove(target);
                }
            }
        }

        private ResolvedDependencyWithReferenceCount head() {
            return allVersions.isEmpty() ? null : allVersions.peek();
        }

        private boolean isEmpty() {
            return allVersions.isEmpty();
        }

        private void decreaseAllVersionsReferenceCount() {
            List<ResolvedDependencyWithReferenceCount> copy = new ArrayList<>(allVersions);
            copy.stream()
                    .map(ResolvedDependencyWithReferenceCount::getResolvedDependency)
                    .forEach(DefaultDependencyRegistry.this::decreaseAllDescendantsReferenceCount);
        }
    }

    private void decreaseAllDescendantsReferenceCount(ResolvedDependency target) {
        SameResolvedDependencyInAllVersions allVersions = getAllVersions(target.getName());
        allVersions.decreaseReferenceCount(target);

        target.getDependencies().stream()
                .map(DefaultDependencyRegistry.this::toResolvedDependency)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::decreaseAllDescendantsReferenceCount);
    }

    private static class ResolvedDependencyWithReferenceCount {
        private static final Comparator<ResolvedDependencyWithReferenceCount> COMPARATOR =
                comparing(ResolvedDependencyWithReferenceCount::getResolvedDependency, PackageComparator.INSTANCE);
        private final ResolvedDependency resolvedDependency;
        private int referenceCount;

        private ResolvedDependency getResolvedDependency() {
            return resolvedDependency;
        }

        private ResolvedDependencyWithReferenceCount(ResolvedDependency resolvedDependency) {
            this.resolvedDependency = resolvedDependency;
            this.referenceCount = 1;
        }
    }

    private enum PackageComparator implements Comparator<ResolvedDependency> {
        INSTANCE;

        @Override
        public int compare(ResolvedDependency pkg1, ResolvedDependency pkg2) {
            if (pkg1.isFirstLevel() && pkg2.isFirstLevel()) {
                throw new IllegalStateException("First-level package " + pkg1.getName()
                        + " conflict!");
            } else if (pkg1.isFirstLevel() && !pkg2.isFirstLevel()) {
                return -1;
            } else if (pkg2.isFirstLevel() && !pkg1.isFirstLevel()) {
                return 1;
            } else {
                return Long.compare(pkg2.getUpdateTime(), pkg1.getUpdateTime());
            }
        }
    }

    private boolean currentDependencyShouldReplaceExistedOnes(int compareResult) {
        return compareResult < 0;
    }

    private boolean currentDependencyEqualsLatestOne(int compareResult) {
        return compareResult == 0;
    }

    private Optional<ResolvedDependency> toResolvedDependency(GolangDependency dependency) {
        if (dependency instanceof AbstractNotationDependency
                && AbstractNotationDependency.class.cast(dependency).hasBeenResolved()) {
            return Optional.of(dependency.resolve(null));
        } else if (dependency instanceof ResolvedDependency) {
            return Optional.of(ResolvedDependency.class.cast(dependency));
        } else {
            return Optional.empty();
        }
    }

    private SameResolvedDependencyInAllVersions getAllVersions(String name) {
        name = packagePathResolver.produce(name).get().getRootPathString();
        SameResolvedDependencyInAllVersions allVersions = packages.get(name);
        if (allVersions == null) {
            allVersions = new SameResolvedDependencyInAllVersions();
            packages.put(name, allVersions);
        }
        return allVersions;
    }
}
