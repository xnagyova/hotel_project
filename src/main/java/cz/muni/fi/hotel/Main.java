package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.RoomException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * @author kkatanik & snagyova
 */
public class Main {


    public static void main(String[] args) throws RoomException, IOException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        GuestManager guestManager = ctx.getBean(GuestManager.class);
        RoomManager roomManager = ctx.getBean(RoomManager.class);

        roomManager.listAllRooms().forEach(System.out::println);

        guestManager.findAllGuests().forEach(System.out::println);

        Room room = new Room(null,4,4,true);
        roomManager.buildRoom(room);
        Guest guest = new Guest(null, "Samantha Fox", LocalDate.of(1988, Month.FEBRUARY,12),
                "+421911412221");
        guestManager.createGuest(guest);

        Booking booking = new Booking(null,30, room, guest, LocalDate.now(),LocalDate.now().plusDays(30));
        BookingManager bookingManager = ctx.getBean(BookingManager.class);
        bookingManager.createBooking(booking);

        List<Booking> bookingsForGuest = bookingManager.findAllBookingsOfGuest(guest);
        System.out.println("bookingsForGuest = " + bookingsForGuest);

    }

    @Configuration  //je to konfigurace pro Spring
    @EnableTransactionManagement //bude řídit transakce u metod označených @Transactional
    @PropertySource("classpath:myconf.properties") //načte konfiguraci z myconf.properties
    public static class SpringConfig {

        @Autowired
        Environment env;

        @Bean
        public DataSource dataSource() {
            BasicDataSource bds = new BasicDataSource(); //Apache DBCP connection pooling DataSource
            bds.setDriverClassName(env.getProperty("jdbc.driver"));
            bds.setUrl(env.getProperty("jdbc.url"));
            bds.setUsername(env.getProperty("jdbc.user"));
            bds.setPassword(env.getProperty("jdbc.password"));
            return bds;
        }

        @Bean //potřeba pro @EnableTransactionManagement
        public PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean //náš manager, bude automaticky obalen řízením transakcí
        public GuestManager guestManager() {
            return new GuestManagerImpl(dataSource());
        }

        @Bean
        public RoomManager roomManager() {
            // RoomManagerImpl nepoužívá Spring JDBC, musíme mu vnutit spolupráci se Spring transakcemi
            return new RoomManagerImpl(dataSource());
        }

        @Bean
        public BookingManager bookingManager() {
            BookingManagerImpl bookingManager = new BookingManagerImpl(dataSource());
            bookingManager.setRoomManager(roomManager());
            bookingManager.setGuestManager(guestManager());
            return bookingManager;
        }
    }
}
