#平头哥项目

## 构建
仅仅支持linux或者mac，不支持windows，因为本项目的构建不是使用AndroidStudio的gradle脚本，而是使用shell脚本

## 生产构建：
```
./script/create-dist.sh
```

## debug环境
```
./script/build_ratel_repkg.sh xxx.apk
```

## 单步调试
将命令行参数放到``container-builder-repkg/src/main/java/ratelentry/Main.java``,然后单步调试main函数的执行流程即可

