package cz.muni.fi.hotel;


import java.util.List;

/**
 *@author kkatanik & snagyova
 */
public interface GuestManager {

    public void createGuest(Guest guest);

    public void deleteGuest(Guest guest);

    public void updateGuestInformation(Guest guest);

    public List<Guest> findAllGuests();

    public Guest findGuestById(Long id);

    public List<Guest> findGuestByName(String name);

}
