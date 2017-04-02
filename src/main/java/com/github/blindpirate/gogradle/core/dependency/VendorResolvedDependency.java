package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.VcsAccessor;
import com.github.blindpirate.gogradle.vcs.VcsResolvedDependency;
import org.gradle.api.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.blindpirate.gogradle.core.GolangConfiguration.BUILD;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.HOST_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.VENDOR_PATH_KEY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

public class VendorResolvedDependency extends AbstractResolvedDependency {
    private ResolvedDependency hostDependency;

    // java.io.NotSerializableException: sun.nio.fs.UnixPath
    private Path relativePathToHost;

    private VendorResolvedDependency(String name,
                                     String version,
                                     long updateTime,
                                     ResolvedDependency hostDependency,
                                     Path relativePathToHost) {
        super(name, version, updateTime);
        this.hostDependency = hostDependency;
        this.relativePathToHost = relativePathToHost;
    }

    public static VendorResolvedDependency fromParent(String name,
                                                      ResolvedDependency parent,
                                                      File rootDirOfThisVendor) {
        ResolvedDependency hostDependency = determineHostDependency(parent);
        Path relativePathToHost = calculateRootPathToHost(parent, name);
        File hostRootDir = calculateHostRootDir(rootDirOfThisVendor, relativePathToHost);
        String version = hostDependency.getVersion() + "/" + StringUtils.toUnixString(relativePathToHost);
        long updateTime = determineUpdateTime(hostDependency, hostRootDir, rootDirOfThisVendor, relativePathToHost);

        VendorResolvedDependency ret = new VendorResolvedDependency(name,
                version,
                updateTime,
                hostDependency,
                relativePathToHost);
        ret.setFirstLevel(isRoot(hostDependency));

        DependencyVisitor visitor = GogradleGlobal.getInstance(DependencyVisitor.class);
        VendorOnlyProduceStrategy strategy = GogradleGlobal.getInstance(VendorOnlyProduceStrategy.class);
        ret.setDependencies(strategy.produce(ret, rootDirOfThisVendor, visitor, BUILD));
        return ret;
    }

    private static File calculateHostRootDir(File rootDirOfThisVendor, Path relativePathToHost) {
        // <hostRoot>/vendor/a/vendor, vendor/a/vendor -> <hostRoot>
        File ret = rootDirOfThisVendor;
        for (int i = 0; i < relativePathToHost.getNameCount(); ++i) {
            ret = ret.getParentFile();
        }
        return ret;
    }

    private static long determineUpdateTime(ResolvedDependency hostDependency,
                                            File hostRootDir,
                                            File rootDirOfThisVendor,
                                            Path relativePathToHost) {
        if (hostDependency instanceof VcsResolvedDependency) {
            return VcsResolvedDependency.class.cast(hostDependency).getVcsType()
                    .getService(VcsAccessor.class).lastCommitTimeOfPath(hostRootDir, relativePathToHost);
        } else if (hostDependency instanceof LocalDirectoryDependency) {
            return rootDirOfThisVendor.lastModified();
        } else {
            throw new IllegalStateException();
        }
    }

    // if a hostDependency is root project
    private static boolean isRoot(ResolvedDependency hostDependency) {
        if (hostDependency instanceof LocalDirectoryDependency) {
            File rootDir = LocalDirectoryDependency.class.cast(hostDependency).getRootDir();
            return rootDir.equals(GogradleGlobal.getInstance(Project.class).getRootDir());
        } else {
            return false;
        }
    }


    private static Path calculateRootPathToHost(ResolvedDependency parent, String packagePath) {
        if (parent instanceof VendorResolvedDependency) {
            VendorResolvedDependency parentVendorResolvedDependency = (VendorResolvedDependency) parent;
            return parentVendorResolvedDependency.relativePathToHost
                    .resolve(VENDOR_DIRECTORY).resolve(packagePath);
        } else {
            return Paths.get(VENDOR_DIRECTORY).resolve(packagePath);
        }
    }

    private static ResolvedDependency determineHostDependency(ResolvedDependency parent) {
        if (parent instanceof VendorResolvedDependency) {
            return VendorResolvedDependency.class.cast(parent).hostDependency;
        } else {
            return parent;
        }
    }

    public ResolvedDependency getHostDependency() {
        return hostDependency;
    }

    public Path getRelativePathToHost() {
        return relativePathToHost;
    }

    @Override
    protected DependencyInstaller getInstaller() {
        if (hostDependency instanceof LocalDirectoryDependency) {
            return GogradleGlobal.getInstance(LocalDirectoryDependencyInstaller.class);
        } else {
            return AbstractResolvedDependency.class.cast(hostDependency).getInstaller();
        }
    }

    @Override
    public String formatVersion() {
        return hostDependency.getName() + "#" + hostDependency.formatVersion()
                + "/" + toUnixString(relativePathToHost);
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        Map<String, Object> ret = MapUtils.asMap(NAME_KEY, getName());
        Map<String, Object> host = new HashMap<>(hostDependency.toLockedNotation());
        ret.put(VENDOR_PATH_KEY, toUnixString(relativePathToHost));
        ret.put(HOST_KEY, host);
        return ret;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VendorResolvedDependency that = (VendorResolvedDependency) o;
        return Objects.equals(hostDependency, that.hostDependency)
                && Objects.equals(relativePathToHost, that.relativePathToHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostDependency, relativePathToHost);
    }
}
