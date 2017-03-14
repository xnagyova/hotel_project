package cz.muni.fi.hotel;

import java.time.LocalDate;
import java.util.Date;

import static java.lang.Long.compare;

/**
 * Created by User on 8.3.2017.
 */
public class Guest  {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;

    public Guest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Guest guest = (Guest) o;

        return id == guest.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


/*
    @Override
    public int compareTo(Guest o) {
        return compare(this.id,o.getId());
    }
    */
}
