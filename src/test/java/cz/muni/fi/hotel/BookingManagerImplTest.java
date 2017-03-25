package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import javax.xml.bind.ValidationException;
import java.sql.SQLException;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author kkatanik & snagyova
 */
public class BookingManagerImplTest {
    private BookingManagerImpl bookingManager;
    private DataSource ds;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        // we will use in memory database
        ds.setDatabaseName("memory:hotelmgr-test");
        // database is created automatically if it does not exist yet
        ds.setCreateDatabase("create");
        return ds;
    }

    private static Clock prepareClockMock(ZonedDateTime now) {
        // We don't need to use Mockito, because java already contais
        // implementation of Clock which returns fixed time.
        return Clock.fixed(now.toInstant(), now.getZone());
    }

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,BookingManager.class.getResource("createTables.sql"));
        bookingManager = new BookingManagerImpl(prepareClockMock(TODAY));
        bookingManager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,BookingManager.class.getResource("dropTables.sql"));
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private BookingBuilder sampleFirstBookingBuilder() {
        return new BookingBuilder()
                .price(20)
                .room(sampleBigRoomBuilder().build())
                .guest(sampleSamanthaGuestBuilder().build())
                .arrivalDate(2016,Month.APRIL,12)
                .departureDate(2016,Month.APRIL,19);

    }

    private BookingBuilder sampleSecondBookingBuilder() {
        return new BookingBuilder()
                .price(10)
                .room(sampleSmallRoomBuilder().build())
                .guest(sampleJohnGuestBuilder().build())
                .arrivalDate(2016,Month.OCTOBER,7)
                .departureDate(2016,Month.OCTOBER,31);

    }


    private GuestBuilder sampleJohnGuestBuilder() {
        return new GuestBuilder()
                .name("John Fox")
                .dateOfBirth(1980,Month.APRIL,22)
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
                .dateOfBirth(1975,Month.NOVEMBER,21)
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
    public void createBooking() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);

        Long bookingId = booking.getId();
        assertThat(bookingId).isNotNull();

        assertThat(bookingManager.getBookingById(booking.getId()))
                .isNotSameAs(booking)
                .isEqualToComparingFieldByField(booking);
    }

    @Test(expected = IllegalArgumentException.class)
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
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingWithNonExistingArrivalDate() {
        Booking booking = sampleFirstBookingBuilder()
                .arrivalDate(-1,Month.FEBRUARY,29)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingWithNonExistingDepartureDate() {
        Booking booking = sampleFirstBookingBuilder()
                .departureDate(-1,Month.FEBRUARY,29)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingNegativePrice() {
        Booking booking = sampleFirstBookingBuilder()
                .price(-1)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingZeroPrice() {
        Booking booking = sampleFirstBookingBuilder()
                .price(0)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createBookingWithArrivalDateAfterDepartureDate() {
        Booking booking = sampleFirstBookingBuilder()
                .arrivalDate(2016,Month.FEBRUARY,18)
                .departureDate(2016,Month.FEBRUARY,12)
                .build();
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                .isInstanceOf(ValidationException.class);
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

    @Test(expected = IllegalArgumentException.class)
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


    @Test(expected = IllegalArgumentException.class)
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
        testUpdateBooking((booking) -> booking.setArrivalDate(LocalDate.of(2016,Month.DECEMBER,12)));
    }

    @Test
    public void updateBookingDepartureDate() {
        testUpdateBooking((booking) -> booking.setDepartureDate(LocalDate.of(2016,Month.DECEMBER,19)));
    }

    @Test
    public void updateNonExistingBooking() {
        Booking booking = sampleFirstBookingBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullGuest() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setGuest(null);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullRoom() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setRoom(null);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullArrivalDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setArrivalDate(null);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }
    @Test
    public void updateBookingWithNonExistingArrivalDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setArrivalDate(LocalDate.of(-1,Month.APRIL,22));
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNullDepartureDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setDepartureDate(null);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNonExistingDepartureDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setDepartureDate(LocalDate.of(-1,Month.APRIL,22));
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithArrivalDateAfterDepartureDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setArrivalDate(booking.getDepartureDate().plusDays(1));
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithDepartureDateBeforeArrivalDate() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setDepartureDate(booking.getDepartureDate().minusDays(1));
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithNegativePrice() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setPrice(-1);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }

    @Test
    public void updateBookingWithZeroPrice() {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        booking.setPrice(0);
        expectedException.expect(IllegalEntityException.class);
        bookingManager.updateBooking(booking);
    }




    @Test
    public void findAllBookings() {
        assertThat(bookingManager.findAllBookings()).isEmpty();

        Booking firstBooking = sampleFirstBookingBuilder().build();
        Booking secondBooking = sampleSecondBookingBuilder().build();

        bookingManager.createBooking(firstBooking);
        bookingManager.createBooking(secondBooking);

        assertThat(bookingManager.findAllBookings())
                .usingFieldByFieldElementComparator()
                .containsOnly(firstBooking,secondBooking);

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
                .containsOnly(firstBooking,secondBooking);

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
                .containsOnly(firstBooking,secondBooking);

    }

    @Test
    public void createBookingWithSqlExceptionThrown() throws SQLException {
        // Create sqlException, which will be thrown by our DataSource mock
        // object to simulate DB operation failure
        SQLException sqlException = new SQLException();
        // Create DataSource mock object
        DataSource failingDataSource = mock(DataSource.class);
        // Instruct our DataSource mock object to throw our sqlException when
        // DataSource.getConnection() method is called.
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        // Configure our manager to use DataSource mock object
        bookingManager.setDataSource(failingDataSource);

        // Create Booking instance for our test
        Booking booking = sampleFirstBookingBuilder().build();

        // Try to call guestManager.createGuest(Guest) method and expect that exception
        // will be thrown
        assertThatThrownBy(() -> bookingManager.createBooking(booking))
                // Check that thrown exception is ServiceFailureException
                .isInstanceOf(ServiceFailureException.class)
                // Check if cause is properly set
                .hasCause(sqlException);
    }


    private void testExpectedServiceFailureException(BookingManagerImplTest.Operation<BookingManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        bookingManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(bookingManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateBookingWithSqlExceptionThrown() throws SQLException {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        testExpectedServiceFailureException((bookingManager) -> bookingManager.updateBooking(booking));
    }

    @Test
    public void getBookingByIdWithSqlExceptionThrown() throws SQLException {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        testExpectedServiceFailureException((bookingManager) -> bookingManager.getBookingById(booking.getId()));
    }

    @Test
    public void findAllBookingsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(BookingManager::findAllBookings);
    }

    @Test
    public void deleteBookingWithSqlExceptionThrown() throws SQLException {
        Booking booking = sampleFirstBookingBuilder().build();
        bookingManager.createBooking(booking);
        testExpectedServiceFailureException((bookingManager) -> bookingManager.deleteBooking(booking));
    }

    @Test
    public void findAllBookingsOfGuestWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((bookingManager) ->
                bookingManager.findAllBookingsOfGuest(sampleSamantha2GuestBuilder().build()));
    }

    @Test
    public void findAllBookingsOfRoomWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((bookingManager) ->
                bookingManager.findAllBookingsOfRoom(sampleBigRoomBuilder().build()));
    }


}