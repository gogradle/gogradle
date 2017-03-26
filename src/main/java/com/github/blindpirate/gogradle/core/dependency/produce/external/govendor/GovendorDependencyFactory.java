package com.github.blindpirate.gogradle.core.dependency.produce.external.govendor;

import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.DataExchange.parseJson;

/**
 * Converts vendor/vendor.json in repos managed by govendor to gogradle map notations.
 *
 * @see <a href="https://github.com/kardianos/govendor">govendor</a>
 */
@Singleton
public class GovendorDependencyFactory extends ExternalDependencyFactory {

    @Override
    public String identityFileName() {
        return "vendor/vendor.json";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        VendorDotJsonModel model = parseJson(file, VendorDotJsonModel.class);
        return model.toNotations();
    }

}
