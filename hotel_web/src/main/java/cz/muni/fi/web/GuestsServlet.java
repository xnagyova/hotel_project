package cz.muni.fi.web;

import cz.muni.fi.hotel.Guest;
import cz.muni.fi.hotel.GuestManager;
import cz.muni.fi.hotel.common.GuestException;
import org.apache.derby.client.am.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 *@author kkatanik & snagyova
 */
@WebServlet(GuestsServlet.URL_MAPPING + "/*")
public class GuestsServlet extends HttpServlet{
    private static final String LIST_JSP = "/list2.jsp";
    public static final String URL_MAPPING = "/guests";

    private final static Logger log = LoggerFactory.getLogger(GuestsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("GET ...");
        showGuestsList(request, response);
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
                if((request.getParameter("name") == "") || (request.getParameter("dateOfBirth") == "")||
                        (request.getParameter("phoneNumber") == "")){
                    request.setAttribute("chyba", "Je nutné vyplniť všetky hodnoty!");
                    log.debug("form data invalid");
                    showGuestsList(request, response);
                    return;
                }
                if ((request.getParameter("phoneNumber").matches("[^+0-9]"))){
                    request.setAttribute("chyba", "Nekorektný formát telefónneho čísla!");
                    log.debug("form data invalid");
                    showGuestsList(request, response);
                    return;
                }
                if ((!request.getParameter("dateOfBirth").matches(("\\d{4}-\\d{2}-\\d{2}")))){
                    request.setAttribute("chyba", "Nekorektný formát dátumu narodenia. Korektný formát: yyyy-mm-dd!");
                    log.debug("form data invalid");
                    showGuestsList(request, response);
                    return;
                }

                //getting POST parameters from form
                String name = request.getParameter("name");
                String[] dateParameter = request.getParameter("dateOfBirth").split("-");
                LocalDate dateOfBirth = LocalDate.of(Integer.parseInt(dateParameter[0]),
                        Integer.parseInt(dateParameter[1]),Integer.parseInt(dateParameter[2]));
                String phoneNumber = request.getParameter("phoneNumber");
                if (dateOfBirth.isAfter(LocalDate.now())){
                    request.setAttribute("chyba", "Nekorektný dátum narodenia!");
                    log.debug("form data invalid");
                    showGuestsList(request, response);
                    return;

                }



                try {
                    Guest guest = new Guest(null, name, dateOfBirth, phoneNumber);
                    getGuestManager().createGuest(guest);
                    //redirect-after-POST protects from multiple submission
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot add guest", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getGuestManager().deleteGuest(getGuestManager().findGuestById(id));
                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot delete guest", e);
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
    private GuestManager getGuestManager() {
        return (GuestManager) getServletContext().getAttribute("guestManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showGuestsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.debug("showing table of guests");
            request.setAttribute("guests", getGuestManager().findAllGuests());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show guests", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
