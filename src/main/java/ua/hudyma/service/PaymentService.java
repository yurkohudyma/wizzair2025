package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Payment;
import ua.hudyma.domain.Payment.PaymentStatus;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public PaymentDTO addPayment(String confirmationCode) {
        var newPayment = new Payment();
        var booking = bookingRepository
                .findByConfirmationCode(confirmationCode)
                .orElseThrow();
        newPayment.setBooking(booking);
        newPayment.setAmount(booking.getPrice());
        newPayment.setPaymentStatus(PaymentStatus.PENDING);
        booking.getPaymentList().add(newPayment);
        return PaymentDTO.from(paymentRepository.save(newPayment));
    }
}
