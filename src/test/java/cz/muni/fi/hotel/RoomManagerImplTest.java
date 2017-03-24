package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.DBUtils;
import cz.muni.fi.hotel.common.ServiceFailureException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.mockito.Mockito.*;

import javax.xml.bind.ValidationException;


import static org.assertj.core.api.Assertions.*;

/**
 * @author kkatanik & snagyova
 */
public class RoomManagerImplTest {

    private RoomManagerImpl roomManager;
    private DataSource ds;

    @Before
    public void setUp() throws SQLException {
        ds = prepareDataSource();
        DBUtils.executeSqlScript(ds,RoomManager.class.getResource("createTables.sql"));
        roomManager = new RoomManagerImpl();
        roomManager.setDataSource(ds);
    }

    @After
    public void tearDown() throws SQLException {
        // Drop tables after each test
        DBUtils.executeSqlScript(ds,RoomManager.class.getResource("dropTables.sql"));
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        // we will use in memory database
        ds.setDatabaseName("memory:roommgr-test");
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
        assertThatThrownBy(() -> roomManager.buildRoom(room))
                .isInstanceOf(ValidationException.class);
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

        Room bigRoom = sampleBigRoomBuilder().build();
        Room smallRoom = sampleSmallRoomBuilder().build();

        roomManager.buildRoom(bigRoom);
        roomManager.buildRoom(smallRoom);

        Long bigRoomId = bigRoom.getId();

        assertThat(roomManager.findRoomById(bigRoomId))
                .isEqualToComparingFieldByField(bigRoom);
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

    @Test
    public void buildRoomWithSqlExceptionThrown() throws SQLException {
        // Create sqlException, which will be thrown by our DataSource mock
        // object to simulate DB operation failure
        SQLException sqlException = new SQLException();
        // Create DataSource mock object
        DataSource failingDataSource = mock(DataSource.class);
        // Instruct our DataSource mock object to throw our sqlException when
        // DataSource.getConnection() method is called.
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        // Configure our manager to use DataSource mock object
        roomManager.setDataSource(failingDataSource);

        // Create Room instance for our test
        Room room = sampleSmallRoomBuilder().build();

        // Try to call roomManager.buildRoom(Room) method and expect that
        // exception will be thrown
        assertThatThrownBy(() -> roomManager.buildRoom(room))
                // Check that thrown exception is ServiceFailureException
                .isInstanceOf(ServiceFailureException.class)
                // Check if cause is properly set
                .hasCause(sqlException);
    }

    // Now we want to test also other methods of GraveManager. To avoid having
    // couple of method with lots of duplicit code, we will use the similar
    // approach as with testUpdateGrave(Operation) method.

    private void testExpectedServiceFailureException(Operation<RoomManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        roomManager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.callOn(roomManager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void updateRoomInformationsWithSqlExceptionThrown() throws SQLException {
        Room room = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room);
        testExpectedServiceFailureException((roomManager) -> roomManager.updateRoomInformation(room));
    }

    @Test
    public void findRoomByIdWithSqlExceptionThrown() throws SQLException {
        Room room = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room);
        testExpectedServiceFailureException((roomManager) -> roomManager.findRoomById(room.getId()));
    }

    @Test
    public void deleteRoomWithSqlExceptionThrown() throws SQLException {
        Room room = sampleSmallRoomBuilder().build();
        roomManager.buildRoom(room);
        testExpectedServiceFailureException((roomManager) -> roomManager.deleteRoom(room));
    }

    @Test
    public void findAllRoomWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(RoomManager::listAllRooms);
    }

    @Test
    public void findFreeRoomsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException(RoomManager::findFreeRoom);
    }


}