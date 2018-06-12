package com.github.blindpirate.gogradle.core.dependency.produce.external.dep;

import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.pack.DefaultPackagePathResolver.OriginalPackagePathResolvers;

/**
 * Converts Gopkg.lock in repos managed by dep to gogradle map notations.
 *
 * @see <a href="https://github.com/golang/dep" >dep</a>
 */
@Singleton
public class DepDependencyFactory extends ExternalDependencyFactory {
    private final PackagePathResolver originalPackagePathResolver;

    @Inject
    public DepDependencyFactory(@OriginalPackagePathResolvers PackagePathResolver originalPackagePathResolver) {
        this.originalPackagePathResolver = originalPackagePathResolver;
    }

    @Override
    public String identityFileName() {
        return "Gopkg.lock";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        return GopkgDotLockModel.parse(originalPackagePathResolver, file);
    }
}
