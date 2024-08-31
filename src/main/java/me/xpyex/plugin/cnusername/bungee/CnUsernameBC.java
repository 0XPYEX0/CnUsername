package me.xpyex.plugin.cnusername.bungee;

import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.UpdateChecker;
import me.xpyex.module.cnusername.bungee.ClassVisitorAllowedCharacters;
import me.xpyex.plugin.cnusername.CnUsernamePlugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class CnUsernameBC extends Plugin implements CnUsernamePlugin {

    /**
     * 运行中动态加载字节码
     *
     * @param className 类名
     * @param bytes     字节码
     */
    public static void loadClass(String className, byte[] bytes) {
        try {
            CnUsernamePlugin.getDefineClassMethod().invoke(ProxyServer.class.getClassLoader(), className, bytes, 0, bytes.length);
        } catch (Throwable e) {
            throw new IllegalStateException("修改类 " + className + " 失败!", e);
        }
    }

    @Override
    public void onEnable() {
        Logging.setLogger(getProxy().getLogger());
        Logging.info("已加载");
        Logging.info("如遇Bug，或需提出建议: QQ1723275529");
        getProxy().getScheduler().runAsync(this, UpdateChecker::check);
        try {
            ClassReader classReader = new ClassReader(ProxyServer.class.getClassLoader().getResourceAsStream(ClassVisitorAllowedCharacters.CLASS_PATH + ".class"));
            String className = classReader.getClassName().replace("/", ".");
            Logging.info("开始修改类 " + className);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
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
            if (CnUsername.DEBUG) e.printStackTrace();
            Logging.warning("修改失败: " + e);
        }
        getProxy().getPluginManager().registerListener(this, new Listener() {
            @EventHandler
            public void onPreLogin(PreLoginEvent event) {
                if ("CS-CoreLib".equals(event.getConnection().getName())) {
                    event.setCancelReason("Invalid username\nCnUsername Defend");
                    event.setCancelled(true);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        Logging.info("已卸载");
        //
    }
}
