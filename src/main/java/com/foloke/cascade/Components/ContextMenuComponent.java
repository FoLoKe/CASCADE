package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;

public class ContextMenuComponent implements Component {
    public ContextMenu contextMenu = new ContextMenu();
    public Node anchor;
    public boolean open;
    public boolean close;
    public double x;
    public double y;

    public ContextMenuComponent(Node canvas) {
        this.anchor = canvas;
    }
}
