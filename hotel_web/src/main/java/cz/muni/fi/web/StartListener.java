package cz.muni.fi.web;

import cz.muni.fi.hotel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;

/**
 * Created by ${KristianKatanik} on 03.04.2017.
 */
@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = LoggerFactory.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("webová aplikácia inicializována");
        ServletContext servletContext = ev.getServletContext();
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        servletContext.setAttribute("guestManager", springContext.getBean("guestManager", GuestManager.class));
        servletContext.setAttribute("roomManager", springContext.getBean("roomManager", RoomManager.class));
        servletContext.setAttribute("bookingManager", springContext.getBean("bookingManager", BookingManager.class));
        log.info("vytvořeny manažery");
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("aplikácia končí");
    }
}
