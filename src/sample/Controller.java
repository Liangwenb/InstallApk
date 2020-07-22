package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class Controller implements Initializable, DownloadUtils.OnProgressListener {
    DownloadUtils mDownload = new DownloadUtils();
    ApkUtils mApkUtils = new ApkUtils();
    @FXML
    public TextField address;
    @FXML
    public Button start;
    @FXML
    public ListView<Device> devices;
    @FXML
    public ListView<String> log;
    public ListView<String> apkListView;

    Thread thread;
    public ObservableList<String> logList = FXCollections.observableArrayList();

    public void onStart(ActionEvent event) {
        if (thread != null) {
            try {
                thread.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ObservableList<String> selectedItems = apkListView.getSelectionModel().getSelectedItems();

        if (selectedItems.size() > 0) {
            String path = selectedItems.get(0);
            if (path.equals("从网络安装")) {
                downloadApk();
            } else {
                installApk(path);
            }
        }


    }

    private void installApk(String name) {
        installApk(new File(Config.apkPath + "/" + name));
    }

    private void installApk(File path) {
        thread = new Thread(() -> {
            for (Device device : devices.getItems()) {
                try {
                    execCmd("platform-tools/adb.exe  -s " + device.id + "  install -d  -t -r " + path.getPath());
                    execCmd("platform-tools/adb.exe -s " + device.id + " shell am start  com.hkfuliao.chamet/com.oversea.chat.splash.SplashActivity");
                } catch (IOException e) {
                    e.printStackTrace();
                    addLog(e.getMessage());
                }
            }
        });
        thread.start();
    }

    private void downloadApk() {
        if (address.getText().isEmpty()) {
            addLog("下载地址为空");
            return;
        }

        thread = new Thread(() -> {
            try {
                mDownload.download(address.getText(), this);
            } catch (Exception e) {
                onError(e);
            }
        });
        thread.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.setItems(logList);
        initApkList();
        initDevicesList();

    }

    private void initDevicesList() {
        devices.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        devices.setCellFactory(param -> new ListCell<Device>() {
            @Override
            protected void updateItem(Device item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.device + " " + item.model);
                }
            }
        });
        getDevices();
    }

    private void initApkList() {

        apkListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        apkListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ObservableList<String> selectedItems = apkListView.getSelectionModel().getSelectedItems();
                if (selectedItems.size() > 0) {
                    if (selectedItems.get(0).equals("从网络安装")) {
                        start.setText("下载并安装");
                    } else {
                        start.setText("安装");

                    }
                }

            }
        });
        uploadApkDis();
    }

    private void getDevices() {
        new Thread(() -> {
                try {
                    List<String> list = execCmd("platform-tools/adb.exe devices -l");
                    for (String item : list) {
                        String list_of_devices_attached = item.replace("List of devices attached", "");
                        System.out.println("----> " + list_of_devices_attached);
                        String[] items = list_of_devices_attached.replaceAll(" +", " ").trim().split("\n");
                        getDevice(items);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }).start();
    }

    private void getDevice(String[] items) {
        List<Device> list = new ArrayList<>();
        for (String item : items) {
            String[] deviceItems = item.replaceAll(" +", " ").trim().split(" ");
            Device device = new Device();
            device.id = deviceItems[0];
            for (String str : deviceItems) {
                if (str.contains("product:")) {
                    device.product = str.replace("product:", "");
                }
                if (str.contains("model:")) {
                    device.model = str.replace("model:", "");
                }
                if (str.contains("device:")) {
                    device.device = str.replace("device:", "");
                }
            }
            System.err.println(device);
            if (device.model != null) {
                list.add(device);

            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                devices.setItems(FXCollections.observableArrayList(list));
            }
        });


    }

    public List<String> execCmd(String cmd) throws IOException {
        System.out.println(cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        List<String> strings = new ArrayList<>();
        if (s.hasNext()) {
            String next = s.next();
            strings.add(next);
            addLog(next);

        }
        BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));
        String line = null;
        while ((line = br.readLine()) != null) {
            addLog(line);
        }
        is.close();
        br.close();
        s.close();
        return strings;
    }

    public void addLog(String logString) {
        Platform.runLater(() -> {
            logList.add(logString);
            log.scrollTo(logList.size());
        });
    }

    @Override
    public void onProgress(int progress) {

        if (logList.size() > 0) {
            if (!logList.get(logList.size() - 1).equals("下载进度 %" + progress)) {
                addLog("下载进度 %" + progress);
            }
        } else {
            addLog("下载进度 %" + progress);
        }
    }

    @Override
    public void onError(Exception e) {
        addLog(e.getMessage());

    }

    @Override
    public void onSuccess(File file) {
        addLog("下载完成 : " + file.getName());
        uploadApkDis();
        installApk(file);

    }

    private void uploadApkDis() {
        Platform.runLater(() -> {
            ObservableList<String> apkList = FXCollections.observableArrayList();
            apkList.add("从网络安装");
            apkList.addAll(mApkUtils.getApkList());
            apkListView.setItems(apkList);
            apkListView.getSelectionModel().selectFirst();

        });
    }

    public void refreshDevices(ActionEvent actionEvent) {
        getDevices();
    }

    public void refreshApks(ActionEvent actionEvent) {
        uploadApkDis();
    }

}
