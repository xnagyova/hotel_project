package cz.muni.fi.hotel;

import java.util.Collection;
import java.util.List;

/**
 * @author kkatanik & snagyova
 */
public interface BookingManager {

    public void createBooking(Booking booking);

    public void deleteBooking(Booking booking);

    public void updateBooking(Booking booking);

    public List<Booking> findAllBookings();

    public Booking getBookingById(Long id);

    public List <Booking> findAllBookingsOfGuest(Guest guest);

    public List <Booking> findAllBookingsOfRoom(Room room);




}
