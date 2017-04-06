package cz.muni.fi.hotel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.PlatformTransactionManager;




import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;

/**
 * Created by User on 28.3.2017.
 */
@Configuration
public class MySpringTestConfig {

    @Bean
    public DataSource dataSource() {
        //embedded databáze
        return new EmbeddedDatabaseBuilder()
                .setType(DERBY)
                .addScript("classpath:schema-javadb.sql")
                .addScript("classpath:my-test-data.sql")
                .build();
    }

    @Bean
    public GuestManager guestManager() {
        return new GuestManagerImpl(dataSource());
    }

    @Bean
    public RoomManager roomManager() {
        return new RoomManagerImpl(dataSource());
    }

    @Bean
    public BookingManager bookingManager() {
        BookingManagerImpl bookingManager = new BookingManagerImpl(dataSource());
        bookingManager.setGuestManager(guestManager());
        bookingManager.setRoomManager(roomManager());

        return bookingManager;
    }
}
