package com.github.blindpirate.gogradle.core.pack

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.core.GolangPackage
import com.github.blindpirate.gogradle.core.IncompleteGolangPackage
import com.github.blindpirate.gogradle.core.VcsGolangPackage
import com.github.blindpirate.gogradle.util.HttpUtils
import com.github.blindpirate.gogradle.vcs.VcsType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

import static org.mockito.Mockito.when

@RunWith(GogradleRunner)
class BitbucketPackagePathResolverTest {
    @Mock
    HttpUtils httpUtils

    BitbucketPackagePathResolver resolver

    @Before
    void setUp() {
        resolver = new BitbucketPackagePathResolver(httpUtils)
    }

    @Test
    void 'non-bitbucket package should be reject'() {
        assert !resolver.produce('').isPresent()
        assert !resolver.produce('bitbucket.org2').isPresent()
    }

    @Test
    void 'incomplete package should be recognized correctly'() {
        assert resolver.produce('bitbucket.org').get() instanceof IncompleteGolangPackage
        assert resolver.produce('bitbucket.org/a').get() instanceof IncompleteGolangPackage
    }

    @Test
    void 'package should be recognized correctly'() {
        // given
        when(httpUtils.get("https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf")).thenReturn('''
{
    "scm": "hg", 
    "website": "", 
    "has_wiki": false, 
    "name": "gopdf", 
    "links": {
        "watchers": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/watchers"
        }, 
        "branches": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/refs/branches"
        }, 
        "tags": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/refs/tags"
        }, 
        "commits": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/commits"
        }, 
        "clone": [
            {
                "href": "https://bitbucket.org/zombiezen/gopdf", 
                "name": "https"
            }, 
            {
                "href": "ssh://hg@bitbucket.org/zombiezen/gopdf", 
                "name": "ssh"
            }
        ], 
        "self": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf"
        }, 
        "html": {
            "href": "https://bitbucket.org/zombiezen/gopdf"
        }, 
        "avatar": {
            "href": "https://bitbucket.org/zombiezen/gopdf/avatar/32/"
        }, 
        "hooks": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/hooks"
        }, 
        "forks": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/forks"
        }, 
        "downloads": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/downloads"
        }, 
        "pullrequests": {
            "href": "https://api.bitbucket.org/2.0/repositories/zombiezen/gopdf/pullrequests"
        }
    }, 
    "fork_policy": "allow_forks", 
    "uuid": "{4bd97284-546a-4896-b8bf-eaa7a664cfdb}", 
    "language": "go", 
    "created_on": "2011-11-09T19:23:17.716028+00:00", 
    "full_name": "zombiezen/gopdf", 
    "has_issues": true, 
    "owner": {
        "username": "zombiezen", 
        "display_name": "Ross Light", 
        "type": "user", 
        "uuid": "{65b5e98e-cf22-41c0-98b8-678de14c94ba}", 
        "links": {
            "self": {
                "href": "https://api.bitbucket.org/2.0/users/zombiezen"
            }, 
            "html": {
                "href": "https://bitbucket.org/zombiezen/"
            }, 
            "avatar": {
                "href": "https://bitbucket.org/account/zombiezen/avatar/32/"
            }
        }
    }, 
    "updated_on": "2016-04-08T07:05:27.551737+00:00", 
    "size": 555188, 
    "type": "repository", 
    "slug": "gopdf", 
    "is_private": false, 
    "description": "gopdf is a Go library for creating PDF files."
}''')
        // when
        GolangPackage pkg = resolver.produce('bitbucket.org/zombiezen/gopdf/a').get()
        // then
        assert pkg instanceof VcsGolangPackage
        assert pkg.pathString == 'bitbucket.org/zombiezen/gopdf/a'
        assert pkg.rootPathString == 'bitbucket.org/zombiezen/gopdf'
        assert pkg.vcsType == VcsType.MERCURIAL
        assert pkg.urls == ["https://bitbucket.org/zombiezen/gopdf", "ssh://hg@bitbucket.org/zombiezen/gopdf"]
    }
}
