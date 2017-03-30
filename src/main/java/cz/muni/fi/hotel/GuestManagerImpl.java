package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;

/**
 * @author kkatanik & snagyova
 */
public class GuestManagerImpl implements GuestManager {
    private JdbcTemplate jdbc;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));



    public GuestManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void deleteGuest(Guest guest) {
        validate(guest);

        jdbc.update("DELETE FROM guests WHERE id=?", guest.getId());
    }

    @Override
    public void updateGuestInformation(Guest guest) {
        validate(guest);
        jdbc.update("UPDATE guests set name=?,dateOfBirth=?,phoneNumber=? where id=?",
                guest.getName(), toSQLDate(guest.getDateOfBirth()), guest.getPhoneNumber(),guest.getId());
        validate(guest);
    }
    private java.sql.Date toSQLDate(LocalDate localDate) {
        if (localDate == null) return null;
        return new java.sql.Date(ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    private RowMapper<Guest> guestMapper = (rs, rowNum) ->
            new Guest(rs.getLong("id"), rs.getString("name"),
                    rs.getDate("dateOfBirth").toLocalDate(), rs.getString("phoneNumber"));
    @Transactional
    @Override
    public List<Guest> findAllGuests() {
        return jdbc.query("SELECT * FROM guests", guestMapper);
    }

    @Override
    public Guest findGuestById(Long id) {
        return jdbc.queryForObject("SELECT * FROM guests WHERE id=?", guestMapper, id);
    }

    @Override
    public List<Guest> findGuestByName(String name){
        return jdbc.query("SELECT * FROM guests WHERE name=?", guestMapper, name);
    }


    @Override
    public void createGuest(Guest guest) {

        SimpleJdbcInsert insertGuest = new SimpleJdbcInsert(jdbc)
                .withTableName("guests").usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", guest.getName())
                .addValue("dateOfBirth", toSQLDate(guest.getDateOfBirth()))
                .addValue("phoneNumber", guest.getPhoneNumber());

        Number id = insertGuest.executeAndReturnKey(parameters);
        guest.setId(id.longValue());
        validate(guest);
    }

    private static void validate(Guest guest){

        if (guest==null){
            throw new IllegalArgumentException("guest is null");
        }
        if (guest.getName()==null){
            throw new IllegalArgumentException("name is null");
        }
        if (guest.getPhoneNumber()==null){
            throw new IllegalArgumentException("name is null");
        }
        if (guest.getDateOfBirth() ==null){
            throw new IllegalArgumentException("date of birth is null");
        }
        if (guest.getDateOfBirth().isAfter(ChronoLocalDate.from(TODAY))){
            throw new ValidationException("wrong date of birth");
        }

    }
}
