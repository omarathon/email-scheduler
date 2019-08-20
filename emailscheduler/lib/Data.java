/*
    Class responsible for I/O.
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler.lib;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.omartanner.emailscheduler.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Data {
    // Loads a JSON in the launch directory of the JAR
    private static Map<String, Object> loadProperties(String fileName) throws URISyntaxException, FileNotFoundException {
        File file = getRunningDirectoryFile().toPath().resolve(fileName).toFile();
        final Type DATA_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(file));
        return gson.fromJson(reader, DATA_TYPE);
    }

    // Loads the JSON  in the directory of the JAR with name "mainProperties.json"
    public static Map<String, Object> loadMainProperties() throws URISyntaxException, FileNotFoundException {
        return loadProperties("mainProperties.json");
    }

    // Loads the JSON  in the directory of the JAR with name "emailerProperties.json"
    public static Map<String, Object> loadEmailerProperties() throws URISyntaxException, FileNotFoundException {
        return loadProperties("emailerProperties.json");
    }

    // Constructs a Logger at the launch directory of the JAR with name `logName` and returns the instance. Note file is logs.log.
    public static Logger getNewLogger(String logName) throws URISyntaxException, IOException {
        Path path = getRunningDirectoryFile().toPath().resolve("logs.log");
        Logger log = Logger.getLogger(logName);
        // Configure the logger with handler and formatter
        FileHandler fh = new FileHandler(path.toString());
        log.addHandler(fh);
        return log;
    }

    // Obtains a File object of the running directory of the JAR
    private static File getRunningDirectoryFile() throws URISyntaxException {
        return new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile();
    }
}
