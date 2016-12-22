package com.github.blindpirate.gogradle.core.dependency.resolve;

import com.github.blindpirate.gogradle.core.FileSystemModule;
import com.github.blindpirate.gogradle.core.dependency.DependencyHelper;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.produce.VendorOnlyProduceStrategy;
import com.github.blindpirate.gogradle.core.pack.PackageInfo;
import com.github.blindpirate.gogradle.core.pack.PackageNameResolver;
import com.google.common.base.Optional;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.github.blindpirate.gogradle.core.dependency.resolve.VendorDependencyFactory.VENDOR_DIRECTORY;

/**
 * Analyze vendor directory to generate dependencies.
 */
public class VendorDirectoryVistor extends SimpleFileVisitor<Path> {

    static final int MAX_DEPTH = 100;

    private Path parentModuleVendor;

    private FileSystemModule parentModule;

    private PackageNameResolver resolver;

    private GolangDependencySet dependencies = new GolangDependencySet();

    public GolangDependencySet getDependencies() {
        return dependencies;
    }

    public VendorDirectoryVistor(FileSystemModule parentModule,
                                 PackageNameResolver resolver) {
        this.resolver = resolver;
        this.parentModule = parentModule;
        this.parentModuleVendor = parentModule.getRootDir().resolve(VENDOR_DIRECTORY);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path currentAbsolutePath, BasicFileAttributes attrs)
            throws IOException {
        if (currentAbsolutePath.equals(parentModuleVendor)) {
            // ignore vendor root
            return FileVisitResult.CONTINUE;
        }

        // relative path, i.e "github.com/a/b"
        Path currentPath = parentModuleVendor.relativize(currentAbsolutePath);

        Optional<PackageInfo> packageInfo = resolver.produce(currentPath.toString());
        if (packageInfo.isPresent()) {
            // current path is root of a repo
            dependencies.add(createDependency(currentPath.toString()));
            return FileVisitResult.SKIP_SUBTREE;
        } else {
            return FileVisitResult.CONTINUE;
        }
    }

    private GolangDependency createDependency(String packageName) {
        FileSystemModule module = parentModule.vendor(packageName);
        module.setStrategy(DependencyHelper.strategy(VendorOnlyProduceStrategy.class));
        module.getDependencies();
        return module;
    }
}
