package cz.muni.fi.hotel;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.*;

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


    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));


    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private BookingBuilder sampleFirstBookingBuilder()  {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        return new BookingBuilder()
                .price(20)
                .room(room)
                .guest(guest)
                .arrivalDate(1975, Month.APRIL,12)
                .departureDate(1978,Month.MAY,30);

    }

    private BookingBuilder sampleSecondBookingBuilder()  {
        Room room = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room);
        Guest guest = sampleSamanthaGuestBuilder().build();
        guestManager.createGuest(guest);
        return new BookingBuilder()
                .price(10)
                .room(room)
                .guest(guest)
                .arrivalDate(2015,Month.AUGUST,7)
                .departureDate(2016,Month.SEPTEMBER,30);

    }


    private GuestBuilder sampleJohnGuestBuilder() {
        return new GuestBuilder()
                .name("John Fox")
                .dateOfBirth(1976,Month.APRIL,22)
                .phoneNumber("+421947865586");

    }

    private GuestBuilder sampleSamanthaGuestBuilder() {
        return new GuestBuilder()
                .name("Samantha Fox")
                .dateOfBirth(1974,Month.AUGUST,10)
                .phoneNumber("+421947842396");

    }
    private GuestBuilder sampleSamantha2GuestBuilder() {
        return new GuestBuilder()
                .name("Samantha Fox")
                .dateOfBirth(1972,Month.NOVEMBER,21)
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
        booking.setArrivalDate(LocalDate.of(2013,12,10));
        booking.setDepartureDate(LocalDate.of(2015,12,10));
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
                .arrivalDate(2016,Month.FEBRUARY,18)
                .departureDate(2016,Month.FEBRUARY,5)
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
    @Test(expected = NullPointerException.class)
    public void updateNullBooking() {
        bookingManager.updateBooking(null);
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
        booking.setDepartureDate(LocalDate.of(2015,10,1));
        booking.setArrivalDate(LocalDate.of(2015,12,1));
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
    public void findAllBookingsOfGuest() {

        List<Booking> bookingList = bookingManager.findAllBookingsOfGuest(guestManager.findGuestById(1L));
        assertThat(bookingList.size()==1);


    }

    @Test
    public void findAllBookingsOfRoom() {
        List<Booking> bookingList = bookingManager.findAllBookingsOfRoom(roomManager.findRoomById(1L));
        assertThat(bookingList.size()==1);


    }


    @Test
    public void getBookingById() {
        Guest guest = sampleJohnGuestBuilder().build();
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        guestManager.createGuest(guest);
        Booking firstBooking = sampleFirstBookingBuilder().guest(guest).room(room).build();
        Booking secondBooking = sampleSecondBookingBuilder().guest(guest).room(room).build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);

        Long firstBookingId = firstBooking.getId();

        assertThat(bookingManager.getBookingById(firstBookingId))
                .isEqualToComparingFieldByField(firstBooking);


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
        Guest guest = sampleJohnGuestBuilder().build();
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        guestManager.createGuest(guest);
        Booking bookingToUpdate = sampleFirstBookingBuilder().guest(guest).room(room).build();
        Booking secondBooking = sampleSecondBookingBuilder().guest(guest).room(room).build();


        bookingManager.createBooking(bookingToUpdate);
        bookingManager.createBooking(secondBooking);

        updateOperation.callOn(bookingToUpdate);

        bookingManager.updateBooking(bookingToUpdate);

        assertThat(bookingManager.getBookingById(bookingToUpdate.getId()))
                .isEqualToComparingFieldByField(bookingToUpdate);

        assertThat(bookingManager.getBookingById(secondBooking.getId()))
                .isEqualToComparingFieldByField(secondBooking);
    }




    @Test
    public void updateBookingPrice() {
        testUpdateBooking((booking) -> booking.setPrice(100));

    }

    @Test
    public void updateBookingGuest() {
        Guest guest = sampleSamantha2GuestBuilder().build();
        guestManager.createGuest(guest);
        testUpdateBooking((booking) -> booking.setGuest(guest));

    }

    @Test
    public void updateBookingRoom() {
        Room room = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room);
        testUpdateBooking((booking) -> booking.setRoom(room));

    }

    @Test
    public void updateBookingArrivalDate() {
        testUpdateBooking((booking) -> booking.setArrivalDate(LocalDate.of(1950,Month.AUGUST,12)));
    }

    @Test
    public void updateBookingDepartureDate() {
        testUpdateBooking((booking) -> booking.setDepartureDate(LocalDate.of(1979,Month.APRIL,19)));
    }



    @Test
    public void findFreeRooms() {
        Room room1 = sampleBigRoomBuilder().build();
        Room room2 = sampleSmallRoomBuilder().build();
        Room roomInBooking = sampleBigRoomBuilder().capacity(100).build();
        roomManager.buildRoom(room1);
        roomManager.buildRoom(room2);
        roomManager.buildRoom(roomInBooking);

        Booking booking = sampleFirstBookingBuilder().room(roomInBooking).build();
        bookingManager.createBooking(booking);

        assertThat(bookingManager.findFreeRooms()).contains(room1,room2)
                .doesNotContain(roomInBooking);
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











}