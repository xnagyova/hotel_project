package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;

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
    public void deleteRoom(Long id) {
        jdbc.update("DELETE FROM rooms WHERE id=?", id);
    }

    @Override
    public void updateRoomInformation(Room room) {
        jdbc.update("UPDATE rooms set floorNumber=?,capacity=?,balcony=? where id=?",
                room.getFloorNumber(), room.getCapacity(), room.isBalcony()?1:0,room.getId());
    }

    private RowMapper<Room> roomMapper = (rs, rowNum) ->
            new Room(rs.getLong("id"), rs.getInt("floorNumber"),
                    rs.getInt("capacity"), rs.getBoolean("balcony"));

    @Override
    public List<Room> listAllRooms() {
        return jdbc.query("SELECT * FROM rooms", roomMapper);
    }

    @Override
    public List<Room> findFreeRooms() {
        return jdbc.query("SELECT * FROM rooms WHERE capacity > 0", roomMapper);
    }

    @Override
    public Room findRoomById(Long id) {
        return jdbc.queryForObject("SELECT * FROM rooms WHERE id=?", roomMapper, id);
    }


    @Override
    public void buildRoom(Room room) {
        SimpleJdbcInsert insertRoom = new SimpleJdbcInsert(jdbc)
                .withTableName("rooms").usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("floorNumber", room.getFloorNumber())
                .addValue("capacity", room.getCapacity())
                .addValue("balcony", room.isBalcony()?1:0);

        Number id = insertRoom.executeAndReturnKey(parameters);
        room.setId(id.longValue());
    }

}
