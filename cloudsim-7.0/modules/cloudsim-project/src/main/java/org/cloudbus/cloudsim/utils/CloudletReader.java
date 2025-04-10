package org.cloudbus.cloudsim.utils;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.models.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CloudletReader {
    public static List<CloudletData> readCloudletData()  {
        List<CloudletData> cloudletDataList = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            InputStream inputStream = CloudletReader.class.getClassLoader().getResourceAsStream("simpleTasks.json");

            if (inputStream == null) {
                throw new FileNotFoundException("simpleTasks.json not found in classpath");
            }

            InputStreamReader reader = new InputStreamReader(inputStream);
            JSONArray jsonArray = (JSONArray) parser.parse(reader);

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;

                int id = ((Long) jsonObject.get("id")).intValue();
                long length = (Long) jsonObject.get("length");
                int pesNumber = 1; // TODO: set pesNumber based on tier
                long fileSize = (Long) jsonObject.get("fileSize");
                long outputSize = (Long) jsonObject.get("outputSize");
                UtilizationModel utilizationCpu = new UtilizationModelFull();
                UtilizationModel utilizationRam = new UtilizationModelFull();
                UtilizationModel utilizationBw = new UtilizationModelFull();

                cloudletDataList.add(new CloudletData(id, length, pesNumber, fileSize,
                        outputSize, utilizationCpu, utilizationRam, utilizationBw));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloudletDataList;
    }
}
