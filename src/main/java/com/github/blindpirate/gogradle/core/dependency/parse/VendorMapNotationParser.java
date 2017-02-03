package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.core.dependency.NotationDependency;
import com.github.blindpirate.gogradle.core.dependency.VendorNotationDependency;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class VendorMapNotationParser extends AutoConfigureMapNotationParser<VendorNotationDependency> {
    private final MapNotationParser mapNotationParser;

    @Inject
    public VendorMapNotationParser(MapNotationParser mapNotationParser) {
        this.mapNotationParser = mapNotationParser;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void postConfigure(Map<String, Object> notationMap, NotationDependency ret) {
        Assert.isTrue(notationMap.containsKey(HOST_KEY));
        Map<String, Object> hostNotationMap = MapUtils.getValue(notationMap, HOST_KEY, Map.class);
        VendorNotationDependency vendorNotationDependency = VendorNotationDependency.class.cast(ret);
        vendorNotationDependency.setHostNotationDependency(mapNotationParser.parse(hostNotationMap));
    }
}
