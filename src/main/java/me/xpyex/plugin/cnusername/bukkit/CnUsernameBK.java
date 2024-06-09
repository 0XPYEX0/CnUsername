package me.xpyex.plugin.cnusername.bukkit;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.CnUsernamePlugin;
import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.UpdateChecker;
import me.xpyex.module.cnusername.minecraft.ClassVisitorLoginListener;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

public final class CnUsernameBK extends JavaPlugin implements CnUsernamePlugin {
    private final static MethodHandle DEFINE_CLASS_METHOD;

    static {
        try {
            Unsafe unsafeInstance;
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafeInstance = (Unsafe) unsafeField.get(null);  //Unsafe.theUnsafe静态变量

            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");  //Lookup.IMPL_LOOKUP常量
            Object lookupBase = unsafeInstance.staticFieldBase(lookupField);
            long lookupOffset = unsafeInstance.staticFieldOffset(lookupField);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafeInstance.getObject(lookupBase, lookupOffset);
            DEFINE_CLASS_METHOD = lookup.findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("初始化失败", e);
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
            DEFINE_CLASS_METHOD.invoke(Bukkit.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new IllegalStateException("修改类 " + className + " 失败!", e);
        }
    }

    @Override
    public void onLoad() {
        Logging.setLogger(getServer().getLogger());
        Logging.info("已加载");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");

        try {
            // net.minecraft.util.StringUtil
            ClassReader reader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(ClassVisitorStringUtil.CLASS_PATH + ".class"));
            String className = reader.getClassName().replace("/", ".");
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(reader, 0);
            ClassVisitor classVisitor = new ClassVisitorStringUtil(className, classWriter, readPluginPattern());
            reader.accept(classVisitor, 0);
            loadClass(className, classWriter.toByteArray());
            Logging.info("修改完成并保存");
            if (CnUsername.DEBUG) {
                try {
                    Logging.info("Debug模式开启，保存修改后的样本以供调试");
                    Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(classWriter, className).getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logging.warning("修改StringUtil类失败: " + e);
            Logging.warning("1.20.5以下的版本忽略此条，无用");
        }

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
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter, readPluginPattern());
            classReader.accept(classVisitor, 0);
            loadClass(className, classWriter.toByteArray());
            Logging.info("修改完成并保存");
            if (CnUsername.DEBUG) {
                try {
                    Logging.info("Debug模式开启，保存修改后的样本以供调试");
                    Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(classWriter, className).getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        getServer().getScheduler().runTaskAsynchronously(this, UpdateChecker::check);
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            if (getServer().getPluginManager().isPluginEnabled("XPLib")) {
                try {
                    Class<?> metricsClass = Class.forName("me.xpyex.plugin.xplib.bukkit.bstats.Metrics");
                    metricsClass.getConstructor(JavaPlugin.class, int.class).newInstance(this, 19275);
                } catch (ReflectiveOperationException e) {
                    Logging.warning("无法调用XPLib的BStats库: " + e);
                    e.printStackTrace();
                    Logging.info("不用担心，这并不会影响你的使用 :)");
                }
            }
        });
    }
}
