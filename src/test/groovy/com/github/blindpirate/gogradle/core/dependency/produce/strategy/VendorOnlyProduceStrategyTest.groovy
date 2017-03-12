package com.github.blindpirate.gogradle.core.dependency.produce.strategy

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.build.Configuration
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
class VendorOnlyProduceStrategyTest extends DependencyProduceStrategyTest {
    VendorOnlyProduceStrategy strategy = new VendorOnlyProduceStrategy()

    @Test
    void 'only vendor dependencies can be produced'() {
        // given
        externalDependencies(a1)
        vendorDependencies(b1)
        sourceCodeDependencies(c1)

        // when
        GolangDependencySet result = strategy.produce(resolvedDependency, rootDir, visitor, Configuration.BUILD)

        // then
        assert result.size() == 1
        assert result.contains(b1)
    }
}
