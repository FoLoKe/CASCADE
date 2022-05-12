package com.foloke.cascade.utils;

import com.foloke.cascade.Entities.Device;
import jakarta.persistence.*;

@Entity
@Table(name = "flowStat")
public class FlowStat {
    @Id
    @Column(name = "port")
    public int port;

    @Column(name = "count")
    public int count;

    public FlowStat(int port, int count) {
        this.port = port;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowStat flowStat = (FlowStat) o;

        return port == flowStat.port;
    }

    @Override
    public int hashCode() {
        return port;
    }
}
