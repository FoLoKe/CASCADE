package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Camera;
import com.foloke.cascade.Entities.Cable;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Entities.Entity;
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

    public MapController(Application context) {
        this.camera = new Camera(0, 0, 4);
        this.context = context;
    }

    public void render(GraphicsContext gc) {
        gc.scale(this.camera.scale, this.camera.scale);
        gc.translate(camera.x, camera.y);
        gc.setLineWidth(5.0D);
        gc.setStroke(Color.RED);
        gc.strokeRect(-1, -1, 1 ,1);

        for (Entity entity : entityList) {
            entity.render(gc);
        }

        if (touchPoint.object != null) {
            Rectangle rectangle = touchPoint.object.getHitBox();
            gc.setLineWidth(1.0D);
            gc.setStroke(Color.YELLOW);
            gc.strokeRect(rectangle.getX(), rectangle.getY(), rectangle.getHeight(), rectangle.getHeight());
        }

    }

    public void tick(long timestamp) {
        entityList.addAll(toAdd);
        toAdd.clear();
        Iterator<Entity> iterator = this.entityList.iterator();

        while(iterator.hasNext()) {
            Entity entity = iterator.next();
            if(entity.destroyed) {
                iterator.remove();
                continue;
            }
            entity.tick(timestamp);
        }

    }

    public void addEntity(Entity entity) {
        toAdd.add(entity);
    }

    public void addOrUpdate(String address) {
        for (Entity entity : entityList) {
            if (entity instanceof Device) {
                for (Device.Port port : ((Device) entity).getPorts()) {
                    if (port.address.equals(address)) {
                        return;
                    }
                }
            }
        }

        Device device = new Device(Application.image, this);
        toAdd.add(device);
        device.addPort(address);
    }

    public void pick(double x, double y) {
        Point2D point2D = camera.translate(x, y);

        if(touchPoint.object != null ) {
            touchPoint.object.selected = false;
            if(touchPoint.object.getHitBox().contains(point2D)) {
                touchPoint.prevX = (float) point2D.getX() - touchPoint.object.getX();
                touchPoint.prevY = (float) point2D.getY() - touchPoint.object.getY();
                touchPoint.object.selected = true;
                return;
            } else if (touchPoint.object instanceof Device) {
                touchPoint.object = ((Device) touchPoint.object).pickPort(point2D);
                if (touchPoint.object != null) {
                    touchPoint.object.selected = true;
                    return;
                }
            } else if (touchPoint.object instanceof Device.Port) {
                touchPoint.object = (((Device.Port) touchPoint.object).parent).pickPort(point2D);
                if (touchPoint.object != null) {
                    touchPoint.object.selected = true;
                    return;
                }
            }
        }

        touchPoint.object = null;

        for (Entity entity : entityList) {
            touchPoint.object = entity.hit(point2D);
            if(touchPoint.object != null) {
                context.getProps(touchPoint.object);
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
        if(touchPoint.object != null && touchPoint.object instanceof Cable.Connector) {
            for (Entity entity : entityList) {
                if (entity instanceof Device) {
                    for (Device.Port port : ((Device) entity).getPorts()) {
                        if (port.getHitBox().contains(point2D.getX(), point2D.getY())) {
                            ((Cable.Connector)touchPoint.object).connect(port);
                            System.out.println("connected");
                            return;
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
}