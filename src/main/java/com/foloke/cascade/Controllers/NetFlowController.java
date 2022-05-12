package com.foloke.cascade.Controllers;

import com.foloke.cascade.Entities.Device;
import com.foloke.cascade.utils.LogUtils;
import com.foloke.cascade.utils.SimpleCumulativeEMWA;
import com.foloke.cascade.utils.SimpleFlow;
import com.lumaserv.netflow.NetFlowCollector;
import com.lumaserv.netflow.NetFlowSession;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;

public class NetFlowController {

    private NetFlowCollector collector;

    //private final List<SimpleFlow> toAdd = new ArrayList<>();

    //TODO: REFACTOR THIS SHIT
    // MAP: DEVICE : FLOWS SET (UNIQUE: DST PORT:ADR)
    //private final Map<Integer, HashSet<SimpleFlow>> latestFlows = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<Integer, Sampler> samplers = new HashMap<>();

    //statistics
    private final Map<Integer, ObservableMap<Integer, IntegerProperty>> latestFlowsStat = new HashMap<>();
    private final Map<Integer, ObservableMap<Integer, IntegerProperty>> endedFlowsStat = new HashMap<>();

    //MAP: DEVICE: TIMESTAMP: FLOWTYPE: FLOWS-COUNT TODO: should be in database
    //private final Map<Integer, Map<Long, List<FlowStat>>> flowStat = new HashMap<>();

    private final ScheduledExecutorService scheduledExecutorService;

    MapController mapController;

    public NetFlowController(MapController mapController) {
        this.mapController = mapController;
        scheduledExecutorService = new ScheduledThreadPoolExecutor(100);
        NetFlowSession session = new NetFlowSession(source -> {
            try {
                ByteBuffer ipBuffer = ByteBuffer.allocate(4);
                ipBuffer.putInt(source.getDeviceIP());
                String ip = SimpleFlow.ipToString(ipBuffer.array());
                LogUtils.log("Incoming NetFlow from " + ip + " source id: " + source.getId());
                Device device = mapController.addOrUpdate(ip);

                Sampler sampler = getSampler(source.getDeviceIP(), device);

                source.listen((id, values) -> {

                    SimpleFlow flow = new SimpleFlow(source.getDeviceIP(), values);
                    try {
                        sampler.add(flow);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            collector = new NetFlowCollector(session, 9996);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Sampler getSampler(int ip, Device device) {
        Sampler sampler = samplers.get(ip);
        if (sampler == null) {
            sampler = new Sampler(device);
            samplers.put(ip, sampler);
            scheduledExecutorService.scheduleWithFixedDelay(sampler, 1, 1, TimeUnit.SECONDS);
        }
        return sampler;
    }

    public void close() {
        collector.close();
        scheduledExecutorService.shutdown();
    }

//                long timestamp = System.currentTimeMillis();

//                for (SimpleFlow flow : toAdd) {
//                    Set<SimpleFlow> deviceFlows = latestFlows.computeIfAbsent(flow.sourceIP, k -> new HashSet<>());
//
//
//                    if (flow.timeout > 0) {
//                        if(!deviceFlows.remove(flow))
//                            putIntoActive(flow);
//
//                        deviceFlows.add(flow);
//                    } else {
//                        deviceFlows.remove(flow); //TODO:????
//                        putIntoEnded(flow);
//                    }
//                }
//
//                toAdd.clear();
//
//                for (Map.Entry<Integer, HashSet<SimpleFlow>> activeFlowsEntry : latestFlows.entrySet()){
//                    Iterator<SimpleFlow> it = activeFlowsEntry.getValue().iterator();
//                    Map<Integer, IntegerProperty> activeMap = latestFlowsStat.get(activeFlowsEntry.getKey());
//                    //Map<Long, List<FlowStat>> trafficStat = flowStat.computeIfAbsent(activeFlowsEntry.getKey(), k -> new HashMap<>());
//                    //List<FlowStat> trafficList = trafficStat.computeIfAbsent(timestamp, k -> new ArrayList<>());
//
//
//                    while (it.hasNext()) {
//                        SimpleFlow flow = it.next();
//
//                        flow.tick();
//                        if(flow.expired) {
//                            it.remove();
//                            putIntoEnded(flow);
//                        }
//                    }
//
//                    if(activeMap!= null) {
//                        for (Map.Entry<Integer, IntegerProperty> e : activeMap.entrySet()) {
//                            FlowStat flowStat = new FlowStat(e.getKey(), e.getValue().intValue());
//                            trafficList.add(flowStat);
//                            //Transaction transaction = Application.databaseSession.beginTransaction();
//                            //Application.databaseSession.persist(flowStat);
//                            //Application.databaseSession.flush();
//                            //transaction.commit();
//                        }
//                        if(window != null && window.deviceIp == activeFlowsEntry.getKey()) {
//                            window.updateTraffic(trafficList, timestamp);
//                        }
//                    }
//
//                    //System.out.println(activeFlowsEntry.getValue().size());
//                }

    private void putIntoActive(SimpleFlow flow) {
        Map<Integer, IntegerProperty> active = latestFlowsStat.computeIfAbsent(flow.sourceIP, k -> FXCollections.observableHashMap());
        IntegerProperty value;
        value = active.get(flow.port);

        if(value != null) {
            value.setValue(value.intValue() + 1);
        } else {
            value = new SimpleIntegerProperty(1);
            active.put(flow.port, value);
            //if (window != null && window.deviceIp == flow.sourceIP) {
            //    window.putActive(flow.port, value);
            //}
        }
    }

    private void putIntoEnded(SimpleFlow flow) {
        Map<Integer, IntegerProperty> ended = endedFlowsStat.computeIfAbsent(flow.sourceIP, k -> FXCollections.observableHashMap());
        Map<Integer, IntegerProperty> active = latestFlowsStat.get(flow.sourceIP);

        IntegerProperty value = ended.get(flow.port);

        if(value != null) {
            value.setValue(value.intValue() + 1);
        } else {
            value = new SimpleIntegerProperty(1);
            ended.put(flow.port, value);
            //if (window != null && window.deviceIp == flow.sourceIP) {
            //    window.putEnded(flow.port, value);
            //}
        }

        if (active == null)
            return;

        value = active.get(flow.port);
        if (value != null) {
            value.setValue(value.intValue() - 1);
            if (value.intValue() <= 0) {
                active.remove(flow.port);
                //if (window != null && window.deviceIp == flow.sourceIP) {
                //    window.removeActive(flow.port);
                //}
            }
        }
    }

    public void bind(NetFlowDialogController netFlowDialogController) {
        Sampler sampler = getSampler(netFlowDialogController.device.primaryIp, netFlowDialogController.device); // TODO: should be device
        sampler.bind(netFlowDialogController);
        //netFlowDialogController.activeChartData.clear();
        //netFlowDialogController.endedChartData.clear();
        //netFlowDialogController.trafficChart.getData().clear();
    }

    private static class Sampler implements Runnable {
        private final long startTimestamp;
        private int count = 0;
        private final Semaphore semaphore = new Semaphore(1);
        private final List<SimpleFlow> toAdd = new ArrayList<>();

        private final HashSet<SimpleFlow> latestFlows = new HashSet<>();
        private final SimpleCumulativeEMWA simpleCumulativeEMWA = new SimpleCumulativeEMWA();
        private NetFlowDialogController window;
        private Device device;

        // cached stats (there should be tail-drop to prevent leaks)
        private final Map<Long, Integer> stampedCounts = new HashMap<>();
        private final Map<Long, Integer> stampedDeltas = new HashMap<>();
        private final Map<Long, Boolean> stampedAlarms = new HashMap<>();
        private final Map<Integer, Integer> latestStatistics = new HashMap<>();
        private final Map<Integer, Integer> wholeStatistics = new HashMap<>();

        public Sampler(Device device) {
            this.device = device;
            startTimestamp = System.currentTimeMillis();
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();

                long timestamp = System.currentTimeMillis();

                int created = toAdd.size();
                for (SimpleFlow flow : toAdd) {
                    latestFlows.remove(flow);
                    latestFlows.add(flow);

                    Integer value = wholeStatistics.putIfAbsent(flow.port, 1);
                    if(value != null) {
                        wholeStatistics.put(flow.port, value + 1);
                    }

                    value = latestStatistics.putIfAbsent(flow.port, 1);
                    if(value != null) {
                        latestStatistics.put(flow.port, value + 1);
                    }
                    count++;
                }
                toAdd.clear();

                semaphore.release();

                Iterator<SimpleFlow> simpleFlowIterator = latestFlows.iterator();
                while (simpleFlowIterator.hasNext()) {
                    SimpleFlow flow = simpleFlowIterator.next();
                    flow.tick();
                    if (flow.expired) {
                        simpleFlowIterator.remove();
                        Integer value = latestStatistics.get(flow.port);
                        if(value != null) {
                            if (value > 1) {
                                latestStatistics.put(flow.port, value - 1);
                            } else {
                                latestStatistics.remove(flow.port);
                            }
                        }
                    }
                }
                boolean alarm = simpleCumulativeEMWA.put(created);
                stampedAlarms.put(timestamp, alarm);
                stampedCounts.put(timestamp, latestFlows.size());
                stampedDeltas.put(timestamp, created);

                if (window != null) {
                    window.updateTraffic((int) ((timestamp - startTimestamp) / 1000), latestFlows.size(), created, alarm, count, new HashMap<>(latestStatistics), new HashMap<>(wholeStatistics));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void add(SimpleFlow flow) throws InterruptedException {
            semaphore.acquire();
            toAdd.add(flow);
            semaphore.release();
        }

        public void bind(NetFlowDialogController netFlowDialogController) {
            this.window = netFlowDialogController;
        }
    }
}
