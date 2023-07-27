package me.xpyex.model.cnusername;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CnUsername {
    public static final String CLASS_PATH_LOGIN_SPIGOT = "net/minecraft/server/network/LoginListener";
    public static final String CLASS_PATH_LOGIN_MCP = "net/minecraft/server/network/ServerLoginPacketListenerImpl";
    public static final String CLASS_PATH_LOGIN_YARN = "net/minecraft/server/network/ServerLoginNetworkHandler";
    public static final String CLASS_PATH_STRING = "com/mojang/brigadier/StringReader";

    public static void premain(final String agentArgs, final Instrumentation inst) {
        Logging.info("开始载入模块 CnUsername");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
        Logging.info("等待Minecraft加载...");
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
                switch (className) {
                    case CLASS_PATH_LOGIN_MCP:
                    case CLASS_PATH_LOGIN_SPIGOT:
                    case CLASS_PATH_LOGIN_YARN:
                        Logging.info("开始修改类 " + className);
                        try {
                            ClassReader classReader = new ClassReader(classFileBuffer);
                            ClassWriter classWriter = new ClassWriter(classReader, 0);
                            ClassVisitor classVisitor = new ClassVisitorLoginListener(className, classWriter, agentArgs);
                            classReader.accept(classVisitor, 0);
                            Logging.info("修改完成并保存");
                            return classWriter.toByteArray();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                        break;
                    case CLASS_PATH_STRING:
                        Logging.info("开始修改类 " + className);
                        try {
                            ClassReader classReader = new ClassReader(classFileBuffer);
                            ClassWriter classWriter = new ClassWriter(classReader, 0);
                            ClassVisitor classVisitor = new ClassVisitorStringReader(className, classWriter);
                            classReader.accept(classVisitor, 0);
                            Logging.info("修改完成并保存");
                            return classWriter.toByteArray();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Logging.warning("修改失败: " + e);
                        }
                        break;
                    case "org/bukkit/plugin/EventExecutor$1":
                        try {
                            Logging.setLogger((Logger) Class.forName("org.bukkit.Bukkit", false, loader).getMethod("getLogger").invoke(null));
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "me.xpyex.plugin.xplib.bukkit.bstats.Metrics":
                        try {
                            classBeingRedefined.getConstructor(JavaPlugin.class, int.class).newInstance((JavaPlugin) Bukkit.getPluginManager().getPlugin("XPLib"), 19275);
                        } catch (ReflectiveOperationException e) {
                            Logging.warning("无法调用XPLib的BStats库: " + e);
                            e.printStackTrace();
                            Logging.info("不用担心，这并不会影响你的使用 :)");
                        }
                        break;
                }
                return null;
            }
        });
    }
}