package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.LocalDirectoryGolangPackage;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DirMapNotationParser extends AutoConfigureMapNotationParser<LocalDirectoryDependency> {
    protected void preConfigure(Map<String, Object> notationMap) {
        GolangPackage pkg = MapUtils.getValue(notationMap, PACKAGE_KEY, GolangPackage.class);
        if (pkg instanceof LocalDirectoryGolangPackage) {
            notationMap.put(DIR_KEY, LocalDirectoryGolangPackage.class.cast(pkg).getDir());
        }
    }
}
