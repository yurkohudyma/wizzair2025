package ua.hudyma.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
@Data
public class Profile {

    private String name;
    private String surname;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date birthday;
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
    @ElementCollection
    @CollectionTable(name = "user_addresses", joinColumns = @JoinColumn(name = "user_id"))
    private List<Address> addressList = new ArrayList<>();
}
