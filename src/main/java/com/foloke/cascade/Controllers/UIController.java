package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.SnmpUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
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
    private AnchorPane logAnchor;

    @FXML
    private ScrollPane logScroll;

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

        outerLogAnchor.widthProperty().addListener(((ov, oldValue, newValue) -> {
            logAnchor.setPrefWidth(newValue.doubleValue());
        }));

        outerLogAnchor.heightProperty().addListener(((ov, oldValue, newValue) -> {
            logAnchor.setPrefHeight(newValue.doubleValue());
        }));

        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        LogUtils.init(logTextArea);

        objectContextMenu = new ObjectContextMenu();
        noneObjectContextMenu = new NoneObjectContextMenu(mapController);

        canvas.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                Entity entity = mapController.hit(event.getX(), event.getY());
                if(entity != null) {
                    objectContextMenu.update(entity);
                    objectContextMenu.show(canvas, event.getScreenX(), event.getScreenY());
                } else {
                    noneObjectContextMenu.show(canvas, event.getScreenX(), event.getScreenY());
                }
            }
        });

    }


    private class ObjectContextMenu extends ContextMenu {
        Entity entity;

        public ObjectContextMenu() {

            setAutoHide(true);

        }

        public void update(Entity entity) {
            this.entity = entity;
            getItems().clear();
            if(entity instanceof Device.Port) {
                CheckMenuItem checkMenuItem = new CheckMenuItem("Check status");
                checkMenuItem.setSelected(((Device.Port) entity).pinging);
                checkMenuItem.setOnAction(event -> {((Device.Port) entity).pinging = checkMenuItem.isSelected();});
                getItems().addAll(checkMenuItem);
            }
        }
    }

    private static class NoneObjectContextMenu extends ContextMenu {
        MapController mapController;

        public NoneObjectContextMenu(MapController mapController) {
            this.mapController = mapController;
            setAutoHide(true);
            MenuItem item1 = new MenuItem("Ping Scan");
            item1.setOnAction(event -> UIController.openPingScanDialog(mapController));


            MenuItem item2 = new MenuItem("Menu Item 2");
            item2.setOnAction(event -> System.out.println("B"));

            getItems().addAll(item1, item2);
        }
    }

    public static void openPingScanDialog(MapController mapController)  {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Application.pingDialogURL);
            PingDialogController dialogController = new PingDialogController(mapController);

            fxmlLoader.setController(dialogController);
            Parent parent = fxmlLoader.load();


            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            LogUtils.log(e.toString());
        }
    }

    public void getProps(Entity entity) {

        ObservableList<Property> properties = FXCollections.observableArrayList(
                new Property("destroyed", Boolean.toString(entity.destroyed))
        );

        if(entity instanceof Device) {
            SnmpUtils.walkRequest(((Device)entity).communityTarget, new OID(".1.3.6"), properties);
            SnmpUtils.initDevice(((Device)entity));
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                propTable.setItems(properties);
            }
        });

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