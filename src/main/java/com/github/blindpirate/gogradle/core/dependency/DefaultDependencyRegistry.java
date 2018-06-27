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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

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

        if (allVersions.noValidVersionExists()) {
            LOGGER.debug("{} doesn't exit in registry, add it.", dependencyToResolve);
            allVersions.add(dependencyToResolve);
            return true;
        }

        ResolvedDependencyWithReferenceCount head = allVersions.headOfValidVersions();
        int compareResult = PackageComparator.INSTANCE.compare(dependencyToResolve, head.resolvedDependency);
        if (currentDependencyEqualsLatestOne(compareResult)) {
            LOGGER.debug("Same version of {} already exited in registry, increase reference count to {}",
                    dependencyToResolve, head.referenceCount);
            head.referenceCount++;
            return false;
        } else if (currentDependencyShouldReplaceExistedOnes(compareResult)) {
            LOGGER.debug("{} is newer, replace the old version {} in registry",
                    dependencyToResolve, head.resolvedDependency);
            allVersions.onlyDecreaseDescendantsReferenceCount();
            allVersions.add(dependencyToResolve);
            return true;
        } else {
            LOGGER.debug("{} is older, do nothing.", dependencyToResolve);
            allVersions.addEvictedDependency(dependencyToResolve);
            return false;
        }
    }

    @Override
    public synchronized Optional<ResolvedDependency> retrieve(String name) {
        return getAllVersions(name).retrieve();
    }

    private class SameResolvedDependencyInAllVersions {
        private PriorityQueue<ResolvedDependencyWithReferenceCount> validVersions
                = new PriorityQueue<>(ResolvedDependencyWithReferenceCount.COMPARATOR);
        private PriorityQueue<ResolvedDependency> evictedVersions
                = new PriorityQueue<>(PackageComparator.INSTANCE);

        private void add(ResolvedDependency dependency) {
            validVersions.add(new ResolvedDependencyWithReferenceCount(dependency));
            evictedVersions.remove(dependency);
        }

        private void addEvictedDependency(ResolvedDependency dependency) {
            if (!evictedVersions.contains(dependency)) {
                // TODO maybe inefficient
                evictedVersions.add(dependency);
            }
        }

        private Optional<ResolvedDependencyWithReferenceCount> find(ResolvedDependency resolvedDependency) {
            return validVersions
                    .stream()
                    .filter(pkgWithRC -> pkgWithRC.resolvedDependency.equals(resolvedDependency))
                    .findFirst();
        }

        private Optional<ResolvedDependency> retrieve() {
            if (validVersions.isEmpty()) {
                return evictedVersions.isEmpty() ? Optional.empty() : Optional.of(evictedVersions.peek());
            } else {
                return Optional.of(validVersions.peek().resolvedDependency);
            }
        }

        private void decreaseReferenceCount(ResolvedDependency resolvedDependency) {
            Optional<ResolvedDependencyWithReferenceCount> optionalTarget = find(resolvedDependency);
            if (optionalTarget.isPresent()) {
                ResolvedDependencyWithReferenceCount target = optionalTarget.get();
                target.referenceCount--;
                LOGGER.debug("Decrease reference count of {} to {}", target.resolvedDependency, target.referenceCount);
                if (target.referenceCount == 0) {
                    validVersions.remove(target);
                    addEvictedDependency(target.resolvedDependency);
                }
            }
        }

        private boolean noValidVersionExists() {
            return validVersions.isEmpty();
        }

        private void onlyDecreaseDescendantsReferenceCount() {
            List<ResolvedDependency> allVersionDependencies = validVersions.stream()
                    .map(ResolvedDependencyWithReferenceCount::getResolvedDependency)
                    .collect(Collectors.toList());
            allVersionDependencies.forEach(DefaultDependencyRegistry.this::decreaseAllDescendantsReferenceCount);
        }

        private ResolvedDependencyWithReferenceCount headOfValidVersions() {
            return validVersions.peek();
        }
    }

    private void decreaseSelfAndAllDescendantsReferenceCount(ResolvedDependency target) {
        SameResolvedDependencyInAllVersions allVersions = getAllVersions(target.getName());
        allVersions.decreaseReferenceCount(target);

        decreaseAllDescendantsReferenceCount(target);
    }

    private void decreaseAllDescendantsReferenceCount(ResolvedDependency target) {
        target.getDependencies().stream()
                .map(DefaultDependencyRegistry.this::toResolvedDependency)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::decreaseSelfAndAllDescendantsReferenceCount);
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
