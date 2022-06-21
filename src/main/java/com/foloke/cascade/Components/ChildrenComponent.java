package com.foloke.cascade.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import java.util.ArrayList;
import java.util.List;

public class ChildrenComponent implements Component {
    public List<Entity> children = new ArrayList<>();
}
