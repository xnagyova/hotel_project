package cz.muni.fi.hotel;

import java.util.Collections;
import java.util.List;

/**
 * Created by User on 8.3.2017.
 */
public interface RoomManager {

    public void buildRoom(Room room);
    public void updateRoomInformation(Room room);
    public void deleteRoom(Long id);
    public Room findRoomById(Long id);
    public List<Room> listAllRooms();
    public List<Room> findFreeRooms();


}

