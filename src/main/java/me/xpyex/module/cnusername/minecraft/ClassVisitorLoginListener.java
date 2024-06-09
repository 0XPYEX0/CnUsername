package me.xpyex.module.cnusername.minecraft;

import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.impl.PatternVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorLoginListener extends PatternVisitor {
    public static final String CLASS_PATH_SPIGOT = "net/minecraft/server/network/LoginListener";
    public static final String CLASS_PATH_MOJANG = "net/minecraft/server/network/ServerLoginPacketListenerImpl";
    public static final String CLASS_PATH_YARN = "net/minecraft/server/network/ServerLoginNetworkHandler";

    public ClassVisitorLoginListener(String className, ClassVisitor classVisitor, String pattern) {
        super(className, classVisitor, pattern);
        //
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("(Ljava/lang/String;)Z".equals(descriptor) && (access & Opcodes.ACC_STATIC) > 0) {  //类内静态isValidUsername(String)方法
            Logging.info("正在修改 " + getClassName() + " 类中的 " + name + "(String) 方法");
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitLdcInsn(getPattern());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/regex/Pattern", "compile", "(Ljava/lang/String;)Ljava/util/regex/Pattern;", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Pattern", "matcher", "(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Matcher", "matches", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label label1 = new Label();
            mv.visitLabel(label1);
            mv.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label1, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
            return null;
        }
        return mv;
    }
}
