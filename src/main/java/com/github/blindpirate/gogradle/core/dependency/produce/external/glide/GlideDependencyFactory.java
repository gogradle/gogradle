package com.github.blindpirate.gogradle.core.dependency.produce.external.glide;

import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.DataExchange.parseYaml;

/**
 * Converts glide.lock in repos managed by glide to gogradle map notations.
 *
 * @see <a href="https://github.com/Masterminds/glide">glide</a>
 */
@Singleton
public class GlideDependencyFactory extends ExternalDependencyFactory {
    @Inject
    public GlideDependencyFactory(MapNotationParser mapNotationParser) {
        super(mapNotationParser);
    }

    @Override
    protected String identityFileName() {
        return "glide.lock";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        GlideDotLockModel model = parseYaml(file, GlideDotLockModel.class);
        return model.toNotations();
    }
}
