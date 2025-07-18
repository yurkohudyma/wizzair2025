package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.Airplane;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.repository.AirplaneRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AirplaneService {
    private final AirplaneRepository airplaneRepository;

    @Cacheable(value = "planes", key = "'ALL'", unless = "#result == null || #result.isEmpty()")
    public List<Airplane> getAll() {
        return airplaneRepository.findAll();
    }

    public Airplane getByType(AirplaneType type) {
        return airplaneRepository.findByType (type);
    }

    public ResponseEntity<String> saveAllPlanes(Airplane[] airplanes) {
        List<Airplane> planeList = new ArrayList<>();
        for (Airplane plane : airplanes) {
            if (plane.getType() == null) {
                throw new IllegalArgumentException("Airplane type must not be null");
            }
            plane.setSeatsQuantity(plane.getType().getSeatsQuantity());
            planeList.add(plane);
        }
        // Якщо робити foreach.repository::save, то буде N звернень до БД.
        airplaneRepository.saveAll(planeList);
        return ResponseEntity.status(HttpStatus.CREATED).body("Saved " + planeList.size() + " airplanes");
    }

}
