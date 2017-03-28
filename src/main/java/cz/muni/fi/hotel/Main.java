package cz.muni.fi.hotel;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) throws  IOException {

        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        GuestManager guestManager = ctx.getBean(GuestManager.class);
        RoomManager roomManager = ctx.getBean(RoomManager.class);

        guestManager.findAllGuests().forEach(System.out::println);

        roomManager.listAllRooms().forEach(System.out::println);

        Guest guest = new Guest(null, "Jan Novák", LocalDate.of (1986,05,4), "602123456");
        guestManager.createGuest(guest);

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
            // BookManagerImpl nepoužívá Spring JDBC, musíme mu vnutit spolupráci se Spring transakcemi
            return new RoomManagerImpl(new TransactionAwareDataSourceProxy(dataSource()));
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