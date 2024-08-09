package me.xpyex.module.cnusername.paper;

import me.xpyex.module.cnusername.Logging;
import me.xpyex.module.cnusername.impl.PatternVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Paper在ClassPlayerProfile.createAuthLibProfile(UUID, String)中
 * 在使用StringUtil.isValidPlayerName(String)之前还检查了一次玩家名长度
 * CnUsername能够覆写isValidPlayerName(String)，但覆写String.length()方法会造成大面积杀伤
 * 故选择覆写createAuthLibProfile(UUID, String)方法，仅删除检查玩家名长度的部分，其余不变.
 */
public class ClassVisitorCraftPlayerProfile extends PatternVisitor {
    public static final String CLASS_PATH = "com/destroystokyo/paper/profile/CraftPlayerProfile";

    public ClassVisitorCraftPlayerProfile(String className, ClassVisitor classVisitor, String pattern) {
        super(className, classVisitor, pattern);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("createAuthLibProfile".equals(name) && (access & Opcodes.ACC_STATIC) > 0 && "(Ljava/util/UUID;Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;".equals(descriptor)) {
            Logging.info("正在修改 " + getClassName() + " 类中的 " + name + "(UUID, String) 方法");
            return new MethodVisitor(Opcodes.ASM9, visitor) {  //删除该方法内第一行代码 string.length() 的检查
                private boolean gotCheckArgument = false;

                @Override
                public void visitCode() {
                    super.visitCode();
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    if (Opcodes.INVOKESTATIC == opcode && "checkArgument".equals(name)) {  //走到checkArgument时
                        if (!gotCheckArgument) {  //如果之前还没走到过checkArgument的话
                            gotCheckArgument = true;  //这就是第一个了
                            mv = null;  //删除这行
                        }
                    }
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    mv = visitor;  //恢复
                }
            };
        }
        return visitor;
    }
}
