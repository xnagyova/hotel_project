package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.IllegalEntityException;
import cz.muni.fi.hotel.common.ServiceFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.*;
import java.sql.SQLException;
import java.time.*;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static java.time.Month.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import javax.xml.bind.ValidationException;



/**
 * @author kkatanik & snagyova
 */

public class GuestManagerImplTest {


    private GuestManagerImpl guestManager;
    private DataSource ds;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        // we will use in memory database
        ds.setDatabaseName("memory:guestmgr-test");
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
        DBUtils.executeSqlScript(ds,GuestManager.class.getResource("createTables.sql"));
        guestManager = new GuestManagerImpl(prepareClockMock(TODAY));
        guestManager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(ds,GuestManager.class.getResource("dropTables.sql"));
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

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


    @Test
    public void createGuest() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);

        Long guestId = guest.getId();
        assertThat(guestId).isNotNull();

        assertThat(guestManager.findGuestById(guestId))
                .isNotSameAs(guest)
                .isEqualToComparingFieldByField(guest);

    }


    @Test(expected = IllegalArgumentException.class)
    public void createNullGuest() {
        guestManager.createGuest(null);
    }

    @Test
    public void createGuestBornTomorrow() {

        LocalDate tomorrow = TODAY.toLocalDate().plusDays(1);
        Guest guest =sampleJohnGuestBuilder()
                .dateOfBirth(tomorrow.getYear(),tomorrow.getMonth(),tomorrow.getDayOfMonth())
                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createGuestBornToday() {
        Guest guest = sampleJohnGuestBuilder()
                .dateOfBirth(TODAY.getYear(),TODAY.getMonth(),TODAY.getDayOfMonth())
                .build();
        guestManager.createGuest(guest);

        assertThat(guestManager.findGuestById(guest.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(guest);

    }


    @Test
    public void createGuestWithNullName() {
        Guest guest = sampleJohnGuestBuilder()
                .name(null)
                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }


    @Test
    public void createGuestWithNonExistingYearOfBirth() {
        Guest guest = sampleJohnGuestBuilder()
                .dateOfBirth(-1,Month.DECEMBER,1)
                .build();

        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }


    @Test
    public void createGuestWithNonExistingDayOfBirth() {
        Guest guest = sampleJohnGuestBuilder()
                .dateOfBirth(1998,Month.FEBRUARY,32)
                .build();

        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }



    @Test
    public void createGuestWithNullPhoneNumber() {
        Guest guest = sampleJohnGuestBuilder()
                .phoneNumber(null)
                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void createGuestWithWrongPhoneNumber() {

        Guest guest = sampleJohnGuestBuilder()
                .phoneNumber("+12a871654")
                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(ValidationException.class);
    }




    @Test
    public void deleteGuest() {

        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);

        assertThat(guestManager.findGuestById(john.getId())).isNotNull();
        assertThat(guestManager.findGuestById(samantha.getId())).isNotNull();

        guestManager.deleteGuest(john);
        assertThat(guestManager.findGuestById(john.getId())).isNull();
        assertThat(guestManager.findGuestById(samantha.getId())).isNotNull();

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullGuest() {
        guestManager.deleteGuest(null);
    }



    @Test
    public void updateGuestInformation() {
        Guest guestToUpdate = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();
        guestManager.createGuest(guestToUpdate);
        guestManager.createGuest(samantha);

        guestToUpdate.setName("Peter Clock");

        guestManager.updateGuestInformation(guestToUpdate);

        assertThat(guestManager.findGuestById(guestToUpdate.getId()))
                .isEqualToComparingFieldByField(guestToUpdate);

        assertThat(guestManager.findGuestById(samantha.getId()))
                .isEqualToComparingFieldByField(samantha);



    }

    @FunctionalInterface
    private interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateGuestInformation(Operation<Guest> updateOperation) {
        Guest johnToUpdate = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();
        guestManager.createGuest(johnToUpdate);
        guestManager.createGuest(samantha);

        updateOperation.callOn(johnToUpdate);

        guestManager.updateGuestInformation(johnToUpdate);
        assertThat(guestManager.findGuestById(johnToUpdate.getId()))
                .isEqualToComparingFieldByField(johnToUpdate);

        assertThat(guestManager.findGuestById(samantha.getId()))
                .isEqualToComparingFieldByField(samantha);
    }


    @Test(expected = IllegalArgumentException.class)
    public void updateNullGuest() {
        guestManager.updateGuestInformation(null);
    }

    @Test
    public void updateGuestName() {
        testUpdateGuestInformation((guest) -> guest.setName("Stephen Duke"));

    }

    @Test
    public void updateGuestDateOfBirth() {
        testUpdateGuestInformation((guest) -> guest.setDateOfBirth(LocalDate.of(1999,Month.DECEMBER,12)));
    }

    @Test
    public void updateGuestPhoneNumber() {
        testUpdateGuestInformation((guest) -> guest.setPhoneNumber("+421947362584"));

    }

    @Test
    public void updateNonExistingGuest() {
        Guest guest = sampleJohnGuestBuilder().id(1L).build();
        expectedException.expect(IllegalEntityException.class);
        guestManager.updateGuestInformation(guest);
    }



    @Test
    public void updateGuestWithWrongPhoneNumber() {
        Guest guest = sampleJohnGuestBuilder().phoneNumber("+420777125551").build();
        guestManager.createGuest(guest);
        guest.setPhoneNumber("+420a12456789");

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }




    @Test
    public void updateGuestWithNullPhoneNumber() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setPhoneNumber(null);

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullDateOfBirth() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(null);

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }


    @Test
    public void updateGuestWithTomorrowDateOfBirth() {
        LocalDate tomorrow = TODAY.toLocalDate().plusDays(1);
        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.APRIL,15).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(tomorrow);

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongDayOfBirth() {
        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.APRIL,15).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(1998,Month.JANUARY,32));

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);

    }


    @Test
    public void updateGuestWithWrongYearOfBirth() {
        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.APRIL,15).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(-1,Month.JANUARY,30));

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullName() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setName(null);

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }






    @Test
    public void findAllGuests(){

        assertThat(guestManager.findAllGuests()).isEmpty();

        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);

        assertThat(guestManager.findAllGuests())
                .usingFieldByFieldElementComparator()
                .containsOnly(john,samantha);
    }



    @Test
    public void findGuestById() {


        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);
        Long johnId = john.getId();

        assertThat(guestManager.findGuestById(johnId))
                .isEqualToComparingFieldByField(john);

    }

    @Test
    public void findGuestByName() throws Exception {

        assertThat(guestManager.findAllGuests()).isEmpty();

        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();
        Guest anotherSamantha = sampleSamantha2GuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);
        guestManager.createGuest(anotherSamantha);

        assertThat(guestManager.findGuestByName(samantha.getName()))
                .usingFieldByFieldElementComparator()
                .containsOnly(samantha,anotherSamantha);

    }

    @Test
    public void createGuestWithSqlExceptionThrown() throws SQLException {
        // Create sqlException, which will be thrown by our DataSource mock
        // object to simulate DB operation failure
        SQLException sqlException = new SQLException();
        // Create DataSource mock object
        DataSource failingDataSource = mock(DataSource.class);
        // Instruct our DataSource mock object to throw our sqlException when
        // DataSource.getConnection() method is called.
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        // Configure our manager to use DataSource mock object
        guestManager.setDataSource(failingDataSource);

        // Create Guest instance for our test
        Guest guest = sampleJohnGuestBuilder().build();

        // Try to call guestManager.createGuest(Guest) method and expect that exception
        // will be thrown
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                // Check that thrown exception is ServiceFailureException
                .isInstanceOf(ServiceFailureException.class)
                // Check if cause is properly set
                .hasCause(sqlException);
    }

    // Now we want to test also other methods of GuestManager. To avoid having
    // couple of method with lots of duplicit code, we will use the similar
    // approach as with testUpdateGuest(Operation) method.

    private void testExpectedServiceFailureException(Operation<GuestManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        guestManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(guestManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateBodyWithSqlExceptionThrown() throws SQLException {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        testExpectedServiceFailureException((guestManager) -> guestManager.updateGuestInformation(guest));
    }

    @Test
    public void findGuestByIdWithSqlExceptionThrown() throws SQLException {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        testExpectedServiceFailureException((guestManager) -> guestManager.findGuestById(guest.getId()));
    }

    @Test
    public void findGuestByNameWithSqlExceptionThrown() throws SQLException {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        testExpectedServiceFailureException((guestManager) -> guestManager.findGuestByName(guest.getName()));
    }

    @Test
    public void deleteGuestWithSqlExceptionThrown() throws SQLException {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        testExpectedServiceFailureException((guestManager) -> guestManager.deleteGuest(guest));
    }

    @Test
    public void findAllGuestsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(GuestManager::findAllGuests);
    }





}


