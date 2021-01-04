//package harreader imports

import harreader.HarReader;
import harreader.HarReaderException;
import harreader.model.Har;
import harreader.model.HarEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StorageService extends UnicastRemoteObject implements StorageServiceInterface{

    private final String path_saved_files = ".\\received\\";
    private LinkedHashMap<String,ArrayList<ResourceInfo>> timeHashMap;
    //constructor
    public StorageService() throws RemoteException {
        this.timeHashMap = new LinkedHashMap<>();
    }

    private void FillResourcesMap(String path, String fileName, LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap) throws HarReaderException {
        //private functions
        int[] count = new int[]{0};
        int fileCount = 0;
        try {
            HarReader harReader = new HarReader();
            File file = new File(path + fileName + ".har");
            System.out.println("STORAGE: "+"Vou carregar ficheiro");
            while (file.exists()) {
                Har otherHar = harReader.readFromFile(file);
                System.out.println("STORAGE: "+"FICHEIRO EXISTE");
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
                                    return;
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
        System.out.println("STORAGE: "+timeHashMap.size());

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
            FillResourcesMap(path_saved_files, filename, this.timeHashMap);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

}
