package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Components.Network.SnmpComponent;
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
import org.snmp4j.CommunityTarget;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
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
    private Spinner<String> interfaceSpinner;

    @FXML
    private javafx.scene.control.Button cancelButton;

    @FXML
    private javafx.scene.control.Button okButton;

    private final SnmpComponent snmpComponent;

    public SNMPSettingsDialogController(SnmpComponent snmpComponent) {
        this.snmpComponent = snmpComponent;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> versions
                = FXCollections.observableArrayList("version 1", "version 2c", "version 3");
        SpinnerValueFactory<String> versionsSpinnerValueFactory
                = new SpinnerValueFactory.ListSpinnerValueFactory<>(versions);

        Target<UdpAddress> target = snmpComponent.target;
        UsmUser user = snmpComponent.user;

        if (snmpComponent.target == null) {
            target = new CommunityTarget<>();
            ((CommunityTarget<UdpAddress>)target).setCommunity(new OctetString("public"));
            target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);

            target.setVersion(SnmpConstants.version2c);
            target.setTimeout(5000);
            target.setAddress(new UdpAddress("127.0.0.1/161"));
            target.setSecurityName(new OctetString("public"));
        }

        if (user == null) {
            user = new UsmUser(new OctetString("username"),
                    AuthMD5.ID, new OctetString("password"),
                    PrivDES.ID, new OctetString("encryption password"));
        }

        switch (target.getVersion()) {
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

        //TODO: REPLACE WITH textInput and optional spinner
        ObservableList<String> portsNames = FXCollections.observableArrayList(target.getAddress().getInetAddress().getHostAddress());//device.getPorts());
        SpinnerValueFactory<String> interfacesSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(portsNames);
        interfacesSpinnerValueFactory.setValue(target.getAddress().getInetAddress().getHostName());

        ObservableList<String> encryptionList = FXCollections.observableArrayList("DES", "AES");
        SpinnerValueFactory<String> encryptionSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(encryptionList);
        if(user.getPrivacyProtocol().equals(PrivDES.ID)) {
            encryptionSpinnerValueFactory.setValue("DES");
        } else {
            encryptionSpinnerValueFactory.setValue("AES");
        }
        encryptionSpinner.setValueFactory(encryptionSpinnerValueFactory);

        ObservableList<String> authList = FXCollections.observableArrayList("SHA1", "MD5");
        SpinnerValueFactory<String> authSpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(authList);
        if(user.getAuthenticationProtocol().equals(AuthMD5.ID)) {
            authSpinnerValueFactory.setValue("MD5");
        } else {
            authSpinnerValueFactory.setValue("SHA1");
        }
        authSpinner.setValueFactory(authSpinnerValueFactory);

        ObservableList<String> securityList = FXCollections.observableArrayList("none", "auth", "private auth");
        SpinnerValueFactory<String> securitySpinnerValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(securityList);
        if(target.getSecurityLevel() == SecurityLevel.NOAUTH_NOPRIV) {
            securitySpinnerValueFactory.setValue("none");
        } else if (target.getSecurityLevel() == SecurityLevel.AUTH_NOPRIV) {
            securitySpinnerValueFactory.setValue("auth");
        } else {
            securitySpinnerValueFactory.setValue("private auth");
        }
        securityLevelSpinner.setValueFactory(securitySpinnerValueFactory);

        interfaceSpinner.setValueFactory(interfacesSpinnerValueFactory);

        communityTextField.setText(target.getSecurityName().toString());
        timeoutTextField.setText(Long.toString(target.getTimeout()));
        portTextField.setText(Integer.toString(target.getAddress().getPort()));

        passwordTextField.setText(user.getAuthenticationPassphrase().toString());
        encryptionTextField.setText(user.getPrivacyPassphrase().toString());

        cancelButton.setOnMousePressed(SNMPSettingsDialogController.this::closeStage);

        okButton.setOnMouseClicked(event -> {
            String community = communityTextField.getText();
            String password = passwordTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            int timeout = Integer.parseInt(timeoutTextField.getText());

            int version = versionSpinner.getValue().equals("version 3") ? SnmpConstants.version3 :
                    versionSpinner.getValue().equals("version 2c") ? SnmpConstants.version2c : SnmpConstants.version1;

            OID encryption = encryptionSpinner.getValue().equals("DES") ? PrivDES.ID : PrivAES128.ID;
            String encryptionPassword = encryptionTextField.getText();

            OID authProtocol = authSpinner.getValue().equals("MD5") ? AuthMD5.ID : AuthSHA.ID;
            int level = securityLevelSpinner.getValue().equals("none") ? SecurityLevel.NOAUTH_NOPRIV :
                    securityLevelSpinner.getValue().equals("auth") ? SecurityLevel.AUTH_NOPRIV : SecurityLevel.AUTH_PRIV;

            InetAddress address;

            try {
                address = InetAddress.getByName(interfaceSpinner.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    address = InetAddress.getByName("127.0.0.1");
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                    return;
                }
            }

            Target<UdpAddress> newTarget;
            UsmUser newUser;

            if(version != SnmpConstants.version3) {
                newTarget = new CommunityTarget<>();
                ((CommunityTarget<UdpAddress>) newTarget).setCommunity(new OctetString(community));
                newTarget.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            } else  {
                newTarget = new UserTarget<>();
                newTarget.setSecurityLevel(level);
            }

            newUser = new UsmUser(new OctetString(community),
                    authProtocol, new OctetString(password),
                    encryption, new OctetString(encryptionPassword));

            newTarget.setVersion(version);
            newTarget.setTimeout(timeout);
            newTarget.setAddress(new UdpAddress(address, port));
            newTarget.setSecurityName(new OctetString(community));

            Application.updater.runOnECS(() -> {
                snmpComponent.target = newTarget;
                snmpComponent.user = newUser;
            });

            SNMPSettingsDialogController.this.closeStage(event);
        });
    }

    private void closeStage(MouseEvent event) {
        Node source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
