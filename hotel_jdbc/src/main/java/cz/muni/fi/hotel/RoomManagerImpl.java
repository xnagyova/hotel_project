package cz.muni.fi.hotel;
import cz.muni.fi.hotel.common.ValidationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import java.util.List;
import javax.sql.DataSource;

/**
 *@author kkatanik & snagyova
 */
public class RoomManagerImpl implements RoomManager {

    private JdbcTemplate jdbc;

    public RoomManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }


    @Override
    public void deleteRoom(Room room) {
        validate(room);
        jdbc.update("DELETE FROM rooms WHERE id=?", room.getId());
    }

    @Override
    public void updateRoomInformation(Room room) {
        validate(room);
        jdbc.update("UPDATE rooms set floorNumber=?,capacity=?,balcony=? where id=?",
                room.getFloorNumber(), room.getCapacity(), room.isBalcony() ? 1 : 0, room.getId());
        validate(room);
    }

    private RowMapper<Room> roomMapper = (rs, rowNum) ->
            new Room(rs.getLong("id"), rs.getInt("floorNumber"),
                    rs.getInt("capacity"), rs.getBoolean("balcony"));

    @Override
    public List<Room> listAllRooms() {
        return jdbc.query("SELECT * FROM rooms", roomMapper);
    }


    @Override
    public Room findRoomById(Long id) {
        if (id == null) {
            return null;
        }
        return jdbc.queryForObject("SELECT * FROM rooms WHERE id=?", roomMapper, id);
    }


    @Override
    public void buildRoom(Room room) {
        validate(room);
        SimpleJdbcInsert insertRoom = new SimpleJdbcInsert(jdbc)
                .withTableName("rooms").usingGeneratedKeyColumns("id");


        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("floorNumber", room.getFloorNumber())
                .addValue("capacity", room.getCapacity())
                .addValue("balcony", room.isBalcony() ? 1 : 0);

        Number id = insertRoom.executeAndReturnKey(parameters);
        room.setId(id.longValue());
        validate(room);
    }



    private static void validate(Room room){
        if (room==null){
            throw new IllegalArgumentException("room is null");
        }
        if (room.getCapacity()<=0){
            throw new ValidationException("capacity must be positive");
        }
        if (room.getFloorNumber()<0){
            throw new ValidationException("floor number cannot be negative");
        }
        if (!room.isBalcony() && room.isBalcony()){
            throw new ValidationException("balcony unspecified");
        }
    }


}
