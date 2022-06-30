package com.foloke.cascade.Components.Network;

import com.badlogic.ashley.core.Component;
import org.snmp4j.Target;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.UdpAddress;

public class SnmpComponent implements Component {
    public Target<UdpAddress> target;
    public UsmUser user;
}
