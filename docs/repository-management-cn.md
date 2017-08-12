# Repository Management 

## 镜像仓库
Gogradle支持私有仓库和仓库url替换。这实际上支持了仓库的镜像。你可以在`build.gradle`的`repositories`中声明一个仓库。

例如，你想要把一个特定的包替换成自己的实现，可以这样做：

```
repositories{
    golang {
        root 'github.com/package/to-be-hacked' 
        vcs 'hg' // optional, git by default
        url 'http://my-repo.com/my/implementation.git'
    }    
}
```

在上面的DSK中，`root`和`url`并不一定要是字符串，它可以是任意对象。Gogradle使用Groovy内建的[`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html)方法来判定一个包的路径是否匹配该仓库的声明。

这意味着你可以使用正则表达式和闭包。下面的例子展示了如何使用一个`github.com`的镜像仓库。

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
## 私有仓库
另外一个应用场景是`bitbucket`的私有仓库。Go本身不支持`bitbucket.org`的私有仓库，因为Go需要通过无验证的http方法获取仓库的vcs类型和url，详见[这里](https://groups.google.com/forum/#!msg/golang-nuts/li8J9a-Tbz0/sGqklQcSR8cJ) 
Gogradle优雅地解决了此问题：

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

## 本地包依赖

此外，你还可以声明一个位于本地目录中的依赖包。

```
repositories {
    golang {
        root '/the/repo/root' 
        dir '/path/to/my/own/implmentation'
    }
}    
```

有时，可能希望简单地忽略某个包，例如`appengine`，可以使用`emptyDir`：

```
repositories {
    golang {
        root 'appengine'
        emptyDir()
    }
}
```

需要注意的是，`root`必须只匹配包的根路径。不要声明一个既能匹配根路径又能匹配子路径的`root`，如下所示：

```
repositories {
    golang {
        root {it.startsWith('github.com')} // DONOT DO THIS!
        ...
    }
}  
```

这样做相比下面的声明的好处在于：

- `repositories`声明是全局的，它会影响所有的传递性依赖。
- `repositories`声明中可以使用Groovy的闭包，轻松实现自定义逻辑的包替换。

```
dependencies {
    golang {
        build name:'github.com/my/project', url: 'http://my-repo/my/project.git'
    }
}
```

## Gitlab相关问题处理

Gitlab的实现并未完全遵守[go的import路径约定](https://golang.org/cmd/go/#hdr-Relative_import_paths)，详见此[讨论](https://gitlab.com/gitlab-org/gitlab-ce/issues/35101#note_35565222)。因此，在这种情况下，Gogradle可能无法正确识别`vendor`中的包结构，从而产生莫名其妙的问题。你需要在`build.gradle`中添加如下配置：

```
repositories {
    golang {
        incomplete ~/yourgitlab\.com(\/\w+)?/
    }
}
```

这样做会通知Gogradle：类似`yourgitlab.com`和`yourgitlab.com/username`这样的路径不是一个项目的根路径。

Gitlab 9.5已经修正此问题。
