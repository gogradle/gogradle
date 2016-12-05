package com.github.blindpirate.gogradle.vcs;

import java.nio.file.Path;

// import "example.org/pkg/foo"
// will result in the following requests:
//
// https://example.org/pkg/foo?go-get=1 (preferred)
// http://example.org/pkg/foo?go-get=1  (fallback, only with -insecure)
// If that page contains the meta tag
//
// <meta name="go-import" content="example.org git https://code.org/r/p/exproj">
// the go tool will verify that https://example.org/?go-get=1 contains the same meta tag and
// then git clone https://code.org/r/p/exproj into GOPATH/src/example.org.
// TODO the verification not implemented yet
public class GoImportMetadataFetcher implements PackageFetcher {
    @Override
    public void fetch(String packageName, Path location) {
        try {
            FetchViaHttps(packageName, location);
        } catch (Throwable e) {

        }
    }

    private void FetchViaHttps(String packageName, Path location) {
    }
}
