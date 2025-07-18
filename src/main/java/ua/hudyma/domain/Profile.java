package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.util.Date;

@Embeddable
@Data
public class Profile {

    private String name;
    private String surname;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthday;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @CreationTimestamp
    private Date registeredOn;
    @JsonFormat(pattern = "dd-MM-yyyy")
    @UpdateTimestamp
    private Date updatedOn;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    private String phoneNumber;

}
