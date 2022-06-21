package com.foloke.cascade.Controllers;

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

public class PingDialogController implements Initializable {
    @FXML
    private TextField maskTextField;

    @FXML
    private TextField addressTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    private final MapController mapController;

    public PingDialogController(MapController mapController) {
        this.mapController = mapController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelButton.setOnMousePressed(PingDialogController.this::closeStage);

        okButton.setOnMousePressed(event -> {
            ScanUtils.pingScan(mapController, addressTextField.getText(), maskTextField.getText());
            PingDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node  source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
