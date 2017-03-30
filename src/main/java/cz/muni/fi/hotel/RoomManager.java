package cz.muni.fi.hotel;

import java.util.List;

/**
 * @author kkatanik & snagyova
 */
public interface RoomManager {

    public void buildRoom(Room room);
    public void updateRoomInformation(Room room);
    public void deleteRoom(Room room);
    public Room findRoomById(Long id);
    public List<Room> listAllRooms();



}

