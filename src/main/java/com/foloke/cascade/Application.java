package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import com.lumaserv.netflow.NetFlowCollector;
import com.lumaserv.netflow.NetFlowSession;
import com.lumaserv.netflow.flowset.FlowField;
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class Application extends javafx.application.Application {
    private Renderer renderer;
    public MapController mapController;
    public UIController uiController;
    public static Image image;
    public static Image icon;
    public static URL pingDialogURL;
    public static URL pingOneDialogURL;
    public static URL traceDialogURL;
    public static URL snmpDialogURL;
    public static URL paramDialogURL;
    public static URL mainURL;
    private Scene scene;

    public static OctetString localEngineId;

    private Thread netFlowThread;
    private NetFlowCollector collector;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this.uiController);
        loader.setLocation(mainURL);
        try {
            SplitPane rootPane = loader.load();
            scene = new Scene(rootPane, 1024, 640.0D, false, SceneAntialiasing.DISABLED);
            stage.setTitle("CASCADE");
            stage.getIcons().add(icon);
            stage.setScene(scene);

            Device device = ScanUtils.initLocal(mapController);
            mapController.addEntity(device);

            NetFlowSession session = new NetFlowSession(source -> {
                System.out.println("source: " + source.getId());
                source.listen((id, values) -> {
                    // DO WHATEVER YOU WANT
                    System.out.println(sdf.format(Calendar.getInstance().getTime())
                            + " " + values.get(FlowField.L4_DST_PORT).asUShort()
                            + " " + toAddress(values.get(FlowField.IPV4_DST_ADDR).asBytes()));
                });
            });

            collector = new NetFlowCollector(session);

            netFlowThread = new Thread(collector);
            netFlowThread.setDaemon(true);
            netFlowThread.start();

        } catch (Exception e) {
            LogUtils.log(e.toString());
        }

        this.renderer = new Renderer(this);
        stage.show();
        this.renderer.start();
    }

    private String toAddress(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");

        stringBuilder.append(bytes[0] > 0 ? bytes[0] : 256 + bytes[0]).append('.');
        stringBuilder.append(bytes[1] > 0 ? bytes[1] : 256 + bytes[1]).append('.');
        stringBuilder.append(bytes[2] > 0 ? bytes[2] : 256 + bytes[2]).append('.');
        stringBuilder.append(bytes[3] > 0 ? bytes[3] : 256 + bytes[3]);

        return stringBuilder.toString();
    }

    @Override
    public void init() throws Exception {
        super.init();

        image = new Image("/images/spritesheet.png", 16.0D, 16.0D, false, false);
        icon = new Image("/images/icon.png");

        this.mapController = new MapController(this);
        this.uiController = new UIController(this.mapController);

        mainURL = this.getClass().getResource("/static/main.fxml");
        pingDialogURL = this.getClass().getResource("/static/pingDialog.fxml");
        pingOneDialogURL = this.getClass().getResource("/static/pingOneDialog.fxml");
        traceDialogURL = this.getClass().getResource("/static/traceDialog.fxml");
        snmpDialogURL = this.getClass().getResource("/static/SNMPSettingsDialog.fxml");
        paramDialogURL = this.getClass().getResource("/static/paramDialog.fxml");

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

        collector.close();
        collector.join();
        netFlowThread.interrupt();
    }
}
