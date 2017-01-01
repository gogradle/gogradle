package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;

import java.util.ArrayList;
import java.util.List;

public class DependencyTreeNode {
    private String name;
    private ResolvedDependency originalDependency;
    private ResolvedDependency finalDependency;
    private List<DependencyTreeNode> children = new ArrayList<>();

    private DependencyTreeNode(ResolvedDependency originalDependency, ResolvedDependency finalDependency) {
        this.originalDependency = originalDependency;
        this.finalDependency = finalDependency;
        this.name = originalDependency.getName();
    }

    public static DependencyTreeNode withOrignalAndFinal(ResolvedDependency original, ResolvedDependency finalResult) {
        return new DependencyTreeNode(original, finalResult);
    }


    public DependencyTreeNode addChild(DependencyTreeNode child) {
        children.add(child);
        return this;
    }

    public String output() {
        return print("", true);
    }


    private String print(String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
                .append(isTail ? "└── " : "├── ")
                .append(format())
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

    private String format() {
        return name;
    }

}
