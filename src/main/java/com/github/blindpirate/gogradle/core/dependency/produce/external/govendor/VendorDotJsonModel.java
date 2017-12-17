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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.blindpirate.gogradle.core.GolangPackage;
import com.github.blindpirate.gogradle.core.ResolvableGolangPackage;
import com.github.blindpirate.gogradle.core.pack.PackagePathResolver;
import com.github.blindpirate.gogradle.util.Assert;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.util.MapUtils.asMapWithoutNull;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.toUnixString;

/**
 * Model of vendor/vendor.json in repos managed by govendor.
 *
 * @see <a href="https://github.com/kardianos/govendor/blob/master/vendor/vendor.json">vendor/vendor.json</a>
 */
@SuppressWarnings({"checkstyle:membername", "checkstyle:parametername"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDotJsonModel {
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("ignore")
    private String ignore;
    @JsonProperty("rootPath")
    private String rootPath;

    @JsonProperty("package")
    private List<PackageBean> packageX;

    public List<Map<String, Object>> toNotations(PackagePathResolver packagePathResolver) {
        return packageX.stream().map(bean -> bean.toNotation(packagePathResolver)).collect(Collectors.toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageBean {
        @JsonProperty("checksumSHA1")
        private String checksumSHA1;
        @JsonProperty("path")
        private String path;
        @JsonProperty("origin")
        private String origin;
        @JsonProperty("revision")
        private String revision;
        @JsonProperty("revisionTime")
        private String revisionTime;

        Map<String, Object> toNotation(PackagePathResolver packagePathResolver) {
            Assert.isNotBlank(path);
            /*
            {
                    "checksumSHA1": "CujWu7+PWlZSX5+zAPJH91O5AVQ=",
                    "origin": "github.com/docker/distribution/vendor/github.com/Sirupsen/logrus",
                    "path": "github.com/Sirupsen/logrus",
                    "revision": "0700fa570d7bcc1b3e46ee127c4489fd25f4daa3",
                    "revisionTime": "2017-03-21T17:14:25Z"
            },
             {
                 "path": "appengine",
                 "revision": ""
             },
             {
                 "path": "appengine_internal",
                 "revision": ""
             }
             */

            Map<String, Object> ret = asMapWithoutNull("name", path, "transitive", false);
            if (isNotBlank(origin)) {
                recognizeHostAndVendorPath(ret, packagePathResolver);
            } else if (isNotBlank(revision)) {
                ret.put("version", revision);
            }
            return ret;
        }

        private void recognizeHostAndVendorPath(Map<String, Object> ret, PackagePathResolver packagePathResolver) {
            GolangPackage pkg = packagePathResolver.produce(origin).get();

            Assert.isTrue(pkg instanceof ResolvableGolangPackage, "Cannot resolve package in vendor.json: " + origin);

            Path hostImportPath = ResolvableGolangPackage.class.cast(pkg).getRootPath();
            Path vendorPath = hostImportPath.relativize(Paths.get(origin));
            Map<String, Object> host = asMapWithoutNull("name", toUnixString(hostImportPath),
                    "version", revision);

            ret.put("vendorPath", toUnixString(vendorPath));
            ret.put("host", host);
        }
    }
}
