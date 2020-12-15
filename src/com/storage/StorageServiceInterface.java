package com.storage;

import harreader.HarReaderException;

import java.rmi.Remote;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface StorageServiceInterface extends Remote {
    void FillResourcesMap(String path, String fileName, LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap) throws HarReaderException;
}
