package com.github.blindpirate.gogradle.vcs.mercurial;

import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

public class MercurialNotationDependency extends AbstractNotationDependency {
    public static final String NEWEST_COMMIT = "NEWEST_COMMIT";

    public static final String URL_KEY = "url";
    public static final String TAG_KEY = "tag";
    public static final String NODE_ID_KEY = "nodeId";

    private String nodeId;

    private String tag;

    private String url;

    private List<String> urls = new ArrayList<>();

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getUrls() {
        if (StringUtils.isNotBlank(url)) {
            return singletonList(url);
        } else {
            return urls;
        }
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        return MercurialDependencyManager.class;
    }
}
