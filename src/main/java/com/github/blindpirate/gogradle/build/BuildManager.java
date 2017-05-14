package com.github.blindpirate.gogradle.build;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface BuildManager {

    void ensureDotVendorDirNotExist();

    void prepareSymbolicLinks();

    String getBuildGopath();

    String getTestGopath();

    Path getInstallationDirectory(String configuration);

    void go(List<String> args, Map<String, String> env);

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
