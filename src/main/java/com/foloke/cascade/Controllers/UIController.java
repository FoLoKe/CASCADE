package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
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

public class UIController {
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

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void initialize() {
        LogUtils.init(logTextArea);

        this.anchorPane.widthProperty().addListener((ov, oldValue, newValue) -> this.canvas.setWidth(newValue.doubleValue()));

        this.anchorPane.heightProperty().addListener((ov, oldValue, newValue) -> this.canvas.setHeight(newValue.doubleValue()));

        this.canvas.setOnMousePressed(mouseEvent -> Application.updater.mouseInput(mouseEvent));

        this.canvas.setOnMouseDragged(mouseEvent -> Application.updater.mouseInput(mouseEvent));

        this.canvas.setOnMouseReleased(mouseEvent -> Application.updater.mouseInput(mouseEvent));

        this.canvas.setOnScroll(scrollEvent -> Application.updater.mouseScroll(scrollEvent));

        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }

    public static void openDialog(Initializable controller, URL url) {
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
            LogUtils.log(e.toString());
        }
    }

    //TODO: LOAD MIB NAMES AND DESCRIPTION

    public void getProps(Entity entity) {
        ObservableList<Property> properties = FXCollections.observableArrayList(
                //new Property("name", entity.getName())
        );

        if(entity instanceof Device) {
            //SnmpUtils.walkRequest(((Device)entity).target, ((Device)entity).user, new OID(".1.3.6"), properties);
            //SnmpUtils.initDevice(((Device)entity));
        }

        //Platform.runLater(() -> propTable.setItems(properties));
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