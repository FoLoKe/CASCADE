package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Application;
import com.foloke.cascade.Components.CollisionComponent;
import com.foloke.cascade.Components.ContextMenuComponent;
import com.foloke.cascade.Components.Network.PingComponent;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.Tags.SelectedTag;
import com.foloke.cascade.Controllers.PingDialogController;
import com.foloke.cascade.Controllers.UIController;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Port;
import com.foloke.cascade.utils.EcsHelper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ContextMenuSystem extends EntitySystem {
    private ImmutableArray<Entity> selected;
    private ImmutableArray<Entity> uiControllers;

    @Override
    public void addedToEngine(Engine engine) {
        selected = engine.getEntitiesFor(Family.all(SelectedTag.class).get());
        uiControllers = engine.getEntitiesFor(Family.all(ContextMenuComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        uiControllers.forEach((entity) -> {
            ContextMenuComponent component = EcsHelper.cmCm.get(entity);
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
                if (EcsHelper.dtCm.has(entity)) {
                    MenuItem snmpMenuItem = new MenuItem("SNMP settings");
                    //snmpMenuItem.setOnAction(event -> com.foloke.cascade.Controllers.UIController.openDialog(new SNMPSettingsDialogController((Device) entity), Application.snmpDialogURL));

                    MenuItem updateItem = new MenuItem("Update by SNMP");
                    //updateItem.setOnAction(event -> UIController.getProps(entity));

                    MenuItem addPort = new MenuItem("Add new Port");
                    addPort.setOnAction(event -> {
                        double childX = 0;
                        double childY = 0;

                        if (EcsHelper.posCm.has(entity)) {
                            PositionComponent positionComponent = EcsHelper.posCm.get(entity);
                            childX = positionComponent.x;
                            childY = positionComponent.y;
                        }

                        if (EcsHelper.colCm.has(entity)) {
                            CollisionComponent collisionComponent = EcsHelper.colCm.get(entity);
                            childX += collisionComponent.hitBox.getWidth();
                            childY += collisionComponent.hitBox.getHeight();
                        }

                        Entity child = Port.instance(childX, childY);
                        Application.updater.spawnEntityLater(child);
                        Application.updater.assignChildLater(entity, child);
                    });

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
                }

                if (EcsHelper.aCm.has(entity)){
                    CheckMenuItem checkMenuItem = new CheckMenuItem("Check status");
                    checkMenuItem.setSelected(EcsHelper.pingCm.has(entity));

                    checkMenuItem.setOnAction(event -> Application.updater.runOnECS(() -> {
                        boolean alreadyPinging = EcsHelper.pingCm.has(entity);
                        if(alreadyPinging) {
                            entity.remove(PingComponent.class);
                        } else {
                            entity.add(new PingComponent());
                        }
                    }));

                    MenuItem ipItem = new MenuItem("Change IP");

                    menuItems.add(checkMenuItem);
                    menuItems.add(ipItem);
                }
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

                    menuItems.add(macItem);



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

            MenuItem pingScanItem = new MenuItem("Ping scan");
            pingScanItem.setOnAction(event -> UIController.openDialog(new PingDialogController(), Application.pingDialogURL));

            MenuItem pingOneItem = new MenuItem("Ping and add one");
            //pingOneItem.setOnAction(event -> UIController.openDialog(new PingOneDialogController(mapController), Application.pingOneDialogURL));

            MenuItem traceItem = new MenuItem("Trace to");
            //traceItem.setOnAction(event -> UIController.openDialog(new TraceDialogController(mapController), Application.traceDialogURL));

            Menu subContext = new Menu("Add new");

            MenuItem addDeviceItem = new MenuItem("Device");
            addDeviceItem.setOnAction((event) -> {
                Application.updater.spawnEntityLater(Device.instance(x, y));
            });

            MenuItem addDCableItem = new MenuItem("Cable");

            subContext.getItems().addAll(addDeviceItem, addDCableItem);
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

            menuItems.add(pingScanItem);
            menuItems.add(pingOneItem);
            menuItems.add(traceItem);
            menuItems.add(subContext);
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
