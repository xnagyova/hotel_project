package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.BookingException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.io.IOException;
import java.time.LocalDate;


public class Main {
    /* MyForm myForm = new MainForm();*/
   /* final static Logger log = LoggerFactory.getLogger(Main.class);

    public static DataSource createMemoryDatabase() {
        BasicDataSource bds = new BasicDataSource();
        //set JDBC driver and URL
        bds.setDriverClassName(EmbeddedDriver.class.getName());
        bds.setUrl("jdbc:derby:memory:booksDB;create=true");
        //populate db with tables and data
        new ResourceDatabasePopulator(
                new ClassPathResource("schema-javadb.sql"),
                new ClassPathResource("my-test-data.sql"))
                .execute(bds);
        return bds;
    }

    public static void main(String[] args) throws GuestException {

        log.info("zaciname");

        DataSource dataSource = createMemoryDatabase();
        GuestManager guestManager = new GuestManagerImpl(dataSource);

        List<Guest> allGuests = guestManager.findAllGuests();
        System.out.println("allGuests = " + allGuests);

    }*/

    public static void main(String[] args) throws BookingException, IOException {

       // Properties myconf = new Properties();
       // myconf.load(Main.class.getResourceAsStream("/myconf.properties"));


        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        GuestManager guestManager = ctx.getBean(GuestManager.class);
        RoomManager roomManager = ctx.getBean(RoomManager.class);
        BookingManager bookingManager = ctx.getBean(BookingManager.class);


        guestManager.findAllGuests().forEach(System.out::println);

        roomManager.listAllRooms().forEach(System.out::println);

        bookingManager.findAllBookings().forEach(System.out::println);

        Guest guest = new Guest(null, "Jan Novak", LocalDate.of(1986,05,4), "602123456");
        Guest guest2 = new Guest(null,"Jan Novak", LocalDate.of(1977, 05,12),"+456465");
        guestManager.createGuest(guest);
        guestManager.createGuest(guest2);
        System.out.println(guest);
        System.out.println(guestManager.findGuestById(guest2.getId()));
        System.out.println(guestManager.findGuestByName("Jan Novak"));
        guest.setName("Jozko Kosko");
        guest.setPhoneNumber("+123456");
        guestManager.updateGuestInformation(guest);
        System.out.println(guest);
        Room room = new Room(null, 4,5,true);
        roomManager.buildRoom(room);
        System.out.println(roomManager.findRoomById(room.getId()));
        System.out.println(room);
        System.out.println(roomManager.findRoomById(room.getId()));
        room.setCapacity(78);
        room.setBalcony(false);
        roomManager.updateRoomInformation(room);
        System.out.println(room);

        Booking booking = new Booking(null, 52, room,guest, LocalDate.of(2000,2,2), LocalDate.of(2000,5,5));
        bookingManager.createBooking(booking);
        System.out.println(booking);
        System.out.println(bookingManager.getBookingById(booking.getId()));
        booking.setPrice(12);
        booking.setArrivalDate(LocalDate.of(1980,11,1));
        bookingManager.updateBooking(booking);
        System.out.println(booking);
        guestManager.findAllGuests().forEach(System.out::println);
        System.out.println(bookingManager.findAllBookingsOfGuest(guest));
        System.out.println(bookingManager.findAllBookingsOfRoom(room));

        roomManager.listAllRooms().forEach(System.out::println);
        bookingManager.findAllBookings().forEach(System.out::println);




    }
/*
    @Configuration  //je to konfigurace pro Spring
    @EnableTransactionManagement //bude řídit transakce u metod označených @Transactional
    @PropertySource("classpath:myconf.properties") //načte konfiguraci z myconf.properties
    public static class SpringConfig {

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


    }*/

    }
