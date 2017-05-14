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

package com.github.blindpirate.gogradle.core.dependency.produce.external.govendor;

import com.github.blindpirate.gogradle.core.dependency.produce.ExternalDependencyFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.util.DataExchange.parseJson;

/**
 * Converts vendor/vendor.json in repos managed by govendor to gogradle map notations.
 *
 * @see <a href="https://github.com/kardianos/govendor">govendor</a>
 */
@Singleton
public class GovendorDependencyFactory extends ExternalDependencyFactory {

    @Override
    public String identityFileName() {
        return "vendor/vendor.json";
    }

    @Override
    protected List<Map<String, Object>> adapt(File file) {
        VendorDotJsonModel model = parseJson(file, VendorDotJsonModel.class);
        return model.toNotations();
    }

}
