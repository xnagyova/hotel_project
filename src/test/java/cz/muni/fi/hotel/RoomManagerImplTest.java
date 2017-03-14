package cz.muni.fi.hotel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by ${KristianKatanik} on 11.03.2017.
 */
public class RoomManagerImplTest {

    private RoomManagerImpl roomManager;

    @Before
    public void setUp() throws Exception {

        roomManager = new RoomManagerImpl();

    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void buildRoom(){

        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);

        Long roomId = room.getId();
        assertNotNull(roomId);
        Room result = roomManager.findRoomById(roomId);
        assertEquals(room,result);
        assertNotSame(room,result);
        assertDeepEquals(room,result);

    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullRoom() {
        roomManager.buildRoom(null);
    }


    @Test
    public void buildRoomWithExistingId() {
        Room room = newRoom(5,5,true);
        room.setId(1L);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.buildRoom(room);
    }


    @Test
    public void buildRoomWithNegativeFloor() {
        Room room = newRoom(-1,5,true);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.buildRoom(room);
    }


    @Test
    public void buildRoomWithNegativeCapacity() {
        Room room = newRoom(5,-1,true);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.buildRoom(room);
    }



    @Test
    public void buildRoomWithZeroCapacity() {
        Room room = newRoom(5,0,true);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.buildRoom(room);
    }



    @Test
    public void updateRoomInformation() throws Exception {
        Room room = newRoom(5,5,true);
        Room anotherRoom = newRoom(4,4,false);
        roomManager.buildRoom(room);
        roomManager.buildRoom(anotherRoom);
        Long roomId = room.getId();

        room = roomManager.findRoomById(roomId);
        room.setFloorNumber(0);
        roomManager.updateRoomInformation(room);
        assertEquals(0,room.getFloorNumber());
        assertEquals(5,room.getCapacity());
        assertEquals(true,room.isBalcony());

        room = roomManager.findRoomById(roomId);
        room.setCapacity(1);
        roomManager.updateRoomInformation(room);
        assertEquals(0,room.getFloorNumber());
        assertEquals(1,room.getCapacity());
        assertEquals(true,room.isBalcony());

        room = roomManager.findRoomById(roomId);
        room.setBalcony(false);
        roomManager.updateRoomInformation(room);
        assertEquals(0,room.getFloorNumber());
        assertEquals(1,room.getCapacity());
        assertEquals(false,room.isBalcony());


        // Check if updates didn't affected other records
        assertDeepEquals(anotherRoom, roomManager.findRoomById(anotherRoom.getId()));

    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullRoom() {
        roomManager.updateRoomInformation(null);
    }


    @Test
    public void updateRoomWithNullId() {
        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);
        room.setId(Long.parseLong(null));
        expectedException.expect(IllegalArgumentException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithNonExistingId() {
        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);
        room.setId(room.getId() + 1);
        expectedException.expect(EntityNotFoundException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithNegativeFloor() {
        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);
        room.setFloorNumber(-1);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithZeroCapacity() {
        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);
        room.setCapacity(0);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithNegativeCapacity() {
        Room room = newRoom(5,5,true);
        roomManager.buildRoom(room);
        room.setCapacity(-1);
        expectedException.expect(IllegalArgumentException.class);
        roomManager.updateRoomInformation(room);
    }

    @Test
    public void deleteRoom()  {
        Room r1 = newRoom(5,5,true);
        Room r2 = newRoom(4,4,false);
        roomManager.buildRoom(r1);
        roomManager.buildRoom(r2);

        assertNotNull(roomManager.findRoomById(r1.getId()));
        assertNotNull(roomManager.findRoomById(r2.getId()));

        roomManager.deleteRoom(r1);

        assertNotNull(roomManager.findRoomById(r2.getId()));
        assertNull(roomManager.findRoomById(r1.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullRoom() {
        roomManager.deleteRoom(null);
    }


    @Test
    public void deleteRoomWithNullId() {
        Room room = newRoom(5,5,true);
        room.setId(Long.parseLong(null));
        expectedException.expect(IllegalArgumentException.class);
        roomManager.deleteRoom(room);
    }


    @Test
    public void deleteRoomWithNonExistingId() {
        Room room = newRoom(5,5,true);
        room.setId(1L);
        expectedException.expect(EntityNotFoundException.class);
        roomManager.deleteRoom(room);
    }


    @Test
    public void findFreeRoom() throws Exception {

    }

    @Test
    public void findRoomById() throws Exception {

    }

    @Test
    public void listAllRooms() {
        assertTrue(roomManager.listAllRooms().isEmpty());

        Room r1 = newRoom(5,5,true);
        Room r2 = newRoom(4,4,false);

        roomManager.buildRoom(r1);
        roomManager.buildRoom(r2);

        List<Room> expected = Arrays.asList(r1,r2);
        List<Room> actual = roomManager.listAllRooms();

        /*actual.sort(ROOM_ID_COMPARATOR);
        expected.sort(ROOM_ID_COMPARATOR);*/

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);

    }

    private static Room newRoom(int floorNumber, int capacity, boolean balcony) {
        Room room = new Room();
        room.setFloorNumber(floorNumber);
        room.setCapacity(capacity);
        room.setBalcony(balcony);
        return room;
    }


    private void assertDeepEquals(List<Room> expectedList, List<Room> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Room expected = expectedList.get(i);
            Room actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }


    private void assertDeepEquals(Room expected, Room actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFloorNumber(), actual.getFloorNumber());
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.isBalcony(), actual.isBalcony());
    }

    /*private static final Comparator<Room> ROOM_ID_COMPARATOR =
            (r1, r2) -> r1.getId().compareTo(r2.getId());*/

}