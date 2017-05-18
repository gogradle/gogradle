/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.core.dependency.tree;

import com.github.blindpirate.gogradle.GogradleGlobal;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependencySet;
import com.github.blindpirate.gogradle.core.dependency.ResolvedDependency;
import com.github.blindpirate.gogradle.util.Assert;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DependencyTreeNode implements Comparable<DependencyTreeNode>, Serializable {
    private String name;
    private boolean star;
    private ResolvedDependency originalDependency;
    private ResolvedDependency finalDependency;
    private List<DependencyTreeNode> children = new ArrayList<>();

    private DependencyTreeNode(ResolvedDependency originalDependency,
                               ResolvedDependency finalDependency,
                               boolean star) {
        this.originalDependency = originalDependency;
        this.finalDependency = finalDependency;
        this.name = originalDependency.getName();
        this.star = star;
    }

    public static DependencyTreeNode withOrignalAndFinal(ResolvedDependency original,
                                                         ResolvedDependency finalResult,
                                                         boolean star) {
        return new DependencyTreeNode(original, finalResult, star);
    }


    public DependencyTreeNode addChild(DependencyTreeNode child) {
        children.add(child);
        Collections.sort(children);
        return this;
    }

    public String output() {
        return print("", true, true);
    }


    private String print(String prefix, boolean isTail, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
                .append(branch(isRoot, isTail))
                .append(format(isRoot))
                .append("\n");

        String prefixOfChildren = prefix + padding(isRoot, isTail);

        for (int i = 0; i < children.size() - 1; i++) {
            sb.append(children.get(i).print(prefixOfChildren, false, false));
        }
        if (children.size() > 0) {
            sb.append(children.get(children.size() - 1)
                    .print(prefixOfChildren, true, false));
        }
        return sb.toString();
    }

    private String padding(boolean isRoot, boolean isTail) {
        if (isRoot) {
            return "";
        }
        return isTail ? "    " : "|   ";
    }

    private String branch(boolean isRoot, boolean isTail) {
        if (isRoot) {
            return "";
        }
        return isTail ? "\\-- " : "|-- ";
    }

    private String format(boolean isRoot) {
        if (isRoot) {
            return name;
        } else if (originalDependency.equals(finalDependency)) {
            return withName() + star();
        } else {
            return withArrow() + star();
        }
    }

    private String star() {
        return star ? " (*)" : "";
    }

    private String withArrow() {
        return finalDependency.getName() + ":"
                + formatResolvedDependency(originalDependency) + " -> " + formatResolvedDependency(finalDependency);
    }

    private String formatResolvedDependency(ResolvedDependency dependency) {
        if (dependency.getSubpackages().contains(GolangDependency.ALL_DESCENDANTS)) {
            return dependency.formatVersion();
        } else {
            return dependency.formatVersion() + " " + dependency.getSubpackages();
        }
    }

    private String withName() {
        return finalDependency.getName() + ":" + finalDependency.formatVersion();
    }

    public GolangDependencySet flatten() {
        GolangDependencySet result = new GolangDependencySet();
        dfs(result, 0);
        return result;
    }

    private void dfs(GolangDependencySet result, int depth) {
        Assert.isTrue(depth < GogradleGlobal.MAX_DFS_DEPTH);
        for (DependencyTreeNode child : children) {
            result.add(child.finalDependency);
            child.dfs(result, depth + 1);
        }
    }

    @SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
    @Override
    public int compareTo(DependencyTreeNode o) {
        return this.name.compareTo(o.name);
    }
}
