package sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import static sample.Config.*;

public class ApkUtils {

    List<String> getApkList(){
        try {
            init();
            List<String> strings = new ArrayList<>(Arrays.asList(apkPath.toFile().list()));
            Iterator<String> iterator = strings.iterator();
            if (iterator.hasNext()) {
                if (!iterator.next().contains("apk")) {
                    iterator.remove();
                }
            }
            return strings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


}
