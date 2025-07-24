package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Airplane.AirplaneType;
import ua.hudyma.domain.SeatSelection;
import ua.hudyma.repository.FlightRepository;
import ua.hudyma.repository.SeatSelectionRepository;

@Service
@RequiredArgsConstructor
@Log4j2

public class SeatSelectionService {

    private final SeatSelectionRepository seatSelectionRepository;
    private final FlightRepository flightRepository;

    @Transactional
    public String[][] getSeatMap(String flightNumber) {
        var flight = flightRepository
                .findByFlightNumber(flightNumber).orElseThrow();
        var map = prepareSeatMap(
                flight.getAirplane().getType());
        var newSelection = new SeatSelection();
        newSelection.setSeatMap(map);
        seatSelectionRepository.save(newSelection);
        flight.setSeatSelection(newSelection);
        return map;
    }

    private String[][] prepareSeatMap(AirplaneType airplaneType) {
        if (airplaneType != null) {
            var seats = airplaneType.getSeatsQuantity();
            var seatInRow = airplaneType.getSeatInRow();
            var rows = seats / seatInRow;
            var seatMap = new String[rows][seatInRow];
            for (int row = 0; row < rows; row++) {
                for (int seatt = 0; seatt < seatInRow; seatt++) {
                    seatMap[row][seatt] = row + 1 + "" + (char) (seatt + 65);
                }
            }
            return seatMap;
        }
        throw new IllegalArgumentException("airplane is NULL");
    }


    public void save(SeatSelection seatSelection) {
        seatSelectionRepository.save(seatSelection);
    }
}
