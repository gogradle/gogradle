# 依赖管理

依赖包管理是所有包管理工具的噩梦。幸运的是，Gogradle的包管理机制非常优秀，足以面对极端复杂的情况。
众所周知，Go语言本身不参与代码包的管理；它假设所有的包都位于一个或者多个[Workspace](https://golang.org/doc/code.html#Workspaces)中，
这些Workspace由`GOPATH`指定。`GOPATH`可以包含多个路径，在构建时，Go语言构建工具会依次在这些路径中寻找所需的代码包。这就带来了许多问题：

- Go项目缺乏依赖包的版本信息，难以进行稳定、可重复的构建
- 同一台计算机（或者构建服务器）上可能同时进行多个构建，这些构建可能依赖同一个代码包的不同版本
- 由于传递性依赖的存在，同一个项目可能依赖同一代码包的多个版本

饱受包管理问题困扰的Go语言不得已在1.5之后引入了[vendor](https://docs.google.com/document/d/1Bz5-UB7g2uPBdOx-rw5t9MxJwkfpx90cqG9AFL0JAYo)机制，
允许一个Go项目与自身的依赖包一起进入源代码管理系统。这一定程度上缓解了上述情况，却引入了新的问题：

- 冗余代码的存在使得项目臃肿不堪
- 同一个工程中存在同一个代码包的多个版本，这迟早会带来[问题](https://github.com/blindpirate/golang-broken-vendor)
- 众多的[外部包管理工具](https://github.com/golang/go/wiki/PackageManagementTools)互不兼容，参差不齐

Gogradle致力于改善这种情况。与`glide`类似，它通过将声明的依赖包安装到`vendor`中来完成项目级依赖的隔离，从而避免与全局`GOPATH`发生关系。

## 声明依赖

Gogradle管理的依赖包声明于`build.gradle`的`dependencies`块中。当前只支持Git管理的依赖包，其他源代码管理系统的支持正在开发中。一般来说，你只需要声明你直接依赖的包，Gogradle会自动帮你解析传递性依赖并解决冲突。
下面是一些示例：

```groovy
dependencies {
    golang {
        build 'github.com/user/project'  // No specific version, the latest will be used
        build name:'github.com/user/project' // Equivalent to last line
    
        build 'github.com/user/project@1.0.0-RELEASE' // Specify a version(tag in Git)
        build name:'github.com/user/project', tag:'1.0.0-RELEASE' // Equivalent to last line
        
        build 'github.com/user/project@master' // Specify a branch 
        build name:'github.com/user/project', branch:'master' // Equivalent to last line
    
        test 'github.com/user/project#d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Specify a commit
        test name:'github.com/user/project', commit:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line
        build name:'github.com/user/project', version:'d3fbe10ecf7294331763e5c219bb5aa3a6a86e80' // Equivalent to last line
    }
}
```

默认情况下，如果你的声明没有指定commit/tag/branch的话，Gogradle会每次执行`git fetch`或者`hg update -u`，以保证获取远端的最新版本。

依赖声明支持[语义化版本](http://semver.org/)。在Git中，"版本"即Git的tag。

```groovy
dependencies {
    golang {
        build 'github.com/user/project@1.*'  // Equivalent to >=1.0.0 & <2.0.0
        build 'github.com/user/project@1.x'  // Equivalent to last line
        build 'github.com/user/project@1.X'  // Equivalent to last line

        build 'github.com/user/project@~1.5' // Equivalent to >=1.5.0 & <1.6.0
        build 'github.com/user/project@1.0-2.0' // Equivalent to >=1.0.0 & <=2.0.0
        build 'github.com/user/project@^0.2.3' // Equivalent to >=0.2.3 & <0.3.0
        build 'github.com/user/project@1' // Equivalent to 1.X or >=1.0.0 & <2.0.0
        build 'github.com/user/project@!(1.x)' // Equivalent to <1.0.0 & >=2.0.0
        build 'github.com/user/project@ ~1.3 | (1.4.* & !=1.4.5) | ~2' // Very complicated expression
    }
}
```

可以在声明时指定仓库的url。这尤其适用于私有仓库。有关私有仓库的权限验证请参考[仓库管理](./repository-management-cn.md)。

```groovy
dependencies {
    golang {
        build name: 'github.com/user/project', url:'https://github.com/user/project.git', tag:'v1.0.0'
        build name: 'github.com/user/project', url:'git@github.com:user/project.git', tag:'v2.0.0'
    }
}
```

可以同时声明多个依赖：

```groovy
dependencies {
    golang {
        build 'github.com/a/b@1.0.0', 'github.com/c/d@2.0.0', 'github.com/e/f#commitId'
    
        build([name: 'github.com/g/h', tag: '2.5'],
               [name: 'github.com/i/j', commit: 'commitId'])
    }
}
```

Gogradle支持对传递性依赖的管理。例如，下列声明禁止了`github.com/user/project`的传递性依赖。

```groovy
dependencies {
    golang {
        build('github.com/user/project') {
            transitive = false
        }
    }
}
```

此外，还可以排除指定条件的传递性依赖，例如，下列声明从`github.com/a/b`的后代中排除了全部`github.com/c/d`依赖包和指定版本的`github.com/e/f`依赖包。

```groovy
dependencies {
    golang {
        build('github.com/a/b') {
            exclude name:'github.com/c/d'
            exclude name:'github.com/c/d', tag: 'v1.0.0'
        }
    }
}
```

若依赖包位于本地，可以使用如下方式予以声明：

```groovy
dependencies {
    golang {
        build name: 'a/local/package', dir: 'path/to/local/package' // It must be absolute
    }
}
```

## 子包

子包的概念受[glide](https://github.com/Masterminds/glide)启发。许多情况下，我们只需要依赖一个仓库的某些子包，因此，你可以进行如下声明：

```
dependencies {
    golang {
        build name: 'github.com/big/package', subpackages: ['.', 'sub1', 'sub2/subsub']
    }
}
```

这段代码声明依赖`github.com/big/package`仓库的三个子包：根目录、`sub1`的所有后代目录、`sub2/subsub`的所有后代目录。如果你只需要`sub1`目录下的所有go文件（即`sub1`包），你需要声明：


```
dependencies {
    golang {
        build name: 'github.com/big/package', subpackages: 'sub1/.'
    }
}
```

## build依赖与test依赖

你可能注意到了，上面的依赖声明中始终包含`build`和`test`字样。它们是Gradle构建模型中的一个概念，称为[Configuration](https://docs.gradle.org/current/userguide/dependency_management.html#sub:configurations)。
Gogradle预定义了两个名为`build`和`test`的Configuration。无需深究其细节，你可以将它们理解成两组完全独立的依赖包集合。
在构建中，只有`build`依赖会生效；在测试中，`build`和`test`依赖同时生效，且`build`中的优先级更高。

## 依赖包管理

Gogradle将依赖包分为四种：

- 受源代码管理系统管理的代码包
- 位于本地的代码包
- 上述二者中的vendor目录中的代码包
- 源代码`import`语句中声明的代码包

Go语言本身没有依赖包的概念，一个包就是一个普通的文件夹。

在Gogradle中，依赖包通常以被源代码管理系统所管理的仓库为最小单位，例如，一个被Git管理的仓库中的所有go文件属于同一个依赖包。
Gogradle按照[Go语言默认的方式](https://golang.org/cmd/go/#hdr-Relative_import_paths)解析包的路径，将原本散乱的代码包看作一个个的依赖包。

## 依赖解析

依赖解析，即将依赖包解析成实际代码的过程。这个过程通常需要借助源代码管理系统，如Git。
Gogradle的目标是支持Go语言原生支持的全部四种（Git/Mercurial/Svn/Bazaar）源代码管理工具，不过当前只实现了Git和Mercurial。

## 传递性依赖

一个项目的依赖包（传递性依赖）可以由以下途径产生：

- `vendor`目录中的依赖包
- 项目目录下，外部包管理工具（包括Gogradle本身）的依赖锁定文件
- 源代码中的`import`声明

默认情况下，Gogradle会读取前两者作为传递性依赖，且`vendor`中的优先级更高。若这样得到的结果为空，Gogradle会扫描`.go`源代码中的`import`语句，
提取其中的代码包当作传递性依赖。

## 依赖冲突

由于传递性依赖的存在，在实际的构建中，依赖关系可能错综复杂。
当一个项目依赖了同一个代码包的不同版本（无论它们位于何处），我们认为这些版本处于冲突状态，需要解决。例如，A依赖了B的版本1和C，
C依赖了B的版本2，此时，B的版本1和版本2就存在冲突。Go语言的`vendor`机制允许这些版本同时存在，这是Gogradle所反对的。
因为这样做迟早会带来[问题](https://github.com/blindpirate/golang-broken-vendor)。Gogradle会尝试解决所有的依赖冲突（扁平化），
并将解决后的结果放在`vendor`中，以便进行隔离的、可复现的构建。

Gogradle解决依赖的策略是：

- 一级依赖优先级最高：声明在待构建项目中的依赖（build.gradle/gogradle.lock）具有最高的优先级
- 越新的依赖包优先级越高：较新的代码包比较旧的代码包优先级高

具体来说，Gogradle会识别每个依赖包的"更新时间"，并将这些更新时间作为解决冲突的依据。

- 受源代码管理系统管理的代码包的更新时间为特定版本的提交时间，如Git的commit time
- 位于本地文件夹中的代码包的更新时间为该文件夹的最后修改时间

## 依赖锁定

你可以令Gogradle锁定当前构建的依赖，这会在项目目录下生成一个名为`gogradle.lock`的文件，记录了构建所需的全部依赖的详细版本，以便进行稳定的、可重复的构建。无论何时，此文件都不应被手动修改。

Gogradle推荐将此文件提交到源代码管理系统中。可以通过

```
gradlew goLock 
```

生成依赖锁定文件。

## 全局排除的包

一些广泛使用的包中的import声明包含无法识别的包声明，这样会导致Gogradle报错"Cannot recognize package xxx"，从而使用户感到迷惑。
例如，`github.com/golang/mock`包含声明`import "a"`，这会阻碍Gogradle进行进一步的代码分析。
为解决此问题，从0.9开始，Gogradle默认排除了[一些这样的包](https://github.com/gogradle/gogradle/blob/master/src/main/java/com/github/blindpirate/gogradle/core/pack/GloballyIgnoredPackages.java)。

欲增加默认排除的包，在`build.gradle`中加入：

```
golang {
    ignorePackage('package1', 'package2')
}
```

如果你碰巧需要被排除的包，使用下列配置：

```
golang {
    ignoredPackages = []
}
``` 
