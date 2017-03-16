package cz.muni.fi.hotel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.*;

import java.sql.SQLException;
import java.time.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.rules.ExpectedException;
import static org.assertj.core.api.Assertions.*;

import javax.xml.bind.ValidationException;

import static org.junit.Assert.*;


/**
 * @author kkatanik & snagyova
 */

public class GuestManagerImplTest {


    private GuestManagerImpl guestManager;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));

    @Before
    public void setUp() throws SQLException {

        guestManager = new GuestManagerImpl();

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
        expectedException.expect(ValidationException.class);
        guestManager.createGuest(guest);
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

        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithNonExistingDayOfBirth() {
        Guest guest = sampleJohnGuestBuilder()
                .dateOfBirth(1998,Month.FEBRUARY,32)
                .build();

        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
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

        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
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
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateGuestInformation(GuestManagerImplTest.Operation<Guest> updateOperation) {
        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();
        guestManager.createGuest(john);
        guestManager.createGuest(john);

        updateOperation.callOn(john);

        guestManager.updateGuestInformation(john);
        assertThat(guestManager.findGuestById(john.getId()))
                .isEqualToComparingFieldByField(john);

        assertThat(guestManager.findGuestById(samantha.getId()))
                .isEqualToComparingFieldByField(samantha);
    }


    @Test(expected = IllegalArgumentException.class)
    public void updateNullGuest() {
        guestManager.updateGuestInformation(null);
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
        guest.setDateOfBirth(LocalDate.of(1998,11,32));

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);

    }

    @Test
    public void updateGuestWithWrongMonthOfBirth() {
        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.APRIL,15).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(1998,13,32));

        expectedException.expect(ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongYearOfBirth() {
        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.APRIL,15).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(-1,11,32));

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

        assertThat(guestManager.findGuestById(john.getId()))
                .usingComparatorForFields(GUEST_ID_COMPARATOR)
                .isSameAs(john);

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


    private static final Comparator<Guest> GUEST_ID_COMPARATOR =
            (g1, g2) -> g1.getId().compareTo(g2.getId());



}


