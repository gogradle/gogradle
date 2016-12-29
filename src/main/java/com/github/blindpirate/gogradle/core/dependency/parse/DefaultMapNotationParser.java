package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DefaultMapNotationParser implements MapNotationParser {
    private final DirMapNotationParser dirMapNotationParser;
    private final PackagePathResolver packagePathResolver;

    @Inject
    public DefaultMapNotationParser(DirMapNotationParser dirMapNotationParser,
                                    PackagePathResolver packagePathResolver) {
        this.dirMapNotationParser = dirMapNotationParser;
        this.packagePathResolver = packagePathResolver;
    }

    @Override
    public GolangDependency parse(Map<String, Object> notation) {
        Assert.isTrue(notation.containsKey(NAME_KEY), "Name must be specified!");
        if (notation.containsKey(DIR_KEY)) {
            return dirMapNotationParser.parse(notation);
        } else {
            return parseWithVcs(notation);
        }
    }

    private GolangDependency parseWithVcs(Map<String, Object> notation) {
        String packagePath = MapUtils.getString(notation, NAME_KEY);
        PackageInfo packageInfo = packagePathResolver.produce(packagePath).get();
        notation.put(INFO_KEY, packageInfo);
        MapNotationParser parser =
                packageInfo.getVcsType().getService(MapNotationParser.class);

        return parser.parse(notation);
    }

}
