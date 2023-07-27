package me.xpyex.plugin.cnusername.bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Scanner;
import me.xpyex.model.cnusername.ClassVisitorLoginListener;
import me.xpyex.model.cnusername.CnUsername;
import me.xpyex.model.cnusername.Logging;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

public final class CnUsernameBK extends JavaPlugin {
    private final static Unsafe UNSAFE_INSTANCE;
    private final static MethodHandle DEFINE_CLASS_METHOD;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE_INSTANCE = (Unsafe) unsafeField.get(null);

            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = UNSAFE_INSTANCE.staticFieldBase(lookupField);
            long lookupOffset = UNSAFE_INSTANCE.staticFieldOffset(lookupField);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) UNSAFE_INSTANCE.getObject(lookupBase, lookupOffset);
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
    public void onDisable() {
        Logging.info("已卸载");
        //
    }

    @Override
    public void onEnable() {
        Logging.setLogger(getServer().getLogger());
        Logging.info("已加载");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
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
        try {
            ClassReader classReader = null;
            for (String classPath : new String[]{CnUsername.CLASS_PATH_LOGIN_MCP, CnUsername.CLASS_PATH_LOGIN_SPIGOT, CnUsername.CLASS_PATH_LOGIN_YARN}) {
                try {
                    classReader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(classPath + ".class"));
                    break;
                } catch (IOException ignored) {
                }
            }
            if (classReader == null) {
                throw new IllegalStateException();
            }
            String className = classReader.getClassName().replace("/", ".");
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter, readPluginPattern());
            classReader.accept(classVisitor, 0);
            loadClass(className, classWriter.toByteArray());
            Logging.info("修改完成并保存");
        } catch (Exception e) {
            e.printStackTrace();
            Logging.warning("修改失败");
        }
    }

    public String readPluginPattern() {
        Scanner input = null;
        try {
            File f = new File(getDataFolder(), "pattern.txt");
            if (!f.exists()) {
                f.createNewFile();
            }
            input = new Scanner(f);
            return input.nextLine();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            if (input != null) input.close();
        }
    }
}
