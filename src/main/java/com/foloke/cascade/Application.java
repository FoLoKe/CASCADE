package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.NetFlowController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.utils.HibernateUtil;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.OctetString;

import java.net.URL;

public class Application extends javafx.application.Application {
    public MapController mapController;
    public UIController uiController;
    public static Image spriteSheet;
    public static Image icon;
    public static URL pingDialogURL;
    public static URL pingOneDialogURL;
    public static URL traceDialogURL;
    public static URL snmpDialogURL;
    public static URL paramDialogURL;
    public static URL mainURL;
    public static URL netflowURL;

    public static OctetString localEngineId;
    public static NetFlowController netFlowController;
    public static Session databaseSession;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this.uiController);
        loader.setLocation(mainURL);
        try {
            SplitPane rootPane = loader.load();
            Scene scene = new Scene(rootPane, 1024, 640.0D, false, SceneAntialiasing.DISABLED);
            stage.setTitle("CASCADE");
            stage.getIcons().add(icon);
            stage.setScene(scene);
        } catch (Exception e) {
            LogUtils.log(e.toString());
        }

        //databaseSession = HibernateUtil.getSessionFactory().openSession();

        Device device = ScanUtils.initLocal(mapController);
        mapController.addEntity(device);

        Renderer renderer = new Renderer(this);
        stage.show();
        renderer.start();

        netFlowController = new NetFlowController(mapController);
    }

    @Override
    public void init() throws Exception {
        super.init();

        spriteSheet = new Image("/images/spritesheet.png", 0, 0, false, false);
        icon = new Image("/images/icon.png");

        this.mapController = new MapController();
        this.uiController = new UIController(this.mapController);

        mainURL = this.getClass().getResource("/static/main.fxml");
        pingDialogURL = this.getClass().getResource("/static/pingDialog.fxml");
        pingOneDialogURL = this.getClass().getResource("/static/pingOneDialog.fxml");
        traceDialogURL = this.getClass().getResource("/static/traceDialog.fxml");
        snmpDialogURL = this.getClass().getResource("/static/SNMPSettingsDialog.fxml");
        paramDialogURL = this.getClass().getResource("/static/paramDialog.fxml");
        netflowURL = this.getClass().getResource("/static/flowDialog.fxml");

        localEngineId = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthMD5());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivDES());
        SecurityProtocols.getInstance().addAuthenticationProtocol(new AuthSHA());
        SecurityProtocols.getInstance().addPrivacyProtocol(new PrivAES128());

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        netFlowController.close();
        HibernateUtil.shutdown();
    }
}
