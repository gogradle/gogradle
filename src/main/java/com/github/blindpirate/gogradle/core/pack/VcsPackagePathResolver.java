package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.GolangRepository;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VcsPackagePathResolver extends AbstractPackagePathResolver {
    @Override
    protected GolangPackage doProduce(String packagePath) {
        VcsType vcsType = findEndingVcs(packagePath).get();

        GolangRepository repository = GolangRepository.newOriginalRepository(vcsType,
                vcsType.getSchemes().stream().map(scheme-> scheme.buildUrl(packagePath)).collect(Collectors.toList()));

        return VcsGolangPackage.builder()
                .withPath(packagePath)
                .withRootPath(packagePath)
                .withRepository(repository)
                .build();
    }

    @Override
    protected boolean isIncomplete(String packagePath) {
        return false;
    }

    @Override
    protected boolean cannotRecognize(String packagePath) {
        return !findEndingVcs(packagePath).isPresent();
    }

    private Optional<VcsType> findEndingVcs(String packagePath) {
        return Stream.of(VcsType.values()).filter(vcsType -> packagePath.endsWith(vcsType.getSuffix())).findAny();
    }
}
