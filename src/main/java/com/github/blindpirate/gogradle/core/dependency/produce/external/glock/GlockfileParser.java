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
 */
public class GlockfileParser {
    public List<Map<String, Object>> parse(File file) {
        return IOUtils.getLines(file)
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
                "version", packageAndRevision[1]);
    }

    private boolean isNotCmdLine(String line) {
        return !line.trim().startsWith("cmd");
    }
}
