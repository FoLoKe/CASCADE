package com.foloke.cascade.Controllers;

import com.foloke.cascade.utils.SimpleFlow;
import com.lumaserv.netflow.NetFlowCollector;
import com.lumaserv.netflow.NetFlowSession;
import com.lumaserv.netflow.flowset.FlowField;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetFlowController implements Runnable {

    private NetFlowCollector collector;
    private Thread updaterThread;
    NetFlowDialogController window;

    private final List<SimpleFlow> toAdd = Collections.synchronizedList(new ArrayList<>());

    // MAP: DEVICE : FLOWS SET (UNIQUE: DST PORT:ADR)
    private final Map<Integer, HashSet<SimpleFlow>> activeFlows = Collections.synchronizedMap(new ConcurrentHashMap<>());

    //statistics
    private final Map<Integer, ObservableMap<String, IntegerProperty>> activeFlowsStat = new HashMap<>();
    private final Map<Integer, ObservableMap<String, IntegerProperty>> endedFlowsStat = new HashMap<>();
    MapController mapController;


    public NetFlowController(MapController mapController) {
        this.mapController = mapController;
        NetFlowSession session = new NetFlowSession(source -> {
            try {
                ByteBuffer ipBuffer = ByteBuffer.allocate(4);
                ipBuffer.putInt(source.getDeviceIP());

                System.out.println("Incoming NetFlow from " + SimpleFlow.ipToString(ipBuffer.array()) + " source id: " + source.getId());
                mapController.addOrUpdate(SimpleFlow.ipToString(ipBuffer.array()));
                source.listen((id, values) -> {
                    int timeout =
                            (values.get(FlowField.LAST_SWITCHED).asInt()
                                    - values.get(FlowField.FIRST_SWITCHED).asInt()) == 0 ? 20000 : 0;

                    SimpleFlow flow = new SimpleFlow(source.getDeviceIP(), values, timeout);

                    toAdd.add(flow);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            collector = new NetFlowCollector(session, 9996);
            updaterThread = new Thread(this);
            updaterThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        collector.close();
        updaterThread.interrupt();
    }

    @Override
    public void run() {
        try {
            while (!updaterThread.isInterrupted()) {
                for (SimpleFlow flow : toAdd) {
                    Set<SimpleFlow> deviceFlows = activeFlows.computeIfAbsent(flow.sourceIP, k -> new HashSet<>());


                    if (flow.timeout > 0) {
                        if(!deviceFlows.remove(flow))
                            putIntoActive(flow);

                        deviceFlows.add(flow);
                    } else {
                        putIntoEnded(flow);
                    }
                }

                //TODO: Device specific
                for (Map.Entry<Integer, HashSet<SimpleFlow>> activeFlowsEntry : activeFlows.entrySet()){
                    Iterator<SimpleFlow> it = activeFlowsEntry.getValue().iterator();
                    Map<String, IntegerProperty> activeMap = activeFlowsStat.get(activeFlowsEntry.getKey());

                    while (it.hasNext()) {
                        SimpleFlow flow = it.next();

                        flow.tick();
                        if(flow.expired) {
                            it.remove();
                            putIntoEnded(flow);
                        }
                    }

                    if(activeMap!= null && window != null && window.deviceIp == activeFlowsEntry.getKey()) {
                        //window.updateTraffic(activeMap);
                    }

                    //System.out.println(activeFlowsEntry.getValue().size());
                }

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putIntoActive(SimpleFlow flow) {
        Map<String, IntegerProperty> active = activeFlowsStat.computeIfAbsent(flow.sourceIP, k -> FXCollections.observableHashMap());
        String app = categories.get(flow.port);
        IntegerProperty value;

        if (app == null) {
            value = active.get("Other");
        } else {
            value = active.get(app);
        }


        if(value != null) {
            value.setValue(value.intValue() + 1);
        } else {
            value = new SimpleIntegerProperty(1);
            active.put(app, value);
            if (window != null && window.deviceIp == flow.sourceIP) {
                window.putActive(flow.port, value);
            }
        }
    }

    private void putIntoEnded(SimpleFlow flow) {
        Map<String, IntegerProperty> ended = endedFlowsStat.computeIfAbsent(flow.sourceIP, k -> FXCollections.observableHashMap());
        Map<String, IntegerProperty> active = activeFlowsStat.get(flow.sourceIP);

        String app = categories.get(flow.port);
        IntegerProperty value;
        if (app == null) {
            value = ended.get("Other");
        } else {
            value = ended.get(app);
        }

        if(value != null) {
            value.setValue(value.intValue() + 1);
        } else {
            value = new SimpleIntegerProperty(1);
            ended.put(app, value);
            if (window != null && window.deviceIp == flow.sourceIP) {
                window.putEnded(flow.port, value);
            }
        }

        if (active == null)
            return;

        value = active.get(app);
        if (value != null) {
            value.setValue(value.intValue() - 1);
            if (value.intValue() <= 0) {
                active.remove(app);
                if (window != null && window.deviceIp == flow.sourceIP) {
                    window.removeActive(flow.port);
                }
            }
        }
    }

    public void bind(NetFlowDialogController netFlowDialogController) {
        this.window = netFlowDialogController;
        window.activeChartData.clear();
        window.endedChartData.clear();

    }

    // user-defined categories PORT: APP NAME; Otherwise it should be "OTHER" category
    Map<Integer, String> categories = new HashMap<>();
}
