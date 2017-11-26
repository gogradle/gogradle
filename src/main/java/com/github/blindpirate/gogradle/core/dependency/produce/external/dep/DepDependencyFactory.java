package com.github.blindpirate.gogradle.core.dependency.produce.external.dep;

import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Converts Gopkg.lock in repos managed by dep to gogradle map notations.
 *
 * @see <a href="https://github.com/golang/dep" >dep</a>
 */
@Singleton
public class DepDependencyFactory extends ExternalDependencyFactory {
    @Override
    public String identityFileName() {
        return "Gopkg.lock";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        return GopkgDotLockModel.parse(file);
    }
}
