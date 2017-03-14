package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface BuildManager {

    void ensureDotVendorDirNotExist();

    void prepareSymbolicLinks();

    void installDependency(ResolvedDependency dependency, Configuration configuration);

    void installDependencyToVendor(ResolvedDependency dependency);

    String getBuildGopath();

    String getTestGopath();

    void go(List<String> args, Map<String, String> env);

    void run(List<String> args, Map<String, String> env);

    void go(List<String> args,
            Map<String, String> env,
            Consumer<String> stdoutLineConsumer,
            Consumer<String> stderrLineConsumer,
            Consumer<Integer> retcodeConsumer);

    void run(List<String> args,
             Map<String, String> env,
             Consumer<String> stdoutLineConsumer,
             Consumer<String> stderrLineConsumer,
             Consumer<Integer> retcodeConsumer);

}
