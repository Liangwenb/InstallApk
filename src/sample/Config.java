package sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface Config {
    String USER_HOME = System.getProperty("user.home");
    Path installApkPath = Paths.get(USER_HOME, ".InstallApk");
    Path apkPath = Paths.get(installApkPath.toString(), "apk");

    static void init() throws IOException {
        if (!Files.exists(installApkPath)) {
            Files.createDirectory(installApkPath);
        }
        if (!Files.exists(apkPath)) {
            Files.createDirectory(apkPath);
        }
    }


}
