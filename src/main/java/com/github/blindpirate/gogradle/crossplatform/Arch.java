package com.github.blindpirate.gogradle.crossplatform;

// https://github.com/golang/go/blob/master/src/go/build/syslist.go
// https://github.com/trustin/os-maven-plugin/blob/master/src/main/java/kr/motd/maven/os/Detector.java
public enum Arch {
    I386 {
        @Override
        public String toString() {
            return "386";
        }
    },
    Amd64,
    Amd64p32,
    Arm,
    Armbe,
    Arm64,
    Arm64be,
    Ppc64,
    Ppc64le,
    Mips,
    Mipsle,
    Mips64,
    Mips64le,
    Mips64p32,
    Mips64p32le,
    Ppc,
    S390,
    S390x,
    Sparc,
    Sparc64;

    private static Arch hostArch;

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

    private static Arch detectHostArch() {
        String arch = System.getProperty("os.arch");
        if (arch.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return Amd64;
        }
        if (arch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return I386;
        }
        if (arch.matches("^(arm|arm32)$")) {
            return Arm;
        }
        if (arch.matches("^(ppc|ppc32)$")) {
            return Ppc;
        }
        if ("ppc64".equals(arch)) {
            return Ppc64;
        }
        if ("ppc64le".equals(arch)) {
            return Ppc64le;
        }
        if ("s390".equals(arch)) {
            return S390;
        }
        if ("s390x".equals(arch)) {
            return S390x;
        }
        throw new IllegalStateException("Unrecognized architecture:" + arch);
    }

}
