package sample;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.File;

public class AutoDetect {

    File[] oldListRoot = File.listRoots();
    public static String usb = null;

    public void waitForNotifying(Label message) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (File.listRoots().length > oldListRoot.length) {
                        System.out.println("new drive detected");
                        oldListRoot = File.listRoots();
                        System.out.println("drive "+oldListRoot[oldListRoot.length-1]+" detected");
                        Platform.runLater(new Runnable() {
                            @Override                            public void run() {
                                message.setText("USB is connected");
                            }
                        });
                        if (checkUSB(oldListRoot[oldListRoot.length-1], message)){
                            usb = oldListRoot[oldListRoot.length-1].getAbsolutePath();
                        }
                    } else if (File.listRoots().length < oldListRoot.length) {
                        System.out.println(oldListRoot[oldListRoot.length-1]+" drive removed");
                        oldListRoot = File.listRoots();
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                message.setText("USB has been removed");
                            }
                        });
                    }

                }
            }
        });
        t.start();
    }

    private boolean checkUSB(File file, Label message) {
        File admin = new File(file.getAbsolutePath() + File.separator +  "admin.json");
        if(admin.exists()){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    message.setText("Admin private key has been found");
                }
            });
            return true;
        }else{
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    message.setText("Admin private key has not been found");
                }
            });
            return false;
        }
    }

    public String getUsb() {
        return usb;
    }
}