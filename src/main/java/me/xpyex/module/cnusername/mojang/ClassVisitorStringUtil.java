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
        if (("isValidPlayerName".equals(name) || "isReasonablePlayerName"/* 去你的Paper */.equals(name)) && (access & Opcodes.ACC_STATIC) > 0 && "(Ljava/lang/String;)Z".equals(descriptor)) {
            //  寻找 static boolean isValidPlayerName(String name) 方法并覆写
            Logging.info("正在修改 " + getClassName() + " 类中的 " + name + "(String) 方法");
            visitor.visitCode();

            // if (string.isEmpty()) { return true; }
            // Label0
            Label label0 = new Label();
            visitor.visitLabel(label0);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "isEmpty", "()Z", false);
            Label label1 = new Label();
            visitor.visitJumpInsn(Opcodes.IFEQ, label1);
            visitor.visitInsn(Opcodes.ICONST_1);
            visitor.visitInsn(Opcodes.IRETURN);

            // Label1
            visitor.visitLabel(label1);
            visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            visitor.visitLdcInsn(getPattern());
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/regex/Pattern", "compile", "(Ljava/lang/String;)Ljava/util/regex/Pattern;", false);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Pattern", "matcher", "(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;", false);
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Matcher", "matches", "()Z", false);
            visitor.visitInsn(Opcodes.IRETURN);

            // Label2
            Label label2 = new Label();
            visitor.visitLabel(label2);
            visitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label2, 0);
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
            return null;
        }
        return visitor;
    }
}
