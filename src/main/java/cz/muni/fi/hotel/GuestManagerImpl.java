package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;
import cz.muni.fi.hotel.common.ValidationException;

import java.util.Collection;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * @author kkatanik & snagyova
 */
public class GuestManagerImpl implements GuestManager {

    private static final Logger logger = Logger.getLogger(
            GuestManagerImpl.class.getName());

    private DataSource dataSource;
    private final Clock clock;

    public GuestManagerImpl(Clock clock) {
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
    public void createGuest(Guest guest) throws ServiceFailureException{
        checkDataSource();
        validate(guest);
        if (guest.getId() != null) {
            throw new IllegalEntityException("guest id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Guest (name,dateOfBirth,phoneNumber) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, guest.getName());
            st.setDate(2, toSqlDate(guest.getDateOfBirth()));
            st.setString(3, guest.getPhoneNumber());

            // This is the proper way, how to handle LocalDate, however it is not
            // supported by Derby yet - see https://issues.apache.org/jira/browse/DERBY-6445
            //st.setObject(3, body.getBorn());
            //st.setObject(4, body.getDied());


            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, guest, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            guest.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting guest into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    public void deleteGuest(Guest guest) throws ServiceFailureException{
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
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Guest WHERE id = ?");
            st.setLong(1, guest.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, guest, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting guest from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    public void updateGuestInformation(Guest guest) throws ServiceFailureException{
        checkDataSource();
        validate(guest);

        if (guest.getId() == null) {
            throw new IllegalEntityException("guest id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Guest SET name = ?, dateOfBirth = ?, phoneNumber = ? WHERE id = ?");
            st.setString(1, guest.getName());

            // This is the proper way, how to handle LocalDate, however it is not
            // supported by Derby yet - see https://issues.apache.org/jira/browse/DERBY-6445
            // st.setObject(3, body.getBorn());
            // st.setObject(4, body.getDied());

            st.setDate(2, toSqlDate(guest.getDateOfBirth()));
            st.setString(3, guest.getPhoneNumber());
            st.setLong(4, guest.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, guest, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating guest in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    public List<Guest> findAllGuests() throws ServiceFailureException{
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, dateOfBirth, phoneNumber FROM Guest");
            return executeQueryForMultipleGuests(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all guests from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    public Guest findGuestById(Long id) throws ServiceFailureException{

        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, dateOfBirth, phoneNumber FROM Guest WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleGuest(st);
        } catch (SQLException ex) {
            String msg = "Error when getting guest with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    public List<Guest> findGuestByName(String name) {
        checkDataSource();

        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, dateOfBirth, phoneNumber FROM Guest WHERE name = ?");
            st.setString(1, name);
            return executeQueryForMultipleGuests(st);
        } catch (SQLException ex) {
            String msg = "Error when getting guest with name = " + name + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    static Guest executeQueryForSingleGuest(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Guest result = rowToGuest(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more guests with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }

    static List<Guest> executeQueryForMultipleGuests(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Guest> result = new ArrayList<Guest>();
        while (rs.next()) {
            result.add(rowToGuest(rs));
        }
        return result;
    }

    static private Guest rowToGuest(ResultSet rs) throws SQLException {
        Guest result = new Guest();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));

        // This is the proper way, how to handle LocalDate, however it is not
        // supported by Derby yet - see https://issues.apache.org/jira/browse/DERBY-6445
        //result.setBorn(rs.getObject("born", LocalDate.class));
        //result.setDied(rs.getObject("died", LocalDate.class));

        result.setDateOfBirth(toLocalDate(rs.getDate("dateOfBirth")));
        result.setPhoneNumber(rs.getString("phoneNumber"));
        return result;
    }

    private void validate(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("guest is null");
        }
        if (guest.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (guest.getDateOfBirth() != null ) {
            throw new ValidationException("dateOfBirth is null");
        }
        LocalDate today = LocalDate.now(clock);
        if (guest.getDateOfBirth() != null && guest.getDateOfBirth().isAfter(today)) {
            throw new ValidationException("dateOfBirth is in future");
        }
        if (guest.getPhoneNumber() == null) {
            throw new ValidationException("phoneNumber is null");
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
