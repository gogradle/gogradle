package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.InjectionHelper;
import com.github.blindpirate.gogradle.core.dependency.produce.DependencyVisitor;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_DIRECTORY;
import static com.github.blindpirate.gogradle.core.dependency.produce.VendorDependencyFactory.VENDOR_ONLY_PRODUCE_STRATEGY;
import static com.github.blindpirate.gogradle.util.Cast.cast;

public class VendorDependency extends AbstractResolvedDependency {

    private ResolvedDependency hostDependency;

    private Path relativePathToHost;

    public static VendorDependency basedOnParent(String name,
                                                 ResolvedDependency parent,
                                                 File rootDir) {
        ResolvedDependency hostDependency = determineHostDependency(parent);
        VendorDependency ret = new VendorDependency(name,
                hostDependency.getVersion(),
                hostDependency.getUpdateTime(),
                hostDependency,
                caculateRootPathToHost(parent, name));

        DependencyVisitor visitor = InjectionHelper.INJECTOR_INSTANCE.getInstance(DependencyVisitor.class);
        GolangDependencySet dependencies = VENDOR_ONLY_PRODUCE_STRATEGY.produce(ret, rootDir, visitor);
        ret.setDependencies(dependencies);
        return ret;
    }

    private VendorDependency(String name,
                             String version,
                             long updateTime,
                             ResolvedDependency hostDependency,
                             Path relativePathToHost) {
        super(name, version, updateTime);

        this.hostDependency = hostDependency;
        this.relativePathToHost = relativePathToHost;
    }

    private static Path caculateRootPathToHost(ResolvedDependency parent, String packagePath) {
        if (parent instanceof VendorDependency) {
            VendorDependency parentVendorDependency = (VendorDependency) parent;
            return parentVendorDependency.relativePathToHost.resolve(VENDOR_DIRECTORY).resolve(packagePath);
        } else {
            return Paths.get(VENDOR_DIRECTORY).resolve(packagePath);
        }
    }

    private static ResolvedDependency determineHostDependency(ResolvedDependency parent) {
        if (parent instanceof VendorDependency) {
            return cast(VendorDependency.class, parent).hostDependency;
        } else {
            return parent;
        }
    }


    @Override
    public ResolvedDependency resolve() {
        return this;
    }

    @Override
    public Map<String, Object> toLockedNotation() {
        Map<String, Object> ret = new HashMap<>(hostDependency.toLockedNotation());
        ret.put(VendorNotationDependency.VENDOR_PATH_KEY, relativePathToHost.toString());
        return ret;
    }

    @Override
    public Class<? extends DependencyResolver> getResolverClass() {
        return null;
    }
}
