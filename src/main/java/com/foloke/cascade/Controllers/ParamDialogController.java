package com.foloke.cascade.Controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ParamDialogController implements Initializable {
    @FXML
    private TextField paramTextField;

    @FXML
    private Label paramLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    private EventHandler<MouseEvent> event;

    private String name;
    private String value;

    public ParamDialogController(EventHandler<MouseEvent> event, String name, String value) {
        this.event = event;
        this.name = name;
        this.value = value;
    }

    public ParamDialogController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paramTextField.setText(value);
        paramLabel.setText(name);

        cancelButton.setOnMousePressed(ParamDialogController.this::closeStage);

        okButton.setOnMousePressed(event);
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public void setEvent(EventHandler<MouseEvent> event) {
        this.event = event;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return paramTextField.getText();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void close(MouseEvent event) {
        this.closeStage(event);
    }
}