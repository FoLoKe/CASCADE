package com.foloke.cascade.Controllers;

import com.foloke.cascade.utils.LogUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private TableView propTable;

    @FXML
    private AnchorPane propAnchor;

    @FXML
    private AnchorPane outerPropAnchor;

    private final MapController mapController;

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

        this.canvas.setOnMousePressed(mouseEvent -> UIController.this.mapController.pick((float) mouseEvent.getX(), (float) mouseEvent.getY()));

        this.canvas.setOnMouseDragged(mouseEvent -> UIController.this.mapController.drag((float) mouseEvent.getX(), (float) mouseEvent.getY()));

        this.canvas.setOnMouseReleased(mouseEvent -> UIController.this.mapController.drop(mouseEvent.getX(), mouseEvent.getY()));

        this.canvas.setOnScroll(scrollEvent -> this.mapController.zoom( scrollEvent.getDeltaY() > 0));

        outerLogAnchor.widthProperty().addListener(((ov, oldValue, newValue) -> {
            logAnchor.setPrefWidth(newValue.doubleValue());
        }));

        outerLogAnchor.heightProperty().addListener(((ov, oldValue, newValue) -> {
            logAnchor.setPrefHeight(newValue.doubleValue());
        }));

        outerPropAnchor.widthProperty().addListener(((ov, oldValue, newValue) -> {
            propAnchor.setPrefWidth(newValue.doubleValue());
        }));

        outerPropAnchor.heightProperty().addListener(((ov, oldValue, newValue) -> {
            propAnchor.setPrefHeight(newValue.doubleValue());
        }));

        LogUtils.init(logTextArea);

    }

    public void addProp(HBox hBox) {
        propsVBox.getChildren().add(hBox);
    }
}