package com.github.blindpirate.gogradle.vcs;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.VcsUtils.getVcsType;

/**
 * https://golang.org/cmd/go/#hdr-Remote_import_paths
 */
public class DefaultPackageFetcher implements PackageFetcher {

    @SuppressWarnings("unchecked")
    private Map<String, PackageFetcher> knownHosts = (Map) ImmutableMap.of(
            "bitbucket.org", new BitbucketPackageFetcher(),
            "github.com", new GithubPackageFetcher(),
            "launchpad.net", new LaunchpadFetcher(),
            "hub.jazz.net", new JazzFetcher()
    );

    @SuppressWarnings("unchecked")
    private Map<VcsType, PackageFetcher> vcsFetchers = (Map) ImmutableMap.of(
            VcsType.Git, new GitPackageFetcher(),
            VcsType.Bazaar, new BazaarPackageFetcher(),
            VcsType.Mercurial, new MercurialPackageFetcher(),
            VcsType.Svn, new SvnPackageFetcher()
    );

    @Inject
    private GoImportMetadataFetcher goImportMetadataFetcher;

    @Override
    public void fetch(String packageName, Path location) {
        if (isKnowHost(packageName)) {
            fetchAsKnownHost(packageName, location);
        } else if (packageNameContainsVcsType(packageName)) {
            fetchWithVcs(packageName, location);
        } else {
            fetchByFindingMetaTag(packageName, location);
        }
    }

    private void fetchByFindingMetaTag(String packageName, Path location) {

    }

    private void fetchWithVcs(String packageName, Path location) {
        VcsType type = getVcsType(packageName).get();
        vcsFetchers.get(type).fetch(packageName, location);
    }


    private boolean packageNameContainsVcsType(String packageName) {
        return getVcsType(packageName).isPresent();
    }

    private void fetchAsKnownHost(String packageName, Path location) {
        PackageFetcher fetcher = getKnownHostFetcher(packageName).get();
        fetcher.fetch(packageName, location);
    }

    private boolean isKnowHost(String packageName) {
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

}
