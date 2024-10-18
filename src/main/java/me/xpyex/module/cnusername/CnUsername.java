package me.xpyex.module.cnusername;

import java.io.File;
import java.io.IOException;
import java.lang.Runtime.Version;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.xpyex.module.cnusername.bungee.ClassVisitorAllowedCharacters;
import me.xpyex.module.cnusername.minecraft.ClassVisitorLoginListener;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringReader;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringUtil;
import me.xpyex.module.cnusername.paper.ClassVisitorCraftPlayerProfile;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CnUsername {
    public static final String DEFAULT_PATTERN = "^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}|CS\\-CoreLib$";
    public static final File MODULE_FOLDER = new File("CnUsername");
    public static final boolean DEBUG;
    public static Version MC_VERSION = null;

    static {
        boolean debugResult;
        try {
            if (MODULE_FOLDER.exists() && MODULE_FOLDER.isFile()) {
                throw new IllegalStateException("错误: 服务端根目录下已存在CnUsername文件，且非文件夹");
            }
            if (!MODULE_FOLDER.exists()) {
                MODULE_FOLDER.mkdirs();
            }
            File debugFile = new File(MODULE_FOLDER, "debug.txt");
            if (!debugFile.exists()) {
                Files.write(debugFile.toPath(), "false".getBytes(StandardCharsets.UTF_8));
            }
            debugResult = "true".equalsIgnoreCase(Files.readAllLines(debugFile.toPath(), StandardCharsets.UTF_8).get(0));
        } catch (Exception e) {
            debugResult = false;
            e.printStackTrace();
        }
        if (DEBUG = debugResult) {
            Logging.info("当前Debug已启用，修改类时将会保存样本");
        }
    }

    public static void premain(final String agentArgs, final Instrumentation inst) {
        System.out.println(agentArgs);
        Logging.info("开始载入模块 §eCnUsername");
        Logging.info("如遇Bug，或需提出建议: §aQQ群546338486 §r| §eQQ1723275529");
        Logging.info("开源地址§6§o(GitHub)§r: https://github.com/0XPYEX0/CnUsername");
        Logging.info("有空可以去看看有没有更新噢~");
        Logging.info("===========================================================");
        try {
            Logging.info("开始检查banned-players.json文件，以添加补丁");
            addToBanList("CS-CoreLib");
            Logging.info("补丁应用完成");
        } catch (Exception e) {
            Logging.warning("添加补丁失败: " + e);
            if (DEBUG) e.printStackTrace();
            Logging.warning("建议服务器启动后手动封禁CS-CoreLib玩家名");
        }
        Logging.info("===========================================================");
        Logging.info("当前服务端运行于: §e" + getMcVersion());
        UpdateChecker.check();
        Logging.info("等待Minecraft加载...");
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
                switch (className) {
                    case ClassVisitorAllowedCharacters.CLASS_PATH:
                    case ClassVisitorCraftPlayerProfile.CLASS_PATH:
                    case ClassVisitorLoginListener.CLASS_PATH_MOJANG:
                    case ClassVisitorLoginListener.CLASS_PATH_SPIGOT:
                    case ClassVisitorLoginListener.CLASS_PATH_YARN:
                    case ClassVisitorStringReader.CLASS_PATH:
                    case ClassVisitorStringUtil.CLASS_PATH:
                        Logging.info("开始修改类 " + className);
                        try {
                            ClassReader reader = new ClassReader(classFileBuffer);
                            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                            ClassVisitor visitor;
                            switch (className) {
                                case ClassVisitorAllowedCharacters.CLASS_PATH:
                                    Logging.setLogger(ProxyServer.getInstance().getLogger());  //BungeeCord Logger
                                    visitor = new ClassVisitorAllowedCharacters(className, writer, agentArgs);
                                    break;
                                case ClassVisitorCraftPlayerProfile.CLASS_PATH:
                                    try {
                                        Class.forName(ClassVisitorStringUtil.CLASS_PATH.replace("/", "."), true, loader);
                                    } catch (ClassNotFoundException ignored) {
                                    }
                                    if (Version.parse("1.20.4").compareToIgnoreOptional(MC_VERSION) >= 0) {
                                        Logging.info("服务端处于§e1.20.5以下§r版本，无需修改CraftPlayerProfile类");
                                        return null;
                                    }
                                    visitor = new ClassVisitorCraftPlayerProfile(className, writer, agentArgs);
                                    break;
                                case ClassVisitorLoginListener.CLASS_PATH_MOJANG:
                                case ClassVisitorLoginListener.CLASS_PATH_SPIGOT:
                                case ClassVisitorLoginListener.CLASS_PATH_YARN:
                                    visitor = new ClassVisitorLoginListener(className, writer, agentArgs);
                                    break;
                                case ClassVisitorStringReader.CLASS_PATH:
                                    visitor = new ClassVisitorStringReader(className, writer);
                                    break;
                                case ClassVisitorStringUtil.CLASS_PATH:
                                    visitor = new ClassVisitorStringUtil(className, writer, agentArgs);
                                    break;
                                default:
                                    Logging.info("修改失败: 未捕捉className");
                                    return null;
                            }
                            reader.accept(visitor, 0);
                            Logging.info("修改完成并保存");
                            if (DEBUG) {
                                try {
                                    Logging.info("Debug模式开启，保存修改后的样本以供调试");
                                    Logging.info("已保存 " + className + " 类的文件样本至: " + saveClassFile(writer, className).getPath());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return writer.toByteArray();
                        } catch (Exception e) {
                            if (DEBUG) e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                        break;
                    case "org/bukkit/plugin/EventExecutor$1":
                    case "org/bukkit/craftbukkit/CraftServer$2":
                    case "org/bukkit/command/ConsoleCommandSender":
                        Logging.setLogger(Bukkit.getLogger());
                        Logging.info("CraftServer loaded");
                        break;
                    case "me/xpyex/plugin/xplib/bukkit/bstats/Metrics":
                        try {
                            classBeingRedefined.getConstructor(JavaPlugin.class, int.class).newInstance(Bukkit.getPluginManager().getPlugin("XPLib"), 19275);
                        } catch (ReflectiveOperationException e) {
                            Logging.warning("无法调用XPLib的BStats库: " + e);
                            if (DEBUG) e.printStackTrace();
                            Logging.info("不用担心，这并不会影响你的使用 :)");
                        }
                        break;
                }
                return null;
            }
        });
    }

    public static File saveClassFile(ClassWriter writer, String className) throws IOException {
        return saveClassFile(writer.toByteArray(), className);
        //
    }

    public static File saveClassFile(byte[] data, String className) throws IOException {
        File file = new File(MODULE_FOLDER, className.replace("/", ".") + ".class");
        Files.write(file.toPath(), data);
        return file;
    }

    public static void addToBanList(String name) throws IOException {
        File f = new File("banned-players.json");
        if (f.isDirectory()) {
            throw new RuntimeException("banned-players.json是个文件夹？\nWhy banned-players.json is a directory?");
        }
        if (!f.exists()) f.createNewFile();
        String content = Files.readString(f.toPath());
        if (content == null || content.trim().isEmpty() || "[]".equals(content.trim())) {
            Logging.info("banned-players.json文件内容为空，执行覆写操作");
            Files.write(f.toPath(), ("[\n" +
                                         "  {\n" +
                                         "    \"uuid\": \"" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()) + "\",\n" +  //只需要考虑离线，在线服务器不会用CnUsername也用不了
                                         "    \"name\": \"" + name + "\",\n" +
                                         "    \"created\": \"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " +0800\",\n" +
                                         "    \"source\": \"CnUsername\",\n" +
                                         "    \"expires\": \"forever\",\n" +
                                         "    \"reason\": \"Invalid username\"\n" +
                                         "  }\n" +
                                         "]").getBytes());
        } else if (!content.contains(name)) {
            Logging.info("banned-players.json文件内不存在 " + name + " 玩家，执行添加操作");
            Files.write(f.toPath(), (content.substring(0, content.length() - 1)  //去掉最后一个右中括号
                                         + ",\n" +  //JsonArray新增
                                         "  {\n" +
                                         "    \"uuid\": \"" + UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()) + "\",\n" +
                                         "    \"name\": \"" + name + "\",\n" +
                                         "    \"created\": \"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " +0800\",\n" +
                                         "    \"source\": \"CnUsername\",\n" +
                                         "    \"expires\": \"forever\",\n" +
                                         "    \"reason\": \"Invalid username\"\n" +
                                         "  }\n" +
                                         "]"
            ).getBytes());
        }
    }

    public static Version getMcVersion() {
        if (MC_VERSION == null) {
            File properties = new File("server.properties").getAbsoluteFile();
            files: for (File file : properties.getParentFile().listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    try (JarFile jar = new JarFile(file)) {
                        Enumeration<JarEntry> enumFiles = jar.entries();
                        while (enumFiles.hasMoreElements()) {
                            JarEntry entry = enumFiles.nextElement();
                            String entryName = entry.getName();
                            if (entryName.contains("META-INF/versions/1.") && entryName.endsWith("/")) {
                                String[] split = entryName.split("/");
                                MC_VERSION = Version.parse(split[split.length - 1]);
                                break files;
                            } else if (entryName.contains("META-INF/versions/") && (entryName.endsWith(".jar") || entryName.endsWith(".jar.patch"))) {
                                String[] split = entryName.split("/");
                                MC_VERSION = Version.parse(split[split.length - 1].split("-")[1]);
                                break files;
                            }
                        }
                    } catch (Exception e) {
                        if (DEBUG) e.printStackTrace();
                    }
                }
            }
        }
        return MC_VERSION;
    }
}