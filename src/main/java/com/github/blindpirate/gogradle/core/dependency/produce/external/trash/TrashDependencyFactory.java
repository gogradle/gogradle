package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Converts vendor.conf in repos managed by trash to gogradle map notations
 *
 * @see <a href="https://github.com/rancher/trash">trash</a>
 */
@Singleton
public class TrashDependencyFactory extends ExternalDependencyFactory {
    private VendorDotConfParser parser = new VendorDotConfParser();

    @Inject
    public TrashDependencyFactory(MapNotationParser mapNotationParser) {
        super(mapNotationParser);
    }

    @Override
    protected String identityFileName() {
        return "vendor.conf";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        return parser.parse(file);
    }
}
