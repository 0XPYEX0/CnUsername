package me.xpyex.module.cnusername.mojang;

import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.impl.PatternVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorStringUtil extends PatternVisitor {
    public static final String CLASS_PATH = "net/minecraft/util/StringUtil";

    public ClassVisitorStringUtil(String className, ClassVisitor classVisitor, String pattern) {
        super(className, classVisitor, pattern);
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("isValidPlayerName".equals(name) && (access & Opcodes.ACC_STATIC) > 0 && "(Ljava/lang/String;)Z".equals(descriptor)) {
            //  寻找 static boolean isValidPlayerName(String name) 方法并覆写
            Logging.info("正在修改 " + getClassName() + " 类中的 " + name + "(String) 方法");
            visitor.visitCode();
            Label label0 = new Label();
            visitor.visitLabel(label0);
            visitor.visitLdcInsn(getPattern());
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/regex/Pattern", "compile", "(Ljava/lang/String;)Ljava/util/regex/Pattern;", false);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Pattern", "matcher", "(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;", false);
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Matcher", "matches", "()Z", false);
            visitor.visitInsn(Opcodes.IRETURN);
            Label label1 = new Label();
            visitor.visitLabel(label1);
            visitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label1, 0);
            visitor.visitMaxs(2, 1);
            visitor.visitEnd();
            return null;
        }
        return visitor;
    }
}
