package cz.muni.fi.hotel;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.Collection;
import java.util.List;

/**
 * Created by User on 8.3.2017.
 */
public interface GuestManager {

    public void createGuest(Guest guest);

    public void deleteGuest(Guest guest);

    public void updateGuestInformation(Guest guest);

    public List<Guest> findAllGuests();

    public Guest findGuestById(long id);

    public List<Guest> findGuestByName(String name);

}
