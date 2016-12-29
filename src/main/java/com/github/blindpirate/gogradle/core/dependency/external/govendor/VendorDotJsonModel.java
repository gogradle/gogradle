package com.github.blindpirate.gogradle.core.dependency.external.govendor;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

@SuppressWarnings({"checkstyle:membername", "checkstyle:parametername"})
public class VendorDotJsonModel {

    private String comment;
    private String ignore;
    private String rootPath;

    private List<PackageBean> packageX;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIgnore() {
        return ignore;
    }

    public void setIgnore(String ignore) {
        this.ignore = ignore;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @JSONField(name = "package")
    public List<PackageBean> getPackageX() {
        return packageX;
    }

    public void setPackageX(List<PackageBean> packageX) {
        this.packageX = packageX;
    }

    public static class PackageBean {
        private String checksumSHA1;
        private String path;
        private String revision;
        private String revisionTime;

        public String getChecksumSHA1() {
            return checksumSHA1;
        }

        public void setChecksumSHA1(String checksumSHA1) {
            this.checksumSHA1 = checksumSHA1;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getRevision() {
            return revision;
        }

        public void setRevision(String revision) {
            this.revision = revision;
        }

        public String getRevisionTime() {
            return revisionTime;
        }

        public void setRevisionTime(String revisionTime) {
            this.revisionTime = revisionTime;
        }
    }
}
