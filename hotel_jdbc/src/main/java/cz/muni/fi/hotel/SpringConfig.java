package cz.muni.fi.hotel;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created by ${KristianKatanik} on 04.04.2017.
 */
@Configuration  //je to konfigurace pro Spring
@EnableTransactionManagement //bude řídit transakce u metod označených @Transactional
@PropertySource("classpath:myconf.properties") //načte konfiguraci z myconf.properties
public class SpringConfig {

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource() {
        BasicDataSource bds = new BasicDataSource(); //Apache DBCP connection pooling DataSource
        //bds.setConnectionProperties("create=true");
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
