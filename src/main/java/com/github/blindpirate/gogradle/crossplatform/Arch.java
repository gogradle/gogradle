package com.github.blindpirate.gogradle.crossplatform;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

// https://github.com/golang/go/blob/master/src/go/build/syslist.go
// http://lopica.sourceforge.net/os.html
// https://github.com/trustin/os-maven-plugin/blob/master/src/main/java/kr/motd/maven/os/Detector.java
public enum Arch {
    I386 {
        @Override
        public String toString() {
            return "386";
        }
    },
    AMD64,
    AMD64P32,
    ARM,
    ARMBE,
    ARM64,
    ARM64BE,
    PPC64,
    PPC64LE,
    MIPS,
    MIPSLE,
    MIPS64,
    MIPS64LE,
    MIPS64P32,
    MIPS64P32LE,
    PPC,
    S390,
    S390X,
    SPARC,
    SPARC64;

    private static Arch hostArch;

    private static final Map<String, Arch> ARCH_DETECTION_MAP = ImmutableMap.<String, Arch>builder()
            .put("x86_64", AMD64)
            .put("x64", AMD64)
            .put("amd64", AMD64)
            .put("x86_32", I386)
            .put("x86", I386)
            .put("x32", I386)
            .put("i386", I386)
            .put("i486", I386)
            .put("i586", I386)
            .put("i686", I386)
            .put("arm", ARM)
            .put("arm32", ARM)
            .put("ppc", PPC)
            .put("ppc32", PPC)
            .put("ppc64", PPC64)
            .put("ppc64le", PPC64LE)
            .put("s390", S390)
            .put("s390x", S390X)
            .build();

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static Arch getHostArch() {
        if (hostArch == null) {
            hostArch = detectHostArch();
        }
        return hostArch;
    }

    public static Arch of(String lowercase) {
        for (Arch a : values()) {
            if (a.toString().equals(lowercase)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unrecognized arch: " + lowercase);
    }

    private static Arch detectHostArch() {
        String arch = System.getProperty("os.arch");
        if (ARCH_DETECTION_MAP.containsKey(arch)) {
            return ARCH_DETECTION_MAP.get(arch);
        }
        throw new IllegalStateException("Unrecognized architecture:" + arch);
    }

}
