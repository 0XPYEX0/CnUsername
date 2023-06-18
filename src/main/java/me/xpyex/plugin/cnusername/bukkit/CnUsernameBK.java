package me.xpyex.plugin.cnusername.bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import me.xpyex.model.cnusername.CnUsername;
import me.xpyex.model.cnusername.ClassVisitorLoginListener;
import me.xpyex.model.cnusername.Logging;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

public final class CnUsernameBK extends JavaPlugin {
    private static final String CLASS_NAME = "net.minecraft.server.network.LoginListener";

    /**
     * 运行中动态加载字节码
     *
     * @param className 类名
     * @param bytes     字节码
     */
    public static void loadClass(String className, byte[] bytes) {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeField.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
            MethodHandle defineClassMethod = lookup.findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class));
            defineClassMethod.invoke(Bukkit.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new IllegalStateException("修改失败!", e);
        }
    }

    public static boolean a(String name) {
        return Pattern.compile("^[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,16}$").matcher(name).matches();
    }

    public static boolean isAllowedInUnquotedString(char c) {
        return ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || c == '-' || c == '.' || c == '+') || (c > '\u4e00' && c < '\u9fa5');
    }

    @Override
    public void onDisable() {
        Logging.info("已卸载");
        //
    }

    @Override
    public void onEnable() {
        Logging.info("已加载");
        Logging.info("开始修改类 " + CLASS_NAME);
        try {
            ClassReader classReader = new ClassReader(Bukkit.class.getClassLoader().getResourceAsStream(CnUsername.CLASS_PATH_LOGIN + ".class"));
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitorLoginListener(CLASS_NAME, classWriter);
            classReader.accept(classVisitor, 0);
            loadClass(CLASS_NAME, classWriter.toByteArray());
            getLogger().info("修改完成");
            // 加载类
        } catch (Exception e) {
            e.printStackTrace();
            Logging.warning("修改失败");
        }
    }
}
