package com.github.blindpirate.gogradle.core;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.Assert;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultBuildConstraintManager implements BuildConstraintManager {
    private static final Pattern GO_VERSION_REGEX = Pattern.compile("(\\d+)\\.(\\d+).*");
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    private Set<String> allConstraints;
    private Set<String> extraConstraints;

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

        Set<String> tmpExtraConstraints = new HashSet<>(setting.getBuildTags());

        allConstraints = Collections.unmodifiableSet(tmpAllConstraints);
        extraConstraints = Collections.unmodifiableSet(tmpExtraConstraints);
    }

    private Set<String> allGoVersionConstraints() {
        Set<String> ret = new HashSet<>();

        String goVersion = goBinaryManager.getGoVersion();
        Matcher m = GO_VERSION_REGEX.matcher(goVersion);
        Assert.isTrue(m.find(), "Unrecognized version:" + goVersion);

        int major = Integer.valueOf(m.group(1));
        int minor = Integer.valueOf(m.group(2));

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

    @Override
    public Set<String> getExtraConstraints() {
        return extraConstraints;
    }
}
