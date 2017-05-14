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

package com.github.blindpirate.gogradle.crossplatform;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.SystemUtils.IS_OS_FREE_BSD;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX;
import static org.apache.commons.lang3.SystemUtils.IS_OS_NET_BSD;
import static org.apache.commons.lang3.SystemUtils.IS_OS_SOLARIS;
import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;
import static org.apache.commons.lang3.SystemUtils.IS_OS_ZOS;

// https://github.com/golang/go/blob/master/src/go/build/syslist.go
public enum Os {
    ANDROID,
    DARWIN,
    DRAGONFLY,
    FREEBSD,
    LINUX,
    NACL,
    NETBSD,
    OPENBSD,
    PLAN9,
    SOLARIS,
    WINDOWS {
        @Override
        public String exeExtension() {
            return ".exe";
        }

        @Override
        public String archiveExtension() {
            return ".zip";
        }
    },
    ZOS;

    public String toString() {
        return name().toLowerCase();
    }

    public String exeExtension() {
        return "";
    }

    public String archiveExtension() {
        return ".tar.gz";
    }

    private static Os hostOs;

    public static Os getHostOs() {
        if (hostOs == null) {
            hostOs = detectOs();
        }
        return hostOs;
    }

    public static Os of(String lowercase) {
        for (Os os : values()) {
            if (os.toString().equals(lowercase)) {
                return os;
            }
        }
        throw new IllegalArgumentException("Unrecognized os: " + lowercase);
    }

    private static final Map<Os, Boolean> OS_DETECTION_MAP = ImmutableMap.<Os, Boolean>builder()
            .put(LINUX, IS_OS_LINUX)
            .put(WINDOWS, IS_OS_WINDOWS)
            .put(DARWIN, IS_OS_MAC_OSX)
            .put(FREEBSD, IS_OS_FREE_BSD)
            .put(NETBSD, IS_OS_NET_BSD)
            .put(SOLARIS, IS_OS_SOLARIS)
            .put(ZOS, IS_OS_ZOS)
            .build();

    private static Os detectOs() {
        Optional<Map.Entry<Os, Boolean>> result = OS_DETECTION_MAP.entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .findFirst();
        if (result.isPresent()) {
            return result.get().getKey();
        }
        throw new IllegalStateException("Unrecognized operation system:" + System.getProperty("os.name"));
    }

}
