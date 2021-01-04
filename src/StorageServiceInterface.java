import harreader.HarReaderException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public interface StorageServiceInterface extends Remote {
    void LoadFiles(String filename) throws RemoteException;
    boolean sendData(String filename, byte[] data, int len) throws RemoteException;
}
