package cz.muni.fi.hotel;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDate;
import javax.sql.DataSource;

/**
 * @author kkatanik & snagyova
 */
public class GuestManagerImpl implements GuestManager {
    private JdbcTemplate jdbc;


    public GuestManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public void deleteGuest(Long id) {
        jdbc.update("DELETE FROM guests WHERE id=?", id);
    }

    @Override
    public void updateGuestInformation(Guest guest) {
        jdbc.update("UPDATE guests set name=?,dateOfBirth=?,phoneNumber=? where id=?",
                guest.getName(), guest.getDateOfBirth(), guest.getPhoneNumber(),guest.getId());
    }

    private RowMapper<Guest> guestMapper = (rs, rowNum) ->
            new Guest(rs.getLong("id"), rs.getString("name"),
                    rs.getObject("dateOfBirth", LocalDate.class), rs.getString("phoneNumber"));
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
                .addValue("dateOfBirth", guest.getDateOfBirth())
                .addValue("phoneNumber", guest.getPhoneNumber());

        Number id = insertGuest.executeAndReturnKey(parameters);
        guest.setId(id.longValue());
    }
}
