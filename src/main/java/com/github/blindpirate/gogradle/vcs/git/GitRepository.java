package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.util.ConfigureUtil;

public class GitRepository {
    private boolean all;
    private Object urlPattern;
    private Object namePattern;
    private String privateKeyFilePath;
    private String username;
    private String password;

    public void all() {
        this.all = true;
    }

    public void url(Object url) {
        urlPattern = url;
    }

    public void name(Object name) {
        namePattern = name;
    }

    public void credentials(Closure closure) {
        ConfigureUtil.configure(closure, this);
    }

    public void privateKeyFile(String path) {
        privateKeyFilePath = path;
    }

    public void username(String username) {
        this.username = username;
    }

    public void password(String password) {
        this.password = password;
    }

    public boolean match(String name, String url) {
        if (all) {
            return true;
        }

        Assert.isTrue(namePattern != null || urlPattern != null);

        if (urlPattern != null) {
            return urlMatch(url);
        } else if (namePattern != null) {
            return nameMatch(name);
        } else {
            return urlMatch(url) && nameMatch(name);
        }
    }

    private boolean nameMatch(String name) {
        return (Boolean) InvokerHelper.invokeMethod(namePattern, "isCase", name);
    }

    private boolean urlMatch(String url) {
        return (Boolean) InvokerHelper.invokeMethod(urlPattern, "isCase", url);
    }

    public String getPrivateKeyFilePath() {
        return privateKeyFilePath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
