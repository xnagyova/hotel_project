package cz.muni.fi.hotel;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;

/**
 * @author kkatanik & snagyova
 */
public class BookingBuilder {

    private int price;
    private Room room;
    private Guest guest;
    private LocalDate arrivalDate;
    private LocalDate departureDate;


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

    public BookingBuilder arrivalDate(int year, Month month, int day) {
        this.arrivalDate = LocalDate.of(year,month,day);
        return this;
    }

    public BookingBuilder departureDate(int year,Month month, int day) {
        this.departureDate = LocalDate.of(year, month, day);
        return this;
    }


    public Booking build(){
        Booking booking = new Booking();
        booking.setPrice(price);
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setArrivalDate(arrivalDate);
        booking.setDepartureDate(departureDate);
        return booking;
    }
}
