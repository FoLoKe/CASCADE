package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import com.foloke.cascade.utils.Sprite;

public class SpriteComponent implements Component {
    public Sprite sprite;

    public SpriteComponent(Sprite sprite) {
        this.sprite = sprite;
    }
}
