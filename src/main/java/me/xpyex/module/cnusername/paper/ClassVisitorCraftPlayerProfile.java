package me.xpyex.module.cnusername.paper;

import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.impl.PatternVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Paper在ClassPlayerProfile.createAuthLibProfile(UUID, String)中
 * 在使用StringUtil.isValidPlayerName(String)之前还检查了一次玩家名长度
 * CnUsername能够覆写isValidPlayerName(String)，如覆写String.length()方法会造成大面积杀伤
 * 故选择覆写createAuthLibProfile(UUID, String)方法
 */
public class ClassVisitorCraftPlayerProfile extends PatternVisitor {
    public static final String CLASS_PATH = "com/destroystokyo/paper/profile/CraftPlayerProfile";

    public ClassVisitorCraftPlayerProfile(String className, ClassVisitor classVisitor, String pattern) {
        super(className, classVisitor, pattern);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("createAuthLibProfile".equals(name) && (access & Opcodes.ACC_STATIC) > 0 && "(Ljava/util/UUID;Ljava/lang/String;)Lcom/destroystokyo/paper/profile/GameProfile;".equals(descriptor)) {
            Logging.info("正在修改 " + getClassName() + " 类中的 " + name + "(UUID, String) 方法");
            visitor.visitCode();
            Label label0 = new Label();
            visitor.visitLabel(label0);
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            Label label1 = new Label();
            visitor.visitJumpInsn(Opcodes.IFNULL, label1);
            visitor.visitLdcInsn(getPattern());
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/regex/Pattern", "matches", "(Ljava/lang/String;Ljava/lang/CharSequence;)Z", false);
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
            visitor.visitLdcInsn("The name of the profile contains invalid characters: %s");
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, "com/google/common/base/Preconditions", "checkArgument", "(ZLjava/lang/String;Ljava/lang/Object;)V", false);
            Label label4 = new Label();
            visitor.visitLabel(label4);
            visitor.visitTypeInsn(Opcodes.NEW, "com/mojang/authlib/GameProfile");
            visitor.visitInsn(Opcodes.DUP);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            Label label5 = new Label();
            visitor.visitJumpInsn(Opcodes.IFNULL, label5);
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            Label label6 = new Label();
            visitor.visitJumpInsn(Opcodes.GOTO, label6);
            visitor.visitLabel(label5);
            visitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{"java/util/UUID", "java/lang/String"}, 2, new Object[]{label4, label4});
            visitor.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraft/Util", "NIL_UUID", "Ljava/util/UUID;");
            visitor.visitLabel(label6);
            visitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{"java/util/UUID", "java/lang/String"}, 3, new Object[]{label4, label4, "java/util/UUID"});
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            Label label7 = new Label();
            visitor.visitJumpInsn(Opcodes.IFNULL, label7);
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            Label label8 = new Label();
            visitor.visitJumpInsn(Opcodes.GOTO, label8);
            visitor.visitLabel(label7);
            visitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{"java/util/UUID", "java/lang/String"}, 3, new Object[]{label4, label4, "java/util/UUID"});
            visitor.visitLdcInsn("");
            visitor.visitLabel(label8);
            visitor.visitFrame(Opcodes.F_FULL, 2, new Object[]{"java/util/UUID", "java/lang/String"}, 4, new Object[]{label4, label4, "java/util/UUID", "java/lang/String"});
            visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/mojang/authlib/GameProfile", "<init>", "(Ljava/util/UUID;Ljava/lang/String;)V", false);
            visitor.visitInsn(Opcodes.ARETURN);
            Label label9 = new Label();
            visitor.visitLabel(label9);
            visitor.visitLocalVariable("uUID", "Ljava/util/UUID;", null, label0, label9, 0);
            visitor.visitLocalVariable("string", "Ljava/lang/String;", null, label0, label9, 1);
            visitor.visitMaxs(4, 2);
            visitor.visitEnd();
            return null;
        }
        return visitor;
    }
}
