# Repository Management 

Gogradle supports private repository and repository url substitution. Actually, it supports registry mirrors. You can declare repositories in `repositories` block of `build.gradle`.

> NOTE `repositories` block must be placed before `dependencies` block !!!!

For example, you may want to substitute a specific package with your own implementation, just do as follows:

```
repositories{
    golang {
        root 'github.com/package/to-be-hacked' 
        vcs 'hg' // optional, git by default
        url 'http://my-repo.com/my/implementation.git'
    }    
}
```

In the DSL above, `root` and `url` can be any object other than string. Gogradle will use built-in Groovy method [`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html) to test if it matches the declaration.

Actually, you can use regular expressions and closures. The following example demonstrates how to use a mirror registry of `github.com`.

```
repositories {
    golang {
        root ~/github\.com\/[\w-]+\/[\w-]+/
        url { path->
            def split = path.split('/')
           "https://my-repo.com/${split[1]}/${split[2]}" 
        }
    }
}    
```

Another application scenario are `bitbucket` private repo. It has been reported [here](https://groups.google.com/forum/#!msg/golang-nuts/li8J9a-Tbz0/sGqklQcSR8cJ) that
Go does not support `bitbucket` private repo since it need to use unauthenticated http request to query the vcs type and url. With Gogradle, you can solve this problem gracefully:

```
repositories {
    golang {
        root ~/bitbucket\.org\/myprivaterepo\/[\w-]+/
        vcs 'hg'
        url { 
            "ssh://hg@bitbucket.org/myprivaterepo/${it.split('/')[2]}" 
        }
        
    }
}    
```

Moreover, you can declare a local directory as dependency package.

```
repositories {
    golang {
        root '/the/repo/root' 
        dir '/path/to/my/own/implmentation'
    }
}    
```

NOTE the `root` declaration must match root path only. You can't declare a `root` matching both root path and non-root path:

```
repositories {
    golang {
        root {it.startsWith('github.com')} // DONOT DO THIS!
        ...
    }
}  
``` 
