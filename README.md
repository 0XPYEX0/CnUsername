# CnUsername

Allow player to use Chinese chars in username of Minecraft

允许玩家使用中文名甚至特殊字符进入服务器

介绍贴: https://www.mcbbs.net/thread-1449800-1-1.html (已似)

# 插件方式加载教程

### 推荐所有有条件的服主使用[JavaAgent方式](https://github.com/0XPYEX0/CnUsername?tab=readme-ov-file#javaagent%E5%8A%A0%E8%BD%BD%E6%95%99%E7%A8%8B)加载，以解锁所有功能

1. 在[Releases](https://github.com/0XPYEX0/CnUsername/releases)中下载<br>
2. 放入`plugins`文件夹 [仅Bukkit|BungeeCord，及其所有分支(如Spigot|Paper|WaterFall等)]<br>
3. 插件方式加载有诸多限制，如:
   <br>    ①原版实体选择器不支持特殊名字玩家. 例如无法使用`/tp`命令，请使用`/tp "<username>"`  其中`<username>`替换为玩家名字
   <br>    ②在1.20.5+，`Paper`及其分支服务端，玩家名字长度不能长于16，否则无法进入服务器. JavaAgent加载方式不受此限制<br>
4. 如需自定义正则匹配，请修改`plugins/CnUsername/pattern.txt`

# JavaAgent加载教程

### 推荐所有有条件的服主使用[JavaAgent方式](https://github.com/0XPYEX0/CnUsername?tab=readme-ov-file#javaagent%E5%8A%A0%E8%BD%BD%E6%95%99%E7%A8%8B)加载，以解锁所有功能

1. 在[Releases](https://github.com/0XPYEX0/CnUsername/releases)中下载
2. 放入`服务端根目录`
3. 修改你的启动命令，在`java`后写入`-javaagent:CnUsername-version-all.jar`. 例如:
   <br>    `java -javaagent:CnUsername-1.0.7-all.jar -jar server.jar`
   <br>    **注意，此处仅为举例说明，请根据实际情况编写**
4. JavaAgent加载模式可以解锁所有功能，包括但不限于:
   <br>    ①玩家名字长度可通过修改正则自定义
   <br>    ②能够正常使用原版实体选择器选择特殊名字玩家
5. 如需自定义正则，修改前面启动命令为`-javaagent:CnUsername-<version>-all.jar=<正则表达式>`，例如:
   `-javaagent:CnUsername-1.0.7-all.jar=^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_一-龥]{2,10}$`

### 注意事项

1. 在`Paper`及其分支服务端中，需要在配置文件中修改`perform-validate-username`为`false`，否则无法进入服务器；
2. 安装`AuthMe`插件的情况下，需修改`AuthMe`插件的配置文件`config.yml`中的`allowedNicknameCharacters`
   。这代表被允许的玩家名的正则表达式，否则无法进入服务器；
3. 安装`LuckPerms`插件的情况下，需修改`LuckPerms`插件的配置文件`config.yml`中的`allow-invalid-usernames`为`true`
   ，否则无法正常处理权限；
4. 安装`Skript`插件的情况下，需修改`Skript`插件的配置文件`config.sk`中的`player name regex pattern`，此为正则表达式，否则无法正常使用玩家功能.

默认正则规则: `^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}|CS\-CoreLib$`
