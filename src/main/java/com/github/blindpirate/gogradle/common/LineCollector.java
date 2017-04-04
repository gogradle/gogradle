package com.github.blindpirate.gogradle.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LineCollector implements Consumer<String> {
    private List<String> lines = new ArrayList<>();

    @Override
    public synchronized void accept(String s) {
        lines.add(s);
    }

    public List<String> getLines() {
        return lines;
    }

    public String getOutput() {
        return String.join("\n", lines);
    }
}
