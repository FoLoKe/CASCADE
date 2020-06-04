package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Cable;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.utils.FileUtils;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.SnmpUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.snmp4j.smi.OID;

import java.net.URL;
import java.util.ResourceBundle;

public class UIController implements Initializable {
    @FXML
    private Canvas canvas;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TextArea logTextArea;

    @FXML
    private AnchorPane outerLogAnchor;

    @FXML
    private VBox propsVBox;

    @FXML
    private TableView<Property> propTable;

    @FXML
    private AnchorPane propAnchor;

    @FXML
    private AnchorPane outerPropAnchor;

    @FXML
    private TableColumn<Property, String> propertyColumn;

    @FXML
    private TableColumn<Property, String> valueColumn;

    private final MapController mapController;
    private ObjectContextMenu objectContextMenu;
    private NoneObjectContextMenu noneObjectContextMenu;

    public UIController(MapController mapController) {
        this.mapController = mapController;
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.canvas.getGraphicsContext2D();

        this.anchorPane.widthProperty().addListener((ov, oldValue, newValue) -> {
            this.canvas.setWidth(newValue.doubleValue());
        });

        this.anchorPane.heightProperty().addListener((ov, oldValue, newValue) -> {
            this.canvas.setHeight(newValue.doubleValue());
        });

        this.canvas.setOnMousePressed(mouseEvent -> {
            objectContextMenu.hide();
            noneObjectContextMenu.hide();
            UIController.this.mapController.pick((float) mouseEvent.getX(), (float) mouseEvent.getY());
        });

        this.canvas.setOnMouseDragged(mouseEvent -> UIController.this.mapController.drag((float) mouseEvent.getX(), (float) mouseEvent.getY()));

        this.canvas.setOnMouseReleased(mouseEvent -> UIController.this.mapController.drop(mouseEvent.getX(), mouseEvent.getY()));

        this.canvas.setOnScroll(scrollEvent -> this.mapController.zoom( scrollEvent.getDeltaY() > 0));

        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        LogUtils.init(logTextArea);

        objectContextMenu = new ObjectContextMenu();
        noneObjectContextMenu = new NoneObjectContextMenu(mapController);

        canvas.setOnContextMenuRequested(event -> {
            Entity entity = mapController.hit(event.getX(), event.getY());
            if (entity != null) {
                objectContextMenu.update(entity);
                objectContextMenu.show(canvas, event.getScreenX(), event.getScreenY());
            } else {
                noneObjectContextMenu.show(canvas, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private static class ObjectContextMenu extends ContextMenu {
        Entity entity;

        public ObjectContextMenu() {

            setAutoHide(true);

        }

        public void update(Entity entity) {
            this.entity = entity;
            getItems().clear();

            ParamDialogController paramDialogController = new ParamDialogController();
            if (entity instanceof Device.Port) {
                CheckMenuItem checkMenuItem = new CheckMenuItem("Check status");
                checkMenuItem.setSelected(((Device.Port) entity).pinging);
                checkMenuItem.setOnAction(event -> {
                    ((Device.Port) entity).pinging = checkMenuItem.isSelected();
                });
                getItems().add(checkMenuItem);

                MenuItem ipItem = new MenuItem("Change IP");
                ipItem.setOnAction(event -> {
                    paramDialogController.setName("IP address");
                    paramDialogController.setValue(((Device.Port)entity).address);
                    paramDialogController.setEvent(event1 -> {
                        ((Device.Port)entity).address = paramDialogController.getValue();
                        paramDialogController.close(event1);
                    });
                    UIController.openDialog(paramDialogController, Application.paramDialogURL);
                });
                getItems().add(ipItem);

                MenuItem macItem = new MenuItem("Chang Mac");
                macItem.setOnAction(event -> {
                    paramDialogController.setName("Mac address");
                    paramDialogController.setValue(((Device.Port)entity).mac);
                    paramDialogController.setEvent(event1 -> {
                        ((Device.Port)entity).mac = paramDialogController.getValue();
                        paramDialogController.close(event1);
                    });
                    UIController.openDialog(paramDialogController, Application.paramDialogURL);
                });
                getItems().add(macItem);

            } else if (entity instanceof Device) {
                MenuItem snmpMenuItem = new MenuItem("SNMP settings");
                snmpMenuItem.setOnAction(event -> {
                    UIController.openDialog(new SNMPSettingsDialogController((Device) entity), Application.snmpDialogURL);
                });
                getItems().add(snmpMenuItem);
            }

            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                entity.destroy();
                entity.mapController.pick(-1, -1);
            });



            MenuItem renameItem = new MenuItem("Rename");
            renameItem.setOnAction(event -> {
                paramDialogController.setName("Name");
                paramDialogController.setValue(entity.getName());
                paramDialogController.setEvent(event1 -> {
                    entity.setName(paramDialogController.getValue());
                    paramDialogController.close(event1);
                });
                UIController.openDialog(paramDialogController, Application.paramDialogURL);
            });

            getItems().addAll(renameItem, deleteItem);
        }
    }

    private static class NoneObjectContextMenu extends ContextMenu {
        MapController mapController;

        public NoneObjectContextMenu(MapController mapController) {
            this.mapController = mapController;
            setAutoHide(true);

            MenuItem pingItem = new MenuItem("Ping scan");
            pingItem.setOnAction(event -> UIController.openDialog(new PingDialogController(mapController), Application.pingDialogURL));

            MenuItem pingOneItem = new MenuItem("Ping and add one");
            pingOneItem.setOnAction(event -> UIController.openDialog(new PingOneDialogController(mapController), Application.pingOneDialogURL));

            MenuItem traceItem = new MenuItem("Trace to");
            traceItem.setOnAction(event -> UIController.openDialog(new TraceDialogController(mapController), Application.traceDialogURL));

            MenuItem addCableItem = new MenuItem("Add Cable");
            addCableItem.setOnAction(event -> {
                Cable cable = new Cable(mapController);
                cable.connectorA.setLocation(mapController.getTouchPointX(), mapController.getTouchPointY());
                cable.connectorB.setLocation(mapController.getTouchPointX() + 32, mapController.getTouchPointY());
                mapController.addEntity(cable);
            });

            MenuItem saveItem = new MenuItem("Save map");
            saveItem.setOnAction(event -> FileUtils.save(mapController, "map"));

            MenuItem loadItem = new MenuItem("Load map");
            loadItem.setOnAction(event -> FileUtils.load(mapController, "map"));

            getItems().addAll(pingItem, pingOneItem, traceItem, addCableItem, saveItem, loadItem);


        }
    }

    public static void openDialog(Initializable controller, URL url)  {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(url);

            fxmlLoader.setController(controller);
            Parent parent = fxmlLoader.load();

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.log(e.toString());
        }
    }

    public static void openParamDialog(Initializable controller, URL url) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(url);

            fxmlLoader.setController(controller);
            Parent parent = fxmlLoader.load();

            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.log(e.toString());
        }
    }

    public void getProps(Entity entity) {

        ObservableList<Property> properties = FXCollections.observableArrayList(
                new Property("destroyed", Boolean.toString(entity.destroyed))
        );

        if(entity instanceof Device) {
            SnmpUtils.walkRequest(((Device)entity).target, ((Device)entity).user, new OID(".1.3.6"), properties);
            SnmpUtils.initDevice(((Device)entity));
        }

        Platform.runLater(() -> propTable.setItems(properties));

    }

    public static class Property {
        private String property;
        private String value;

        public Property(String property, String value) {
            this.property = property;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}