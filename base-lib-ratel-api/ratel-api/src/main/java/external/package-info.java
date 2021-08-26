/**
 * 一些api，为避免和用户自定义引用的代码冲突。这里单独迁移一份，类似xposed framework中对common-lang的处理
 * <p>
 * 代码生成方法:
 * 1. 找到希望迁移的代码，如在maven仓库下载jar的source http://central.maven.org/maven2/com/alibaba/fastjson/1.1.71.android/fastjson-1.1.71.android-sources.jar
 * 2. 解压代码，并解压到external包中
 * 3. 执行命令：find .|grep ".java" | xargs sed -i ""  "s/com\.alibaba\.fastjson/external\.com\.alibaba\.fastjson/g"
 *
 * <ul>
 * <li>find .|grep ".java" | xargs sed -i ""  "s/package okio;/package external\.okio;/g"</li>
 * </ul>
 * 注意，上述命令仅在MAC上面支持
 */
package external;
