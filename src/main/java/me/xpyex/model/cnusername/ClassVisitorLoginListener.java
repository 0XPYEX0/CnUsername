package me.xpyex.model.cnusername;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class ClassVisitorLoginListener extends ClassVisitor {
    private final String className;

    public ClassVisitorLoginListener(String className, ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("(Ljava/lang/String;)Z".equals(descriptor) && (access & Opcodes.ACC_STATIC) > 0) {  //类内静态isValidUsername(String)方法
            Logging.info("正在修改 " + className + " 类中的 " + name + "(String) 方法");
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitLineNumber(44, label0);
            mv.visitLdcInsn("^[a-zA-Z0-9_]{3,16}|[a-zA-Z0-9_\u4e00-\u9fa5]{2,10}$");
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
