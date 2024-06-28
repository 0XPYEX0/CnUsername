package me.xpyex.module.cnusername;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    public static String version = "";

    static {
        try (InputStream is = UpdateChecker.class.getClassLoader().getResourceAsStream("version")) {  //由Gradle填充的版本文件
            if (is != null) {
                version = new String(is.readAllBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void check() {
        try {
            String api = "https://api.github.com/repos/0XPYEX0/CnUsername/releases/latest";
            URLConnection connection = new URL(api).openConnection();
            connection.setConnectTimeout(5000);  //5s超时

            JsonObject result = new GsonBuilder().disableHtmlEscaping().create().fromJson(new String(readInputStream(connection.getInputStream())), JsonObject.class);

            if (!("v" + version).equalsIgnoreCase(result.get("tag_name").getAsString())) {
                Logging.info("发现新版本: " + result.get("tag_name").getAsString());
                Logging.info("更新内容: " + result.get("body").getAsString());
                Logging.info("下载地址(Github): https://github.com/0XPYEX0/CnUsername/releases");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Logging.warning("获取更新失败，但不影响当前使用");
        }
    }

    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream ba = new ByteArrayOutputStream(16384)) {
            int nRead;
            byte[] data = new byte[4096];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                ba.write(data, 0, nRead);
            }
            return ba.toByteArray();
        }
    }
}
