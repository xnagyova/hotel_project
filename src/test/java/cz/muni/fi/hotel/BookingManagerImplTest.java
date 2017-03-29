package cz.muni.fi.hotel;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import javax.xml.bind.ValidationException;
import java.awt.print.Book;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.List;

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
    private RoomManager roomManager;
    @Autowired
    private GuestManager guestManager;


    //private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));


    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private BookingBuilder sampleFirstBookingBuilder()  {
        return new BookingBuilder()
                .price(20)
                .room(sampleBigRoomBuilder().build())
                .guest(sampleSamanthaGuestBuilder().build())
                .arrivalDate(1975,3,12)
                .departureDate(1978,4,30);

    }

    private BookingBuilder sampleSecondBookingBuilder()  {
        return new BookingBuilder()
                .price(10)
                .room(sampleSmallRoomBuilder().build())
                .guest(sampleJohnGuestBuilder().build())
                .arrivalDate(1076,8,7)
                .departureDate(1080,9,31);

    }


    private GuestBuilder sampleJohnGuestBuilder() {
        return new GuestBuilder()
                .name("John Fox")
                .dateOfBirth(1976,4,22)
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
                .dateOfBirth(1972,11,21)
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
        Room room = roomManager.findRoomById(1l);
        Guest guest = guestManager.findGuestById(1L);
        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setRoom(room);
        booking.setPrice(51);
        booking.setArrivalDate(new Date(2013,12,10));
        booking.setDepartureDate(new Date(2015,12,10));
        bookingManager.createBooking(booking);
        assertThat(booking.getId()).isNotNull();

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
                .isInstanceOf(NullPointerException.class);
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
        bookingManager.deleteBooking(bookingManager.getBookingById(1L));
        try {
            bookingManager.getBookingById(1L);
            fail("booking 1 not deleted");
        } catch (EmptyResultDataAccessException e) {
            //no code
        }

    }

    @Test(expected = NullPointerException.class)
    public void deleteNullBooking() {
        bookingManager.deleteBooking(null);
    }
    @Test
    public void updateBooking() {
        Booking bookingToUpdate = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();
        bookingManager.createBooking(bookingToUpdate);
        bookingManager.createBooking(secondBooking);

        bookingToUpdate.setPrice(150);

        bookingManager.updateBooking(bookingToUpdate);

        assertThat(bookingManager.getBookingById(bookingToUpdate.getId()))
                .isEqualToComparingFieldByField(bookingToUpdate);

        assertThat(bookingManager.getBookingById(secondBooking.getId()))
                .isEqualToComparingFieldByField(secondBooking);



    }

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateBooking(Operation<Booking> updateOperation) {
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
        testUpdateBooking((booking) -> booking.setArrivalDate(new Date(1950,1,12)));
    }

    @Test
    public void updateBookingDepartureDate() {
        testUpdateBooking((booking) -> booking.setDepartureDate(new Date(1979,12,19)));
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
        System.out.print("buildol som");
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

        List<Booking> bookingList = bookingManager.findAllBookingsOfGuest(guestManager.findGuestById(1L));
        assertThat(bookingList.size()==1);


    }

    @Test
    public void findAllBookingsOfRoom() {
        List<Booking> bookingList = bookingManager.findAllBookingsOfRoom(roomManager.findRoomById(1L));
        assertThat(bookingList.size()==1);


    }






}