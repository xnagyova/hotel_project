package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.*;

import jdk.nashorn.internal.ir.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;



/**
 * @author kkatanik & snagyova
 */
public class BookingManagerImpl implements BookingManager {

    private JdbcTemplate jdbc;
    private RoomManager roomManager;
    private GuestManager guestManager;

    public BookingManagerImpl(DataSource ds){
        jdbc = new JdbcTemplate(ds);
    }

    public void setRoomManager(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public void setGuestManager(GuestManager guestManager) {
        this.guestManager = guestManager;
    }

    @Override
    public List<Booking> findAllBookingsOfGuest(final Guest guest) {
        return jdbc.query("SELECT * FROM bookings WHERE guestId=?", bookingMapper, guest.getId());

    }

    @Override
    public List<Booking> findAllBookingsOfRoom(final Room room) {
        return jdbc.query("SELECT * FROM bookings WHERE roomId=?", bookingMapper, room.getId());
    }

    @Override
    public List<Booking> findAllBookings() {

        return jdbc.query("SELECT * FROM bookings", bookingMapper);

    }


    @Override
    public Booking getBookingById(Long id) {
        return jdbc.queryForObject("SELECT * FROM bookings WHERE id=?", bookingMapper, id);
    }

    private RowMapper<Booking> bookingMapper = (rs, rowNum) ->
            new Booking(rs.getLong("id"), rs.getInt("price"),
                    roomManager.findRoomById(rs.getLong("roomId")),
                    guestManager.findGuestById(rs.getLong("guestId")),
                    rs.getDate("arrivalDate").toLocalDate(),
                    rs.getDate("departureDate").toLocalDate());


    @Override
    public void createBooking(Booking booking) {
        SimpleJdbcInsert insertBooking = new SimpleJdbcInsert(jdbc).withTableName("bookings").usingGeneratedKeyColumns("id");
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("price",booking.getPrice())
                .addValue("roomId", booking.getRoom().getId())
                .addValue("guestId", booking.getGuest().getId())
                .addValue("arrivalDate", toSQLDate(booking.getArrivalDate()))
                .addValue("departureDate", toSQLDate(booking.getDepartureDate()));
        Number id = insertBooking.executeAndReturnKey(parameters);
        booking.setId(id.longValue());
        validate(booking);
    }

    @Override
    public void updateBooking(Booking booking) {
        validate(booking);
        this.jdbc.update("UPDATE bookings SET price=?,roomId=?,guestId=?, arrivalDate=?, departureDate=? WHERE id=?",
                booking.getPrice(), booking.getRoom().getId(), booking.getGuest().getId(),
                toSQLDate(booking.getArrivalDate()),
                toSQLDate(booking.getDepartureDate()),booking.getId());
        validate(booking);
    }

    @Override
    public void deleteBooking(Booking booking) {
        validate(booking);

        jdbc.update("DELETE FROM bookings WHERE id=?", booking.getId());
    }

    private Date toSQLDate(LocalDate localDate) {
        if (localDate == null) return null;
        return new Date(ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private void validate(Booking booking){
        if (booking.getArrivalDate().isAfter(booking.getDepartureDate())){
            throw new ValidationException("Departure after arrival");
        }
        if (booking.getPrice()<=0){
            throw new ValidationException("Incorrect price");
        }if (booking.getGuest()==null || booking.getRoom()==null
                || booking.getArrivalDate()==null || booking.getDepartureDate()==null ){
            throw new IllegalArgumentException("Parameter is null");
        }
    }

}
