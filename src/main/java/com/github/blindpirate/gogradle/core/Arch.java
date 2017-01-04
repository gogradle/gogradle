package com.github.blindpirate.gogradle.core;

//https://github.com/golang/go/blob/master/src/go/build/syslist.go
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

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
