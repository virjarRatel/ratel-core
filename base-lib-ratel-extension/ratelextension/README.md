# RatelExtension
这是Ratel(平头哥)的RatelExtension代码模块，剥离自Ratel整体工程，以git父子module进行关联。

剥离RatelExtension工程的目的是使得用户可以通过代码使用RatelEngine已经支持，但是RatelExtension还没有发布到jar包的功能。

由于RatelExtension是一个可选模块，使用compile作用域引入，所以如果release版本中的代码存在bug，你可以直接通过修改这个工程的代码来fix

另外RatelExtension有很多实验性质的代码逻辑，且作为一个工具API，需要足够的业务场景覆盖测试，所以特地开放可编辑源码给各个业务方。

# 外部工程通过git submodule依赖本项目的方法

1. 创建子module，注意path需要提供``二级``目录

```
 git submodule add git@git.virjar.com:ratel/ratelextension.git base-lib-ratel-extension/ratelextension
```
此时工程将会初始化到``base-lib-ratel-extension/ratelextension``下面

2. 在settings.gradle里面配置该子项目 ``':base-lib-ratel-extension'``,请注意不要带上``/ratelextension``,否则将会使用ratelextension下面的build.gradle文件。
这是由于ratel-api自带的编译规则可能有他的独立性。

3. 在``base-lib-ratel-extension``目录下创建``build.gradle``并写入如下内容

```
apply plugin: 'java-library'

sourceSets {
    main {
        resources {
            srcDir 'ratelextension/src/main/resources'
        }
        java {
            srcDir 'ratelextension/src/main/java'
        }
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    compileOnly 'com.google.android:android:4.1.1.4'
    compileOnly 'com.google.code.findbugs:jsr305:3.0.2'
    compileOnly 'com.android.support:support-annotations:28.0.0'
    compileOnly 'com.virjar:ratel-api:1.3.2'
    compileOnly 'com.virjar:sekiro-api:1.0.1'
}


```

4. 在你的工程中添加本项目依赖
dependencies {
    api ':base-lib-ratel-extension'
}

