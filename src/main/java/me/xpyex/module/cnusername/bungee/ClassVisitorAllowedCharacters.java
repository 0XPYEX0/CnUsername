package me.xpyex.module.cnusername.bungee;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import me.xpyex.module.cnusername.CnUsername;
import me.xpyex.module.cnusername.Logging;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassVisitorAllowedCharacters extends ClassVisitor {
    public static final String CLASS_PATH = "net/md_5/bungee/util/AllowedCharacters";
    private final String className;
    private final String pattern;

    public ClassVisitorAllowedCharacters(String className, ClassVisitor classVisitor, String pattern) {
        super(Opcodes.ASM9, classVisitor);
        this.className = className;
        String s;
        if (pattern == null || pattern.isEmpty()) {
            s = CnUsername.DEFAULT_PATTERN;
            Logging.info("当前玩家名规则将使用本组件的默认正则规则");
        } else {
            try {
                Pattern.compile(pattern);
                s = pattern;
            } catch (PatternSyntaxException e) {
                s = CnUsername.DEFAULT_PATTERN;
                e.printStackTrace();
                Logging.warning("你自定义的正则格式无效: " + pattern);
                Logging.info("当前玩家名规则将使用本组件的默认正则规则");
                Logging.info("不用担心，该错误不会影响本组件正常工作，但你需要改改你写的正则规则了 :)");
            }
        }
        Logging.info("当前组件使用的正则规则为: " + s);
        this.pattern = s;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ("isValidName".equals(name) && (access & Opcodes.ACC_STATIC) > 0) {  //静态isValidName方法，无视参数
            Logging.info("正在修改 " + className + " 类中的 " + name + "(String, boolean) 方法");
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            mv.visitLdcInsn(pattern);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/regex/Pattern", "compile", "(Ljava/lang/String;)Ljava/util/regex/Pattern;", false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Pattern", "matcher", "(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/regex/Matcher", "matches", "()Z", false);
            mv.visitInsn(Opcodes.IRETURN);
            Label label1 = new Label();
            mv.visitLabel(label1);
            mv.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label1, 0);
            mv.visitLocalVariable("onlineMode", "Z", null, label0, label1, 1);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            return null;
        }
        return mv;
    }
}
