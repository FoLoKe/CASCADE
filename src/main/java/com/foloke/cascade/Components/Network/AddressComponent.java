package com.foloke.cascade.Components.Network;

import com.badlogic.ashley.core.Component;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class AddressComponent implements Component {
    public InetAddress address;
    public List<InetAddress> addresses = new ArrayList<>();

    public AddressComponent(List<InterfaceAddress> addresses) {
        addresses.forEach(interfaceAddress -> {
            this.addresses.add(interfaceAddress.getAddress());
        });

        address = this.addresses.get(0);
    }

    public AddressComponent(InetAddress address) {
        this.address = address;
        this.addresses.add(address);
    }
}
