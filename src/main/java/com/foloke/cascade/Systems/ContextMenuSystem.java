package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Application;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.ContextMenuComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.Tags.DeviceTag;
import com.foloke.cascade.Components.Tags.PortTag;
import com.foloke.cascade.Components.Tags.SelectedTag;
import com.foloke.cascade.Entities.Port;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ContextMenuSystem extends EntitySystem {
    private ImmutableArray<Entity> selected;
    private ImmutableArray<Entity> uiControllers;

    private final ComponentMapper<ContextMenuComponent> cmCm = ComponentMapper.getFor(ContextMenuComponent.class);
    private final ComponentMapper<DeviceTag> dcm = ComponentMapper.getFor(DeviceTag.class);
    private final ComponentMapper<PortTag> pcm = ComponentMapper.getFor(PortTag.class);
    private final ComponentMapper<PositionComponent> posCm = ComponentMapper.getFor(PositionComponent.class);
    private final ComponentMapper<CollisionComponent> ccm = ComponentMapper.getFor(CollisionComponent.class);

    @Override
    public void addedToEngine(Engine engine) {
        selected = engine.getEntitiesFor(Family.all(SelectedTag.class).get());
        uiControllers = engine.getEntitiesFor(Family.all(ContextMenuComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        uiControllers.forEach((entity) -> {
            ContextMenuComponent component = cmCm.get(entity);
            final ContextMenu contextMenu = component.contextMenu;
            if (component.close) {
                Platform.runLater(() -> {
                    contextMenu.getItems().clear();
                    contextMenu.hide();
                });
                component.close = false;
            } else if (component.open) {
                System.out.println("check");
                onOpenContext(component);
                component.open = false;
            }
        });
    }

    private void onOpenContext(ContextMenuComponent cmc) {
        final ContextMenu contextMenu = cmc.contextMenu;
        final List<MenuItem> menuItems = new ArrayList<>();
        final double x = cmc.x;
        final double y = cmc.y;
        final Node anchor = cmc.anchor;

        System.out.println("open");

        if (selected.size() > 0) {
            for(Entity entity : selected) {

                //ParamDialogController paramDialogController = new ParamDialogController();
                if (dcm.has(entity)) {
                    MenuItem snmpMenuItem = new MenuItem("SNMP settings");
                    //snmpMenuItem.setOnAction(event -> com.foloke.cascade.Controllers.UIController.openDialog(new SNMPSettingsDialogController((Device) entity), Application.snmpDialogURL));

                    MenuItem updateItem = new MenuItem("Update by SNMP");
                    //updateItem.setOnAction(event -> UIController.getProps(entity));

                    MenuItem addPort = new MenuItem("Add new Port");
                    addPort.setOnAction(event -> {
                        double childX = 0;
                        double childY = 0;

                        if (posCm.has(entity)) {
                            PositionComponent positionComponent = posCm.get(entity);
                            childX = positionComponent.x;
                            childY = positionComponent.y;
                        }

                        if (ccm.has(entity)) {
                            CollisionComponent collisionComponent = ccm.get(entity);
                            childX += collisionComponent.hitBox.getWidth();
                            childY += collisionComponent.hitBox.getHeight();
                        }

                        Entity child = Port.instance(childX, childY);
                        Application.updater.spawnEntityLater(child);
                        Application.updater.assignChildLater(entity, child);
                    });

//                    addPort.setOnAction(event -> {
//                        Port port = ((Device) entity).addPort("");
//                        port.addType = Port.AddType.MANUAL;
//                    });

                    MenuItem openNetFlow = new MenuItem("NetFlow");
                    //openNetFlow.setOnAction(event -> com.foloke.cascade.Controllers.UIController.openDialog(new NetFlowDialogController((Device) entity), Application.netflowURL));

                    CheckMenuItem showNameItem = new CheckMenuItem("Show name");
                    //showNameItem.setSelected(((Device) entity).showName);
                    //showNameItem.setOnAction(event -> ((Device) entity).showName = showNameItem.isSelected());

                    menuItems.add(showNameItem);
                    menuItems.add(snmpMenuItem);
                    menuItems.add(updateItem);
                    menuItems.add(addPort);
                    menuItems.add(openNetFlow);
                } if (pcm.has(entity)) {
                    CheckMenuItem checkMenuItem = new CheckMenuItem("Check status");
                    //checkMenuItem.setSelected(((Port) entity).pinging);
                    //checkMenuItem.setOnAction(event -> ((Port) entity).pinging = checkMenuItem.isSelected());

                    MenuItem ipItem = new MenuItem("Change IP");
//                    ipItem.setOnAction(event -> {
//                        paramDialogController.setName("IP address");
//                        paramDialogController.setValue(((Port) entity).primaryAddress);
//                        paramDialogController.setEvent(event1 -> {
//                            ((Port) entity).primaryAddress = paramDialogController.getValue();
//                            paramDialogController.close(event1);
//                        });
//                        com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
//                    });


                    MenuItem macItem = new MenuItem("Chang Mac");
//                    macItem.setOnAction(event -> {
//                        paramDialogController.setName("Mac address");
//                        paramDialogController.setValue(((Port) entity).mac);
//                        paramDialogController.setEvent(event1 -> {
//                            ((Port) entity).mac = paramDialogController.getValue();
//                            paramDialogController.close(event1);
//                        });
//                        com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
//                    });

                    menuItems.add(checkMenuItem);
                    menuItems.add(ipItem);
                    menuItems.add(macItem);

                }

                MenuItem deleteItem = new MenuItem("Delete");
//                deleteItem.setOnAction(event -> {
//                    entity.destroy();
//                    //entity.mapController.pick(-1, -1);
//                });


                MenuItem renameItem = new MenuItem("Rename");
//                renameItem.setOnAction(event -> {
//                    paramDialogController.setName("Name");
//                    paramDialogController.setValue(entity.getName());
//                    paramDialogController.setEvent(event1 -> {
//                        entity.setName(paramDialogController.getValue());
//                        paramDialogController.close(event1);
//                    });
//                    com.foloke.cascade.Controllers.UIController.openDialog(paramDialogController, Application.paramDialogURL);
//                });

                menuItems.add(renameItem);
                menuItems.add(deleteItem);

                break;
            }
        } else {

            MenuItem pingItem = new MenuItem("Ping scan");
            //pingItem.setOnAction(event -> UIController.openDialog(new PingDialogController(mapController), Application.pingDialogURL));

            MenuItem pingOneItem = new MenuItem("Ping and add one");
            //pingOneItem.setOnAction(event -> UIController.openDialog(new PingOneDialogController(mapController), Application.pingOneDialogURL));

            MenuItem traceItem = new MenuItem("Trace to");
            //traceItem.setOnAction(event -> UIController.openDialog(new TraceDialogController(mapController), Application.traceDialogURL));

            MenuItem addCableItem = new MenuItem("Add Cable");
//            addCableItem.setOnAction(event -> {
//                Cable cable = new Cable(mapController);
//                cable.connectorA.setPosition(mapController.getTouchPointX(), mapController.getTouchPointY());
//                cable.connectorB.setPosition(mapController.getTouchPointX() + 32, mapController.getTouchPointY());
//                //mapController.addEntity(cable);
//            });

            MenuItem saveItem = new MenuItem("Save map");
            //saveItem.setOnAction(event -> FileUtils.save(mapController, "map"));

            MenuItem loadItem = new MenuItem("Load map");
            //loadItem.setOnAction(event -> FileUtils.load(mapController, "map"));

            menuItems.add(pingItem);
            menuItems.add(pingOneItem);
            menuItems.add(traceItem);
            menuItems.add(addCableItem);
            menuItems.add(saveItem);
            menuItems.add(loadItem);
        }

        Platform.runLater(() -> {
            contextMenu.getItems().clear();
            contextMenu.setAutoHide(true);
            contextMenu.getItems().addAll(menuItems);
            contextMenu.show(anchor, x, y);
        });
    }
}
