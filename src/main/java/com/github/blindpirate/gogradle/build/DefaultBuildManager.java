package com.github.blindpirate.gogradle.build;

import com.github.blindpirate.gogradle.GolangPluginSetting;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.crossplatform.Arch;
import com.github.blindpirate.gogradle.crossplatform.GoBinaryManager;
import com.github.blindpirate.gogradle.crossplatform.Os;
import com.github.blindpirate.gogradle.util.ProcessUtils;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.github.blindpirate.gogradle.util.IOUtils.ensureDirExistAndWritable;
import static com.github.blindpirate.gogradle.util.IOUtils.forceMkdir;
import static java.util.Arrays.asList;

@Singleton
public class DefaultBuildManager implements BuildManager {
    private static final String OUTPUT_FILE_NAME = "%s_%s_%s";

    private final Project project;
    private final GoBinaryManager goBinaryManager;
    private final GolangPluginSetting setting;

    @Inject
    public DefaultBuildManager(Project project,
                               GoBinaryManager goBinaryManager,
                               GolangPluginSetting setting) {
        this.project = project;
        this.goBinaryManager = goBinaryManager;
        this.setting = setting;
    }

    @Override
    public void build() {
        String goBinary = goBinaryManager.binaryPath();
        String outputFilePath = getOutputFilePath();
        String projectGopath = ensureProjectGopathWritable().toString();

        List<String> args = asList(goBinary, "-o", outputFilePath);
        Map<String, String> envs = ImmutableMap.of("GOPATH", projectGopath);

        ProcessUtils.ProcessResult result = ProcessUtils.run(args, envs, project.getRootDir());
        System.out.println(result.getStderr());
    }

    @Override
    public void installDependency(ResolvedDependency dependency) {
        Path targetLocation = ensureProjectGopathWritable().resolve(dependency.getName());
        forceMkdir(targetLocation.toFile());

        dependency.installTo(targetLocation.toFile());
    }

    private Path ensureProjectGopathWritable() {
        Path ret = project.getRootDir().toPath()
                .resolve(GOGRADLE_BUILD_DIR)
                .resolve(BUILD_GOPATH);
        return ensureDirExistAndWritable(ret);
    }

    private String getOutputFilePath() {
        String packageName = Objects.toString(Paths.get(setting.getPackagePath()).getFileName());
        String outputFileName = String.format(OUTPUT_FILE_NAME,
                Os.getHostOs(),
                Arch.getHostArch(),
                packageName);
        return project.getRootDir().toPath()
                .resolve(GOGRADLE_BUILD_DIR)
                .resolve(outputFileName)
                .toString();
    }
}
