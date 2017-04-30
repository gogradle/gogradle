package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.cache.CacheScope;
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyManager;
import com.github.blindpirate.gogradle.core.dependency.parse.DirMapNotationParser;
import com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.github.blindpirate.gogradle.vcs.git.GolangRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;

public class LocalDirectoryDependency extends AbstractNotationDependency implements ResolvedDependency {
    private static final File EMPTY_DIR = null;
    private static final long serialVersionUID = 1;
    private static final Logger LOGGER = Logging.getLogger(LocalDirectoryDependency.class);

    private File rootDir;

    private GolangDependencySet dependencies = GolangDependencySet.empty();

    public static LocalDirectoryDependency fromLocal(String name, File rootDir) {
        LocalDirectoryDependency ret = new LocalDirectoryDependency();
        ret.setName(name);
        ret.setDir(rootDir);
        return ret;
    }

    public File getRootDir() {
        return rootDir;
    }

    public void setDir(String dir) {
        if (!GolangRepository.EMPTY_DIR.equals(dir)) {
            setDir(new File(dir));
        }
    }

    protected void setDir(File rootDir) {
        Assert.isTrue(this.rootDir == null, "rootDir can be set only once!");
        this.rootDir = rootDir;
        if (!isValidDirectory(rootDir)) {
            throw DependencyResolutionException.directoryIsInvalid(rootDir);
        }
    }

    @Override
    public long getUpdateTime() {
        if (rootDir == EMPTY_DIR) {
            return 0L;
        } else {
            return rootDir.lastModified();
        }
    }

    public void setDependencies(GolangDependencySet dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        LOGGER.warn("You are locking {} which exists only on your local filesystem, "
                + "which may cause issues on other one's computer.", getRootDir());
        return MapUtils.asMap(MapNotationParser.NAME_KEY, getName(),
                DirMapNotationParser.DIR_KEY, StringUtils.toUnixString(rootDir));
    }

    @Override
    public void installTo(File targetDirectory) {
        GogradleGlobal.getInstance(LocalDirectoryDependencyManager.class).install(this, targetDirectory);
    }

    @Override
    public String formatVersion() {
        return rootDir == EMPTY_DIR ? "" : StringUtils.toUnixString(rootDir);
    }

    // version of local directory is its timestamp
    @Override
    public String getVersion() {
        return Instant.ofEpochMilli(getUpdateTime()).toString();
    }

    @Override
    public String toString() {
        return getName() + "@" + StringUtils.toUnixString(rootDir);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        LocalDirectoryDependency that = (LocalDirectoryDependency) o;
        return Objects.equals(rootDir, that.rootDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootDir, super.hashCode());
    }

    @Override
    public CacheScope getCacheScope() {
        return CacheScope.BUILD;
    }

    @Override
    protected ResolvedDependency doResolve(ResolveContext context) {
        if (rootDir == EMPTY_DIR) {
            return this;
        } else {
            return GogradleGlobal.getInstance(LocalDirectoryDependencyManager.class).resolve(context, this);
        }
    }

    @Override
    public Object clone() {
        LocalDirectoryDependency ret = (LocalDirectoryDependency) super.clone();
        ret.transitiveDepExclusions = this.getTransitiveDepExclusions();
        Assert.isTrue(onlyVendorDependenciesCanHaveDescendants());
        ret.dependencies = this.dependencies.clone();
        ret.dependencies.flatten().forEach(dependency -> resetVendorHostIfNecessary(dependency, ret));
        return ret;
    }

    private void resetVendorHostIfNecessary(GolangDependency dependency, ResolvedDependency clone) {
        if (dependency instanceof VendorResolvedDependency) {
            VendorResolvedDependency.class.cast(dependency).setHostDependency(clone);
        }
    }

    private boolean onlyVendorDependenciesCanHaveDescendants() {
        return this.dependencies.stream().
                filter(d -> (d instanceof ResolvedDependency) && !(d instanceof VendorResolvedDependency))
                .map(d -> (ResolvedDependency) d)
                .allMatch(d -> d.getDependencies().isEmpty());
    }
}
