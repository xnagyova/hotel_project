package cz.muni.fi.hotel;


import java.time.LocalDate;


/**
 * @author kkatanik & snagyova
 */
public class Guest  {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String phoneNumber;

    public Guest() {
    }

    public Guest(Long id, String name,LocalDate dateOfBirth, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
