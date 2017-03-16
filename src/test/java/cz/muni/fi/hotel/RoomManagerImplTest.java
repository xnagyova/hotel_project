package cz.muni.fi.hotel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.bind.ValidationException;


import static org.assertj.core.api.Assertions.*;

/**
 * @author kkatanik & snagyova
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
    public void buildRoom(){

        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);

        Long roomId = room.getId();
        assertThat(roomId).isNotNull();

        assertThat(roomManager.findRoomById(roomId))
                .isNotSameAs(room)
                .isEqualToComparingFieldByField(room);

    }

    @Test(expected = IllegalArgumentException.class)
    public void buildNullRoom() {
        roomManager.buildRoom(null);
    }



    @Test
    public void buildRoomOnNegativeFloor() {
        Room room = sampleBigRoomBuilder().floorNumber(-1).build();
        assertThatThrownBy(() -> roomManager.buildRoom(room))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void buildRoomOnZeroFloor() {
        Room room = sampleBigRoomBuilder().floorNumber(0).build();
        roomManager.buildRoom(room);
        assertThat(roomManager.findRoomById(room.getId()))
                .isNotNull()
                .isEqualToComparingFieldByField(room);
    }

    @Test
    public void buildRoomWithNegativeCapacity() {
        Room room = sampleBigRoomBuilder().capacity(-1).build();
        assertThatThrownBy(() -> roomManager.buildRoom(room))
                .isInstanceOf(ValidationException.class);
    }



    @Test
    public void buildRoomWithZeroCapacity() {
        Room room = sampleBigRoomBuilder().capacity(0).build();
        expectedException.expect(ValidationException.class);
        roomManager.buildRoom(room);
    }

    @FunctionalInterface
    private static interface Operation<T> {
        void callOn(T subjectOfOperation);
    }

    private void testUpdateRoomInformation(Operation<Room> updateOperation) {
        Room sourceRoom = sampleBigRoomBuilder().build();
        Room anotherRoom = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(sourceRoom);
        roomManager.buildRoom(anotherRoom);

        updateOperation.callOn(sourceRoom);

        roomManager.updateRoomInformation(sourceRoom);
        assertThat(roomManager.findRoomById(sourceRoom.getId()))
                .isEqualToComparingFieldByField(sourceRoom);

        assertThat(roomManager.findRoomById(anotherRoom.getId()))
                .isEqualToComparingFieldByField(anotherRoom);
    }

    @Test
    public void updateRoomFloorNumber() {
        testUpdateRoomInformation((room) -> room.setFloorNumber(4));
    }

    @Test
    public void updateRoomCapacity() {
        testUpdateRoomInformation((room) -> room.setCapacity(4));
    }

    @Test
    public void updateRoomBalcony() {
        testUpdateRoomInformation((room) -> room.setBalcony(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullRoom() {
        roomManager.updateRoomInformation(null);
    }



    @Test
    public void updateRoomOnNegativeFloor() {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setFloorNumber(-1);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithZeroCapacity() {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setCapacity(0);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithNegativeCapacity() {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setCapacity(-1);
        expectedException.expect(ValidationException.class);
        roomManager.updateRoomInformation(room);
    }

    @Test
    public void deleteRoom()  {
        Room r1 = sampleBigRoomBuilder().build();
        Room r2 = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(r1);
        roomManager.buildRoom(r2);

        assertThat(roomManager.findRoomById(r1.getId())).isNotNull();
        assertThat(roomManager.findRoomById(r2.getId())).isNotNull();

        roomManager.deleteRoom(r1);

        assertThat(roomManager.findRoomById(r1.getId())).isNull();
        assertThat(roomManager.findRoomById(r2.getId())).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullRoom() {
        roomManager.deleteRoom(null);
    }



    @Test
    public void findFreeRoom() {
        assertThat(roomManager.findFreeRoom()).isEmpty();

        Room fullRoom1 = sampleBigRoomBuilder().capacity(0).build();
        Room fullRoom2 = sampleSmallRoomBuilder().capacity(0).build();
        Room notFullRoom1 = sampleBigRoomBuilder().capacity(1).build();
        Room notFullRoom2 = sampleSmallRoomBuilder().build();

        roomManager.buildRoom(fullRoom1);
        roomManager.buildRoom(fullRoom2);
        roomManager.buildRoom(notFullRoom1);
        roomManager.buildRoom(notFullRoom2);

        assertThat(roomManager.findFreeRoom())
                .usingFieldByFieldElementComparator()
                .containsOnly(notFullRoom1,notFullRoom2);

    }

    @Test
    public void findRoomById() {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        Long roomId = room.getId();

        assertThat(roomManager.findRoomById(roomId))
                .isEqualTo(room);

    }

    @Test
    public void listAllRooms() {
        assertThat(roomManager.listAllRooms()).isEmpty();

        Room r1 = sampleBigRoomBuilder().build();
        Room r2 = sampleSmallRoomBuilder().build();

        roomManager.buildRoom(r1);
        roomManager.buildRoom(r2);

        assertThat(roomManager.listAllRooms())
                .usingFieldByFieldElementComparator()
                .containsOnly(r1,r2);

    }


}