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

import javax.persistence.EntityNotFoundException;
import javax.xml.bind.ValidationException;

import static org.junit.Assert.*;


/**
 * @author kkatanik & snagyova
 */

public class GuestManagerImplTest {


    private GuestManagerImpl guestManager;
    private final static ZonedDateTime NOW = LocalDateTime.of(2016, 2,25,14,5).atZone(ZoneId.of("UTC"));

    @Before
    public void setUp() throws SQLException {

        guestManager = new GuestManagerImpl();

    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void createGuest() {
        Guest guest = newGuest("John Legend", LocalDate.of(1998, 12, 31), "64536456464");
        guestManager.createGuest(guest);

        Long guestId = guest.getId();
        assertNotNull(guestId);
        Guest result = guestManager.findGuestById(guestId);
        assertEquals(guest, result);
        assertNotSame(guest, result);
        assertDeepEquals(guest, result);

    }


    @Test(expected = IllegalArgumentException.class)
    public void createNullGuest() {
        guestManager.createGuest(null);
    }

    @Test
    public void createGuestBornTomorrow() {

        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Guest guest = newGuest("John Legend", tomorrow, "+421974889962");

        guestManager.createGuest(guest);

        expectedException.expect(ValidationException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestBornToday() {

        LocalDate today = NOW.toLocalDate();
        Guest guest = newGuest("John Legend", today, "+421974889962");

        guestManager.createGuest(guest);

        Long guestId = guest.getId();
        assertNotNull(guestId);
        Guest result = guestManager.findGuestById(guestId);
        assertEquals(guest, result);
        assertNotSame(guest, result);
        assertDeepEquals(guest, result);
    }


    @Test
    public void createGuestWithExistingId() {
        Guest guest = newGuest("Peter Novák", LocalDate.of(1970, 11, 11), "+420555666777");
        guest.setId(1L);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }


    @Test
    public void createGuestWithNullName() {
        Guest guest = newGuest(null, LocalDate.of(1970, 11, 11), "+42199977788");
        guestManager.createGuest(guest);
        Guest result = guestManager.findGuestById(guest.getId());
        assertNotNull(result);
        assertNotNull(result.getName());
    }


    @Test
    public void createGuestWithNullDateOfBirth() {
        Guest guest = newGuest("John Fork", null, "+42199977788");
        guestManager.createGuest(guest);
        Guest result = guestManager.findGuestById(guest.getId());
        assertNotNull(result);
        assertNotNull(result.getDateOfBirth());
    }

    @Test
    public void createGuestWithNonExistingMonthOfBirth() {
        Guest guest = newGuest("John Fork", LocalDate.of(1998,13,1), "+42199977788");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithNonExistingYearOfBirth() {
        Guest guest = newGuest("John Fork", LocalDate.of(-1,12,1), "+42199977788");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
    }

    @Test
    public void createGuestWithNonExistingDayOfBirth() {
        Guest guest = newGuest("John Fork", LocalDate.of(1998,2,30), "+42199977788");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.createGuest(guest);
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
    public void deleteGuest() {

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
        guest.setId(null);
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
        guest.setDateOfBirth(LocalDate.of(1987,11,11));
        guestManager.updateGuestInformation(guest);
        assertEquals("Pavol Bežný",guest.getName());
        assertEquals(LocalDate.of(1987,11,11),guest.getDateOfBirth());
        assertEquals("+420111888777", guest.getPhoneNumber());

        guest = guestManager.findGuestById(guestId);
        guest.setPhoneNumber("+420777777777");
        guestManager.updateGuestInformation(guest);
        assertEquals("Pavol Bežný",guest.getName());
        assertEquals(LocalDate.of(1987,11,11),guest.getDateOfBirth());
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
        guest.setId(null);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);

    }


    @Test
    public void updateGuestWithNonExistingId() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setId(guest.getId() + 1);
        expectedException.expect(EntityNotFoundException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber("+420a12456789");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }




    @Test
    public void updateGuestWithNullPhoneNumber() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setPhoneNumber(null);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullDateOfBirth() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setDateOfBirth(null);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongName() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setName("Michal2 124");
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithTomorrowDateOfBirth() {
        LocalDate tomorrow = NOW.toLocalDate().plusDays(1);
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setDateOfBirth(tomorrow);
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongDayOfBirth() {

        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(1998,11,32));
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongMonthOfBirth() {

        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(1998,13,2));
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithWrongYearOfBirth() {

        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setDateOfBirth(LocalDate.of(-1,10,2));
        expectedException.expect(IllegalArgumentException.class);
        guestManager.updateGuestInformation(guest);
    }

    @Test
    public void updateGuestWithNullName() {
        Guest guest = newGuest("Michal Pekný",LocalDate.of(1998,5,15),"+420777125551");
        guestManager.createGuest(guest);
        guest.setName(null);
        Guest result = guestManager.findGuestById(guest.getId());
        guestManager.updateGuestInformation(guest);
        assertNotNull(result);
        assertNotNull(result.getPhoneNumber());
    }






    @Test
    public void findAllGuests(){
        assertTrue(guestManager.findAllGuests().isEmpty());

        Guest g1 = newGuest("Pavol Rychlý",LocalDate.of(1977,11,11),"+42193555777");
        Guest g2 = newGuest("Petra Bystrá",LocalDate.of(1978,11,11),"+420777888999");

        guestManager.createGuest(g1);
        guestManager.createGuest(g2);

        List<Guest> expected = Arrays.asList(g1,g2);
        List<Guest> actual = guestManager.findAllGuests();

        actual.sort(GUEST_ID_COMPARATOR);
        expected.sort(GUEST_ID_COMPARATOR);

        assertEquals(expected,actual);
        assertDeepEquals(expected,actual);
    }



    @Test
    public void findGuestById() {


        Guest guest = newGuest("Peter Celer",LocalDate.of(1977,11,11),"+420111444777");
        Guest guest2 = newGuest("Peter Celer",LocalDate.of(1975,11,15),"+421987566889");
        guestManager.createGuest(guest);
        guestManager.createGuest(guest2);
        Guest result = guestManager.findGuestById(guest.getId());
        assertEquals(guest,result);
        assertNotEquals(guest2,result);
        assertDeepEquals(guest,result);

    }

    @Test
    public void findGuestByName() throws Exception {

        assertTrue(guestManager.findAllGuests().isEmpty());

        Guest g1 = newGuest("Pavol Rychlý",LocalDate.of(1977,11,11),"+42193555777");
        Guest g2 = newGuest("Petra Bystrá",LocalDate.of(1978,11,11),"+420777888999");
        Guest g3 = newGuest("Petra Bystrá",LocalDate.of(1965,11,22),"+420333222111");
        Guest g4 = newGuest("Petra Bystrá",LocalDate.of(1973,11,7),"+420111999777");
        Guest g5 = newGuest("Petra Bystrá",LocalDate.of(1961,11,13),"+420777888259");

        guestManager.createGuest(g1);
        guestManager.createGuest(g2);
        guestManager.createGuest(g3);
        guestManager.createGuest(g4);
        guestManager.createGuest(g5);

        List<Guest> expected = Arrays.asList(g2,g3, g4, g5);
        List<Guest> actual = guestManager.findGuestByName(g2.getName());

        actual.sort(GUEST_ID_COMPARATOR);
        expected.sort(GUEST_ID_COMPARATOR);

        assertEquals(expected,actual);
        assertDeepEquals(expected,actual);

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

    private static final Comparator<Guest> GUEST_ID_COMPARATOR =
            (g1, g2) -> g1.getId().compareTo(g2.getId());



}


