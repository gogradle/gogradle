package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;

public class GolangRepository {
    public static final GolangRepository EMPTY_INSTANCE = new GolangRepository() {
        public void all() {
            throw new UnsupportedOperationException();
        }

        public void name(Object name) {
            throw new UnsupportedOperationException();
        }

        public void substitute(Closure closure) {
            throw new UnsupportedOperationException();
        }

        public void httpsProxy(String httpsProxy) {
            throw new UnsupportedOperationException();
        }

        public void httpProxy(String httpProxy) {
            throw new UnsupportedOperationException();
        }
    };

    private static final String HTTP_PROXY = "http_proxy";
    private static final String HTTPS_PROXY = "https_proxy";
    private boolean all;
    private Object namePattern;
    private String httpProxy;
    private String httpsProxy;
    private Closure substitution;

    public void all() {
        this.all = true;
    }

    public void name(Object name) {
        namePattern = name;
    }

    public void substitute(Closure closure) {
        this.substitution = closure;
    }

    public void httpsProxy(String httpsProxy) {
        this.httpsProxy = httpsProxy;
    }

    public void httpProxy(String httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String substitute(String url) {
        if (substitution == null) {
            return url;
        }
        return (String) substitution.call(new Object[]{url});
    }

    public Closure getSubstitution() {
        return substitution;
    }

    public Map<String, String> getProxyEnv() {
        return MapUtils.asMap(HTTP_PROXY, httpProxy, HTTPS_PROXY, httpsProxy);
    }

    public boolean match(String name) {
        if (all) {
            return true;
        }

        Assert.isTrue(namePattern != null);

        return nameMatch(name);
    }

    private boolean nameMatch(String name) {
        return (Boolean) InvokerHelper.invokeMethod(namePattern, "isCase", name);
    }
}
