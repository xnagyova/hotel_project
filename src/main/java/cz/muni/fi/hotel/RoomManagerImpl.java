package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;
import cz.muni.fi.hotel.common.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *@author kkatanik & snagyova
 */
public class RoomManagerImpl implements RoomManager{

    private JdbcTemplate jdbc;

    public RoomManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void deleteRoom(Room room) {
        jdbc.update("DELETE FROM room WHERE id=?", room.getId());
    }

    @Override
    public void updateRoomInformation(Room room) {
        jdbc.update("UPDATE room set floorNumber=?,capacity=?,balcony=? where id=?",
                room.getFloorNumber(), room.getCapacity(), room.isBalcony()?1:0,room.getId());
    }

    private RowMapper<Room> roomMapper = (rs, rowNum) ->
            new Room(rs.getLong("id"), rs.getInt("floorNumber"),
                    rs.getInt("capacity"), rs.getBoolean("balcony"));

    @Transactional
    @Override
    public List<Room> listAllRooms() {
        return jdbc.query("SELECT * FROM room", roomMapper);
    }

    @Override
    public List<Room> findFreeRooms() {
        return jdbc.query("SELECT * FROM room WHERE capacity > 0", roomMapper);
    }

    @Override
    public Room findRoomById(Long id) {
        return jdbc.queryForObject("SELECT * FROM room WHERE id=?", roomMapper, id);
    }


    @Override
    public void buildRoom(Room room) {
        SimpleJdbcInsert insertRoom = new SimpleJdbcInsert(jdbc)
                .withTableName("room").usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("floorNumber", room.getFloorNumber())
                .addValue("capacity", room.getCapacity())
                .addValue("balcony", room.isBalcony()?1:0);

        Number id = insertRoom.executeAndReturnKey(parameters);
        room.setId(id.longValue());
    }
/*
    private static final Logger logger = Logger.getLogger(
            RoomManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void buildRoom(Room room) {

        checkDataSource();
        validate(room);
        if (room.getId() != null) {
            throw new IllegalEntityException("room id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO Room (floorNumber,capacity,balcony) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, room.getFloorNumber());
            st.setInt(2, room.getCapacity());
            st.setInt(3, room.isBalcony()?1:0);


            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, room, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            room.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting room into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    @Override
    public void updateRoomInformation(Room room) {
        checkDataSource();
        validate(room);
        if (room.getId() == null) {
            throw new IllegalEntityException("room id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "UPDATE Room SET floorNumber = ?, capacity = ?, balcony = ? WHERE id = ?");
            st.setInt(1, room.getFloorNumber());
            st.setInt(2, room.getCapacity());
            st.setInt(3, room.isBalcony()?1:0);
            st.setLong(4, room.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, room, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating room in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }

    @Override
    public void deleteRoom(Room room) {
        checkDataSource();
        if (room == null) {
            throw new IllegalArgumentException("room is null");
        }
        if (room.getId() == null) {
            throw new IllegalEntityException("room id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in
            // method DBUtils.closeQuietly(...)
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Room WHERE id = ?");
            st.setLong(1, room.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, room, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting room from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }

    }


    @Override
    public Room findRoomById(Long id) {
        checkDataSource();

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, floorNumber, capacity, balcony FROM Room WHERE id = ?");
            st.setLong(1, id);
            return executeQueryForSingleRoom(st);
        } catch (SQLException ex) {
            String msg = "Error when getting room with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Room> listAllRooms() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, floorNumber, capacity, balcony FROM Room");
            return executeQueryForMultipleRooms(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all rooms from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    public List<Room> findFreeRooms(){
        checkDataSource();
        Connection conn =  null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, floorNumber, capacity, balcony FROM Room WHERE room NOT IN (SELECT room FROM booking WHERE room is not null)");
            return executeQueryForMultipleRooms(st);
        }catch (SQLException ex) {
            String msg = "Error when getting free rooms from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);

        }
        }

    static Room executeQueryForSingleRoom(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Room result = rowToRoom(rs);
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more rooms with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }


    static List<Room> executeQueryForMultipleRooms(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Room> result = new ArrayList<Room>();
        while (rs.next()) {
            result.add(rowToRoom(rs));
        }
        return result;
    }

    private static Room rowToRoom(ResultSet rs) throws SQLException {
        Room result = new Room();
        result.setId(rs.getLong("id"));
        result.setFloorNumber(rs.getInt("floorNumber"));
        result.setCapacity(rs.getInt("capacity"));
        result.setBalcony(rs.getBoolean("balcony"));
        return result;
    }

    private static void validate(Room room) {
        if (room == null) {
            throw new IllegalArgumentException("room is null");
        }
        if (room.getFloorNumber() < 0) {
            throw new ValidationException("floorNumber is negative number");
        }
        if (room.getCapacity() < 0) {
            throw new ValidationException("capacity is negative number");
        }
    }*/
}
