package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.models.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CloudletReader {
    public static List<CloudletData> readCloudletData()  {
        String fileName = "simpleTasks.json";
        List<CloudletData> cloudletDataList = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(fileName));

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;

                int id = ((Long) jsonObject.get("id")).intValue();
                long length = (Long) jsonObject.get("length");
                int pesNumber = 1; // TODO: set pesNumber based on tier
                long fileSize = (Long) jsonObject.get("fileSize");
                long outputSize = (Long) jsonObject.get("outputSize");
                UtilizationModel utilizationCpu = (UtilizationModel) jsonObject.get("utilizationCpu");
                UtilizationModel utilizationRam = (UtilizationModel) jsonObject.get("utilizationRam");
                UtilizationModel utilizationBw  = (UtilizationModel) jsonObject.get("utilizationBw");

                cloudletDataList.add(new CloudletData(id, length, pesNumber, fileSize, outputSize, utilizationCpu, utilizationRam, utilizationBw));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloudletDataList;
    }
}
