package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

public class GolangRepository {
    public static final GolangRepository EMPTY_INSTANCE = new GolangRepository() {
        @Override
        public void all() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void name(Object name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void url(Object urlOrClosure) {
            throw new UnsupportedOperationException();
        }

    };

    private boolean all;
    private Object namePattern;
    private Object urlSubstitution;

    public void all() {
        this.all = true;
    }

    public void name(Object name) {
        namePattern = name;
    }

    public void url(Object urlOrClosure) {
        urlSubstitution = urlOrClosure;
    }

    public String substitute(String name, String url) {
        if (urlSubstitution instanceof String) {
            return (String) urlSubstitution;
        }
        if (urlSubstitution instanceof Closure) {
            Closure closure = (Closure) urlSubstitution;
            if (closure.getMaximumNumberOfParameters() == 1) {
                return (String) closure.call(name);
            } else if (closure.getMaximumNumberOfParameters() == 2) {
                return (String) closure.call(name, url);
            }
        }
        return url;
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
