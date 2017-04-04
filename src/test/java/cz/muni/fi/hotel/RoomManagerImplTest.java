package cz.muni.fi.hotel;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;


import static org.assertj.core.api.Assertions.*;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;

/**
 * @author kkatanik & snagyova
 */

@RunWith(SpringJUnit4ClassRunner.class) //Spring se zúčastní unit testů
@ContextConfiguration(classes = {MySpringTestConfig.class}) //konfigurace je ve třídě MySpringTestConfig
public class RoomManagerImplTest {

    @Autowired
    private RoomManager roomManager;
    private final static ZonedDateTime TODAY= LocalDateTime.now().atZone(ZoneId.of("UTC"));


    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        // we will use in memory database
        ds.setDatabaseName("memory:hotelmgr-test");
        // database is created automatically if it does not exist yet
        ds.setCreateDatabase("create");
        return ds;
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
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
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
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }



    @Test
    public void buildRoomWithZeroCapacity() {
        Room room = sampleBigRoomBuilder().capacity(0).build();
        assertThatThrownBy(() -> roomManager.buildRoom(room))
                .isInstanceOf(cz.muni.fi.hotel.common.ValidationException.class);
    }

    @FunctionalInterface
    private interface Operation<T> {
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
        testUpdateRoomInformation((room) -> {
                room.setFloorNumber(4);

        });
    }

    @Test
    public void updateRoomCapacity() {
        testUpdateRoomInformation((room) -> {
                room.setCapacity(4);



        });
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
    public void updateRoomOnNegativeFloor()  {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setFloorNumber(-1);
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithZeroCapacity()  {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setCapacity(0);
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        roomManager.updateRoomInformation(room);
    }


    @Test
    public void updateRoomWithNegativeCapacity()  {
        Room room = sampleBigRoomBuilder().build();
        roomManager.buildRoom(room);
        room.setCapacity(-1);
        expectedException.expect(cz.muni.fi.hotel.common.ValidationException.class);
        roomManager.updateRoomInformation(room);
    }

    @Test
    public void deleteRoom()  {
        Room room1 = sampleBigRoomBuilder().build();
        Room room2 = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room1);
        roomManager.buildRoom(room2);
        Long room1Id = room1.getId();
        roomManager.deleteRoom(roomManager.findRoomById(room1.getId()));
        try {
            roomManager.findRoomById(room1Id);
            fail("room 1 not deleted");
        } catch (EmptyResultDataAccessException e) {

        }
        assertThat(roomManager.findRoomById(room2.getId())).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullRoom() {
        roomManager.deleteRoom(null);
    }




    @Test
    public void findRoomById() {

        Room bigRoom = sampleBigRoomBuilder().build();
        Room smallRoom = sampleSmallRoomBuilder().build();

        roomManager.buildRoom(bigRoom);
        roomManager.buildRoom(smallRoom);

        Long bigRoomId = bigRoom.getId();

        assertThat(roomManager.findRoomById(bigRoomId))
                .isEqualToComparingFieldByField(bigRoom);
    }
    @Before

    @Test
    public void listAllRooms() {


        Room r1 = sampleBigRoomBuilder().build();
        Room r2 = sampleSmallRoomBuilder().build();

        roomManager.buildRoom(r1);
        roomManager.buildRoom(r2);


        assertThat(roomManager.listAllRooms())
                .usingFieldByFieldElementComparator()
                .contains(r1,r2);


    }



}