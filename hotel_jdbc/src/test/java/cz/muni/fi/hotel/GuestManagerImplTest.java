package cz.muni.fi.hotel;


import org.junit.Rule;
import org.junit.Test;

import java.lang.*;

import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.assertj.core.api.Assertions.*;

import java.time.*;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;



/**
 * @author kkatanik & snagyova
 */
@RunWith(SpringJUnit4ClassRunner.class) //Spring se zúčastní unit testů
@ContextConfiguration(classes = {MySpringTestConfig.class}) //konfigurace je ve třídě MySpringTestConfig
public class GuestManagerImplTest {

    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));
    private final static ZonedDateTime TOMORROW = TODAY.plusDays(1);


    @Autowired
    private GuestManager guestManager;




    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private GuestBuilder sampleJohnGuestBuilder() {
        return new GuestBuilder()
                .name("John Fox")
                .dateOfBirth(1989, Month.DECEMBER,5)
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


    @Test(expected = NullPointerException.class)
    public void createNullGuest() {
        guestManager.createGuest(null);
    }

    @Test
    public void createGuestBornTomorrow() {

        Guest guest =sampleJohnGuestBuilder()
                .dateOfBirth(TOMORROW.getYear(),TOMORROW.getMonth(),TOMORROW.getDayOfMonth())

                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }

    @Test
    public void createGuestBornToday() {
        Guest guest = sampleJohnGuestBuilder()
                .dateOfBirth(2000,Month.OCTOBER,5)
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
                .isInstanceOf(IllegalArgumentException.class);
    }





    @Test
    public void createGuestWithNullPhoneNumber() {
        Guest guest = sampleJohnGuestBuilder()
                .phoneNumber(null)
                .build();
        assertThatThrownBy(() -> guestManager.createGuest(guest))
                .isInstanceOf(IllegalArgumentException.class);
    }



    @Test
    public void deleteGuest() {
        Guest guest = sampleJohnGuestBuilder().build();
        Guest secondGuest = sampleSamanthaGuestBuilder().build();
        guestManager.createGuest(guest);
        guestManager.createGuest(secondGuest);
        Long guestId = guest.getId();
        guestManager.deleteGuest(guest);
        try {
            guestManager.findGuestById(guestId);
            fail("guest 1 not deleted");
        } catch (EmptyResultDataAccessException e) {
            //no code
        }
        assertThat(guestManager.findGuestById(secondGuest.getId())).isNotNull();
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
        testUpdateGuestInformation((guest) -> guest.setDateOfBirth(LocalDate.of(1999,12,12)));
    }

    @Test
    public void updateGuestPhoneNumber() {
        testUpdateGuestInformation((guest) -> guest.setPhoneNumber("+421947362584"));

    }




    @Test
    public void updateGuestWithNullPhoneNumber() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setPhoneNumber(null);

        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullDateOfBirth() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(null);

        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }


    @Test
    public void updateGuestWithTomorrowDateOfBirth() {

        Guest guest = sampleJohnGuestBuilder().dateOfBirth(1998,Month.JANUARY,1).build();
        guestManager.createGuest(guest);
        guest.setDateOfBirth(TOMORROW.toLocalDate());

        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        guestManager.updateGuestInformation(guest);
    }




    @Test
    public void updateGuestWithNullName() {
        Guest guest = sampleJohnGuestBuilder().build();
        guestManager.createGuest(guest);
        guest.setName(null);

        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }






    @Test
    public void findAllGuests(){


        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);

        assertThat(guestManager.findAllGuests())
                .usingFieldByFieldElementComparator()
                .contains(john,samantha);
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


        Guest john = sampleJohnGuestBuilder().build();
        Guest samantha = sampleSamanthaGuestBuilder().build();
        Guest anotherSamantha = sampleSamantha2GuestBuilder().build();

        guestManager.createGuest(john);
        guestManager.createGuest(samantha);
        guestManager.createGuest(anotherSamantha);

        assertThat(guestManager.findGuestByName(samantha.getName()))
                .usingFieldByFieldElementComparator()
                .contains(samantha,anotherSamantha);

    }





}


