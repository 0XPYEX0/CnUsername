package me.xpyex.module.cnusername.mojang;

import me.xpyex.module.cnusername.Logging;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorStringReader extends ClassVisitor {
    public static final String CLASS_PATH = "com/mojang/brigadier/StringReader";  //命令选择器
    private static final String METHOD_NAME = "isAllowedInUnquotedString";
    private final String className;

    public ClassVisitorStringReader(String className, ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (METHOD_NAME.equals(name) && "(C)Z".equals(descriptor) && (access & Opcodes.ACC_STATIC) > 0) {  //静态 isAllowedInUnquotedString(char)
            Logging.info("正在修改 " + className + " 类中的 " + METHOD_NAME + "() 方法");
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 48);
            Label label1 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, label1);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 57);
            Label label2 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, label2);
            mv.visitLabel(label1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 65);
            Label label3 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, label3);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 90);
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, label2);
            mv.visitLabel(label3);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 97);
            Label label4 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, label4);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 122);
            mv.visitJumpInsn(Opcodes.IF_ICMPLE, label2);
            mv.visitLabel(label4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 95);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, label2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 45);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, label2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 46);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, label2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.BIPUSH, 43);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, label2);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitIntInsn(Opcodes.SIPUSH, 19968);
            Label label5 = new Label();
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, label5);
            mv.visitVarInsn(Opcodes.ILOAD, 0);
            mv.visitLdcInsn(40869);
            mv.visitJumpInsn(Opcodes.IF_ICMPGT, label5);
            mv.visitLabel(label2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(Opcodes.ICONST_1);
            Label label6 = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, label6);
            mv.visitLabel(label5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitLabel(label6);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{Opcodes.INTEGER});
            mv.visitInsn(Opcodes.IRETURN);
            Label label7 = new Label();
            mv.visitLabel(label7);
            mv.visitLocalVariable("c", "C", null, label0, label7, 0);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
            return null;
        }
        return mv;
    }
}
