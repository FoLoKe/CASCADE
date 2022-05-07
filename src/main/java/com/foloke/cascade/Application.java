package com.foloke.cascade;

import com.foloke.cascade.Controllers.MapController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.ScanUtils;
import com.foloke.cascade.utils.SimpleFlow;
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
import java.nio.ByteBuffer;
import java.util.*;

public class Application extends javafx.application.Application {
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

    public static OctetString localEngineId;

    private Thread netFlowThread;
    private NetFlowCollector collector;
    // MAP: SOURCE : FLOWS
    // FLOWS: PORT : ADDRESS

    private final Map<Integer, Set<SimpleFlow>> flowSources = new HashMap<>();

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

            Device device = ScanUtils.initLocal(mapController);
            mapController.addEntity(device);

            NetFlowSession session = new NetFlowSession(source -> {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    byteBuffer.putInt(source.getDeviceIP());

                    System.out.println("Device: " + SimpleFlow.ipToString(byteBuffer.array()) + " source: " + source.getId());
                    if(!flowSources.containsKey(source.getId())) {
                        flowSources.put(source.getDeviceIP(), new HashSet<>());
                    }

                    Set<SimpleFlow> flows = flowSources.get(source.getDeviceIP());
                    source.listen((id, values) -> {
                        System.out.println(flows.size());
                        synchronized (flows) {
                            float flowTime = (values.get(FlowField.LAST_SWITCHED).asInt() - values.get(FlowField.FIRST_SWITCHED).asInt()) / 1000f;

                            SimpleFlow flow = new SimpleFlow(values, 0);
                            if (flowTime == 0) {
                                if(flows.remove(flow)) {
                                    System.out.println("update");
                                }

                                flows.add(flow);
                            } else {
                                if(flows.remove(flow)) {
                                    System.out.println("ended");
                                }
                            }

                            //Predicate<SimpleFlow> isExpired = simpleFlow -> System.currentTimeMillis() - simpleFlow.timestamp > simpleFlow.timeout;
                            //flows.removeIf(isExpired);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            collector = new NetFlowCollector(session, 9996);

            netFlowThread = new Thread(collector);
            netFlowThread.setDaemon(true);
            netFlowThread.start();


        } catch (Exception e) {
            LogUtils.log(e.toString());
        }

        Renderer renderer = new Renderer(this);
        stage.show();
        renderer.start();
    }

    @Override
    public void init() throws Exception {
        super.init();

        image = new Image("/images/spritesheet.png", 16.0D, 16.0D, false, false);
        icon = new Image("/images/icon.png");

        this.mapController = new MapController();
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
