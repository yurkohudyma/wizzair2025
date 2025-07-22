package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DatabaseExportService {

    private final Flyway flyway;

    @Value("${spring.datasource.dbName}")
    private String dbName;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.mysqldump-path}")
    private String mysqldumpPath;

    public String exportDatabase() {
        try {
            var currentMigration = flyway.info().current();
            if (currentMigration == null) {
                log.warn("No migration applied yet");
                return "No migrations applied yet";
            }
            int version = Integer.parseInt(currentMigration.getVersion().toString()) + 1;
            final String filePath = "src/main/resources/db/migration/V" + version + "__db_export.sql" ;
            List<String> command = List.of(
                    mysqldumpPath,
                    "-u" + username,
                    "-p" + password,
                    "--databases", dbName,
                    "--ignore-table=" + dbName + ".flyway_schema_history"
            );
            var pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            var process = pb.start();
            log.debug("Executing command: {}", String.join(" ", command));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("✅ SQL dump created successfully: {}", filePath);
                return "Dump created: " + filePath;
            } else {
                log.error("❌ mysqldump failed with exit code: {}", exitCode);
                return "mysqldump failed with exit code: " + exitCode;
            }
        } catch (IOException | InterruptedException e) {
            log.error("❌ Error during export", e);
            return "Error: " + e.getMessage();
        }
    }
}