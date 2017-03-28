package cz.muni.fi.hotel;


import cz.muni.fi.hotel.common.IllegalEntityException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.xml.bind.ValidationException;
import java.time.*;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;



/**
 * @author kkatanik & snagyova
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MySpringTestConfig.class})
public class BookingManagerImplTest {


    @Autowired
    private BookingManager bookingManager;

    @Autowired
    private GuestManager guestManager;
    @Autowired
    private RoomManager roomManager;


    private static ApplicationContext ctx;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));

    @BeforeClass
    public static void bookingManagerSetup() {
        ctx = new AnnotationConfigApplicationContext(MySpringTestConfig.class);
    }


    @Before
    public void setUp() throws Exception {
        bookingManager = ctx.getBean("bookingManager", BookingManager.class);
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private BookingBuilder sampleFirstBookingBuilder()  {
        return new BookingBuilder()
                .price(20)
                .room(sampleBigRoomBuilder().build())
                .guest(sampleSamanthaGuestBuilder().build())
                .arrivalDate(1650,4,12)
                .departureDate(1650,4,19);

    }

    private BookingBuilder sampleSecondBookingBuilder()  {
        return new BookingBuilder()
                .price(10)
                .room(sampleSmallRoomBuilder().build())
                .guest(sampleJohnGuestBuilder().build())
                .arrivalDate(1020,8,7)
                .departureDate(1020,8,31);

    }


    private GuestBuilder sampleJohnGuestBuilder() {
        return new GuestBuilder()
                .name("John Fox")
                .dateOfBirth(1980,4,22)
                .phoneNumber("+421947865586");

    }

    private GuestBuilder sampleSamanthaGuestBuilder() {
        return new GuestBuilder()
                .name("Samantha Fox")
                .dateOfBirth(1974,8,10)
                .phoneNumber("+421947842396");

    }
    private GuestBuilder sampleSamantha2GuestBuilder() {
        return new GuestBuilder()
                .name("Samantha Fox")
                .dateOfBirth(1975,11,21)
                .phoneNumber("+421947741366");

    }

    private RoomBuilder sampleBigRoomBuilder() {
        return new RoomBuilder()
                .floorNumber(3)
                .capacity(6)
                .balcony(true);

    }


    private RoomBuilder sampleSmallRoomBuilder() {
        return new RoomBuilder()
                .floorNumber(1)
                .capacity(3)
                .balcony(false);

    }


    @Test
    public void createBooking()  {

        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        Long bookingId = booking.getId();
        assertThat(booking.getId()).isNotNull();

        assertThat(bookingManager.getBookingById(bookingId))
                .isNotSameAs(booking)
                .isEqualToComparingFieldByField(booking);
    }


    @Test(expected = NullPointerException.class)
    public void createNullBooking() {
        bookingManager.createBooking(null);
    }

    @Test
    public void createBookingWithNullGuest() {
        Booking booking = sampleFirstBookingBuilder()
                .guest(null)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingWithNullRoom() {
        Booking booking = sampleFirstBookingBuilder()
                .room(null)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(NullPointerException.class);
    }



    @Test
    public void createBookingNegativePrice() {
        Booking booking = sampleFirstBookingBuilder()
                .price(-1)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }

    @Test
    public void createBookingZeroPrice() {
        Booking booking = sampleFirstBookingBuilder()
                .price(0)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }

    @Test
    public void createBookingWithArrivalDateAfterDepartureDate() {
        Booking booking = sampleFirstBookingBuilder()
                .arrivalDate(2016,2,18)
                .departureDate(2016,2,5)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }


    @Test
    public void deleteBooking() {
        Booking firstBooking = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);

        assertThat(bookingManager.getBookingById(firstBooking.getId())).isNotNull();
        assertThat(bookingManager.getBookingById(secondBooking.getId())).isNotNull();

        bookingManager.deleteBooking(firstBooking);
        assertThat(bookingManager.getBookingById(firstBooking.getId())).isNull();
        assertThat(bookingManager.getBookingById(secondBooking.getId())).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void deleteNullBooking() {
        bookingManager.deleteBooking(null);
    }

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateBooking(BookingManagerImplTest.Operation<Booking> updateOperation) {
        Booking bookingToUpdate = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();
        bookingManager.createBooking(bookingToUpdate);
        bookingManager.createBooking(secondBooking);

        updateOperation.callOn(bookingToUpdate);

        bookingManager.updateBooking(bookingToUpdate);

        assertThat(bookingManager.getBookingById(bookingToUpdate.getId()))
                .isEqualToComparingFieldByField(bookingToUpdate);

        assertThat(bookingManager.getBookingById(secondBooking.getId()))
                .isEqualToComparingFieldByField(secondBooking);
    }


    @Test(expected = NullPointerException.class)
    public void updateNullBooking() {
        bookingManager.updateBooking(null);
    }

    @Test
    public void updateBookingPrice() {
        testUpdateBooking((booking) -> booking.setPrice(100));

    }

    @Test
    public void updateBookingGuest() {
        testUpdateBooking((booking) -> booking.setGuest(sampleSamantha2GuestBuilder().build()));

    }

    @Test
    public void updateBookingRoom() {
        testUpdateBooking((booking) -> booking.setRoom(sampleSmallRoomBuilder().build()));

    }

    @Test
    public void updateBookingArrivalDate() {
        testUpdateBooking((booking) -> booking.setArrivalDate(new Date(1020,1,12)));
    }

    @Test
    public void updateBookingDepartureDate() {
        testUpdateBooking((booking) -> booking.setDepartureDate(new Date(2020,12,19)));
    }


    @Test
    public void updateBookingWithNullGuest() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setGuest(null);
        expectedException.expect(IllegalArgumentException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullRoom() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setRoom(null);
        expectedException.expect(IllegalArgumentException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullArrivalDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setArrivalDate(null);
        expectedException.expect(NullPointerException.class);
        bookingManager.updateBooking(booking);
    }


    @Test
    public void updateBookingWithNullDepartureDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setDepartureDate(null);
        expectedException.expect(NullPointerException.class);
        bookingManager.updateBooking(booking);
    }



    @Test
    public void updateBookingWithArrivalDateAfterDepartureDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setDepartureDate(new Date(2015,10,1));
        booking.setArrivalDate(new Date(2015,12,1));
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        bookingManager.updateBooking(booking);
    }



    @Test
    public void updateBookingWithNegativePrice() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setPrice(-1);
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithZeroPrice() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setPrice(0);
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        bookingManager.updateBooking(booking);
    }




    @Test
    public void findAllBookings() {


        Booking firstBooking = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);

        assertThat(bookingManager.findAllBookings())
                .usingFieldByFieldElementComparator()
                .contains(firstBooking,secondBooking);

    }

    @Test
    public void getBookingById() {
        Booking firstBooking = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);

        Long firstBookingId = firstBooking.getId();

        assertThat(bookingManager.getBookingById(firstBookingId))
                .isEqualToComparingFieldByField(firstBooking);

    }

    @Test
    public void findAllBookingsOfGuest() {
        Booking firstBooking = sampleFirstBookingBuilder().guest(sampleSamantha2GuestBuilder().build()).build();
        Booking secondBooking = sampleSecondBookingBuilder().guest(sampleSamantha2GuestBuilder().build()).build();
        Booking thirdBooking = sampleFirstBookingBuilder().guest(sampleJohnGuestBuilder().build()).build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);
        bookingManager.createBooking(thirdBooking);

        assertThat(bookingManager.findAllBookingsOfGuest(sampleSamantha2GuestBuilder().build()))
                .usingFieldByFieldElementComparator()
                .contains(firstBooking,secondBooking)
                .doesNotContain(thirdBooking);

    }

    @Test
    public void findAllBookingsOfRoom() {
        Booking firstBooking = sampleFirstBookingBuilder().room(sampleBigRoomBuilder().build()).build();
        Booking secondBooking = sampleSecondBookingBuilder().room(sampleBigRoomBuilder().build()).build();
        Booking thirdBooking = sampleFirstBookingBuilder().room(sampleSmallRoomBuilder().build()).build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);
        bookingManager.createBooking(thirdBooking);

        assertThat(bookingManager.findAllBookingsOfGuest(sampleSamantha2GuestBuilder().build()))
                .usingFieldByFieldElementComparator()
                .contains(firstBooking,secondBooking)
                .doesNotContain(thirdBooking);

    }






}