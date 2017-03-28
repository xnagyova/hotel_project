package cz.muni.fi.hotel;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.Collection;
import java.util.List;

/**
 *@author kkatanik & snagyova
 */
public interface GuestManager {

    public void createGuest(Guest guest);

    public void deleteGuest(Long id);

    public void updateGuestInformation(Guest guest);

    public List<Guest> findAllGuests();

    public Guest findGuestById(Long id);

    public List<Guest> findGuestByName(String name);

}
