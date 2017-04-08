# Build Output and Cross Compile

By default, Gogradle will place the build output into `${projectRoot}/.gogradle` directory and name it `${GOOS}_${GOARCH}_${PACKAGE_NAME}`. You can change this behaviour in build as follows:

Moreover, Go1.5+ introduce convenient [cross compile](https://dave.cheney.net/2015/08/22/cross-compilation-with-go-1-5), which enable Gogradle to output results available on multiple platform in a single build.

```
goBuild {
    // For cross compile, go 1.5+ required
    targetPlatform = 'windows-amd64, linux-amd64, linux-386'
    
    // Location of build output 
    // It can be absolute or relative (to project root)
    // ${} will be rendered when cross compiling
    outputLocation = './.gogradle/${GOOS}_${GOARCH}_${PROJECT_NAME}${GOEXE}'
}
```

The configuration above indicates that three results should be outputted by current build. 

