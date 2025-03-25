package org.cloudbus.cloudsim.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.cloudbus.cloudsim.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CloudletReader {
    public static List<CloudletData> readCloudletData()  {
        // Get the JSON file from the resources folder
        InputStream inputStream = CloudletReader.class.getClassLoader().getResourceAsStream("profilingRuns.json");
        if (inputStream == null) {
            try {
                throw new Exception("Unable to find profilingRuns.json in resources folder.");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Convert JSON array into a list of CloudletData
        try {
            return mapper.readValue(inputStream, new TypeReference<List<CloudletData>>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
