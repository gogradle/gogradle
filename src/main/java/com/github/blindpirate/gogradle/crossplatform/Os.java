package com.github.blindpirate.gogradle.crossplatform;

// https://github.com/golang/go/blob/master/src/go/build/syslist.go
// https://github.com/trustin/os-maven-plugin/blob/master/src/main/java/kr/motd/maven/os/Detector.java
public enum Os {
    Android,
    Darwin,
    Dragonfly,
    Freebsd,
    Linux,
    Nacl,
    Netbsd,
    Openbsd,
    Plan9,
    Solaris,
    Windows,
    Zos;

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

    private static Os detectOs() {
        String os = System.getProperty("os.name");
        if (os.startsWith("linux")) {
            return Linux;
        }
        if (os.startsWith("macosx") || os.startsWith("osx")) {
            return Darwin;
        }
        if (os.startsWith("freebsd")) {
            return Freebsd;
        }
        if (os.startsWith("openbsd")) {
            return Openbsd;
        }
        if (os.startsWith("netbsd")) {
            return Netbsd;
        }
        if (os.startsWith("solaris") || os.startsWith("sunos")) {
            return Solaris;
        }
        if (os.startsWith("windows")) {
            return Windows;
        }
        throw new IllegalStateException("Unrecognized operation system:" + os);
    }
}
