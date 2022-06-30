package com.foloke.cascade.Components.Network;

import com.badlogic.ashley.core.Component;

import java.util.concurrent.atomic.AtomicBoolean;

public class PingComponent implements Component {
    public double timestamp;
    public int delay = 1000;
    public int status = 0;
    public AtomicBoolean pinging = new AtomicBoolean(false);
}
