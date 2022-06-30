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

public class TraceDialogController implements Initializable {
    @FXML
    private TextField addressTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField maxHopsTextField;

    @FXML
    private TextField timeoutTextField;

    @FXML
    private Button okButton;

    public TraceDialogController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maxHopsTextField.setText("5");
        timeoutTextField.setText("5000");
        cancelButton.setOnMousePressed(TraceDialogController.this::closeStage);

        okButton.setOnMousePressed(event -> {
            ScanUtils.traceRoute(addressTextField.getText(),
                    Integer.parseInt(timeoutTextField.getText()),
                    Integer.parseInt(maxHopsTextField.getText()));

            TraceDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
