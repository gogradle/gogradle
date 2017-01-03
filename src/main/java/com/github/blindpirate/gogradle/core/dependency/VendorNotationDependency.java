package com.github.blindpirate.gogradle.core.dependency;

import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolver;

public class VendorNotationDependency extends AbstractNotationDependency {

    public static final String VENDOR_PATH_KEY = "vendorPath";

    private AbstractNotationDependency hostNotationDependency;

    private String vendorPath;

    @Override
    protected Class<? extends DependencyResolver> resolverClass() {
        return hostNotationDependency.resolverClass();
    }
}
