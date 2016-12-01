package com.github.blindpirate.golang.plugin.core.dependency.tmp;

public interface PickyFactory<A, B> {
    boolean accept(A material);

    B produce(A material);
}
