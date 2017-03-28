package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.*;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;



/**
 * @author kkatanik & snagyova
 */
public class BookingManagerImpl implements BookingManager {
    final static Logger log = LoggerFactory.getLogger(BookingManagerImpl.class);
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
        return jdbc.query("SELECT * FROM bookings WHERE guestId=?",
                (rs, rowNum) -> {
                    int price = rs.getInt("price");
                    long roomId = rs.getLong("roomId");
                    Room room = null;
                    try {
                        room = roomManager.findRoomById(roomId);
                    } catch (Exception e) {
                        log.error("cannot find room", e);
                    }
                    LocalDate arrivalDate = rs.getDate("arrivalDate").toLocalDate();
                    LocalDate departureDate = rs.getDate("departureDate").toLocalDate();
                    return new Booking(rs.getLong("id"), price, room, guest, arrivalDate, departureDate);
                },
                guest.getId());
    }

    @Override
    public List<Booking> findAllBookingsOfRoom(final Room room) {
        return jdbc.query("SELECT * FROM bookings WHERE roomId=?",
                (rs, rowNum) -> {
                    int price = rs.getInt("price");
                    long guestId = rs.getLong("guestId");
                    Guest guest = null;
                    try {
                        guest = guestManager.findGuestById(guestId);
                    } catch (Exception e) {
                        log.error("cannot find guest", e);
                    }
                    LocalDate arrivalDate = rs.getDate("arrivalDate").toLocalDate();
                    LocalDate departureDate = rs.getDate("departureDate").toLocalDate();
                    return new Booking(rs.getLong("id"), price, room, guest, arrivalDate, departureDate);
                },
                room.getId());
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
                    rs.getObject("arrivalDate", LocalDate.class),
                    rs.getObject("departureDate", LocalDate.class));


    @Override
    public void createBooking(Booking booking) {
        SimpleJdbcInsert insertLease = new SimpleJdbcInsert(jdbc).withTableName("bookings").usingGeneratedKeyColumns("id");
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("price",booking.getPrice())
                .addValue("roomId", booking.getRoom().getId())
                .addValue("guestId", booking.getGuest().getId())
                .addValue("arrivalDate", toSQLDate(booking.getArrivalDate()))
                .addValue("departureDate", toSQLDate(booking.getDepartureDate()));
        Number id = insertLease.executeAndReturnKey(parameters);
        booking.setId(id.longValue());
    }

    @Override
    public void updateBooking(Booking booking) {
        jdbc.update("UPDATE bookings set price=?,roomId=?,guestId=?, arrivalDate=?, departureDate=? where id=?",
                booking.getPrice(), booking.getRoom().getId(), booking.getGuest().getId(),booking.getArrivalDate(),
                booking.getDepartureDate(),booking.getId());
    }

    @Override
    public void deleteBooking(Booking booking) {
        jdbc.update("DELETE FROM bookings WHERE id=?", booking.getId());
    }

    private Date toSQLDate(LocalDate localDate) {
        if (localDate == null) return null;
        return new Date(ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

}
