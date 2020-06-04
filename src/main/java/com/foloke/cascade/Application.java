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
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.OctetString;

import java.net.URL;

public class Application extends javafx.application.Application {
    private Renderer renderer;
    public MapController mapController;
    public UIController uiController;
    public static Image image;
    public static URL pingDialogURL;
    public static URL pingOneDialogURL;
    public static URL traceDialogURL;
    public static URL snmpDialogURL;

    public static OctetString localEngineId;

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
        pingDialogURL = this.getClass().getResource("/static/pingDialog.fxml");
        pingOneDialogURL = this.getClass().getResource("/static/pingOneDialog.fxml");
        traceDialogURL = this.getClass().getResource("/static/traceDialog.fxml");
        snmpDialogURL = this.getClass().getResource("/static/SNMPSettingsDialog.fxml");

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

            Device device = ScanUtils.initLocal(mapController);
            mapController.addEntity(device);

            localEngineId = new OctetString(MPv3.createLocalEngineID());
            USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());
            SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());
            SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
            SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());

            this.renderer.start();
        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
    }


    public void getProps(Entity entity) {
        uiController.getProps(entity);
    }
}
