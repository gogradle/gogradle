package com.github.blindpirate.gogradle.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.core.pack.MetadataPackagePathResolver;
import com.github.blindpirate.gogradle.util.CollectionUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GolangRepository {
    /**
     * The repository urls.
     */
    @JsonProperty("urls")
    protected List<String> urls;
    /**
     * The repository vcs.
     */
    protected VcsType vcs;

    /**
     * "original" means the vcs type specified in go-import meta tag of HTML.
     *
     * @see MetadataPackagePathResolver
     */
    @JsonProperty("original")
    protected boolean original;

    public String getVcs() {
        return vcs.getName();
    }

    public VcsType getVcsType() {
        return vcs;
    }

    public void setVcs(String vcs) {
        this.vcs = VcsType.of(vcs).get();
    }

    public List<String> getUrls() {
        return urls;
    }

    public boolean isOriginal() {
        return original;
    }

    public static GolangRepository newOriginalRepository(VcsType vcs, String url) {
        return builder().withUrls(Collections.singletonList(url)).withVcs(vcs).withOriginal(true).build();
    }

    public static GolangRepository newOriginalRepository(String vcs, List<String> urls) {
        return newOriginalRepository(VcsType.of(vcs).get(), urls);
    }

    public static GolangRepository newOriginalRepository(VcsType vcs, List<String> urls) {
        return builder().withUrls(urls).withVcs(vcs).withOriginal(true).build();
    }

    public static GolangRepository newSubstitutedRepository(VcsType vcs, List<String> urls) {
        return builder().withUrls(urls).withVcs(vcs).withOriginal(false).build();
    }

    public static GolangRepositoryBuilder builder() {
        return new GolangRepositoryBuilder();
    }

    private static List<String> toSorted(List<String> urls) {
        List<String> sorted = new ArrayList<>(urls);
        Collections.sort(sorted);
        return sorted;
    }

    public boolean match(GolangRepository anotherRepo) {
        return CollectionUtils.containsAny(getUrls(), anotherRepo.getUrls());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GolangRepository that = (GolangRepository) o;
        return Objects.equals(toSorted(urls), toSorted(that.urls)) &&
                vcs == that.vcs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toSorted(urls), vcs);
    }

    public static final class GolangRepositoryBuilder {
        private List<String> urls;
        private VcsType vcs;
        private boolean original;

        private GolangRepositoryBuilder() {
        }

        public GolangRepositoryBuilder withUrls(List<String> urls) {
            this.urls = urls;
            return this;
        }

        public GolangRepositoryBuilder withVcs(VcsType vcs) {
            this.vcs = vcs;
            return this;
        }

        public GolangRepositoryBuilder withOriginal(boolean original) {
            this.original = original;
            return this;
        }

        public GolangRepository build() {
            GolangRepository golangRepository = new GolangRepository();
            golangRepository.urls = urls;
            golangRepository.vcs = vcs;
            golangRepository.original = original;
            return golangRepository;
        }

    }
}
