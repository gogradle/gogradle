package com.github.blindpirate.gogradle.core.dependency.external.glock;

import com.github.blindpirate.gogradle.core.dependency.resolve.ExternalDependencyFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Converts GLOCKFILE in repos managed by glock to gogradle map notations
 *
 * @see <a href="https://github.com/robfig/glock">glock</a>
 */
@Singleton
public class GlockDependencyFactory extends ExternalDependencyFactory {
    private GlockfileParser parser=new GlockfileParser();

    @Override
    protected String identityFileName() {
        return "GLOCKFILE";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        return parser.parse(file);
    }
}
