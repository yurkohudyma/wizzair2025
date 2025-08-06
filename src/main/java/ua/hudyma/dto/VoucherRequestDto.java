package ua.hudyma.dto;

import org.jetbrains.annotations.NotNull;
import ua.hudyma.domain.Voucher.VoucherAmount;
import ua.hudyma.domain.Voucher.VoucherCurrency;
import ua.hudyma.domain.Voucher.VoucherType;

public record VoucherRequestDto(
        @NotNull Long tariffId,
        @NotNull VoucherType voucherType,
        @NotNull VoucherAmount voucherAmount,
        @NotNull VoucherCurrency voucherCurrency) {}
