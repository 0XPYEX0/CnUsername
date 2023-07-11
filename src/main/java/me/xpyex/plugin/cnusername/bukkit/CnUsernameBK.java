package me.xpyex.plugin.cnusername.bukkit;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
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
        try {
            ClassReader classReader;
            try {
                classReader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(CnUsername.CLASS_PATH_LOGIN_SPIGOT + ".class"));
            } catch (IOException ignored) {
                classReader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(CnUsername.CLASS_PATH_LOGIN_MCP + ".class"));
            }
            String className = classReader.getClassName().replace("/", ".");
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter);
            classReader.accept(classVisitor, 0);
            loadClass(className, classWriter.toByteArray());
            Logging.info("修改完成并保存");
            // 加载类
        } catch (Exception e) {
            e.printStackTrace();
            Logging.warning("修改失败");
        }
    }
}
