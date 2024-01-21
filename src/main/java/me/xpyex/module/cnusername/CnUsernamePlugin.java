package me.xpyex.module.cnusername;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public interface CnUsernamePlugin {
    default String readPluginPattern() {
        try {
            File f = new File(getDataFolder(), "pattern.txt");
            File folder = f.getParentFile();
            if (folder.exists() && folder.isFile()) {
                Logging.warning("错误: 插件目录下已存在CnUsername文件，且非文件夹");
                return null;
            }
            if (!f.exists()) {
                folder.mkdirs();
                f.createNewFile();
            }
            List<String> content = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            return content.isEmpty() ? null : content.get(0);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    File getDataFolder();
}
