package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class Application extends javafx.application.Application {
    private Renderer renderer;
    public MapController mapController;
    public UIController uiController;
    public static Image image;

    public Application() {
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        this.initUI(stage);
    }

    public void initUI(Stage stage) {
        this.mapController = new MapController(this);

        image = new Image("/images/spritesheet.png", 16.0D, 16.0D, false, false);
        FXMLLoader loader = new FXMLLoader();
        this.uiController = new UIController(this.mapController);

        URL url = this.getClass().getResource("/static/main.fxml");

        try {
            loader.setController(this.uiController);
            loader.setLocation(url);
            SplitPane rootPane = loader.load();

            Scene scene = new Scene(rootPane, 1024, 640.0D, false, SceneAntialiasing.DISABLED);
            stage.setTitle("CASCADE");
            stage.getIcons().add(image);
            stage.setScene(scene);
            this.renderer = new Renderer(this);
            stage.show();

            initLocal();
            this.renderer.start();
        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
    }

    public void initLocal() {
        Device entity = new Device(image, mapController);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                    LogUtils.log(networkInterface.toString());
                    Device.Port port = entity.addPort(networkInterface);
                } else {
                    LogUtils.log(networkInterface + " is loopback or virtual");
                }
            }
        } catch (SocketException e) {
            LogUtils.log(e.toString());
        }

        mapController.addEntity(entity);

        ScanUtils.scanByPing(mapController, "192.168.88.0", "24");
        ScanUtils.traceRoute(mapController, "31.42.45.42");
    }

    public void getProps(Entity entity) {
        uiController.getProps(entity);
    }
}
