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

package com.github.blindpirate.gogradle.core.dependency.produce.external.trash;

import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.base.CharMatcher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.dependency.produce.external.trash.SimpleConfFileHelper.determineVersionAndPutIntoMap;
import static com.github.blindpirate.gogradle.core.dependency.produce.external.trash.SimpleConfFileHelper.removeComment;

/**
 * Parses vendor.conf in repos managed by trash.
 *
 * @see <a href="https://github.com/rancher/trash/blob/master/vendor.conf">vendor.conf</a>
 */

// https://github.com/ethereum/go-ethereum/blob/master/vendor.conf
public class VendorDotConfParser {

    public List<Map<String, Object>> parse(File file) {
        List<String> lines = IOUtils.readLines(file);
        return lines.stream()
                .filter(SimpleConfFileHelper::isNotCommentLine)
                .filter(StringUtils::isNotBlank)
                .filter(this::isNotPackageDeclareLine)
                .map(this::toNotation)
                .collect(Collectors.toList());
    }

    private boolean isNotPackageDeclareLine(String line) {
        // # package
        // github.com/docker/infrakit
        return CharMatcher.whitespace().matchesAnyOf(line.trim());
    }

    private Map<String, Object> toNotation(String line) {
        // github.com/Microsoft/go-winio 0.3.6
        // github.com/davecgh/go-spew v1.0.0-9-g346938d
        // github.com/docker/docker 3a68292
        // github.com/go-check/check 4ed411733c5785b40214c70bce814c3a3a689609 https://github.com/cpuguy83/check.git
        Map<String, Object> ret = new HashMap<>();
        String[] array = StringUtils.splitAndTrim(line, "\\s");
        array = removeComment(array);

        ret.put("transitive", false);
        ret.put("name", array[0]);
        determineVersionAndPutIntoMap(ret, array[1]);
        if (array.length > 2) {
            ret.put("url", array[2]);
        }
        return ret;
    }
}
