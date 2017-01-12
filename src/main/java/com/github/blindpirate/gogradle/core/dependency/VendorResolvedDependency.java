package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.install.DependencyInstaller;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.produce.strategy.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.util.Cast;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.InjectionHelper.INJECTOR_INSTANCE;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.util.Cast.cast;

public class VendorResolvedDependency extends AbstractResolvedDependency {

    public static final long UNKNOWN_UPDATE_TIME = -1;

    private ResolvedDependency hostDependency;

    private Path relativePathToHost;

    public static VendorResolvedDependency fromParent(String name,
                                                      ResolvedDependency parent,
                                                      File rootDir) {
        ResolvedDependency hostDependency = determineHostDependency(parent);
        VendorResolvedDependency ret = new VendorResolvedDependency(name,
                hostDependency.getVersion(),
                UNKNOWN_UPDATE_TIME,
                hostDependency,
                caculateRootPathToHost(parent, name));

        DependencyVisitor visitor = INJECTOR_INSTANCE.getInstance(DependencyVisitor.class);
        VendorOnlyProduceStrategy strategy = INJECTOR_INSTANCE.getInstance(VendorOnlyProduceStrategy.class);
        GolangDependencySet dependencies = strategy.produce(ret, rootDir, visitor);
        ret.setDependencies(dependencies);
        return ret;
    }

    private VendorResolvedDependency(String name,
                                     String version,
                                     long updateTime,
                                     ResolvedDependency hostDependency,
                                     Path relativePathToHost) {
        super(name, version, updateTime);

        this.hostDependency = hostDependency;
        this.relativePathToHost = relativePathToHost;
    }

    private static Path caculateRootPathToHost(ResolvedDependency parent, String packagePath) {
        if (parent instanceof VendorResolvedDependency) {
            VendorResolvedDependency parentVendorResolvedDependency = (VendorResolvedDependency) parent;
            return parentVendorResolvedDependency.relativePathToHost.resolve(VENDOR_DIRECTORY).resolve(packagePath);
        } else {
            return Paths.get(VENDOR_DIRECTORY).resolve(packagePath);
        }
    }

    private static ResolvedDependency determineHostDependency(ResolvedDependency parent) {
        if (parent instanceof VendorResolvedDependency) {
            return cast(VendorResolvedDependency.class, parent).hostDependency;
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
    protected Class<? extends DependencyInstaller> getInstallerClass() {
        return Cast.cast(AbstractResolvedDependency.class, hostDependency).getInstallerClass();
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        Map<String, Object> ret = new HashMap<>(hostDependency.toLockedNotation());
        ret.put(VendorNotationDependency.VENDOR_PATH_KEY, relativePathToHost.toString());
        return ret;
    }

    @Override
    public String formatVersion() {
        return hostDependency.getName() + "#" + hostDependency.formatVersion()
                + "/" + relativePathToHost;
    }

}
