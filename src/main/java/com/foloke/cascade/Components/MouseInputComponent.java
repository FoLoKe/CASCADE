package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class MouseInputComponent implements Component {
    public List<MouseEvent> events = new ArrayList<>();

    public double scroll;
    public Point2D lastEvent;
}
