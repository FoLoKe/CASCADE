package com.foloke.cascade.Systems;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.foloke.cascade.Components.ContextMenuComponent;
import com.foloke.cascade.Components.Tags.SelectedTag;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ContextMenuSystem extends EntitySystem {
    private ImmutableArray<Entity> selected;
    private ImmutableArray<Entity> uiControllers;

    private final ComponentMapper<ContextMenuComponent> cmCm = ComponentMapper.getFor(ContextMenuComponent.class);

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
            } else if (component.open) {
                onOpenContext(component);
            }
        });
    }

    private void onOpenContext(ContextMenuComponent cmc) {
        final ContextMenu contextMenu = cmc.contextMenu;
        final List<MenuItem> menuItems = new ArrayList<>();

        if (selected.size() > 0) {
            selected.forEach((entity) -> {
                //TODO: populate context menu
            });
        } else {

        }

        Platform.runLater(() -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll();

        });
    }
}
