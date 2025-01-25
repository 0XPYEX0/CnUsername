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
            visitor.visitJumpInsn(Opcodes.IFNE, label1);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitLdcInsn(getPattern());
            visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "matches", "(Ljava/lang/String;)Z", false);
            Label label2 = new Label();
            visitor.visitJumpInsn(Opcodes.IFEQ, label2);
            visitor.visitLabel(label1);
            visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            visitor.visitInsn(Opcodes.ICONST_1);
            Label label3 = new Label();
            visitor.visitJumpInsn(Opcodes.GOTO, label3);
            visitor.visitLabel(label2);
            visitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            visitor.visitInsn(Opcodes.ICONST_0);
            visitor.visitLabel(label3);
            visitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.INTEGER});
            visitor.visitInsn(Opcodes.IRETURN);
            Label label4 = new Label();
            visitor.visitLabel(label4);
            visitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label4, 0);
            visitor.visitMaxs(2, 1);
            visitor.visitEnd();
            return null;
        }
        return visitor;
    }
}
