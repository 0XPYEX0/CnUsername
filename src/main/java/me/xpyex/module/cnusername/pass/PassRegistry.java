package me.xpyex.module.cnusername.pass;

import me.xpyex.module.cnusername.bungee.ClassVisitorAllowedCharacters;
import me.xpyex.module.cnusername.minecraft.ClassVisitorLoginListener;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringReader;
import me.xpyex.module.cnusername.mojang.ClassVisitorStringUtil;
import me.xpyex.module.cnusername.paper.ClassVisitorCraftPlayerProfile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PassRegistry {
  private static final Map<String, PassEntry> passMap = new LinkedHashMap<>();

  private static void register(String className, Pass pass) {
    passMap.put(className, new PassEntry(pass));
  }

  public static Set<String> allPossibleClasses() {
    return passMap.keySet();
  }

  public static Pass getPass(String className) {
    PassEntry entry = passMap.get(className);
    if (entry == null) {
      return null;
    }
    return entry.pass;
  }

  public static boolean isModified(String className) {
    PassEntry entry = passMap.get(className);
    return entry != null && entry.modified;
  }

  public static void setModified(String className) {
    PassEntry entry = passMap.get(className);
    if (entry != null) {
      entry.modified = true;
    }
  }

  static {
    // bungee
    register(ClassVisitorAllowedCharacters.CLASS_PATH, ClassVisitorAllowedCharacters::new);

    // minecraft
    register(ClassVisitorLoginListener.CLASS_PATH_SPIGOT, ClassVisitorLoginListener::new);
    register(ClassVisitorLoginListener.CLASS_PATH_MOJANG, ClassVisitorLoginListener::new);
    register(ClassVisitorLoginListener.CLASS_PATH_YARN, ClassVisitorLoginListener::new);

    // mojang
    register(ClassVisitorStringReader.CLASS_PATH, (className, classVisitor, pattern) -> new ClassVisitorStringReader(className, classVisitor));
    register(ClassVisitorStringUtil.CLASS_PATH, ClassVisitorStringUtil::new);

    // paper
    register(ClassVisitorCraftPlayerProfile.CLASS_PATH, ClassVisitorCraftPlayerProfile::new);
  }

  private static final class PassEntry {
    private final Pass pass;
    private boolean modified = false;

    private PassEntry(Pass pass) {
      this.pass = pass;
    }
  }
}
