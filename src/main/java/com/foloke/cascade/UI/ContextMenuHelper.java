package com.foloke.cascade.UI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import javafx.scene.control.ContextMenu;

public class ContextMenuHelper {

    public static void init(ImmutableArray<Entity> selected, ContextMenu contextMenu, double x, double y) {
        if(selected.size() > 0) {
            contextMenu.setX(x);
            contextMenu.setY(y);
        } else {

        }
    }
}
