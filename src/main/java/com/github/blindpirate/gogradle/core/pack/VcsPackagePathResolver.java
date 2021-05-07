package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.GolangRepository;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.blindpirate.gogradle.core.GolangRepository.newOriginalRepository;
import static java.util.stream.Collectors.toList;

public class VcsPackagePathResolver extends AbstractPackagePathResolver {
    @Override
    protected GolangPackage doProduce(String packagePath) {
        Map.Entry<String, VcsType> rootPathAndVcsType = StringUtils.eachSubPath(packagePath)
                .map(subPath -> Maps.immutableEntry(subPath, findEndingVcs(subPath)))
                .filter((Map.Entry<String, VcsType> pair) -> pair.getValue() != null)
                .findFirst().get();

        String rootPath = rootPathAndVcsType.getKey();
        VcsType vcsType = rootPathAndVcsType.getValue();
        GolangRepository repository = newOriginalRepository(vcsType,
                vcsType.getSchemes().stream().map(scheme -> scheme.buildUrl(rootPath)).collect(toList()));

        return VcsGolangPackage.builder()
                .withPath(packagePath)
                .withRootPath(rootPath)
                .withRepository(repository)
                .build();
    }

    @Override
    protected boolean isIncomplete(String packagePath) {
        return false;
    }

    @Override
    protected boolean cannotRecognize(String packagePath) {
        return StringUtils.eachSubPathReverse(packagePath).map(this::findEndingVcs).allMatch(Objects::isNull);
    }

    private VcsType findEndingVcs(String subPath) {
        return Stream.of(VcsType.values())
                .filter(vcsType -> subPath.endsWith(vcsType.getSuffix())).findAny().orElse(null);
    }
}
