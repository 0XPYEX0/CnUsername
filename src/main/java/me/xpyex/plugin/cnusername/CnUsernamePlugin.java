package me.xpyex.plugin.cnusername;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import bot.inker.acj.JvmHacker;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.pass.Pass;
import me.xpyex.module.cnusername.pass.PassRegistry;
import me.xpyex.module.cnusername.pass.RetransformPass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public interface CnUsernamePlugin {
    AtomicReference<MethodHandle> DEFINE_CLASS_METHOD = new AtomicReference<>();

    static MethodHandle getDefineClassMethod() {
        if (DEFINE_CLASS_METHOD.get() == null) {
            try {
                DEFINE_CLASS_METHOD.set(JvmHacker.lookup().findVirtual(
                    ClassLoader.class,
                    "defineClass",
                    MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class)
                ));
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
            Logging.warning("读取正则表达式失败: " + e);
            return null;
        }
    }

    default Instrumentation instrumentationOrNull() {
        try {
            return JvmHacker.instrumentation();
        } catch (Throwable e) {
            Logging.warning("无法获取Instrumentation实例: " + e);
            e.printStackTrace();
            return null;
        }
    }

    default void applyInstrumentation(Instrumentation instrumentation) {
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                return CnUsernamePlugin.this.transform(loader, className, classBeingRedefined, classfileBuffer);
            }
        }, true);

        Map<String, Class<?>> loadedClasses = Arrays.stream(instrumentation.getAllLoadedClasses())
            .collect(Collectors.toMap(
                clazz -> clazz.getName().replace('.', '/'),
                clazz -> clazz
            ));

        Set<Class<?>> pendingRetransformClasses = new LinkedHashSet<>();

        for (String possibleClassName : PassRegistry.allPossibleClasses()) {
            Class<?> loadedClass = loadedClasses.get(possibleClassName);
            if (loadedClass != null) {
                pendingRetransformClasses.add(loadedClass);
            }
        }

        for (Class<?> retransformClass : pendingRetransformClasses) {
            try {
                instrumentation.retransformClasses(retransformClass);
            } catch (UnmodifiableClassException e) {
                Logging.warning("无法重定义类 " + retransformClass.getName() + ": " + e);
            }
        }
    }

    default byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, byte[] classfileBuffer) {
        // fail fast, ensure not loop load
        if (loader == null || className.startsWith("me/xpyex/module/cnusername/") || className.startsWith("me/xpyex/plugin/cnusername/")) {
            return null;
        }

        Pass pass = PassRegistry.getPass(className);
        if (pass == null || PassRegistry.isModified(className)) {
            return null;
        }

        if (pass instanceof RetransformPass) {
            ((RetransformPass) pass).retransform(classBeingRedefined, readPluginPattern());
        }

        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor classVisitor = pass.create(className.replace('/', '.'), classWriter, readPluginPattern());
        reader.accept(classVisitor, 0);
        byte[] modifiedClassfileBuffer = classWriter.toByteArray();

        if (CnUsername.DEBUG) {
            try {
                Logging.info("Debug模式开启，保存修改后的样本以供调试");
                Logging.info("已保存 " + className + " 类的文件样本至: " + CnUsername.saveClassFile(modifiedClassfileBuffer, className).getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PassRegistry.setModified(className);
        return modifiedClassfileBuffer;
    }


    File getDataFolder();
}
