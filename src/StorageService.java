//package harreader imports

import harreader.HarReader;
import harreader.model.Har;
import harreader.model.HarEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class StorageService extends UnicastRemoteObject implements StorageServiceInterface, Serializable {
    //variables
    private final String path_saved_files = ".\\received\\";
    private LinkedHashMap<String,ArrayList<ResourceInfo>> timeHarMap = new LinkedHashMap<>();
    private LinkedList<ProcessCombinationModel> combinationStatistics = new LinkedList<ProcessCombinationModel>();
    private int fileCount;


    //constructor
    public StorageService() throws RemoteException {
    }

    //interface function
    @Override
    public boolean sendData(String filename, byte[] data, int len) throws RemoteException {

        try{
            File f = new File(path_saved_files+filename+ ".har");
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f,true);
            out.write(data,0,len);
            out.flush();
            out.close();
            System.out.println("STORAGE: "+"Done writing data...");
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void LoadFiles(String filename) throws RemoteException {
        try {
            this.fileCount = FillResourcesMap(path_saved_files, filename, this.timeHarMap);
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("STORAGE: HashMapSize="+ timeHarMap.size());
    }

    @Override
    public LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException {
        return this.timeHarMap;
    }

    @Override
    public int getcombinationsStatisticsize() throws RemoteException { return this.combinationStatistics.size(); }

    @Override
    public void clearCombStatistics() throws RemoteException {
        this.combinationStatistics.clear();
    }

    @Override
    public LinkedList<ProcessCombinationModel> getcombinationsStatistic() {
        System.out.println("Giving combinationStatistics"); return combinationStatistics;
    }

    @Override
    public int getFileCount() throws RemoteException { return this.fileCount; }

    @Override
    public void addcombinationsStatistic(ArrayList<ProcessCombinationModel >combinationInfo) {
        combinationStatistics.addAll(combinationInfo);
    }


    //private functions
    private int FillResourcesMap(String path, String fileName, LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap)
    {
        //private functions
        int fileCount = 0;
        try {
            HarReader harReader = new HarReader();
            File file = new File(path + fileName + ".har");
            while (file.exists()) {
                Har otherHar = harReader.readFromFile(file);
                for (HarEntry otherEntry : otherHar.getLog().getEntries()) {
                    if (!otherEntry.getResponse().getHeaders().get(0).getValue().contains("no-cache")) {
                        ResourceInfo resourceInfo = new ResourceInfo();
                        resourceInfo.resourceTime = (float) otherEntry.getTime();
                        resourceInfo.resourceType = otherEntry.get_resourceType();
                        resourceInfo.cachedResource = otherEntry.getResponse().getHeaders().get(0).getValue();
                        resourceInfo.resourceLength = otherEntry.getResponse().getBodySize();
                        resourceInfo.harRun = fileCount;

                        if (timeHarMap.containsKey(otherEntry.getRequest().getUrl())) {
                            ArrayList<ResourceInfo> list = timeHarMap.get(otherEntry.getRequest().getUrl());
                            AtomicBoolean repeatedCall = new AtomicBoolean(false);
                            list.forEach(value -> {
                                if (value.resourceTime.equals(resourceInfo.resourceTime)) {
                                    repeatedCall.set(true);
                                }
                            });
                            if (!repeatedCall.get()) {
                                timeHarMap.get(otherEntry.getRequest().getUrl()).add(resourceInfo);
                            }
                        } else {
                            ArrayList<ResourceInfo> l = new ArrayList<>();
                            l.add(resourceInfo);
                            timeHarMap.put(otherEntry.getRequest().getUrl(), l);
                        }

                    }
                }
                file = new File(path + fileName + "_" + ++fileCount + ".har");
            }
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
        return fileCount;
    }

}
