package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Device;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;

import java.net.URL;
import java.util.*;

public class NetFlowDialogController implements Initializable {
    @FXML
    PieChart activeChart;
    public ObservableList<PieChart.Data> activeChartData = FXCollections.observableArrayList();

    @FXML
    PieChart endedChart;
    public ObservableList<PieChart.Data> endedChartData = FXCollections.observableArrayList();

    @FXML
    LineChart<Number, Number> trafficChart2d;
    private NumberAxis xAxis;

    public Device device;

    // user-defined categories PORT: APP NAME; Otherwise it should be "OTHER" category
    Rules categories = new Rules();

    XYChart.Series<Number, Number> active = new XYChart.Series<>();
    XYChart.Series<Number, Number> delta = new XYChart.Series<>();
    XYChart.Series<Number, Number> anomaly = new XYChart.Series<>();

    public NetFlowDialogController(Device device) {
        this.device = device;
        categories.addRule(443, "Web");
        categories.addRule(80, "Web");
        categories.addRule(9996, "NetFlow");
        categories.addRule(21, "FTP");
        categories.addRule(22, "FTP");
        categories.addRule(23, "Telnet");
        categories.addRule(25, "SMTP");
        categories.addRule(54, "DNS");
        categories.addRule(161, "SNMP");
        categories.addRule(49152, 65535, "Applications");
        categories.addRule(6881, 6887, "BitTorrent");
        categories.addRule(6888, 6900, "BitTorrent");
        categories.addRule(6902, 6970, "BitTorrent");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        activeChart.setData(activeChartData);
        endedChart.setData(endedChartData);
        //trafficChart2d.setData(trafficData);

        trafficChart2d.setCreateSymbols(false);
        trafficChart2d.setAnimated(false);

        anomaly.setName("Anomaly");
        trafficChart2d.getData().add(anomaly);

        delta.setName("Delta");
        trafficChart2d.getData().add(delta);

        active.setName("Activity");
        trafficChart2d.getData().add(active);

        xAxis = (NumberAxis) trafficChart2d.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setAnimated(false);
        xAxis.setTickUnit(1);
        xAxis.setLabel("seconds");

        Application.netFlowController.bind(this);
    }

    //Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();

    // cached
    Map<String, PieChartDataWrapper> latestMap = new HashMap<>();
    Map<String, PieChart.Data> wholeMap = new HashMap<>();

    int samplingRange = 180;
    public void updateTraffic(int timestamp, int activeFlows, int createdFlows, boolean alarm, int wholeCount,
                              Map<Integer, Integer> latest, Map<Integer, Integer> whole) {
        Platform.runLater(() -> {
            xAxis.setLowerBound(Math.max(timestamp - 180, 0));
            xAxis.setUpperBound(Math.max(timestamp, 180));

            System.out.println(timestamp + " " + activeFlows + " " + createdFlows + " " + alarm);
            active.getData().add(new XYChart.Data<>(timestamp, activeFlows));
            delta.getData().add(new XYChart.Data<>(timestamp, createdFlows));

            if(alarm) {
                this.anomaly.getData().add(new XYChart.Data<>(timestamp - 0.1, 0));
                this.anomaly.getData().add(new XYChart.Data<>(timestamp, activeFlows + 2));
                this.anomaly.getData().add(new XYChart.Data<>(timestamp + 0.1, 0));

            }

            cutTraffic(active);
            cutTraffic(delta);
            cutTraffic(this.anomaly);

            activeChart.setTitle("Active: " + activeFlows);

            for (Map.Entry<Integer, Integer> flowStat : latest.entrySet()) {
                String key = categories.getKey(flowStat.getKey());
                PieChartDataWrapper wrapped = latestMap.get(key);
                if (wrapped == null) {
                    PieChart.Data data = new PieChart.Data(key, flowStat.getValue());
                    latestMap.put(key, new PieChartDataWrapper(data, timestamp));
                    activeChartData.add(data);
                } else {
                    wrapped.data.setPieValue(wrapped.data.getPieValue() + flowStat.getValue());
                    wrapped.timestamp = timestamp;
                }
            }

            Iterator<PieChartDataWrapper> iterator = latestMap.values().iterator();
            while (iterator.hasNext()) {
                PieChartDataWrapper wrapped = iterator.next();
                if(wrapped.timestamp != timestamp) {
                    System.out.println("deleted");
                    iterator.remove();
                    activeChartData.remove(wrapped.data);
                }
            }

            for (Map.Entry<Integer, Integer> flowStat : whole.entrySet()) {
                String key = categories.getKey(flowStat.getKey());
                PieChart.Data data = wholeMap.get(key);
                if (data == null) {
                    data = new PieChart.Data(key, flowStat.getValue());
                    wholeMap.put(key,data);
                    endedChartData.add(data);
                } else {
                    data.setPieValue(data.getPieValue() + flowStat.getValue());
                }
            }

            endedChart.setTitle("Ended: " + wholeCount);
        });
    }

    private void cutTraffic(XYChart.Series<Number, Number> series) {
        if (series.getData().size() > samplingRange + 10) {
            series.getData().remove(0);
        }
    }

    public static class PieChartDataWrapper {
        public PieChart.Data data;
        public int timestamp;

        public PieChartDataWrapper(PieChart.Data data, int timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }

    public static class Rules {
        private static String defaultCategory = "Other";
        private List<NamingRule> rules = new ArrayList<>();

        public void addRule(int value, String key) {
            addRule(value, value, key);
        }

        public void addRule(int min, int max, String key) {
            NamingRule rule = new NamingRule(min, max, key);

            int index = 0;
            for (int i = 0; i < rules.size(); i++) {
                if(!rule.isIn(min))
                    break;
                index++;
            }

            rules.add(index, rule);
        }

        public String getKey(int value) {
            for (NamingRule rule : rules) {
                if(rule.isIn(value)) {
                    return rule.key;
                }
            }

            return defaultCategory;
        }
    }

    public static class NamingRule {
        private int min;
        private int max;
        public String key;

        public NamingRule(int value, String key) {
            this(value, value, key);
        }

        public NamingRule(int min, int max, String key) {
            this.min = min;
            this.max = max;
            this.key = key;
        }

        public int range() {
            return 1 + max - min;
        }

        public boolean isIn(int value) {
            return value >= min && value <= max;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            NamingRule that = (NamingRule) o;

            if (key != null)
                if(key.equals(that.key)) return true;

            if (isIn(that.min)) return true;
            return (isIn(that.max));
        }

        @Override
        public int hashCode() {
            int result = min;
            result = 31 * result + max;
            return result;
        }
    }
}
