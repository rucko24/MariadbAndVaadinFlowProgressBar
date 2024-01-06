package com.mariadb.columnstoredemo.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * @author rubn
 *
 * This will simply store the total processing time in the resources directory.
 */
@Service
public class LoggingTotalTimeService {

    public void loggingTotalTime(final int batchSize, final int batches, String totalTimeResult) {
        final var path = Path.of("src/main/resources/totaltime.txt");
        try (var bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write("Total time=" + totalTimeResult);
            bufferedWriter.write("\nBatchSize=" + batchSize);
            bufferedWriter.write("\nBatches=" + batches);
            bufferedWriter.write("\nFinished At=" + LocalDateTime.now());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
