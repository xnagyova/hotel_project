package cz.muni.fi.hotel;


/**
 * @author kkatanik & snagyova
 */
public class RoomBuilder {

    private Long id;
    private int floorNumber;
    private int capacity;
    private boolean balcony;

    public RoomBuilder id(Long id){
        this.id = id;
        return this;
    }

    public RoomBuilder floorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
        return this;
    }

    public RoomBuilder capacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public RoomBuilder balcony(boolean balcony) {
        this.balcony = balcony;
        return this;
    }

    public Room build(){
        Room room = new Room();
        room.setId(id);
        room.setFloorNumber(floorNumber);
        room.setCapacity(capacity);
        room.setBalcony(balcony);
        return room;

    }
}
