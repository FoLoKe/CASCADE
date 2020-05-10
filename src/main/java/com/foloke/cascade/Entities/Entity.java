package com.foloke.cascade.Entities;

import javafx.scene.canvas.GraphicsContext;

public abstract class Entity {
    protected float x;
    protected float y;

    public Entity() {
    }

    public abstract void render(GraphicsContext var1);

    public abstract void tick();
}