package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.*;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



/**
 * @author kkatanik & snagyova
 */
public class BookingManagerImpl implements BookingManager{


    private DataSource dataSource;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
            GuestManagerImpl.class.getName());




    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    public void createBooking(Booking booking) {
        checkDataSource();
        validate(booking);
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Booking (price,room,guest,arrivalDate,departureDate) VALUES (?,?,?,?,?)");
            st.setInt(1, booking.getPrice());
            st.setObject(2,booking.getRoom());
            st.setObject(3,booking.getGuest());
            st.setDate(4, toSqlDate(booking.getArrivalDate()));
            st.setDate(5, toSqlDate(booking.getDepartureDate()));

            // This is the proper way, how to handle LocalDate, however it is not
            // supported by Derby yet - see https://issues.apache.org/jira/browse/DERBY-6445
            //st.setObject(3, body.getBorn());
            //st.setObject(4, body.getDied());


            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, booking, true);

            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting booking into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    public void deleteBooking(Booking booking) {
        checkDataSource();
        if (booking == null) {
            throw new IllegalArgumentException("booking is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Booking WHERE booking.getGuest = ?");
            st.setLong(1, booking.getGuest().getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, booking, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting booking from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    public void updateBooking(Booking booking) {
        checkDataSource();
        validate(booking);

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Booking SET price = ?, room = ?,guest = ?, arrivalDate = ?, departureDate = ? WHERE id = ?");
            st.setInt(1, booking.getPrice());
            st.setObject(2,booking.getRoom());
            st.setObject(3,booking.getGuest());
            st.setDate(4, toSqlDate(booking.getArrivalDate()));
            st.setDate(5, toSqlDate(booking.getDepartureDate()));

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, booking, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating booking in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    static List<Booking> executeQueryForMultipleBookings(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Booking> result = new ArrayList<Booking>();
        while (rs.next()) {
            result.add(rowToBooking(rs));
        }
        return result;
    }

    static private Booking rowToBooking(ResultSet rs) throws SQLException {
        Booking result = new Booking();
        Room room = new Room();

        result.setId(rs.getLong("id"));
        result.setPrice(rs.getInt("price"));
        //result.setGuest(rs.getObject("guest"));
        //result.setRoom(rs.getObject(room,"room"));
        result.setArrivalDate(toLocalDate(rs.getDate("arrivalDate")));
        result.setDepartureDate(toLocalDate(rs.getDate("departureDate")));
        return result;
    }

    public List<Booking> findAllBookings() {

        checkDataSource();
        Connection conn =  null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, price, room, guest, arrivalDate, departureDate FROM Booking");
            return executeQueryForMultipleBookings(st);
        }catch (SQLException ex) {
            String msg = "Error when getting all bodies from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public Booking getBookingById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        checkDataSource();
        Connection conn =  null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, price, guest, room, arrivalDate, departureDate FROM Booking WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleBooking(st);
        }catch (SQLException ex) {
            String msg = "Error when getting booking with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    static Booking executeQueryForSingleBooking(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Booking result = rowToBooking(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more bookings with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    private void validate(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("booking is null");
        }
        if (booking.getRoom() == null ) {
            throw new ValidationException("room is null");
        }
        if (booking.getGuest() == null ) {
            throw new ValidationException("guest is null");
        }

        if (booking.getArrivalDate() == null) {
            throw new ValidationException("arrival date is null");
        }
        if (booking.getDepartureDate() == null) {
            throw new ValidationException("departure date is null");
        }

        LocalDate arrival = booking.getArrivalDate();
        LocalDate departure= booking.getDepartureDate();
        if (arrival != null && departure!= null && arrival.isAfter(departure)) {
            throw new ValidationException("the arrival date cannot be set after departure date");
        }

    }



    public List<Booking> findAllBookingsOfGuest(Guest guest) {

        checkDataSource();
        if (guest == null) {
            throw new IllegalArgumentException("guest is null");
        }
        if (guest.getId() == null) {
            throw  new IllegalArgumentException("Id of guest is null");
        }
        Connection conn =  null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, price, room, guest, arrivalDate, departureDate FROM Booking WHERE guest = ?");
            st.setObject(4, guest);
            return executeQueryForMultipleBookings(st);
        }catch (SQLException ex) {
            String msg = "Error when getting bookings of guest from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }



    }

    public List<Booking> findAllBookingsOfRoom(Room room) {

        checkDataSource();
        if (room == null) {
            throw new IllegalArgumentException("room is null");
        }
        if (room.getId() == null) {
            throw  new IllegalArgumentException("Id of room is null");
        }
        Connection conn =  null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, price, room, guest, arrivalDate, departureDate FROM Booking WHERE room = ?");
            st.setObject(3, room);
            return executeQueryForMultipleBookings(st);
        }catch (SQLException ex) {
            String msg = "Error when getting bookings of room from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
