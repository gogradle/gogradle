package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import java.util.Map;

public interface VersionConverter {
    static void determineVersionAndPutIntoMap(Map<String, Object> ret, String version) {
        if (version.contains(".")) {
            ret.put("tag", version);
        } else {
            ret.put("version", version);
        }
    }
}
