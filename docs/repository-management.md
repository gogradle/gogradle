# Repository Management 

Gogradle supports private repository. You can declare repositories in `repositories` block of `build.gradle`.

By default, Gogradle will read `~/.ssh` when operating on git repositories. If your private key is placed somewhere else, the following configuration can be used:

```
repositories {
    git {
        all()
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
}
```

You may want to apply different credentials to some repositories, as follows:

```
repositories{
    git {
        url 'http://my-repo.com/my/project.git'
        credentials {
            username ''
            password ''
        }

    git {
        name 'import/path/of/anotherpackage'
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }    
}
```

In the DSL above, `name` and `url` can be any object other than string. Gogradle will use built-in Groovy method [`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html) to test if it matches the declaration.

For example, you can use regular expressions:

```
    git {
        url ~/.*github\.com.*/
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

If a repository matches a declaration, then the credential in the declaration will be used. Currently only Git repository is supported, you can use username/password or ssh private key as credentials.

Support for other vcs repositories are under development.
