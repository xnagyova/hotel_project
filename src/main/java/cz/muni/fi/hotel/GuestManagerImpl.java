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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;

    public GuestManagerImpl(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @Override
    public void deleteGuest(Guest guest) {
        jdbc.update("DELETE FROM guests WHERE id=?", guest.getId());
    }

    @Override
    public void updateGuestInformation(Guest guest) {
        jdbc.update("UPDATE guests set name=?,dateOfBirth=?,phoneNumber=? where id=?",
                guest.getName(), guest.getDateOfBirth(), guest.getPhoneNumber(),guest.getId());
    }

    private RowMapper<Guest> guestMapper = (rs, rowNum) ->
            new Guest(rs.getLong("id"), rs.getString("name"),
                    rs.getObject("dateOfBirth",LocalDate.class), rs.getString("phoneNumber"));

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
