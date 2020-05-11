package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
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
        this.mapController = new MapController();

        image = new Image("/images/spritesheet.png", 16.0D, 16.0D, false, false);
        FXMLLoader loader = new FXMLLoader();
        this.uiController = new UIController(this.mapController);
        loader.setController(this.uiController);
        URL url = this.getClass().getResource("/static/main.fxml");
        loader.setLocation(url);
        SplitPane rootPane = null;

        try {
            rootPane = loader.load();
        } catch (Exception exception) {
            System.out.println(exception);
        }

        Scene scene = new Scene(rootPane, 1024, 640.0D, false, SceneAntialiasing.DISABLED);
        stage.setTitle("SNMP-Map");
        stage.setScene(scene);
        this.renderer = new Renderer(this);
        stage.show();

        initLocal();
        this.renderer.start();
    }

    public void initLocal() {
        Device entity = new Device(image);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                    System.out.println(networkInterface);
                    entity.addPort(networkInterface);

                } else {
                    System.out.println(networkInterface + "is loopback");
                }
            }
        } catch (SocketException socketException) {
            System.out.println(socketException);
        }

        mapController.addEntity(entity);
    }
}