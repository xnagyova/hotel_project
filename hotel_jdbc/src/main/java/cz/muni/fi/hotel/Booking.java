package cz.muni.fi.hotel;

import java.time.LocalDate;

/**
 * @author kkatanik & snagyova
 */
public class Booking {

    private Long id;
    private int price;
    private Room room;
    private Guest guest;
    private LocalDate arrivalDate;
    private LocalDate departureDate;


    public Booking() {}

    public Booking(Long id, int price, Room room, Guest guest, LocalDate arrivalDate, LocalDate departureDate) {
        this.id = id;
        this.price = price;
        this.room = room;
        this.guest = guest;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Booking booking = (Booking) o;

        if (price != booking.price) return false;
        if (room != null ? !room.equals(booking.room) : booking.room != null) return false;
        if (guest != null ? !guest.equals(booking.guest) : booking.guest != null) return false;
        if (arrivalDate != null ? !arrivalDate.equals(booking.arrivalDate) : booking.arrivalDate != null) return false;
        return departureDate != null ? departureDate.equals(booking.departureDate) : booking.departureDate == null;
    }

    @Override
    public int hashCode() {
        int result = price;
        result = 31 * result + (room != null ? room.hashCode() : 0);
        result = 31 * result + (guest != null ? guest.hashCode() : 0);
        result = 31 * result + (arrivalDate != null ? arrivalDate.hashCode() : 0);
        result = 31 * result + (departureDate != null ? departureDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "price=" + price +
                ", room=" + room +
                ", guest=" + guest +
                ", arrivalDate=" + arrivalDate +
                ", departureDate=" + departureDate +
                '}';
    }
}
