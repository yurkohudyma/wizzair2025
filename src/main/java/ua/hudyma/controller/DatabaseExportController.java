package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.DatabaseExportService;

@RestController
@RequestMapping("/dbase")
@RequiredArgsConstructor
@Log4j2
public class DatabaseExportController {
    private final DatabaseExportService exportService;

    @PostMapping("/export") public ResponseEntity<String> exportDbase (){
        return ResponseEntity.ok(exportService.exportDatabase());
    }

    @PostMapping("/repair")
    public String triggerRepair() {
        return exportService.repair();
    }
}
