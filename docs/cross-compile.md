# Build Output and Cross Compile

By default, Gogradle will place the build output into `${projectRoot}/.gogradle` directory and name it `${os}_${arch}_${packageName}`. You can change this behaviour in configuration, See [Configuration](#Configuration).


Go1.5+ introduce convenient [cross compile](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5), which enable Gogradle to output results available on multiple platform in a single build.

```
golang {
    ...
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    ...
}
```

The configuration above indicates that three results should be outputted by current build. 
