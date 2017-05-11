package com.github.blindpirate.gogradle.core.dependency

class VendorResolvedDependencyForTest extends VendorResolvedDependency {
    private static final long serialVersionUID = 1

    VendorResolvedDependencyForTest(String name,
                                    String version,
                                    long updateTime,
                                    ResolvedDependency hostDependency,
                                    String relativePathToHost) {
        super(name, version, updateTime, hostDependency, relativePathToHost)
    }
}
