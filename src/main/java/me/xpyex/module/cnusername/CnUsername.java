package me.xpyex.module.cnusername;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
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
    public static final String DEFAULT_PATTERN = "^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$";
    public static final File MODULE_FOLDER = new File("CnUsername");
    public static final boolean DEBUG;

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
        DEBUG = debugResult;
        if (DEBUG) {
            Logging.info("当前Debug已启用，修改类时将会保存样本");
        }
    }

    public static void premain(final String agentArgs, final Instrumentation inst) {
        Logging.info("开始载入模块 CnUsername");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
        Logging.info("下载地址: https://github.com/0XPYEX0/CnUsername/releases");
        Logging.info("有空可以去看看有没有更新噢~");
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
                            ClassWriter writer = new ClassWriter(reader, 0);
                            ClassVisitor visitor;
                            switch (className) {
                                case ClassVisitorAllowedCharacters.CLASS_PATH:
                                    Logging.setLogger(ProxyServer.getInstance().getLogger());
                                    visitor = new ClassVisitorAllowedCharacters(className, writer, agentArgs);
                                    UpdateChecker.check();  //此时Gson必然已加载，顺便检查更新
                                    break;
                                case ClassVisitorCraftPlayerProfile.CLASS_PATH:
                                    visitor = new ClassVisitorCraftPlayerProfile(className, writer, agentArgs);
                                    break;
                                case ClassVisitorLoginListener.CLASS_PATH_MOJANG:
                                case ClassVisitorLoginListener.CLASS_PATH_SPIGOT:
                                case ClassVisitorLoginListener.CLASS_PATH_YARN:
                                    visitor = new ClassVisitorLoginListener(className, writer, agentArgs);
                                    UpdateChecker.check();  //此时Gson必然已加载，顺便检查更新
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
                            e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                    case "org/bukkit/plugin/EventExecutor$1":
                    case "org/bukkit/Bukkit":
                        Logging.setLogger(Bukkit.getLogger());
                    case "me.xpyex.plugin.xplib.bukkit.bstats.Metrics":
                        try {
                            classBeingRedefined.getConstructor(JavaPlugin.class, int.class).newInstance(Bukkit.getPluginManager().getPlugin("XPLib"), 19275);
                        } catch (ReflectiveOperationException e) {
                            Logging.warning("无法调用XPLib的BStats库: " + e);
                            e.printStackTrace();
                            Logging.info("不用担心，这并不会影响你的使用 :)");
                        }
                }
                return null;
            }
        });
    }

    public static File saveClassFile(ClassWriter writer, String className) throws IOException {
        File file = new File(MODULE_FOLDER, className.replace("/", ".") + ".class");
        Files.write(file.toPath(), writer.toByteArray());
        return file;
    }
}