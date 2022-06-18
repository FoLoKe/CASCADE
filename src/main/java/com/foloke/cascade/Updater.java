package com.foloke.cascade;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.foloke.cascade.Components.CameraComponent;
import com.foloke.cascade.Components.MouseInputComponent;
import com.foloke.cascade.Components.Tags.MainCameraTag;
import com.foloke.cascade.Entities.Camera;
import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.Systems.*;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Updater extends AnimationTimer {
    private final Engine engine;

    private final Object runLock = new Object();
    private final List<Runnable> tasks = new ArrayList<>();

    private final MouseInputComponent mouseInputComponent = new MouseInputComponent();
    private final GraphicsContext gc;

    public Updater(Canvas canvas) {
        //GRAPHICS
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.color(0.5D, 0.6D, 0.5D));
        gc.setImageSmoothing(false);

        //ECS
        engine = new Engine();
        engine.addSystem(new CameraSystem());
        engine.addSystem(new SpriteRenderSystem(gc));
        engine.addSystem(new MouseInputSystem());
        engine.addSystem(new SelectionRendererSystem(gc));
        engine.addSystem(new CollisionDebugRendererSystem(gc));

        engine.addSystem(new MovementSystem());

        Entity camera = new Camera(gc, mouseInputComponent);
        engine.addEntity(camera);

        engine.addEntity(Device.instance());
    }

    public void handle(long timestamp) {
        Canvas canvas = gc.getCanvas();
        gc.fillRect(0.0D, 0.0D, canvas.getWidth(), canvas.getHeight());
        gc.save();
        synchronized (runLock) {
            for (Runnable task : tasks) {
                task.run();
            }

            tasks.clear();
        }

        engine.update(timestamp);
        gc.restore();
    }

    public void runOnECS(Runnable runnable) {
        synchronized (runLock) {
            tasks.add(runnable);
        }
    }

    public void mouseInput(MouseEvent mouseEvent) {
        runOnECS(() -> mouseInputComponent.events.add(mouseEvent));
    }

    public void mouseScroll(ScrollEvent scrollEvent) {
        runOnECS(() -> mouseInputComponent.scroll = scrollEvent.getDeltaY() > 0 ? 0.25 : -0.25);
    }

    public void spawnEntity(Entity entity) {
        runOnECS(() -> engine.addEntity(entity));
    }
}