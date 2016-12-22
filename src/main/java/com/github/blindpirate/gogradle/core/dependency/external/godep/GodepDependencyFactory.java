package com.github.blindpirate.gogradle.core.dependency.external.godep;

import com.alibaba.fastjson.JSON;
import com.github.blindpirate.gogradle.core.GolangPackageModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.resolve.ExternalDependencyFactory;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * In newest version of godep, it will read dependency versions from GOPATH
 * and lock them into Godeps/Godeps.json.
 *
 * @see <a href="https://github.com/tools/godep">godep</a>
 */
@Singleton
public class GodepDependencyFactory extends ExternalDependencyFactory {
    public static final String GODEPS_DOT_JSON_LOCATION = "Godeps/Godeps.json";

    @Inject
    private MapNotationParser mapNotationParser;

    private List<String> identityFiles = Arrays.asList(GODEPS_DOT_JSON_LOCATION);

    @Override
    protected List<String> identityFiles() {
        return identityFiles;
    }

    @Override
    public Optional<GolangDependencySet> doProduce(GolangPackageModule module) {
        GodepsDotJsonModel model = parse(module);
        return Optional.of(DependencyHelper.parseMany(model.toNotations(), mapNotationParser));
    }

    private GodepsDotJsonModel parse(GolangPackageModule module) {
        File file = module.getRootDir().resolve(GODEPS_DOT_JSON_LOCATION).toFile();
        try {
            return JSON.parseObject(new FileInputStream(file), GodepsDotJsonModel.class);
        } catch (IOException e) {
            throw new DependencyResolutionException(e);
        }
    }

}
