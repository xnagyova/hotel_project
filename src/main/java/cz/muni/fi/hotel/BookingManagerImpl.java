package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;
import cz.muni.fi.hotel.common.ValidationException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kkatanik & snagyova
 */
public class BookingManagerImpl implements BookingManager{

    private static final Logger logger = Logger.getLogger(
            BookingManagerImpl.class.getName());

    private DataSource dataSource;
    private final Clock clock;

    public BookingManagerImpl(Clock clock) {
        this.clock = clock;
    }

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

    public List<Booking> findAllBookings() {
        return null;
    }

    public Booking getBookingById(long id) {
        return null;
    }

    public List<Room> findFreeRoom(){return null;}

    public List<Booking> findAllBookingsOfGuest(Guest guest) {
        /*
        checkDataSource();
        if (guest == null) {
            throw new IllegalArgumentException("guest is null");
        }
        if (guest.getId() == null) {
            throw new IllegalEntityException("guest id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT Body.id, name, gender, born, died, vampire " +
                            "FROM Body JOIN Grave ON Grave.id = Body.graveId " +
                            "WHERE Grave.id = ?");
            st.setLong(1, grave.getId());
            return BodyManagerImpl.executeQueryForMultipleBodies(st);
        } catch (SQLException ex) {
            String msg = "Error when trying to find bodies in grave " + grave;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
    */
        return null;
    }

    public List<Booking> findAllBookingsOfRoom(Room room) {
        return null;
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
        LocalDate today = LocalDate.now(clock);
        if (booking.getArrivalDate() == null) {
            throw new ValidationException("arrivalDate is null");
        }
        if (booking.getDepartureDate() == null) {
            throw new ValidationException("departureDate is null");
        }
        if ( booking.getArrivalDate().isAfter(booking.getDepartureDate())) {
            throw new ValidationException("arrivalDate is after departureDate");
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
