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
    WINDOWS,
    ZOS;

    public String toString() {
        return name().toLowerCase();
    }

    private static Os hostOs;

    public static Os getHostOs() {
        if (hostOs == null) {
            hostOs = detectOs();
        }
        return hostOs;
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
