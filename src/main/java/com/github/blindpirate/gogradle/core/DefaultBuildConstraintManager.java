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

package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class DefaultBuildConstraintManager implements BuildConstraintManager {
    private static final Pattern GO_VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+).*");
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    private Set<String> allConstraints;

    @Inject
    public DefaultBuildConstraintManager(GoBinaryManager goBinaryManager,
                                         GolangPluginSetting setting) {
        this.goBinaryManager = goBinaryManager;
        this.setting = setting;
    }

    // https://golang.org/pkg/go/build/#hdr-Build_Constraints
    // We need it because we may need to analyze source code for dependencies
    @Override
    public void prepareConstraints() {
        Set<String> tmpAllConstraints = new HashSet<>();
        tmpAllConstraints.add(Os.getHostOs().toString());
        tmpAllConstraints.add(Arch.getHostArch().toString());
        // gccgo/gc/cgo not supported yet
        tmpAllConstraints.addAll(allGoVersionConstraints());
        tmpAllConstraints.addAll(setting.getBuildTags());

        allConstraints = Collections.unmodifiableSet(tmpAllConstraints);
    }

    private Set<String> allGoVersionConstraints() {
        Set<String> ret = new HashSet<>();

        String goVersion = goBinaryManager.getGoVersion();
        Matcher m = GO_VERSION_REGEX.matcher(goVersion);
        Assert.isTrue(m.find(), "Unrecognized version:" + goVersion);

        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));

        Assert.isTrue(major == 1, "Only go1 is supported!");
        for (int i = 1; i <= minor; ++i) {
            ret.add("go1." + i);
        }
        return ret;
    }

    @Override
    public Set<String> getAllConstraints() {
        return allConstraints;
    }

}
