#平头哥项目

## 构建
仅仅支持linux或者mac，不支持windows，因为本项目的构建不是使用AndroidStudio的gradle脚本，而是使用shell脚本

自己搞不定的同学，可以使用这个VMware的虚拟机文件
```
链接: https://pan.baidu.com/s/1hxJ1wi-oE0Fg8pCbQQ60XQ  密码: 3viw
```
- 登陆密码: ``0516``
- 一般情况需要调整虚拟机配置（合适的内存和CPU核心数量）
- 进入代码文件夹，请执行``git pull``刷新代码

## 生产构建：
```
./script/create-dist.sh
```

## debug环境
```
./script/build_ratel_repkg.sh xxx.apk
```

## 单步调试
将命令行参数放到``container-builder-repkg/src/main/java/com/virjar/ratel/builder/ratelentry/Main.java``,然后单步调试main函数的执行流程即可


## RatelManager apk构建(请注意，自2.0版本之后，RM构建需要使用脚本构建，否则无法支持app端进行重打包)：
```
./script/build_ratel_manager.sh
```
