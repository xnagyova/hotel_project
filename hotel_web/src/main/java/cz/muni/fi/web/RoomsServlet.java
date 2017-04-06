package cz.muni.fi.web;

import cz.muni.fi.hotel.Guest;
import cz.muni.fi.hotel.GuestManager;
import cz.muni.fi.hotel.Room;
import cz.muni.fi.hotel.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by ${KristianKatanik} on 04.04.2017.
 */
@WebServlet(RoomsServlet.URL_MAPPING + "/*")
public class RoomsServlet extends HttpServlet {
    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/rooms";

    private final static Logger log = LoggerFactory.getLogger(RoomsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("GET ...");
        showRoomsList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //support non-ASCII characters in form
        request.setCharacterEncoding("utf-8");
        //action specified by pathInfo
        String action = request.getPathInfo();
        log.debug("POST ... {}",action);
        switch (action) {
            case "/add":
                //getting POST parameters from form
                int floorNumber = Integer.parseInt(request.getParameter("floorNumber"));
                int capacity = Integer.parseInt(request.getParameter("capacity"));
                Boolean balcony = Boolean.parseBoolean(request.getParameter("balcony"));
                //form data validity check
                if (floorNumber<0 || capacity <1) {
                    request.setAttribute("chyba", "Je nutné vyplniť všetky hodnoty správne!");
                    log.debug("form data invalid");
                    showRoomsList(request, response);
                    return;
                }
                //form data processing - storing to database
                try {
                    Room room  = new Room(null, floorNumber, capacity, balcony);
                    getRoomManager().buildRoom(room);
                    //redirect-after-POST protects from multiple submission
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot add room", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getRoomManager().deleteRoom(getRoomManager().findRoomById(id));
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot delete room", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                Long id = Long.valueOf(request.getParameter("id"));
                int upFloorNumber = Integer.parseInt(request.getParameter("floorNumber"));
                int upCapacity = Integer.parseInt(request.getParameter("capacity"));
                Boolean upBalcony = Boolean.parseBoolean(request.getParameter("balcony"));
                if (upFloorNumber<0 || upCapacity <1) {
                    request.setAttribute("chyba", "Hodnoty nie su správne!");
                    log.debug("form data invalid");
                    showRoomsList(request, response);
                    return;
                }
                try {
                    getRoomManager().updateRoomInformation(getRoomManager().findRoomById(id));
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                }catch (Exception e) {
                    log.error("Cannot update room", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }

            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private RoomManager getRoomManager() {
        return (RoomManager) getServletContext().getAttribute("roomManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showRoomsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.debug("showing table of rooms");
            request.setAttribute("rooms", getRoomManager().listAllRooms());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show rooms", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
