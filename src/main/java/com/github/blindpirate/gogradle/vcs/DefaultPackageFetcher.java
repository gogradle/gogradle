package com.github.blindpirate.gogradle.vcs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.VcsUtils.getVcsType;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * https://golang.org/cmd/go/#hdr-Remote_import_paths
 */
@Singleton
public class DefaultPackageFetcher implements PackageFetcher {

    private final Map<String, PackageFetcher> knownHosts;

    private GoImportMetadataFetcher goImportMetadataFetcher;

    @Inject
    public DefaultPackageFetcher(@KnownHostPackageFetchers
                                         Map<String, PackageFetcher> knownHosts,
                                 GoImportMetadataFetcher goImportMetadataFetcher) {
        this.knownHosts = knownHosts;
        this.goImportMetadataFetcher = goImportMetadataFetcher;
    }

    @Override
    public void fetch(String packageName, Path location) {
        if (isKnownHost(packageName)) {
            fetchAsKnownHost(packageName, location);
        } else if (packageNameContainsVcsType(packageName)) {
            fetchWithVcs(packageName, location);
        } else {
            fetchByFindingMetaTag(packageName, location);
        }
    }

    private void fetchByFindingMetaTag(String packageName, Path location) {
        goImportMetadataFetcher.fetch(packageName, location);
    }

    private void fetchWithVcs(String packageName, Path location) {
        VcsType type = getVcsType(packageName).get();
        type.getFetcher().fetch(packageName, location);
    }


    private boolean packageNameContainsVcsType(String packageName) {
        return getVcsType(packageName).isPresent();
    }

    private void fetchAsKnownHost(String packageName, Path location) {
        PackageFetcher fetcher = getKnownHostFetcher(packageName).get();
        fetcher.fetch(packageName, location);
    }

    private boolean isKnownHost(String packageName) {
        return getKnownHostFetcher(packageName).isPresent();
    }

    private Optional<PackageFetcher> getKnownHostFetcher(String packageName) {
        Path path = Paths.get(packageName);
        if (path.getNameCount() <= 0) {
            return Optional.absent();
        } else {
            return Optional.fromNullable(knownHosts.get(path.getName(0).toString()));
        }
    }

    @BindingAnnotation
    @Target({FIELD, PARAMETER, METHOD})
    @Retention(RUNTIME)
    public @interface KnownHostPackageFetchers {
    }

}
