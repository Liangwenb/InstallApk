package sample;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static sample.Config.*;


public class DownloadUtils {
    public OkHttpClient mOkHttpClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.MINUTES).build();


    public void download(String url, OnProgressListener listener) {
        try {
            Config.init();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(e);
            return;
        }
        Request request = new Request.Builder().url(url).get().build();
        ;
        Call call = mOkHttpClient.newCall(request);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            Response execute = call.execute();
            if (execute.isSuccessful() && execute.body() != null) {
                inputStream = execute.body().byteStream();
                long contentLength = execute.body().contentLength();
                System.out.println(contentLength);
                File file = new File(apkPath.toFile(), url.substring(url.lastIndexOf("/")));
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
                fileOutputStream = new FileOutputStream(file);
                int len = 0;
                int sum = 0;
                byte[] buf = new byte[2048];
                while ((len = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / contentLength * 100);
                    //下载中更新进度条
                    listener.onProgress(progress);
                }
                fileOutputStream.flush();
                listener.onSuccess(file);
            } else {
                listener.onError(new Exception(execute.message()));

            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(e);

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception ignored) {
            }

        }

    }

    public interface OnProgressListener {
        void onProgress(int progress);

        void onError(Exception e);

        void onSuccess(File file);
    }


}