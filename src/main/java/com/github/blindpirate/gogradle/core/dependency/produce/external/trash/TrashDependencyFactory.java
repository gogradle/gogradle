package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.util.DataExchange;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

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
    private static final Logger LOGGER = Logging.getLogger(TrashDependencyFactory.class);
    private VendorDotConfParser parser = new VendorDotConfParser();

    @Override
    public String identityFileName() {
        return "vendor.conf";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        try {
            return DataExchange.parseYaml(file, VendorDotConfYamlModel.class).toBuildNotations();
        } catch (Exception e) {
            LOGGER.info("Parsing {} as yaml failed, try plain format.", file.getAbsolutePath());
            return parser.parse(file);
        }
    }
}
