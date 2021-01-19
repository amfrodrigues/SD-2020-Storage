import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StorageMain {
    public static void main(String[] args){
        Registry r = null;
        StorageServiceInterface storageService;
        Integer port = Integer.parseInt(args[0]);
        try{
            r = LocateRegistry.createRegistry(port);
        }catch(RemoteException a){
            a.printStackTrace();
        }

        try{
            storageService = new StorageService();
            r.rebind("storageservice", storageService);
            storageService.LoadFiles("www_nytimes_com");
            System.out.println("Storage fileCount = "+storageService.getFileCount());
            System.out.println("Storage ready port: "+port);
        }catch(Exception e) {
            System.out.println("Storage main " + e.getMessage());
        }
    }
}
