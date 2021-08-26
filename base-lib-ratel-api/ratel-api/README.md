# RatelApi
这是Ratel(平头哥)的api代码模块，剥离自Ratel整体工程，以git父子module进行关联。

剥离RatelApi工程的目的是使得用户可以通过代码使用RatelEngine已经支持，但是RatelApi还没有发布到jar包的功能。另一方面方便RatelEngine调试

# 外部工程通过git submodule依赖本项目的方法

1. 创建子module，注意path需要提供``二级``目录

```
 git submodule add git@git.virjar.com:ratel/ratelapi.git base-lib-ratel-api/ratel-api
```
此时工程将会初始化到``base-lib-ratel-api/ratel-api``下面

2. 在settings.gradle里面配置该子项目 ``':base-lib-ratel-api'``,请注意不要带上``/ratel-api``,否则将会使用ratel-api下面的build.gradle文件。
这是由于ratel-api自带的编译规则可能有他的独立性。

3. 在``base-lib-ratel-api``目录下创建``build.gradle``并写入如下内容

```
apply plugin: 'java-library'

sourceSets {
    main {
        resources {
            srcDir 'ratel-api/src/main/resources'
        }
        java {
            srcDir 'ratel-api/src/main/java'
        }
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    compileOnly 'com.google.android:android:4.1.1.4'
    compileOnly 'com.google.code.findbugs:jsr305:3.0.2'
    compileOnly 'com.android.support:support-annotations:28.0.0'
}

```
