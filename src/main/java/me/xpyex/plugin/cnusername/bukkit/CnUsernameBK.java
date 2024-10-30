package me.xpyex.plugin.cnusername.bukkit;

import java.io.IOException;
import java.lang.Runtime.Version;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.UpdateChecker;
import me.xpyex.module.cnusername.impl.PatternVisitor;
import me.xpyex.module.cnusername.minecraft.ClassVisitorLoginListener;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringUtil;
import me.xpyex.module.cnusername.paper.ClassVisitorCraftPlayerProfile;
import me.xpyex.plugin.cnusername.CnUsernamePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public final class CnUsernameBK extends JavaPlugin implements CnUsernamePlugin {
    private static final Version VER_1_20_4 = Version.parse("1.20.4");
    private final Version version;

    public CnUsernameBK() {
        Logging.setLogger(getServer().getLogger());
        Logging.info("Bukkit开始初始化插件");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");

        version = Version.parse(getServer().getBukkitVersion().split("-")[0]);
        Logging.info("当前服务端版本为: §e" + version);

        if (version.compareToIgnoreOptional(VER_1_20_4) == 0) {  // 1.20.4
            Logging.info("服务端为§e1.20.4§r版本，无需使用插件版本修改。");
            Logging.info("您可以使用§eJavaAgent模式§r修复命令选择器的问题");
            Logging.warning("请注意，仅§e1.20.4§r未检查中文名，§e1.20.5§r开始又加入了检查，仍然需要§bCnUsername");
            return;
        }


        if (version.compareToIgnoreOptional(VER_1_20_4) > 0) {
            Logging.info("检测到服务端为§e1.20.4§r以上版本");
            try {
                // net.minecraft.util.StringUtil
                ClassReader reader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(ClassVisitorStringUtil.CLASS_PATH + ".class"));
                String className = reader.getClassName().replace("/", ".");
                byte[] data = modifyClass(reader, ClassVisitorStringUtil.class);
                loadClass(className, data);
                Logging.info("修改完成并保存");
                if (CnUsername.DEBUG) {
                    try {
                        Logging.info("Debug模式开启，保存修改后的样本以供调试");
                        Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(data, className).getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                if (CnUsername.DEBUG) e.printStackTrace();
                Logging.warning("修改§cStringUtil类失败: " + e);
            }

            try {
                // com.destroystokyo.paper.profile.CraftPlayerProfile
                ClassReader reader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(ClassVisitorCraftPlayerProfile.CLASS_PATH + ".class"));
                String className = reader.getClassName().replace("/", ".");
                byte[] data = modifyClass(reader, ClassVisitorCraftPlayerProfile.class);
                loadClass(className, data);
                Logging.info("修改完成并保存");
                if (CnUsername.DEBUG) {
                    try {
                        Logging.info("Debug模式开启，保存修改后的样本以供调试");
                        Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(data, className).getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                if (CnUsername.DEBUG) e.printStackTrace();
                Logging.warning("修改CraftPlayerProfile类失败: " + e);
            }
        }
    }

    /**
     * 运行中动态加载字节码
     *
     * @param className 类名
     * @param bytes     字节码
     */
    public static void loadClass(String className, byte[] bytes) {
        try {
            CnUsernamePlugin.getDefineClassMethod().invoke(Bukkit.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new IllegalStateException("修改类 " + className + " 失败!", e);
        }
    }

    @Override
    public void onLoad() {
        if (version.compareToIgnoreOptional(VER_1_20_4) == 0) return;  // 1.20.4
        Logging.info("进入插件加载流程");

        try {
            ClassReader classReader = null;
            for (String classPath : new String[]{
                ClassVisitorLoginListener.CLASS_PATH_MOJANG,
                ClassVisitorLoginListener.CLASS_PATH_SPIGOT,
                ClassVisitorLoginListener.CLASS_PATH_YARN
            }) {
                try {
                    classReader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(classPath + ".class"));
                    break;
                } catch (IOException ignored) {
                }
            }
            if (classReader == null) {
                throw new IllegalStateException("无法读取对应Class: Class可能不存在，或Class先于插件加载.");
            }
            String className = classReader.getClassName().replace("/", ".");
            byte[] data = modifyClass(classReader, ClassVisitorLoginListener.class);
            loadClass(className, data);
            Logging.info("修改完成并保存");
            if (CnUsername.DEBUG) {
                try {
                    Logging.info("Debug模式开启，保存修改后的样本以供调试");
                    Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(data, className).getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (CnUsername.DEBUG) e.printStackTrace();
            Logging.warning("修改LoginListener类失败: " + e);
        }
    }

    @Override
    public void onDisable() {
        Logging.info("已卸载");
        //
    }

    @Override
    public void onEnable() {
        Logging.info("进入插件启用流程");
        getServer().getScheduler().runTaskAsynchronously(this, UpdateChecker::check);
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            if (getServer().getPluginManager().isPluginEnabled("XPLib")) {
                try {
                    Class<?> metricsClass = Class.forName("me.xpyex.plugin.xplib.bukkit.bstats.Metrics");
                    metricsClass.getConstructor(JavaPlugin.class, int.class).newInstance(this, 19275);
                } catch (ReflectiveOperationException e) {
                    Logging.warning("无法调用XPLib的BStats库: " + e);
                    if (CnUsername.DEBUG) e.printStackTrace();
                    Logging.info("不用担心，这并不会影响你的使用 :)");
                }
            }
        });
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPreLogin(AsyncPlayerPreLoginEvent event) {
                if ("CS-CoreLib".equals(event.getName())) {
                    event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
                    event.setKickMessage("Invalid username\nCnUsername Defend");
                }
            }
        }, this);
    }

    private byte[] modifyClass(ClassReader reader, Class<? extends PatternVisitor> type) {
        String className = reader.getClassName().replace("/", ".");
        Logging.info("开始修改类 " + className);
        ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        try {
            type.getConstructor(String.class, ClassVisitor.class, String.class).newInstance(className, classWriter, readPluginPattern());
            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter, readPluginPattern());
            reader.accept(classVisitor, 0);
            return classWriter.toByteArray();
        } catch (ReflectiveOperationException e) {
            return new byte[0];
        }
    }
}
