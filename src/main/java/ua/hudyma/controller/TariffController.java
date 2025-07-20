package ua.hudyma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.hudyma.service.TariffService;

@RestController
@RequestMapping("/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;
}
