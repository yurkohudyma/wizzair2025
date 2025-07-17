package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String confirmationCode;


     //todo обмежити дублікати бронювань на одного юзера на один рейс -
    //todo ОСКІЛЬКИ бронювання іменні на головного учасника,
    // todo він не може оформити бронювання на ще когось окрім себе,
    //  todo а летіти на двох місцях він не може
     /**Якщо Booking має зв’язок User ↔ Booking ↔ Flight, і це зв’язок @ManyToMany, ти не зможеш напряму зробити унікальний індекс. Але можна ввести окрему таблицю-зв’язок і зробити унікальний складений ключ:
     🔧 Кроки:
    Замість @ManyToMany використай окрему Entity UserBooking
    В ній вказати @ManyToOne на User і Booking
    Додати унікальний індекс по (user_id, flight_id)
      */

    @ManyToMany
    @JoinTable(
            name = "user_booking",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> userList = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;
    @Positive
    @NotNull
    private BigDecimal price;

    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date createdOn;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;

    @Enumerated(value = EnumType.STRING)
    private BookingStatus status;

    @OneToMany(mappedBy = "booking")
    private List<Payment> paymentList = new ArrayList<>();


    private enum BookingStatus {
        CONFIRMED,  /** user has approved the flight selection with fixed price */
        PAID,       /** flight price has been paid in full */
        CANCELED,   /** booking has been canceled by WIZZAIR, but STILL is not REBOOKED or REFUNDED */
        REFUNDED,   /** flight has been canceled by USER or WZZ and been refunded if available */
        REBOOKED,   /** flight has been canceled by WZZ and been rebooked by USER */
        RESCHEDULED /** flight has been canceled by WZZ and been rescheduled by USER or SYSTEM */
    }

}
