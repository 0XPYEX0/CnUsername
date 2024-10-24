package me.xpyex.module.cnusername.pass;

public interface RetransformPass extends Pass {
  void retransform(Class<?> clazz, String pattern);
}
