package com.foloke.cascade.Controllers;

import com.foloke.cascade.Entities.Device;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.snmp4j.mp.SnmpConstants;

import java.net.URL;
import java.util.ResourceBundle;

public class SNMPSettingsDialogController implements Initializable {
    @FXML
    private TextField communityTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField timeoutTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private Spinner<String> versionSpinner;

    @FXML
    private Spinner<Device.Port> interfaceSpinner;

    @FXML
    private javafx.scene.control.Button cancelButton;

    @FXML
    private javafx.scene.control.Button okButton;

    Device device;

    public SNMPSettingsDialogController(Device device) {
        this.device = device;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> versions = FXCollections.observableArrayList("version 1", "version 2c", "version 3");
        SpinnerValueFactory<String> versionsSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(versions);

        switch (device.getSnmpVersion()) {
            case SnmpConstants.version1:
                versionsSpinnerValueFactory.setValue("version 1");
                break;

            case SnmpConstants.version2c:
                versionsSpinnerValueFactory.setValue("version 2c");
                break;

            case SnmpConstants.version3:
                versionsSpinnerValueFactory.setValue("version 3");
                break;

        }

        ObservableList<Device.Port> portsNames = FXCollections.observableArrayList(device.getPorts());
        SpinnerValueFactory<Device.Port> interfacesSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(portsNames);
        interfacesSpinnerValueFactory.setValue(device.findPort(device.getSnmpAddress()));

        interfaceSpinner.setValueFactory(interfacesSpinnerValueFactory);
        versionSpinner.setValueFactory(versionsSpinnerValueFactory);
        communityTextField.setText(device.getSnmpCommunity());
        timeoutTextField.setText(Integer.toString(device.getSnmpTimeout()));
        portTextField.setText(device.getSnmpPort());

        passwordTextField.setText(device.getSnmpPassword());


        //passwordTextField.setText(device.getSnmpSecurityName());

        cancelButton.setOnMousePressed(event -> {
            SNMPSettingsDialogController.this.closeStage(event);
        });

        okButton.setOnMousePressed(event -> {
            device.setSnmpCommunity(communityTextField.getText());
            device.setSnmpPassword(passwordTextField.getText());
            device.setSnmpPort(portTextField.getText());
            device.setSnmpTimeout(Integer.parseInt(timeoutTextField.getText()));

            String version = versionSpinner.getValue();
            if(version.equals("version 1")) {
                device.setSnmpVersion(SnmpConstants.version1);
            } else if (version.equals("version 2c")) {
                device.setSnmpVersion(SnmpConstants.version2c);
            } else {
                device.setSnmpVersion(SnmpConstants.version3);
            }

            device.setCommunityDefaults(interfaceSpinner.getValue().address);

            SNMPSettingsDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
