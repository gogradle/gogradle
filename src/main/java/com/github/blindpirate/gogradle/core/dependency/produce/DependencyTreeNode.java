package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.core.dependency.GolangDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DependencyTreeNode {
    private GolangDependency value;

    private List<DependencyTreeNode> children = new ArrayList<>();

    public DependencyTreeNode(GolangDependency dependency) {
        this.value = dependency;
    }

    public DependencyTreeNode addChild(DependencyTreeNode child) {
        children.add(child);
        return this;
    }

    public String print() {
        return print("", true);
    }


    private String print(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(value.getName())
                .append("\n");

        String prefixOfChildren = prefix + (isTail ? "    " : "│   ");

        for (int i = 0; i < children.size() - 1; i++) {
            sb.append(children.get(i).print(prefixOfChildren, false));
        }
        if (children.size() > 0) {
            sb.append(children.get(children.size() - 1)
                    .print(prefixOfChildren, true));
        }
        return sb.toString();
    }
}
