package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.utils.ScanUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class PingOneDialogController implements Initializable {

    @FXML
    private TextField addressTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    private final MapController mapController;

    public PingOneDialogController(MapController mapController) {
        this.mapController = mapController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelButton.setOnMousePressed(event -> {
            PingOneDialogController.this.closeStage(event);
        });

        okButton.setOnMousePressed(event -> {
            Device.Port port = mapController.findPort(addressTextField.getText());
            if(port != null) {
                ScanUtils.ping(port);
            } else {
                Device device = new Device(Application.image, mapController);
                port = device.addPort(addressTextField.getText());
                ScanUtils.ping(port);
                mapController.addEntity(device);
            }

            PingOneDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
