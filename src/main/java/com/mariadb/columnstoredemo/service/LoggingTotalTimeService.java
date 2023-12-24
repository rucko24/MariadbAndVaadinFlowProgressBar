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

    public void loggingTotalTime(final int bathSize, final int bacthes, String totalTimeResult) {
        try (var bufferedWriter = Files.newBufferedWriter(Path.of("src/main/resources/totaltime.txt"))) {
            bufferedWriter.write("Total time=" + totalTimeResult);
            bufferedWriter.write("\nBathSize=" + bathSize);
            bufferedWriter.write("\nBathes=" + bacthes);
            bufferedWriter.write("\nFinished At=" + LocalDateTime.now());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
