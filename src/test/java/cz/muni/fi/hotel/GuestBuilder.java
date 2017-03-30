package cz.muni.fi.hotel;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author kkatanik & snagyova
 */
public class GuestBuilder {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;

    public GuestBuilder id(Long id){
        this.id = id;
        return this;
    }

    public GuestBuilder name(String name) {
        this.name = name;
        return this;
    }

    public GuestBuilder dateOfBirth(int year, Month month, int day) {
        this.dateOfBirth = LocalDate.of(year, month, day);
        return this;
    }

    public GuestBuilder phoneNumber(String phoneNumber) {
        this.phoneNumber=phoneNumber;
        return this;
    }

    public Guest build(){
        Guest guest = new Guest();
        guest.setDateOfBirth(dateOfBirth);
        guest.setPhoneNumber(phoneNumber);
        guest.setName(name);
        guest.setId(id);
        return guest;
    }


}
