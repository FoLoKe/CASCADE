package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.PositionComponent;
import com.foloke.cascade.Components.SpriteComponent;
import javafx.scene.canvas.GraphicsContext;

public class SpriteRenderSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private final ComponentMapper<PositionComponent> pm = ComponentMapper.getFor(PositionComponent.class);
    private final ComponentMapper<SpriteComponent> sm = ComponentMapper.getFor(SpriteComponent.class);

    private final GraphicsContext gc;

    public SpriteRenderSystem(GraphicsContext graphicsContext) {
        this.gc = graphicsContext;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(PositionComponent.class, SpriteComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            PositionComponent position = pm.get(entity);
            SpriteComponent sprite = sm.get(entity);
            sprite.sprite.setPosition(position.x, position.y);
            sprite.sprite.render(gc);
        }
    }
}
