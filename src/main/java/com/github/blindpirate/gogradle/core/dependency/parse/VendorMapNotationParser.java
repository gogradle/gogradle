/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
