package ua.hudyma.dto;

import ua.hudyma.domain.Tariff.TariffType;

public record TariffDto (
        TariffType tariffType,
        boolean wizzFlex,
        boolean wizzPriority,
        boolean autoOnlineRegistration,
        boolean airportRegistration,
        long voucherId,
        long discountId) {}
