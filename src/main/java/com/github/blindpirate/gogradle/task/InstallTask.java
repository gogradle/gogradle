package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class InstallTask extends AbstractGolangTask {
    private static final String CURRENT_VERSION_META = ".CURRENT_VERSION";
    private static final Logger LOGGER = Logging.getLogger(InstallTask.class);

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void installDependencies() {
        File src = buildManager.getInstallationDirectory(getConfigurationName()).resolve("src").toFile();
        DependencyTreeNode dependencyTree = getUpstreamResolveTask().getDependencyTree();
        GolangDependencySet flatDependencies = dependencyTree.flatten()
                .stream()
                .collect(GolangDependencySet.COLLECTOR);

        clearDirectoriesIfNecessary(src, flatDependencies);

        flatDependencies.forEach(this::installIfNecessary);
    }

    private void installIfNecessary(GolangDependency dependency) {
        File targetDir = buildManager.getInstallationDirectory(getConfigurationName())
                .resolve("src")
                .resolve(dependency.getName())
                .toFile();
        IOUtils.forceMkdir(targetDir);
        if (IOUtils.dirIsEmpty(targetDir)) {
            IOUtils.write(targetDir, CURRENT_VERSION_META, dependency.getVersion());
            ResolvedDependency.class.cast(dependency).installTo(targetDir);
        } else {
            LOGGER.debug("{} is not empty, skip installing {}", targetDir, dependency);
        }
    }

    private void clearDirectoriesIfNecessary(File src, GolangDependencySet flatDependencies) {
        Set<File> dirsToBeCleared = new HashSet<>();
        dfs(src, "", flatDependencies, dirsToBeCleared, 0);
        dirsToBeCleared.forEach(IOUtils::clearDirectory);
    }

    private boolean dfs(File currentDir,
                        String currentRelativePath,
                        GolangDependencySet dependencySet,
                        Set<File> dirsToBeCleared,
                        int depth) {
        Assert.isTrue(depth < GogradleGlobal.MAX_DFS_DEPTH);
        List<File> files = IOUtils.safeListFiles(currentDir);
        boolean ret;
        if (versionFileExists(files)) {
            ret = !currentVersionMatchDependency(currentDir, currentRelativePath, dependencySet);
        } else if (anyDotGoFilesExist(files)) {
            ret = true;
        } else {
            List<Boolean> subdirectoryResults = files.stream()
                    .filter(File::isDirectory)
                    .map(dir -> dfs(dir,
                            determineSubPath(currentRelativePath, dir.getName()),
                            dependencySet,
                            dirsToBeCleared,
                            depth + 1))
                    .collect(Collectors.toList());
            ret = subdirectoryResults.stream().allMatch(b -> b);
        }
        if (ret) {
            dirsToBeCleared.add(currentDir);
        }
        return ret;
    }

    private String determineSubPath(String currentPath, String currentDirName) {
        if (currentPath.length() == 0) {
            return currentDirName;
        } else {
            return currentPath + "/" + currentDirName;
        }
    }

    private boolean anyDotGoFilesExist(List<File> files) {
        return files.stream().map(File::getName).anyMatch(name -> name.endsWith(".go"));
    }

    private boolean currentVersionMatchDependency(File currentDir,
                                                  String currentRelativePath,
                                                  GolangDependencySet dependencySet) {
        Optional<GolangDependency> existed = dependencySet.findByName(currentRelativePath);
        if (existed.isPresent()) {
            ResolvedDependency resolvedDependency = (ResolvedDependency) (existed.get());
            if (isOnDisk(resolvedDependency)) {
                return false;
            } else {
                return IOUtils.toString(new File(currentDir, CURRENT_VERSION_META))
                        .equals(resolvedDependency.getVersion());
            }
        } else {
            return false;
        }
    }

    private boolean isOnDisk(ResolvedDependency dependency) {
        if (dependency instanceof LocalDirectoryDependency) {
            return true;
        } else if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency vendorResolvedDependency = (VendorResolvedDependency) dependency;
            return vendorResolvedDependency.getHostDependency() instanceof LocalDirectoryDependency;
        } else {
            return false;
        }
    }

    private boolean versionFileExists(List<File> files) {
        return files.stream().filter(File::isFile).map(File::getName).anyMatch(CURRENT_VERSION_META::equals);
    }

    @InputFile
    public File getSerializationFile() {
        return getUpstreamResolveTask().getSerializationFile();
    }

    @OutputDirectory
    public File getInstallationDirectory() {
        return buildManager.getInstallationDirectory(getConfigurationName()).toFile();
    }

    private String getConfigurationName() {
        return getUpstreamResolveTask().getConfigurationName();
    }

    protected abstract ResolveTask getUpstreamResolveTask();
}
