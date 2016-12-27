package com.github.blindpirate.gogradle.core;

//https://github.com/golang/go/blob/master/src/go/build/syslist.go
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
}
