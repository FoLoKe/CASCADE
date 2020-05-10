package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.UIController;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {
    private Renderer renderer;
    public MapController mapController;
    public UIController uiController;

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
        FXMLLoader loader = new FXMLLoader();
        this.uiController = new UIController(this.mapController);
        loader.setController(this.uiController);
        URL url = this.getClass().getResource("/static/main.fxml");
        loader.setLocation(url);
        SplitPane rootPane = null;

        try {
            rootPane = (SplitPane) loader.load();
        } catch (Exception var6) {
            System.out.println(var6);
        }

        Scene scene = new Scene(rootPane, 512.0D, 640.0D, false, SceneAntialiasing.DISABLED);
        stage.setTitle("SNMP-Map");
        stage.setScene(scene);
        this.renderer = new Renderer(this);
        stage.show();
        this.renderer.start();
    }
}
