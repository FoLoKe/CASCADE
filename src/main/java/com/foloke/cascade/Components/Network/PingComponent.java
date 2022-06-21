package com.foloke.cascade.Components.Network;

import com.badlogic.ashley.core.Component;

public class PingComponent implements Component {
    public double timestamp;
    public int delay = 1000;
    public int status = 0;
    public boolean pinging = false;
}
