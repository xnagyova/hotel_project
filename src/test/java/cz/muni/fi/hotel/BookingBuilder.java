package cz.muni.fi.hotel;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;

/**
 * @author kkatanik & snagyova
 */
public class BookingBuilder {
    private Long id;
    private int price;
    private Room room;
    private Guest guest;
    private Date arrivalDate;
    private Date departureDate;

    public BookingBuilder id(Long id){
        this.id = id;
        return this;
    }

    public BookingBuilder price(int price){
        this.price = price;
        return this;
    }

    public BookingBuilder room(Room room){
        this.room = room;
        return this;
    }

    public BookingBuilder guest(Guest guest){
        this.guest = guest;
        return this;
    }

    public BookingBuilder arrivalDate(int year, int month, int day) {
        this.arrivalDate = new Date(year, month, day);
        return this;
    }

    public BookingBuilder departureDate(int year,int month, int day) {
        this.departureDate = new Date(year, month, day);
        return this;
    }


    public Booking build(){
        Booking booking = new Booking();
        booking.setId(id);
        booking.setPrice(price);
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setArrivalDate(arrivalDate);
        booking.setDepartureDate(departureDate);
        return booking;
    }
}
