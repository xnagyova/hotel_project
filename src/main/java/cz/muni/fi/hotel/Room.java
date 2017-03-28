package cz.muni.fi.hotel;

import javax.xml.bind.ValidationException;

/**
 * @author kkatanik & nagyova
 */
public class Room {

    private Long id;
    private int floorNumber;
    private int capacity;
    private boolean balcony;

    public Room() {
    }

    public Room(Long id, int floorNumber, int capacity, boolean balcony) {
        this.id = id;
        this.floorNumber = floorNumber;
        this.capacity = capacity;
        this.balcony = balcony;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber){

        this.floorNumber = floorNumber;
    }

    public int getCapacity() {

        return capacity;
    }

    public void setCapacity(int capacity){

        this.capacity = capacity;
    }

    public boolean isBalcony() {
        return balcony;
    }

    public void setBalcony(boolean balcony) {
        this.balcony = balcony;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return id == room.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", floorNumber=" + floorNumber +
                ", capacity=" + capacity +
                ", balcony=" + balcony +
                '}';
    }
}
