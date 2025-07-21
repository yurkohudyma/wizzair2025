package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Payment;
import ua.hudyma.dto.EffectPaymentDto;
import ua.hudyma.exception.InvalidTopUpException;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.PaymentRepository;
import ua.hudyma.repository.UserRepository;

import static ua.hudyma.domain.Payment.PaymentStatus.*;
import static ua.hudyma.util.IdGenerator.generateId;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentDTO addPayment(String confirmationCode) {
        var newPayment = new Payment();
        var booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow();
        newPayment.setBooking(booking);
        newPayment.setAmount(booking.getPrice());
        newPayment.setPaymentStatus(PENDING);
        booking.getPaymentList().add(newPayment);
        newPayment.setPaymentId(generateId(8));
        return PaymentDTO.from(paymentRepository.save(newPayment));
    }

    @Transactional
    public boolean checkBalanceAndApplyPayment(EffectPaymentDto dto) {
        var payment = paymentRepository
                .findByPaymentId(dto.paymentId()).orElseThrow();
        var mainUser = userRepository.findByUserId(dto.mainUserId()).orElseThrow();
        var balance = mainUser.getBalance();
        var charge = payment.getAmount();
        var mail = mainUser.getProfile().getEmail();
        if (balance.compareTo(charge) > 0) {
            mainUser.setBalance(balance.subtract(charge));
            log.info("User {} balance has been charged at {} euro",
                    mail, charge);
            payment.setPaymentStatus(COMPLETE);
            return true;
        } else {
            log.error("Low balance for user {}, replenish first", mail);
            payment.setPaymentStatus(REJECTED);
            return false;
        }
    }

    @Transactional
    public boolean replenishUserBalance(EffectPaymentDto dto) {
        var mainUser = userRepository.findByUserId(dto.mainUserId()).orElseThrow();
        try {
            mainUser.setBalance(mainUser.getBalance().add(dto.charge()));
        } catch (Exception e) {
            throw new InvalidTopUpException(e);
        }
        log.info("user {} balance has been replenished at {} euro",
                mainUser.getProfile().getEmail(), dto.charge());
        return true;
    }
}
