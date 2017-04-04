# 构建输出与交叉编译

默认情况下，Gogradle会将构建的输出放置在`${projectRoot}/.gogradle`目录下，命名为`${GOOS}_${GOARCH}_${PACKAGE_NAME}`。
你可以通过如下配置改变输出位置和命名约定。

此外，Go1.5之后引入了方便的[交叉编译](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5)，因此，Gogradle能够在一次构建中输出多个平台下的构建结果。

```
build {
    // 交叉编译的输出选项，注意，要求go 1.5+
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    
    // 输出文件的路径，可以是绝对路径（相对于项目目录）或者相对路径
    // 其中的${}占位符会在交叉编译时被渲染
    outputLocation = './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'
}
```

上述配置指明，需要当前的构建输出3份结果。Gogradle会自动调用`go build`三次并生成对应的输出文件。`targetPlatform`字符串必须遵循以上格式。


