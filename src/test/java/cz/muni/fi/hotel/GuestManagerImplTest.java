package cz.muni.fi.hotel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.*;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityNotFoundException;
import static org.junit.Assert.*;


/**
 * Created by User on 8.3.2017.
 */

public class GuestManagerImplTest {

   /* private static final Comparator<Guest> GUEST_ID_COMPARATOR =
            (g1, g2) -> g1.getId().compareTo(g2.getId());*/
    private GuestManagerImpl guestManager;
    private CharSequence correctNumber = "+0123456789";

    @Before
    public void setUp() throws SQLException {

        guestManager = new GuestManagerImpl();

    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void createGuest() throws Exception {
        Guest guest = newGuest("Jozef Novy", LocalDate.of(1970,11,11),"+421911888777");
        guestManager.createGuest(guest);

        Long guestId = guest.getId();
        assertNotNull(guestId);
        Guest result = guestManager.findGuestById(guestId);
        assertEquals(guest,result);
        assertNotSame(guest,result);
        assertDeepEquals(guest,result);

    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullGuest() {
        guestManager.createGuest(null);
    }


    @Test
    public void createGuestWithExistingId() {
        Guest guest = newGuest("Peter Novák",LocalDate.of(1970,11,11),"+420555666777");
        guest.setId(1L);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }



    @Test
    public void createGuestWithNullName() {
        Guest guest = newGuest(null, LocalDate.of(1970,11,11), "+42199977788");
        guestManager.createGuest(guest);
        Guest result = guestManager.findGuestById(guest.getId());
        assertNotNull(result);
        assertNotNull(result.getName());
    }

    @Test
    public void createGuestWithWrongName() {
        Guest guest = newGuest("Peter2 123", LocalDate.of(1970,11,11), "+42199977788");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithNullPhoneNumber() {
        Guest guest = newGuest("Jozef Celer",LocalDate.of(1970,11,11),null);
        guestManager.createGuest(guest);
        Guest result = guestManager.findGuestById(guest.getId());
        assertNotNull(result);
        assertNotNull(result.getPhoneNumber());
    }

    @Test
    public void createGuestWithWrongPhoneNumber() {
        Guest guest = newGuest("Jozef Celer", LocalDate.of(1970,11,11),"+12a871654");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithTooLongPhoneNumber() {
        Guest guest = newGuest("Jozef Celer", LocalDate.of(1970,11,11),"+12348716542262");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithTooShortPhoneNumber() {
        Guest guest = newGuest("Jozef Celer", LocalDate.of(1970,11,11),"+12348716");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithFutureDate(){
        Guest guest = newGuest("Jozef Celer", LocalDate.of(2020,11,11), "+421911222333");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithWrongDate(){
        Guest guest = newGuest("Jozef Celer", LocalDate.of(2000,13,30), "+421911222333");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }






    @Test
    public void deleteGuest() throws Exception {

        Guest g1 = newGuest( "Radoslav Bob",LocalDate.of(1977,11,11),"+421911222555");
        Guest g2 = newGuest("Radoslava Boba",LocalDate.of(1978,12,12),"+421911444555");
        guestManager.createGuest(g1);
        guestManager.createGuest(g2);

        assertNotNull(guestManager.findGuestById(g1.getId()));
        assertNotNull(guestManager.findGuestById(g2.getId()));

        guestManager.deleteGuest(g1);

        assertNull(guestManager.findGuestById(g1.getId()));
        assertNotNull(guestManager.findGuestById(g2.getId()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullGuest() {
        guestManager.deleteGuest(null);
    }


    @Test
    public void deleteGuestWithNullId() {
        Guest guest = newGuest("Michal Bob",LocalDate.of(1970,11,11),"+420777888999");
        guest.setId(Long.parseLong(null));
        expectedException.expect(IllegalArgumentException.class);
        guestManager.deleteGuest(guest);
    }


    @Test
    public void deleteGuestWithNonExistingId() {
        Guest guest = newGuest("Michal Bob",LocalDate.of(1977,11,11),"+420777888999");
        guest.setId(1L);
        expectedException.expect(EntityNotFoundException.class);
        guestManager.deleteGuest(guest);

    }



    @Test
    public void updateGuestInformation() {
        Guest guest = newGuest("Peter Tamten",LocalDate.of(1977,11,11),"+420111888777");
        Guest anotherGuest = newGuest("Petra Tamta",LocalDate.of(1988,12,12), "+421944552558");
        guestManager.createGuest(guest);
        guestManager.createGuest(anotherGuest);
        Long guestId = guest.getId();

        guest = guestManager.findGuestById(guestId);
        guest.setName("Pavol Bežný");
        guestManager.updateGuestInformation(guest);
        assertEquals("Pavol Bežný",guest.getName());
        assertEquals(LocalDate.of(1977,11,11),guest.getDateOfBirth());
        assertEquals("+420111888777", guest.getPhoneNumber());

        guest = guestManager.findGuestById(guestId);
        guest.setDateOfBirth(LocalDate.of(1988,11,11));
        guestManager.updateGuestInformation(guest);
        assertEquals("Pavol Bežný",guest.getName());
        assertEquals(LocalDate.of(1988,11,11),guest.getDateOfBirth());
        assertEquals("+420111888777", guest.getPhoneNumber());

        guest = guestManager.findGuestById(guestId);
        guest.setPhoneNumber("+420777777777");
        guestManager.updateGuestInformation(guest);
        assertEquals("Pavol Bežný",guest.getName());
        assertEquals(LocalDate.of(1988,11,11),guest.getDateOfBirth());
        assertEquals("+420777777777", guest.getPhoneNumber());



        // Check if updates didn't affected other records
        assertDeepEquals(anotherGuest, guestManager.findGuestById(anotherGuest.getId()));


    }


    @Test(expected = IllegalArgumentException.class)
    public void updateNullGuest() {
        guestManager.updateGuestInformation(null);
    }

    @Test
    public void updateGuestWithNullId() {
        Guest guest = newGuest( "Michal Novák", LocalDate.of(1980,11,13), "+420999888777");
        guestManager.createGuest(guest);
        guest.setId(Long.parseLong(null));
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);

    }


    @Test
    public void updateGuestWithNonExistingId() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setId(guest.getId() + 1);
        expectedException.expect(EntityNotFoundException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber("+420a12456789");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithTooLongPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber("+42033312456789");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithTooShortPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber("+4203331245");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber(null);
        Guest result = guestManager.findGuestById(guest.getId());
        guestManager.updateGuestInformation(guest);
        assertNotNull(result);
        assertNotNull(result.getName());
    }

    @Test
    public void updateGuestWithWrongName() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setName("Michal2 124");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullName() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(15,5,1988),"+420777125551");
        guestManager.createGuest(guest);
        guest.setName(null);
        Guest result = guestManager.findGuestById(guest.getId());
        guestManager.updateGuestInformation(guest);
        assertNotNull(result);
        assertNotNull(result.getPhoneNumber());
    }






    @Test
    public void findAllGuests() throws Exception {
        assertTrue(guestManager.findAllGuests().isEmpty());

        Guest g1 = newGuest("Pavol Rychlý",LocalDate.of(1977,11,11),"+42193555777");
        Guest g2 = newGuest("Petra Bystrá",LocalDate.of(1978,11,11),"+420777888999");

        guestManager.createGuest(g1);
        guestManager.createGuest(g2);

        List<Guest> expected = Arrays.asList(g1,g2);
        List<Guest> actual = guestManager.findAllGuests();

        /*actual.sort(GUEST_ID_COMPARATOR);
        expected.sort(GUEST_ID_COMPARATOR);*/

        assertEquals(expected,actual);
        assertDeepEquals(expected,actual);
    }



    @Test
    public void findGuestById() {
        Guest guest = newGuest("Peter Celer",LocalDate.of(1977,11,11),"+420111444777");
        guestManager.createGuest(guest);
        Guest result = guestManager.findGuestById(guest.getId());
        assertEquals(guest,result);
        assertDeepEquals(guest,result);

    }

    @Test
    public void findGuestByName() throws Exception {

    }

    private void assertDeepEquals(List<Guest> expectedList, List<Guest> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Guest expected = expectedList.get(i);
            Guest actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }


    private void assertDeepEquals(Guest expected, Guest actual) {

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());

    }

    private static Guest newGuest(String name, LocalDate dateOfBirth, String phoneNumber) {
        Guest guest = new Guest();
        guest.setDateOfBirth(dateOfBirth);
        guest.setName(name);
        guest.setPhoneNumber(phoneNumber);
        return guest;
    }



}


