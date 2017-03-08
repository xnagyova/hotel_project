package cz.muni.fi;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import java.util.Collection;

/**
 * Created by User on 8.3.2017.
 */
public interface GuestManager {

    public void createGuest(Guest guest);

    public void deleteGuest(Guest guest);

    public void updateGuestInformation(Guest guest);

    public Collection <Guest> findAllGuests();

    public Guest findGuestById(long id);

    public Collection<Guest> findGuestByName(String name);

}
