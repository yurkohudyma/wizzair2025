package ua.hudyma.dto;

import org.jetbrains.annotations.NotNull;
import ua.hudyma.domain.Voucher.VoucherAmount;
import ua.hudyma.domain.Voucher.VoucherType;
import ua.hudyma.enums.VoucherCurrency;

public record VoucherRequestDto(
        @NotNull VoucherType voucherType,
        @NotNull VoucherAmount voucherAmount,
        @NotNull VoucherCurrency voucherCurrency) {}
