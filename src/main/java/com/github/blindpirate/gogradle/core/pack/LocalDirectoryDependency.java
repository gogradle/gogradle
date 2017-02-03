package com.github.blindpirate.gogradle.core.pack;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.AbstractNotationDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.core.dependency.install.LocalDirectoryDependencyInstaller;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;
import com.github.blindpirate.gogradle.core.exceptions.DependencyResolutionException;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.IOUtils.isValidDirectory;

public class LocalDirectoryDependency extends AbstractNotationDependency implements ResolvedDependency {
    private long updateTime;

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
        setDir(new File(dir));
    }

    private void setDir(File rootDir) {
        this.rootDir = rootDir;
        this.updateTime = rootDir.lastModified();
        if (!isValidDirectory(rootDir)) {
            throw DependencyResolutionException.directoryIsInvalid(rootDir);
        }
    }

    @Override
    public ResolvedDependency resolve() {
        return this;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    public void setDependencies(GolangDependencySet dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public GolangDependencySet getDependencies() {
        return dependencies
                .stream()
                .filter(this::shouldNotBeExcluded)
                .collect(Collectors.toCollection(GolangDependencySet::new));
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void installTo(File targetDirectory) {
        GogradleGlobal.getInstance(LocalDirectoryDependencyInstaller.class).install(this, targetDirectory);
    }

    @Override
    public String formatVersion() {
        return rootDir.getAbsolutePath();
    }

    @Override
    protected Class<? extends DependencyResolver> getResolverClass() {
        throw new UnsupportedOperationException();
    }

    // version of local directory is its timestamp
    @Override
    public String getVersion() {
        return Instant.ofEpochMilli(getUpdateTime()).toString();
    }
}
