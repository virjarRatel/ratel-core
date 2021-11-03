# apk parser

apk解析器，同步自： https://github.com/hsiafan/apk-parser

在使用apkparser的时候，发现有部分apk的路径编码使用了utf8，
这是因为apkparser是用来jdk原生的zip库：``java.util.zip.xxx``,会导致错误：``java.util.zip.ZipException: invalid CEN header (encrypted entry)``

为了解决这个问题，我们使用apache的zip增强替换apkparser中的zip库，如此原因我们需要自己构建apkparser