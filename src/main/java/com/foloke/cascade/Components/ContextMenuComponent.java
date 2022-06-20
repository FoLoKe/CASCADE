package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import javafx.scene.control.ContextMenu;

public class ContextMenuComponent implements Component {
    public ContextMenu contextMenu = new ContextMenu();
    public boolean open;
    public boolean close;
}
