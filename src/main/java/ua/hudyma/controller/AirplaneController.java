package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.hudyma.domain.Airplane;
import ua.hudyma.service.AirplaneService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/planes")
public class AirplaneController {
    private final AirplaneService airplaneService;

    @PostMapping("/addAll")
    public ResponseEntity<String> addAllPlanes (@RequestBody Airplane [] airplanes){
        return airplaneService.saveAllPlanes (airplanes);
    }

    @GetMapping
    public List<Airplane> getAllPlanes (){
        return airplaneService.getAll();
    }
}
