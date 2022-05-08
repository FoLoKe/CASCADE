package com.foloke.cascade.Controllers;

import com.foloke.cascade.Application;
import com.foloke.cascade.Entities.Device;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class NetFlowDialogController implements Initializable {
    @FXML
    PieChart activeChart;
    public ObservableList<PieChart.Data> activeChartData = FXCollections.observableArrayList();

    @FXML
    PieChart endedChart;
    public ObservableList<PieChart.Data> endedChartData = FXCollections.observableArrayList();

    @FXML
    StackedAreaChart<Integer, Integer> trafficChart;
    public ObservableList<XYChart.Series<Integer, Integer>> trafficData = FXCollections.observableArrayList();

    public int deviceIp;

    public NetFlowDialogController(Device device) {
        this.deviceIp = device.primaryIp;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        activeChart.setData(activeChartData);
        endedChart.setData(endedChartData);
        trafficChart.setData(trafficData);

        trafficChart.setCreateSymbols(false);
        trafficChart.setAnimated(false);
        ((ValueAxis<Integer>)trafficChart.getXAxis()).setLowerBound(counter);

        Application.netFlowController.bind(this);
    }

    int counter = 1;
    Map<Integer, XYChart.Series<Integer, Integer>> seriesMap = new HashMap<>();
    public void updateTraffic(Map<Integer, IntegerProperty> traffic) {
        Platform.runLater(() -> {
            int activeCount = 0;
            for (Map.Entry<Integer, IntegerProperty> trafficEntry : traffic.entrySet()) {
                XYChart.Series<Integer, Integer> series = seriesMap.get(trafficEntry.getKey());
                if(series == null) {
                    series = new XYChart.Series<>();
                    series.setName(trafficEntry.getKey() + "");
                    seriesMap.put(trafficEntry.getKey(), series);
                    series.getData().add(new XYChart.Data<>(counter - 1, 0));
                    trafficData.add(series);
                } else {
                    if(series.getData().size() > 1) {
                        XYChart.Data<Integer, Integer> last = series.getData().get(series.getData().size() - 2);
                        XYChart.Data<Integer, Integer> prev = series.getData().get(series.getData().size() - 1);
                        if (prev != null && prev.getYValue() == trafficEntry.getValue().intValue()) {
                            series.getData().remove(series.getData().size() - 1);
                        }
                    }
                }
                if(trafficEntry.getValue().intValue() <= 0) {
                    System.out.println("ERROR");
                }

                series.getData().add(new XYChart.Data<>(counter, trafficEntry.getValue().intValue()));
                activeCount += trafficEntry.getValue().intValue();
            }

            for (XYChart.Series<Integer, Integer> series : seriesMap.values()) {
                series.getData().removeIf(data -> data.getXValue() < counter - 10);
                //series.getData().g
                System.out.println("dataSize:" + series.getData().size());
            }

            activeChart.setTitle("Active: " + activeCount);
            //((ValueAxis<Integer>)trafficChart.getXAxis()).setLowerBound(counter);
            //((ValueAxis<Integer>)trafficChart.getXAxis()).setUpperBound(counter);
            counter++;
        });
    }

    public void putEnded(int port, IntegerProperty prop) {
        PieChart.Data data = new PieChart.Data(port + "", 0);
        data.pieValueProperty().bind(prop);
        Platform.runLater(() -> {
            endedChartData.add(data);
            endedChart.setTitle("Ended: " + endedChartData.size());
        });
    }

    public void removeActive(int port) {
        String name = port + "";
        Platform.runLater(() -> System.out.println(activeChartData.removeIf(d -> d.getName().equals(name))));
    }

    public void putActive(int port, IntegerProperty prop) {
        PieChart.Data data = new PieChart.Data(port + "", 0);
        data.pieValueProperty().bind(prop);
        Platform.runLater(() -> {
            activeChartData.add(data);
        });
    }
}
