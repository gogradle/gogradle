package com.github.blindpirate.gogradle.core.dependency.external.godep;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Godeps/Godeps.json
public class GodepsDotJsonModel {
    private String ImportPath;
    private String GoVersion;
    private String GodepVersion;
    private List<DepsBean> Deps;

    public List<Map<String, Object>> toNotations() {
        List<Map<String, Object>> ret = new ArrayList<>();
        for (DepsBean dep : Deps) {
            ret.add(dep.toNotation());
        }
        return ret;
    }

    public String getImportPath() {
        return ImportPath;
    }

    public void setImportPath(String ImportPath) {
        this.ImportPath = ImportPath;
    }

    public String getGoVersion() {
        return GoVersion;
    }

    public void setGoVersion(String GoVersion) {
        this.GoVersion = GoVersion;
    }

    public String getGodepVersion() {
        return GodepVersion;
    }

    public void setGodepVersion(String GodepVersion) {
        this.GodepVersion = GodepVersion;
    }

    public List<DepsBean> getDeps() {
        return Deps;
    }

    public void setDeps(List<DepsBean> Deps) {
        this.Deps = Deps;
    }

    public static class DepsBean {
        private String ImportPath;
        private String Rev;
        private String Comment;

        public Map<String, Object> toNotation() {
            return ImmutableMap.<String, Object>of(
                    "name", ImportPath,
                    "commit", Rev);
        }

        public String getImportPath() {
            return ImportPath;
        }

        public void setImportPath(String ImportPath) {
            this.ImportPath = ImportPath;
        }

        public String getRev() {
            return Rev;
        }

        public void setRev(String Rev) {
            this.Rev = Rev;
        }

        public String getComment() {
            return Comment;
        }

        public void setComment(String comment) {
            Comment = comment;
        }
    }
}
