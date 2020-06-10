package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Camera;
import com.foloke.cascade.Entities.Cable;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
import com.foloke.cascade.Entities.Group;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MapController {
    private final List<Entity> entityList = new ArrayList<>();
    private final List<Entity> toAdd = Collections.synchronizedList(new ArrayList<>());
    private final Camera camera;
    private final TouchPoint touchPoint = new TouchPoint();
    private final Application context;
    private Rectangle groupRectangle;

    public MapController(Application context) {
        this.camera = new Camera(0, 0, 4);
        this.context = context;
        groupRectangle = new Rectangle();
    }

    public void render(GraphicsContext gc) {
        gc.scale(this.camera.scale, this.camera.scale);
        gc.translate(camera.x, camera.y);

        for (Entity entity : entityList) {
            entity.render(gc);
        }

        if (touchPoint.object != null) {
            Rectangle rectangle = touchPoint.object.getHitBox();
            gc.setLineWidth(1.0D);
            gc.setStroke(Color.YELLOW);
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

            if(touchPoint.object instanceof Device.Port) {
                rectangle = ((Device.Port)touchPoint.object).parent.getHitBox();
                gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
            }
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4d);
        gc.strokeRect(groupRectangle.getX(), groupRectangle.getY(), groupRectangle.getWidth(), groupRectangle.getHeight());
    }

    public void tick(long timestamp) {
        entityList.addAll(toAdd);
        toAdd.clear();

        Iterator<Entity> iterator = this.entityList.iterator();
        while(iterator.hasNext()) {
            Entity entity = iterator.next();
            if(entity.destroyed) {
                entity.cleanup();
                iterator.remove();
                continue;
            }
            entity.tick(timestamp);
        }
    }

    public void addEntity(Entity entity) {
        toAdd.add(entity);
    }

    public Device addOrUpdate(String address) {
        for (Entity entity : entityList) {
            if (entity instanceof Device) {
                for (Device.Port port : ((Device) entity).getPorts()) {
                    if (port.address.equals(address)) {
                        port.active = true;
                        return (Device) entity;
                    }
                }
            }
        }

        Device device = new Device(Application.image, this);
        toAdd.add(device);
        device.addPort(address);

        return device;
    }

    public void pick(double x, double y) {
        Point2D point2D = camera.translate(x, y);

        if(touchPoint.object != null ) {
            touchPoint.object.selected = false;

            if (touchPoint.object instanceof Group) {
                Entity entity = touchPoint.object.hit(point2D);
                if (entity != null) {
                    touchPoint.object = entity;
                    touchPoint.prevX = (float) point2D.getX() - touchPoint.object.getX();
                    touchPoint.prevY = (float) point2D.getY() - touchPoint.object.getY();
                    touchPoint.object.selected = true;

                    if(touchPoint.object.group != null) {
                        touchPoint.object.group.removeFromGroup(touchPoint.object);
                    }

                    return;
                }
            } else if(touchPoint.object.getHitBox().contains(point2D)) {
                touchPoint.prevX = (float) point2D.getX() - touchPoint.object.getX();
                touchPoint.prevY = (float) point2D.getY() - touchPoint.object.getY();
                touchPoint.object.selected = true;

                if(touchPoint.object.group != null) {
                    touchPoint.object.group.removeFromGroup(touchPoint.object);
                }

                return;
            } else if (touchPoint.object instanceof Device) {
                Entity entity = ((Device) touchPoint.object).pickPort(point2D);
                if (entity != null) {
                    touchPoint.object = entity;
                    touchPoint.object.selected = true;
                    return;
                }
            } else if (touchPoint.object instanceof Device.Port) {
                Entity entity = (((Device.Port) touchPoint.object).parent).pickPort(point2D);
                if (entity != null) {
                    touchPoint.object = entity;
                    touchPoint.object.selected = true;
                    return;
                }
            }
        }

        touchPoint.object = null;

        for (Entity entity : entityList) {
            touchPoint.object = entity.hit(point2D);
            if(touchPoint.object != null) {
                break;
            }
        }

        if(touchPoint.object == null) {
            touchPoint.prevX = x / camera.scale - camera.x;
            touchPoint.prevY = y / camera.scale - camera.y;
        } else {
            touchPoint.prevX = (float) point2D.getX() - touchPoint.object.getX();
            touchPoint.prevY = (float) point2D.getY() - touchPoint.object.getY();
        }
    }

    public void drop(double x, double y) {
        Point2D point2D = camera.translate(x, y);
        if(touchPoint.object != null) {
            if (touchPoint.object instanceof Cable.Connector) {
                for (Entity entity : entityList) {
                    if (entity instanceof Device) {
                        for (Device.Port port : ((Device) entity).getPorts()) {
                            if (port.getHitBox().contains(point2D.getX(), point2D.getY())) {
                                ((Cable.Connector) touchPoint.object).connect(port);
                                System.out.println("connected");
                                return;
                            }
                        }
                    }
                }
            } else if (touchPoint.object instanceof Device || touchPoint.object instanceof Group) {
                Rectangle rectangle = touchPoint.object.getHitBox();
                for (Entity entity : entityList) {
                    Entity subChild = entity.hit(point2D);
                    if(subChild instanceof Group) {
                        entity = subChild;
                    }
                    
                    if(entity instanceof Group && entity != touchPoint.object) {
                        if(entity.getHitBox().intersects(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight())) {
                            if(entity.getHitBox().getWidth() > touchPoint.object.getHitBox().getWidth() &&
                            entity.getHitBox().getHeight() > touchPoint.object.getHitBox().getHeight()) {
                                ((Group) entity).addToGroup(touchPoint.object);
                                return;
                            } else if(touchPoint.object instanceof Group) {
                                ((Group) touchPoint.object).addToGroup(entity);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public Entity hit(double x, double y) {
        Point2D point = camera.translate(x, y);
        for (Entity entity : entityList) {
            Entity hitted = entity.hit(point);
            if(hitted == null) {
                if (entity instanceof Device) {
                    hitted = ((Device) entity).pickPort(point);
                    if(hitted != null) {
                        Entity connector = hitted.hit(point);
                        if(connector != null) {
                            hitted = connector;
                        }
                    }
                }
            }

            if (hitted != null) {
                return hitted;
            }
        }

        return null;
    }

    public void drag(float x, float y) {
        Point2D point2D = camera.translate(x, y);

        if (touchPoint.object != null) {
            if(!(touchPoint.object instanceof Device.Port)) {
               if (touchPoint.object instanceof Cable.Connector) {
                    ((Cable.Connector)touchPoint.object).disconnect();
               }
               touchPoint.object.setLocation((float) point2D.getX() - touchPoint.prevX, (float) point2D.getY() - touchPoint.prevY);
            } else {
                Entity entity = ((Device.Port)touchPoint.object).getObject();
                if (entity != null) {
                    touchPoint.object = entity;
                }
            }
        } else {
            camera.setLocation((x) / camera.scale  - touchPoint.prevX, (y) / camera.scale - touchPoint.prevY);
        }
    }

    public void beginGrouping(double x, double y) {
        Point2D point2D = camera.translate(x, y);
        groupRectangle.setWidth(0);
        groupRectangle.setHeight(0);
        groupRectangle.setX(point2D.getX());
        groupRectangle.setY(point2D.getY());
    }

    public void grouping(double x, double y) {
        Point2D point2D = camera.translate(x, y);
        groupRectangle.setWidth(point2D.getX() - groupRectangle.getX());
        groupRectangle.setHeight(point2D.getY() - groupRectangle.getY());

    }

    public void endGrouping(double x, double y) {
        Point2D point2D = camera.translate(x, y);

        if(groupRectangle.getWidth() > 20 && groupRectangle.getHeight() > 20) {
            Group group = new Group(this);
            group.setLocation(groupRectangle.getX(), groupRectangle.getY());
            group.getHitBox().setWidth(groupRectangle.getWidth());
            group.getHitBox().setHeight(groupRectangle.getHeight());
            for (Entity entity : entityList) {
                Rectangle rectangle = entity.getHitBox();
                if (entity instanceof Device && groupRectangle.intersects(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight())) {
                    group.addToGroup((Device) entity);
                }
            }
            addEntity(group);
        }

        groupRectangle.setHeight(0);
        groupRectangle.setWidth(0);
    }

    public void zoom(boolean direction) {
        if(direction) {
            camera.zoomIn();
        } else {
            camera.zoomOut();
        }
    }

    public Device.Port findPort(String address) {
        for (Entity entity : entityList) {
            if(entity instanceof Device) {
                for(Device.Port port : ((Device)entity).getPorts()) {
                    if(port.address.equals(address)) {
                        return port;
                    }
                }
            }
        }

        return null;
    }

    public void establishConnection(Device.Port port1, Device.Port port2) {
        if(port1.isConnectedTo(port2)) {
            return;
        }
        Cable cable = new Cable(this);
        cable.connectorA.connect(port2);
        cable.connectorB.connect(port1);
        addEntity(cable);
    }

    private static class TouchPoint {
        public TouchPoint() { }

        public double prevX;
        public double prevY;

        public Entity object;
    }

    public double getTouchPointX() {
        return touchPoint.prevX;
    }

    public double getTouchPointY() {
        return touchPoint.prevY;
    }

    public List<Entity> getEntities() {
        return entityList;
    }
}