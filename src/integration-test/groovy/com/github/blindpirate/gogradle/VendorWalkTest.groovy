package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency
import com.github.blindpirate.gogradle.core.dependency.VendorDependency
import com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory
import com.github.blindpirate.gogradle.core.pack.LocalFileSystemDependency
import com.github.blindpirate.gogradle.support.GogradleModuleSupport
import com.github.blindpirate.gogradle.util.ReflectionUtils
import org.junit.Test
import org.junit.runner.RunWith

import javax.inject.Inject

@RunWith(GogradleRunner)
@WithResource('vendor_test.zip')
class VendorWalkTest extends GogradleModuleSupport {

    File resource

    @Inject
    VendorDependencyFactory factory

    @Test
    void 'create cascading vendor package should succeed'() {
        LocalFileSystemDependency localPackage = LocalFileSystemDependency.fromLocal('testpackage', resource);

        GolangDependencySet dependencies = factory.produce(localPackage, resource)

        assert dependencies.size() == 2
        dependencies.each { assert it instanceof VendorDependency }
        assert dependencies.any {
            it.name == 'github.com/e/f' && getRelativePathToHost(it) == 'vendor/github.com/e/f'
        }
        assert dependencies.any {
            it.name == 'github.com/e/g' && getRelativePathToHost(it) == 'vendor/github.com/e/g'
        }
        dependencies.each { assert getHostDependency(it) == localPackage }

        VendorDependency github_e_f = dependencies.find { it.name == 'github.com/e/f' }
        assert github_e_f.dependencies.size() == 1

        VendorDependency github_j_k = github_e_f.dependencies.first()
        assert github_j_k.name == 'github.com/j/k'
        assert getRelativePathToHost(github_j_k) == 'vendor/github.com/e/f/vendor/github.com/j/k'
        assert getHostDependency(github_j_k) == localPackage
    }


    String getRelativePathToHost(VendorDependency dependency) {
        ReflectionUtils.getField(dependency, 'relativePathToHost')
    }

    ResolvedDependency getHostDependency(VendorDependency dependency) {
        ReflectionUtils.getField(dependency, 'hostDependency')
    }
}
