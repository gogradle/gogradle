package com.github.blindpirate.gogradle.core.dependency.produce.external.gvtgbvendor;

import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.DataExchange.parseJson;

/**
 * Converts vendor/manifest in repos managed by gvt or gbvendor to gogradle map notations.
 *
 * @see <a href="https://github.com/FiloSottile/gvt">gvt</a>
 * @see <a href="https://godoc.org/github.com/constabulary/gb/cmd/gb-vendor">gbvendor</a>
 */
@Singleton
public class GvtGbvendorDependencyFactory extends ExternalDependencyFactory {
    @Inject
    public GvtGbvendorDependencyFactory(MapNotationParser mapNotationParser) {
        super(mapNotationParser);
    }

    @Override
    protected String identityFileName() {
        return "vendor/manifest";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        ManifestModel model = parseJson(file, ManifestModel.class);
        return model.toNotations();
    }
}
