package cz.muni.fi.hotel;

import java.util.Collection;

/**
 * Created by User on 8.3.2017.
 */
public interface BookingManager {

    public void createBooking(Booking booking);

    public void deleteBooking(Booking booking);

    public void updateBooking(Booking booking);

    public Collection <Booking> findAllBookings();

    public Booking getBookingById(long id);

    public Collection <Booking> findAllBookingsOfGuest(Guest guest);

    public Collection <Booking> findAllBookingsOfRoom(Room room);


}
