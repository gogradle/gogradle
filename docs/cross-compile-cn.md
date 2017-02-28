# 构建输出与交叉编译

默认情况下，Gogradle会将构建的输出放置在`${projectRoot}/.gogradle`目录下，命名为`${os}_${arch}_${packageName}`。
你可以通过相应配置改变输出位置和命名约定，详见[配置](#配置)。

Go1.5之后引入了方便的[交叉编译](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5)，因此，Gogradle能够在一次构建中输出多个平台下的构建结果。

```
golang {
    ...
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    ...
}
```

上述配置指明，需要当前的构建输出3份结果。`targetPlatform`字符串必须遵循以上格式。