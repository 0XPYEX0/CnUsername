package me.xpyex.plugin.cnusername;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.Logging;
import sun.misc.Unsafe;

public interface CnUsernamePlugin {
    public static final AtomicReference<MethodHandle> DEFINE_CLASS_METHOD = new AtomicReference<>();

    static MethodHandle getDefineClassMethod() {
        if (DEFINE_CLASS_METHOD.get() == null) {
            try {
                Unsafe unsafeInstance;
                Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafeInstance = (Unsafe) unsafeField.get(null);  //Unsafe.theUnsafe静态变量

                Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");  //Lookup.IMPL_LOOKUP常量
                Object lookupBase = unsafeInstance.staticFieldBase(lookupField);
                long lookupOffset = unsafeInstance.staticFieldOffset(lookupField);
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) unsafeInstance.getObject(lookupBase, lookupOffset);
                DEFINE_CLASS_METHOD.set(lookup.findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class)));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("初始化失败", e);
            }
        }
        return DEFINE_CLASS_METHOD.get();
    }

    default String readPluginPattern() {
        try {
            File f = new File(getDataFolder(), "pattern.txt");
            File folder = f.getParentFile();
            if (folder.exists() && folder.isFile()) {
                Logging.warning("错误: 插件目录下已存在CnUsername文件，且非文件夹");
                return null;
            }
            if (!f.exists()) {
                folder.mkdirs();
                f.createNewFile();
            }
            return Files.readString(f.toPath(), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            if (CnUsername.DEBUG) e.printStackTrace();
            return null;
        }
    }

    File getDataFolder();
}
