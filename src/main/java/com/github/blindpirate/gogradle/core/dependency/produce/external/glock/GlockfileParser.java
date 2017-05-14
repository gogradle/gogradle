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

package com.github.blindpirate.gogradle.core.dependency.produce.external.glock;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.MapUtils;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses GLOCKFILE.
 *
 * @see <a href="https://github.com/robfig/glock">glock</a>
 * @see <a href="https://github.com/dcoker/biscuit/blob/master/GLOCKFILE" >docker/biscuit/GLOCKFILE</a>
 * @see <a href="https://github.com/jwilder/dockerize/blob/master/GLOCKFILE" >jwilder/dockerize/GLOCKFILE</a>
 */
public class GlockfileParser {
    public List<Map<String, Object>> parse(File file) {
        return IOUtils.readLines(file)
                .stream()
                .filter(this::isNotCmdLine)
                .filter(StringUtils::isNotBlank)
                .map(this::toNotation)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toNotation(String line) {
        String[] packageAndRevision = StringUtils.splitAndTrim(line, "\\s");
        Assert.isTrue(packageAndRevision.length == 2, "Unrecognized line:" + line);
        return MapUtils.asMap("name", packageAndRevision[0],
                "version", packageAndRevision[1],
                "transitive", false);
    }

    private boolean isNotCmdLine(String line) {
        return !line.trim().startsWith("cmd");
    }
}
