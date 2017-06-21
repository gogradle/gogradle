# FAQ

## 如何处理 'Cannot recognized package: appengine' ?

`appengine`和`appengine_internal`是GCE提供的包，因此无法获取。你可以在`build.gradle`中加入如下配置：

```
repositories {
    golang {
        root { it.startsWith('appengine') }
        emptyDir()
    }
}
```

## 在Windows下如何处理`java.nio.file.FileSystemException: A required privilege is not held by the client`?

Gogradle支持项目级`GOPATH`的方法是在项目下创建一个符号链接，因此需要Windows的该权限。你可以采取以下解决方案之一：

- 使用管理员身份运行或者关闭UAC
- 授予当前用户创建符号链接的权限：[这里](https://stackoverflow.com/questions/23217460/how-to-create-soft-symbolic-link-using-java-nio-files)
- 使用[全局GOPATH](https://golang.org/doc/code.html#Workspaces)


## Gogradle的任务名和其他插件冲突了怎么办？

参考[这里](https://github.com/gogradle/gogradle/blob/master/docs/tasks-cn.md#任务名冲突)

## Gogradle支持多工程构建么？

多工程构建是一个复杂的问题，Gogradle对此的答案是：不完全支持。
