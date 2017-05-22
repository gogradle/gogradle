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

package com.github.blindpirate.gogradle.core.dependency.produce.external.gpm;

import com.github.blindpirate.gogradle.core.dependency.produce.external.trash.SimpleConfFileHelper;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.dependency.produce.external.trash.SimpleConfFileHelper.determineVersionAndPutIntoMap;
import static com.github.blindpirate.gogradle.core.dependency.produce.external.trash.SimpleConfFileHelper.removeComment;

public class GodepsParser {
    public List<Map<String, Object>> parse(File file) {
        List<String> lines = IOUtils.readLines(file);
        return lines.stream()
                .filter(SimpleConfFileHelper::isNotCommentLine)
                .filter(StringUtils::isNotBlank)
                .map(this::toNotation)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toNotation(String line) {
//        github.com/nu7hatch/gotrail               v0.0.2
//        github.com/replicon/fast-archiver         v1.02
//        launchpad.net/gocheck                     r2013.03.03   # Bazaar repositories are supported
//        code.google.com/p/go.example/hello/...    ae081cd1d6cc  # And so are Mercurial ones
        Map<String, Object> ret = new HashMap<>();
        String[] array = StringUtils.splitAndTrim(line, "\\s");
        array = removeComment(array);

        ret.put("transitive", false);
        ret.put("name", array[0]);
        determineVersionAndPutIntoMap(ret, array[1]);
        return ret;
    }
}
