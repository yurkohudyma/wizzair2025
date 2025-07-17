package ua.hudyma.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Address {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
