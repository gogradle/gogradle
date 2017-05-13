package com.github.blindpirate.gogradle.task;

import com.github.blindpirate.gogradle.build.BuildManager;
import com.github.blindpirate.gogradle.common.DeleteUnmarkedDirectoryVistor;
import com.github.blindpirate.gogradle.common.MarkDirectoryVisitor;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.LocalDirectoryDependency;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.tree.DependencyTreeNode;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

public abstract class InstallTask extends AbstractGolangTask {
    private static final String CURRENT_VERSION_META = ".CURRENT_VERSION";
    private static final Logger LOGGER = Logging.getLogger(InstallTask.class);

    @Inject
    private BuildManager buildManager;

    @TaskAction
    public void installDependencies() {
        File src = buildManager.getInstallationDirectory(getConfigurationName()).resolve("src").toFile();
        DependencyTreeNode dependencyTree = getUpstreamResolveTask().getDependencyTree();
        GolangDependencySet flatDependencies = dependencyTree.flatten();

        MarkDirectoryVisitor markVisitor =
                new MarkDirectoryVisitor(dir -> currentVersionMatchDependency(dir, src, flatDependencies), src);
        IOUtils.walkFileTreeSafely(src.toPath(), markVisitor);

        DeleteUnmarkedDirectoryVistor deleteVisitor=new DeleteUnmarkedDirectoryVistor(markVisitor);
        IOUtils.walkFileTreeSafely(src.toPath(),deleteVisitor);

        flatDependencies.forEach(this::installIfNecessary);
    }

    protected String extractPackage(File rootDir, File currentDir) {
        return StringUtils.toUnixString(rootDir.toPath().relativize(currentDir.toPath()));
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


    private boolean currentVersionMatchDependency(File currentDir,
                                                  File srcDir,
                                                  GolangDependencySet dependencySet) {
        Optional<GolangDependency> existed = dependencySet.findByName(extractPackage(srcDir, currentDir));
        if (!existed.isPresent()) {
            return false;
        }

        File versionFile = new File(currentDir, CURRENT_VERSION_META);
        if (!versionFile.exists()) {
            return false;
        }

        ResolvedDependency resolvedDependency = (ResolvedDependency) (existed.get());
        if (isOnDisk(resolvedDependency)) {
            // For the sake of security, we would always think local dependency as out-of-date
            return false;
        } else {
            return IOUtils.toString(new File(currentDir, CURRENT_VERSION_META))
                    .equals(resolvedDependency.getVersion());
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
