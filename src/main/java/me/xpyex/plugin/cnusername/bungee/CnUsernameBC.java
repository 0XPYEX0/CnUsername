package me.xpyex.plugin.cnusername.bungee;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import me.xpyex.module.cnusername.bungee.ClassVisitorAllowedCharacters;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.CnUsernamePlugin;
import me.xpyex.module.cnusername.Logging;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import sun.misc.Unsafe;

public class CnUsernameBC extends Plugin implements CnUsernamePlugin {
    private final static MethodHandle DEFINE_CLASS_METHOD;

    static {
        try {
            Unsafe unsafeInstance;
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafeInstance = (Unsafe) unsafeField.get(null);

            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
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
            DEFINE_CLASS_METHOD.invoke(ProxyServer.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new IllegalStateException("修改类 " + className + " 失败!", e);
        }
    }

    @Override
    public void onEnable() {
        Logging.setLogger(getProxy().getLogger());
        Logging.info("已加载");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
        try {
            ClassReader classReader = new ClassReader(ProxyServer.class.getClassLoader().getResourceAsStream(CnUsername.CLASS_PATH_BUNGEE + ".class"));
            String className = classReader.getClassName().replace("/", ".");
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassVisitor classVisitor = new ClassVisitorAllowedCharacters(className, classWriter, readPluginPattern());
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
            Logging.warning("修改失败");
        }
    }

    @Override
    public void onDisable() {
        Logging.info("已卸载");
        //
    }
}
