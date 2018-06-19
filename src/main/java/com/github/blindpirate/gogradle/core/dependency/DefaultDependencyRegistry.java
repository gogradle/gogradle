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
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

public class DefaultDependencyRegistry implements DependencyRegistry {
    private static final Logger LOGGER = Logging.getLogger(DefaultDependencyRegistry.class);
    private final PackagePathResolver packagePathResolver;
    private final Map<String, PackagesInAllVersions> packages = new HashMap<>();

    public DefaultDependencyRegistry(PackagePathResolver packagePathResolver) {
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public synchronized boolean register(ResolvedDependency dependencyToResolve) {
        PackagesInAllVersions allVersions = getAllVersions(dependencyToResolve.getName());

        if (allVersions.isEmpty()) {
            LOGGER.debug("{} doesn't exit in registry, add it.", dependencyToResolve);
            allVersions.add(dependencyToResolve);
            return true;
        }

        PackageWithReferenceCount head = allVersions.head();
        int compareResult = PackageComparator.INSTANCE.compare(dependencyToResolve, head.pkg);
        if (currentDependencyEqualsLatestOne(compareResult)) {
            LOGGER.debug("Same version of {} already exited in registry, increase reference count to {}",
                    dependencyToResolve, head.referenceCount);
            head.referenceCount++;
            return false;
        } else if (currentDependencyShouldReplaceExistedOnes(compareResult)) {
            LOGGER.debug("{} is newer, replace the old version {} in registry", dependencyToResolve, head.pkg);
            allVersions.decreaseAllVersionsReferenceCount();
            allVersions.add(dependencyToResolve);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized Optional<ResolvedDependency> retrieve(String name) {
        return Optional.ofNullable(getAllVersions(name).head()).map(PackageWithReferenceCount::getPkg);
    }

    private class PackagesInAllVersions {
        private PriorityQueue<PackageWithReferenceCount> allVersions
                = new PriorityQueue<>(PackageWithReferenceCount.COMPARATOR);

        private void add(ResolvedDependency pkg) {
            allVersions.add(new PackageWithReferenceCount(pkg));
        }

        private Optional<PackageWithReferenceCount> find(ResolvedDependency resolvedDependency) {
            return allVersions
                    .stream()
                    .filter(pkgWithRC -> pkgWithRC.pkg.equals(resolvedDependency))
                    .findFirst();
        }

        private void decreaseReferenceCount(PackageWithReferenceCount head) {
            head.referenceCount--;
            LOGGER.debug("Decrease reference count of {} to {}", head.pkg, head.referenceCount);
            if (head.referenceCount == 0) {
                allVersions.remove(head);
            }
        }

        private PackageWithReferenceCount head() {
            return allVersions.isEmpty() ? null : allVersions.peek();
        }

        private boolean isEmpty() {
            return allVersions.isEmpty();
        }

        private void decreaseAllVersionsReferenceCount() {
            allVersions.forEach(this::decreaseAllDescendantsReferenceCount);
        }

        private void decreaseAllDescendantsReferenceCount(PackageWithReferenceCount head) {
            getAllVersions(head.pkg.getName()).decreaseReferenceCount(head);
            head.pkg.getDependencies().stream().map(DefaultDependencyRegistry.this::locate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(this::decreaseAllDescendantsReferenceCount);
        }
    }

    private static class PackageWithReferenceCount {
        private static final Comparator<PackageWithReferenceCount> COMPARATOR =
                Comparator.comparing(PackageWithReferenceCount::getPkg, PackageComparator.INSTANCE);
        private final ResolvedDependency pkg;
        private int referenceCount;

        private ResolvedDependency getPkg() {
            return pkg;
        }

        private PackageWithReferenceCount(ResolvedDependency pkg) {
            this.pkg = pkg;
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

    private Optional<PackageWithReferenceCount> locate(GolangDependency pkg) {
        if (pkg instanceof AbstractNotationDependency && AbstractNotationDependency.class.cast(pkg).hasBeenResolved()) {
            ResolvedDependency resolvedDependency = pkg.resolve(null);
            return getAllVersions(pkg.getName()).find(resolvedDependency);
        } else if (pkg instanceof ResolvedDependency) {
            return getAllVersions(pkg.getName()).find(ResolvedDependency.class.cast(pkg));
        } else {
            return Optional.empty();
        }
    }

    private PackagesInAllVersions getAllVersions(String name) {
        name = packagePathResolver.rootPath(name);
        PackagesInAllVersions allVersions = packages.get(name);
        if (allVersions == null) {
            allVersions = new PackagesInAllVersions();
            packages.put(name, allVersions);
        }
        return allVersions;
    }
}
