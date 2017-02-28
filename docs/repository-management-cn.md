# 仓库管理

Gogradle支持私有仓库。你可以在`build.gradle`文件的`repositories`中声明仓库的相关设置。

默认情况下，Gogradle在执行Git相关操作时会读取本机的ssh相关目录。如果你的ssh文件没有放在默认目录`~/.ssh`，则需要通过以下设置：

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

你可能希望对某些Git仓库应用不同的身份验证信息，那么可以这样：

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

其中，`name`和`url`中的参数并非只能是字符串，还可以是任何对象。Gogradle通过Groovy语言内建的[`Object.isCase()`](http://mrhaki.blogspot.jp/2009/08/groovy-goodness-switch-statement.html)方法判定一个仓库声明是否生效。
例如，你可以在其中使用正则：

```
    git {
        url ~/.*github\.com.*/
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

甚至闭包：

```
    git {
        name {it->it.startsWith('github.com')}
        credentials {
            privateKeyFile '/path/to/private/key'
        }
    }
```

若一个仓库匹配某个仓库声明，那么该声明中的身份验证信息将会被用于拉取代码。Gogradle当前只支持Git仓库，身份验证信息可以使用户名/密码（http协议）或者ssh私钥（ssh协议）。

对其他版本控制系统仓库的开发正在进行中，敬请期待。
