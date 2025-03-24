package org.cloudbus.cloudsim.examples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;

public class JSONCloudletReader {
    public static List<CloudletData> readCloudletData() throws Exception {
        // Get the JSON file from the resources folder
        InputStream inputStream = JSONCloudletReader.class.getClassLoader().getResourceAsStream("profilingRuns.json");
        if (inputStream == null) {
            throw new Exception("Unable to find profilingRuns.json in resources folder.");
        }

        // Create an ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Convert JSON array into a list of CloudletData
        return mapper.readValue(inputStream, new TypeReference<List<CloudletData>>() {});
    }
}
