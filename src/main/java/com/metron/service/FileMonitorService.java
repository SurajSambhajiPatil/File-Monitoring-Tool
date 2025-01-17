package com.metron.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileMonitorService {
    private static	 final Logger logger = LoggerFactory.getLogger(FileMonitorService.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<String> monitoredExtensions = List.of("pdf", "docx", ".jpg");

    @Autowired
    private ElasticSearchService elasticSearchService;

    public void startMonitoring(String folderPath) {
        logger.info("Starting to monitor directory: " + folderPath);
        executor.submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path path = Paths.get(folderPath);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                registerSubdirectories(path, watchService);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filePath = path.resolve((Path) event.context());
                            handleFile(filePath.toString());
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                logger.error("Error occurred while monitoring the folder: ", e);
            }
        });
    }

    private void registerSubdirectories(Path path, WatchService watchService) throws IOException {
        Files.walk(path)
            .filter(Files::isDirectory)
            .forEach(subPath -> {
                try {
                    subPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                } catch (IOException e) {
                    logger.error("Error occurred while registering subdirectory: " + subPath, e);
                }
            });
    }

    private void handleFile(String filePath) {
        logger.info("File detected: " + filePath);
        for (String ext : monitoredExtensions) {
            if (filePath.endsWith(ext)) {
                logger.info("File matches monitored extension: " + ext);
                elasticSearchService.sendAlert(filePath);
            }
        }
    }

    public void stopMonitoring() {
        logger.info("Stopping the file monitoring service.");
        executor.shutdown();
    }

    public void setMonitoredExtensions(List<String> extensions) {
        this.monitoredExtensions = extensions;
    }
}
