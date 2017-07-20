/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.vcs.git;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.vcs.VcsType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Optional;

@SuppressFBWarnings("UMAC_UNCALLABLE_METHOD_OF_ANONYMOUS_CLASS")
public class GolangRepository {
    public static final String EMPTY_DIR = "GOGRADLE_EMPTY_DIR";

    private boolean incomplete;
    private Object pathPattern;
    private Object urlSubstitution;
    private Object dir;
    private VcsType vcsType = VcsType.GIT;

    public void all() {
        this.pathPattern = new Object() {
            boolean isCase(Object candidate) {
                return true;
            }
        };
    }

    public void root(Object pathPattern) {
        this.pathPattern = pathPattern;
    }

    public void incomplete(Object pathPattern) {
        this.pathPattern = pathPattern;
        incomplete = true;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    public void dir(Object urlOrClosure) {
        checkIncomplete();
        dir = urlOrClosure;
    }

    private void checkIncomplete() {
        if (incomplete) {
            throw new UnsupportedOperationException("Not supported for incomplete path!");
        }
    }

    public void url(Object urlOrClosure) {
        checkIncomplete();
        urlSubstitution = urlOrClosure;
    }

    public void vcs(String vcs) {
        checkIncomplete();
        Optional<VcsType> vcsOptional = VcsType.of(vcs);
        Assert.isTrue(vcsOptional.isPresent(), "Unknown vcs type: " + vcs);
        this.vcsType = vcsOptional.get();
    }

    public void emptyDir() {
        dir = EMPTY_DIR;
    }

    public VcsType getVcsType() {
        checkIncomplete();
        return vcsType;
    }

    public String getUrl(String name) {
        checkIncomplete();
        return substitute(name, urlSubstitution);
    }

    private String substitute(String name, Object valueOrClousure) {
        checkIncomplete();
        if (valueOrClousure instanceof String) {
            return (String) valueOrClousure;
        } else if (valueOrClousure instanceof Closure) {
            Closure closure = (Closure) valueOrClousure;
            return Assert.isNotNull(closure.call(name)).toString();
        } else {
            return null;
        }
    }

    public String getDir(String name) {
        checkIncomplete();
        return substitute(name, dir);
    }

    public boolean match(String name) {
        Assert.isTrue(pathPattern != null);
        return (Boolean) InvokerHelper.invokeMethod(pathPattern, "isCase", name);
    }
}
