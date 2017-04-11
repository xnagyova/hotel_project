package cz.muni.fi.web;

import cz.muni.fi.hotel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 *@author kkatanik && snagyova
 */
@WebServlet(BookingsServlet.URL_MAPPING + "/*")
public class BookingsServlet extends HttpServlet {
    private static final String LIST_JSP = "/list3.jsp";
    public static final String URL_MAPPING = "/bookings";

    private final static Logger log = LoggerFactory.getLogger(BookingsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("GET ...");
        showBookingList(request, response);
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
                if ((!request.getParameter("arrivalDate").matches(("\\d{4}-\\d{2}-\\d{2}"))) ||
                        (!request.getParameter("departureDate").matches(("\\d{4}-\\d{2}-\\d{2}")))){
                    request.setAttribute("chyba", "Nekorektný formát dátumu. Korektný formát: yyyy-mm-dd!");
                    log.debug("form data invalid");
                    showBookingList(request, response);
                    return;
                }
                if((request.getParameter("price") == "") || (request.getParameter("roomId") == "")||
                        (request.getParameter("guestId") == "")||
                        (request.getParameter("arrivalDate") == "")||(request.getParameter("departureDate") == "")){
                    request.setAttribute("chyba", "Je nutné vyplniť všetky hodnoty!");
                    log.debug("form data invalid");
                    showBookingList(request, response);
                    return;
                }

                //getting POST parameters from form
                int price = Integer.parseInt(request.getParameter("price"));
                String[] departureDateParameter = request.getParameter("departureDate").split("-");
                LocalDate departureDate = LocalDate.of(Integer.parseInt(departureDateParameter[0]),
                        Integer.parseInt(departureDateParameter[1]),Integer.parseInt(departureDateParameter[2]));
                String[] arrivalDateParameter = request.getParameter("arrivalDate").split("-");
                LocalDate arrivalDate = LocalDate.of(Integer.parseInt(arrivalDateParameter[0]),
                        Integer.parseInt(arrivalDateParameter[1]),Integer.parseInt(arrivalDateParameter[2]));
                Long roomId= Long.parseLong(request.getParameter("roomId"));
                Long guestId= Long.parseLong(request.getParameter("guestId"));
                //form data validity check
                if (arrivalDate.isAfter(departureDate)){
                    request.setAttribute("chyba", "Dátum príchodu nesmie byť neskôr ako dátum odchodu.");
                    log.debug("form data invalid");
                    showBookingList(request, response);
                    return;

                }
                if (price < 1 || roomId<0|| guestId <0) {
                    request.setAttribute("chyba", "Je nutné zadať hodnoty správne !");
                    log.debug("form data invalid");
                    showBookingList(request, response);
                    return;
                }
                //form data processing - storing to database
                try {
                    Booking booking = new Booking(null,price, getRoomManager().findRoomById(roomId),getGuestManager().findGuestById(guestId),arrivalDate,departureDate);
                    getBookingManager().createBooking(booking);
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot add booking", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getBookingManager().deleteBooking(getBookingManager().getBookingById(id));
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot delete booking", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                //TODO
                return;
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
    private BookingManager getBookingManager() {
        return (BookingManager) getServletContext().getAttribute("bookingManager");
    }
    private RoomManager getRoomManager() {
        return (RoomManager) getServletContext().getAttribute("roomManager");
    }
    private GuestManager getGuestManager() {
        return (GuestManager) getServletContext().getAttribute("guestManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showBookingList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.debug("showing table of bookings");
            request.setAttribute("bookings", getBookingManager().findAllBookings());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show bookings", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
