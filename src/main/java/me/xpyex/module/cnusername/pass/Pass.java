package me.xpyex.module.cnusername.pass;

import org.objectweb.asm.ClassVisitor;

@FunctionalInterface
public interface Pass {
  ClassVisitor create(String className, ClassVisitor classVisitor, String pattern);
}
