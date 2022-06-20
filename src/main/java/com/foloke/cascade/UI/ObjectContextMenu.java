package com.foloke.cascade.UI;

import com.foloke.cascade.Application;
import com.foloke.cascade.Controllers.NetFlowDialogController;
import com.foloke.cascade.Controllers.ParamDialogController;
import com.foloke.cascade.Controllers.SNMPSettingsDialogController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.Entities.Port;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class ObjectContextMenu extends ContextMenu {
    private final UIController UIController;

    public ObjectContextMenu(UIController UIController) {
        this.UIController = UIController;
        setAutoHide(true);
    }

    Entity entity;

    public void update(Entity entity) {
        this.entity = entity;
        getItems().clear();

        ParamDialogController paramDialogController = new ParamDialogController();
        if (entity instanceof Port) {
            CheckMenuItem checkMenuItem = new CheckMenuItem("Check status");
            checkMenuItem.setSelected(((Port) entity).pinging);
            checkMenuItem.setOnAction(event -> ((Port) entity).pinging = checkMenuItem.isSelected());
            getItems().add(checkMenuItem);

            MenuItem ipItem = new MenuItem("Change IP");
            ipItem.setOnAction(event -> {
                paramDialogController.setName("IP address");
                paramDialogController.setValue(((Port) entity).primaryAddress);
                paramDialogController.setEvent(event1 -> {
                    ((Port) entity).primaryAddress = paramDialogController.getValue();
                    paramDialogController.close(event1);
                });
                com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
            });
            getItems().add(ipItem);

            MenuItem macItem = new MenuItem("Chang Mac");
            macItem.setOnAction(event -> {
                paramDialogController.setName("Mac address");
                paramDialogController.setValue(((Port) entity).mac);
                paramDialogController.setEvent(event1 -> {
                    ((Port) entity).mac = paramDialogController.getValue();
                    paramDialogController.close(event1);
                });
                com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
            });
            getItems().add(macItem);

        } else if (entity instanceof Device) {
            MenuItem snmpMenuItem = new MenuItem("SNMP settings");
            snmpMenuItem.setOnAction(event -> com.foloke.cascade.Controllers.UIController.openDialog(new SNMPSettingsDialogController((Device) entity), Application.snmpDialogURL));

            MenuItem updateItem = new MenuItem("Update by SNMP");
            updateItem.setOnAction(event -> UIController.getProps(entity));

            MenuItem addPort = new MenuItem("Add new Port");
            addPort.setOnAction(event -> {
                Port port = ((Device) entity).addPort("");
                port.addType = Port.AddType.MANUAL;
            });

            MenuItem openNetFlow = new MenuItem("NetFlow");
            openNetFlow.setOnAction(event -> com.foloke.cascade.Controllers.UIController.openDialog(new NetFlowDialogController((Device) entity), Application.netflowURL));

            CheckMenuItem showNameItem = new CheckMenuItem("Show name");
            showNameItem.setSelected(((Device) entity).showName);
            showNameItem.setOnAction(event -> ((Device) entity).showName = showNameItem.isSelected());

            getItems().addAll(showNameItem, snmpMenuItem, updateItem, addPort, openNetFlow);
        }

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            entity.destroy();
            //entity.mapController.pick(-1, -1);
        });


        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(event -> {
            paramDialogController.setName("Name");
            paramDialogController.setValue(entity.getName());
            paramDialogController.setEvent(event1 -> {
                entity.setName(paramDialogController.getValue());
                paramDialogController.close(event1);
            });
            com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
        });

        getItems().addAll(renameItem, deleteItem);
    }
}
