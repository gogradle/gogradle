package com.github.blindpirate.gogradle.core.pack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.VcsGolangPackage;
import com.github.blindpirate.gogradle.util.DataExchange;
import com.github.blindpirate.gogradle.util.ExceptionHandler;
import com.github.blindpirate.gogradle.util.HttpUtils;
import com.github.blindpirate.gogradle.vcs.VcsType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

// https://developer.atlassian.com/bitbucket/api/2/reference/resource/repositories/%7Busername%7D/%7Brepo_slug%7D#get
// https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf
@Singleton
public class BitbucketPackagePathResolver extends AbstractPackagePathResolver {
    private static final String BITBUCKET_HOST = "bitbucket.org";
    private static final String BITBUCKET_API_PREFIX = "https://api.bitbucket.org/2.0/repositories/";

    private final HttpUtils httpUtils;

    @Inject
    public BitbucketPackagePathResolver(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    @Override
    protected Optional<GolangPackage> doProduce(String packagePath) {
        Path path = Paths.get(packagePath);
        BitbucketApiModel packageInfo = queryByApi(path);
        return Optional.of(buildByApiResponse(path, packageInfo));
    }

    private GolangPackage buildByApiResponse(Path path, BitbucketApiModel packageInfo) {
        List<String> urls = packageInfo.links.clone.stream()
                .map(BitbucketApiModel.LinksBean.CloneBean::getHref)
                .collect(Collectors.toList());
        return VcsGolangPackage.builder()
                .withUrls(urls)
                .withPath(toUnixString(path))
                .withRootPath(toUnixString(path.subpath(0, 3)))
                .withVcsType(VcsType.of(packageInfo.scm).get())
                .build();
    }

    private BitbucketApiModel queryByApi(Path path) {
        String url = BITBUCKET_API_PREFIX + toUnixString(path.subpath(1, 3));
        try {
            String response = httpUtils.get(url);
            return DataExchange.parseJson(response, BitbucketApiModel.class);
        } catch (IOException e) {
            throw ExceptionHandler.uncheckException(e);
        }
    }

    @Override
    protected boolean isIncomplete(String packagePath) {
        return Paths.get(packagePath).getNameCount() < 3;
    }

    @Override
    protected boolean cannotRecognize(String packagePath) {
        return !BITBUCKET_HOST.equals(Paths.get(packagePath).getName(0).toString());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BitbucketApiModel {
        @JsonProperty("scm")
        private String scm;
        @JsonProperty("website")
        private String website;
        @JsonProperty("has_wiki")
        private boolean haswiki;
        @JsonProperty("name")
        private String name;
        @JsonProperty("links")
        private LinksBean links;
        @JsonProperty("fork_policy")
        private String forkpolicy;
        @JsonProperty("uuid")
        private String uuid;
        @JsonProperty("language")
        private String language;
        @JsonProperty("createdon")
        private String createdon;
        @JsonProperty("full_name")
        private String fullname;
        @JsonProperty("has_issues")
        private boolean hasissues;
        @JsonProperty("owner")
        private OwnerBean owner;
        @JsonProperty("updated_on")
        private String updatedon;
        @JsonProperty("size")
        private int size;
        @JsonProperty("type")
        private String type;
        @JsonProperty("slug")
        private String slug;
        @JsonProperty("is_private")
        private boolean isprivate;
        @JsonProperty("description")
        private String description;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class LinksBean {
            @JsonProperty("watchers")
            private WatchersBean watchers;
            @JsonProperty("branches")
            private BranchesBean branches;
            @JsonProperty("tags")
            private TagsBean tags;
            @JsonProperty("commits")
            private CommitsBean commits;
            @JsonProperty("self")
            private SelfBean self;
            @JsonProperty("html")
            private HtmlBean html;
            @JsonProperty("avatar")
            private AvatarBean avatar;
            @JsonProperty("hooks")
            private HooksBean hooks;
            @JsonProperty("forks")
            private ForksBean forks;
            @JsonProperty("downloads")
            private DownloadsBean downloads;
            @JsonProperty("pullrequests")
            private PullrequestsBean pullrequests;
            @JsonProperty("clone")
            private List<CloneBean> clone;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class WatchersBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class BranchesBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class TagsBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class CommitsBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class SelfBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class HtmlBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class AvatarBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class HooksBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class ForksBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class DownloadsBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class PullrequestsBean {
                @JsonProperty("href")
                private String href;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class CloneBean {
                @JsonProperty("href")
                private String href;
                @JsonProperty("name")
                private String name;

                public String getHref() {
                    return href;
                }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class OwnerBean {
            @JsonProperty("username")
            private String username;
            @JsonProperty("display_name")
            private String displayname;
            @JsonProperty("type")
            private String type;
            @JsonProperty("uuid")
            private String uuid;
            @JsonProperty("links")
            private LinksBeanX links;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class LinksBeanX {
                @JsonProperty("self")
                private SelfBeanX self;
                @JsonProperty("html")
                private HtmlBeanX html;
                @JsonProperty("avatar")
                private AvatarBeanX avatar;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class SelfBeanX {
                    @JsonProperty("href")
                    private String href;
                }

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class HtmlBeanX {
                    @JsonProperty("href")
                    private String href;
                }

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class AvatarBeanX {
                    @JsonProperty("href")
                    private String href;
                }
            }
        }
    }
}
