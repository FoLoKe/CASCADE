package com.foloke.cascade.utils;


import java.io.*;

public class FileUtils {
    public static void writeToFile(BufferedWriter bufferedWriter, String string) {
        Writer writer = new Writer(bufferedWriter, string);
        Thread thread = new Thread(writer);
        thread.start();
    }

    private static class Writer implements Runnable {
        final BufferedWriter bufferedWriter;
        String string;

        public Writer(BufferedWriter writer, String s) {
            this.bufferedWriter = writer;
            this.string = s;
        }

        @Override
        public void run() {
            synchronized (bufferedWriter) {
                try {
                    bufferedWriter.write(string);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (Exception e) {
                    LogUtils.log(e.toString());
                }
            }
        }
    }

    public static void save(String saveName) {
        try {
            File mapDir = new File("maps");
            mapDir.mkdir();

            File saveFile = new File("maps\\" + saveName);
            saveFile.createNewFile();

            FileWriter writer = new FileWriter(saveFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            LogUtils.log("saving map " + saveName);

//            for (Entity entity : mapController.getEntities()) {
//                //bufferedWriter.write(entity.getSave());
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
//            }

            bufferedWriter.close();
            writer.close();

            int a = 0;
        } catch (IOException e) {
            LogUtils.log(e.toString());
        }
    }

    public static void load(String saveName) {
//        for(Entity entity : mapController.getEntities()) {
//            entity.destroy();
//        }
//        mapController.deselect();
//
//        File saveFile = new File("maps\\" + saveName);
//
//        try {
//            FileReader fileReader = new FileReader(saveFile);
//            BufferedReader bufferedReader = new BufferedReader(fileReader);
//
//            ArrayList<Port> ports = new ArrayList<>();
//            ArrayList<Cable.Connector> connectors = new ArrayList<>();
//            ArrayList<Entity> grouping = new ArrayList<>();
//
//            String line = null;
//            Device lastDevice = null;
//            Cable lastCable = null;
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] params = line.split(" ");
//                if(params[0].equals("DEVICE")) {
////                    lastDevice = new Device(mapController, params);
////                    mapController.addEntity(lastDevice);
////                    grouping.add(lastDevice);
//                } else if (params[0].equals("PORT") && lastDevice != null) {
////                    Port port = new Port(lastDevice, params);
////                    lastDevice.addPort(port);
////                    ports.add(port);
//                } else if (params[0].equals("CABLE")) {
////                    lastCable = new Cable(mapController, params);
////                    mapController.addEntity(lastCable);
//                } else if (params[0].equals("CONNECTOR") && lastCable != null) {
////                    Cable.Connector connector = new Cable.Connector(lastCable, params);
////                    if(lastCable.connectorA == null) {
////                        lastCable.connectorA = connector;
////                    } else {
////                        lastCable.connectorB = connector;
////                    }
////                    connectors.add(connector);
//                } else if (params[0].equals("GROUP")) {
////                    Group group = new Group(mapController, params);
////                    mapController.addEntity(group);
////                    grouping.add(group);
//                }
//            }
//            for (Cable.Connector connector: connectors) {
//                for (Port port : ports) {
//                    if(connector.getConnectionID() == port.getID()) {
//                        connector.connect(port);
//                        break;
//                    }
//                }
//            }
//
//            for (Entity entity : grouping) {
//                if(entity instanceof Group) {
//                    String[] stringIDs = ((Group)entity).ids.split("\\.");
//                    ArrayList<Long> ids= new ArrayList<>();
//                    for(String string : stringIDs) {
//                       ids.add(Long.parseLong(string));
//                    }
//                    for(Entity toAdd : grouping) {
//                        if(ids.contains(toAdd.ID)) {
//                            ((Group)entity).addToGroup(toAdd);
//                        }
//                    }
//                }
//            }
//
//            bufferedReader.close();
//            fileReader.close();
//        } catch (Exception e) {
//            LogUtils.log(e.toString());
//        }
    }
}
