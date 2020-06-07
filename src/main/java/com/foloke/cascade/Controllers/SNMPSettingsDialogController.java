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
import org.snmp4j.security.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SNMPSettingsDialogController implements Initializable {
    @FXML
    private TextField communityTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField encryptionTextField;

    @FXML
    private TextField timeoutTextField;

    @FXML
    private TextField portTextField;

    @FXML
    private Spinner<String> securityLevelSpinner;

    @FXML
    private Spinner<String> versionSpinner;

    @FXML
    private Spinner<String> authSpinner;

    @FXML
    private Spinner<String> encryptionSpinner;

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
        ObservableList<String> versions
                = FXCollections.observableArrayList("version 1", "version 2c", "version 3");
        SpinnerValueFactory<String> versionsSpinnerValueFactory
                = new SpinnerValueFactory.ListSpinnerValueFactory<>(versions);

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
        versionSpinner.setValueFactory(versionsSpinnerValueFactory);

        ObservableList<Device.Port> portsNames = FXCollections.observableArrayList(device.getPorts());
        SpinnerValueFactory<Device.Port> interfacesSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(portsNames);
        interfacesSpinnerValueFactory.setValue(device.findPort(device.getSnmpAddress()));

        ObservableList<String> encryptionList = FXCollections.observableArrayList("DES", "AES");
        SpinnerValueFactory<String> encryptionSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(encryptionList);
        if(device.getEncryptionProtocol() == PrivDES.ID) {
            encryptionSpinnerValueFactory.setValue("DES");
        } else {
            encryptionSpinnerValueFactory.setValue("AES");
        }
        encryptionSpinner.setValueFactory(encryptionSpinnerValueFactory);

        ObservableList<String> authList = FXCollections.observableArrayList("SHA1", "MD5");
        SpinnerValueFactory<String> authSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(authList);
        if(device.getAuthProtocol() == AuthMD5.ID) {
            authSpinnerValueFactory.setValue("MD5");
        } else {
            authSpinnerValueFactory.setValue("SHA1");
        }
        authSpinner.setValueFactory(authSpinnerValueFactory);

        ObservableList<String> securityList = FXCollections.observableArrayList("none", "auth", "private auth");
        SpinnerValueFactory<String> securitySpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(securityList);
        if(device.getSecurityLevel() == SecurityLevel.NOAUTH_NOPRIV) {
            securitySpinnerValueFactory.setValue("none");
        } else if (device.getSecurityLevel() == SecurityLevel.AUTH_NOPRIV) {
            securitySpinnerValueFactory.setValue("auth");
        } else {
            securitySpinnerValueFactory.setValue("private auth");
        }
        securityLevelSpinner.setValueFactory(securitySpinnerValueFactory);

        interfaceSpinner.setValueFactory(interfacesSpinnerValueFactory);

        communityTextField.setText(device.getSnmpCommunity());
        timeoutTextField.setText(Integer.toString(device.getSnmpTimeout()));
        portTextField.setText(device.getSnmpPort());

        passwordTextField.setText(device.getSnmpPassword());
        encryptionTextField.setText(device.getSnmpEncryptionPass());

        cancelButton.setOnMousePressed(event -> {
            SNMPSettingsDialogController.this.closeStage(event);
        });

        okButton.setOnMousePressed(event -> {
            device.setSnmpCommunity(communityTextField.getText());
            device.setSnmpPassword(passwordTextField.getText());
            device.setSnmpPort(portTextField.getText());
            device.setSnmpTimeout(Integer.parseInt(timeoutTextField.getText()));
            device.setSnmpEncryptionPass(encryptionTextField.getText());

            String version = versionSpinner.getValue();
            if(version.equals("version 1")) {
                device.setSnmpVersion(SnmpConstants.version1);
            } else if (version.equals("version 2c")) {
                device.setSnmpVersion(SnmpConstants.version2c);
            } else {
                device.setSnmpVersion(SnmpConstants.version3);
            }

            String encryption = encryptionSpinner.getValue();
            if(encryption.equals("DES")) {
                device.setEncryptionProtocol(PrivDES.ID);
            } else {
                device.setEncryptionProtocol(PrivAES128.ID);
            }

            String auth = authSpinner.getValue();
            if(auth.equals("MD5")) {
                device.setAuthProtocol(AuthMD5.ID);
            } else {
                device.setAuthProtocol(AuthSHA.ID);
            }

            String level = securityLevelSpinner.getValue();
            if(level.equals("none")) {
                device.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            } else if (level.equals("auth")){
                device.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
            } else {
                device.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            }

            device.updateSnmpConfiguration(interfaceSpinner.getValue().address);

            SNMPSettingsDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
