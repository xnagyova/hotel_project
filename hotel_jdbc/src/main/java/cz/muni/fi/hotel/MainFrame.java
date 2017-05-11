package cz.muni.fi.hotel;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by User on 9.5.2017.
 */
public class MainFrame extends JFrame {
    JPanel panel1;
    JButton button1;
    public JButton button2;
    public JButton button3;
    private JLabel labelHotel;
    private JComboBox langBox;
    private JLabel labelLang;
    BookingManager bookingManager;
    RoomManager roomManager;
    GuestManager guestManager;
    Locale locale = Locale.getDefault();
    ResourceBundle resourceBundle;

    public MainFrame(BookingManager bookingManager, RoomManager roomManager, GuestManager guestManager){
        resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);

        langBox.addItem("en");
        langBox.addItem("fr");
        langBox.addItem("sk");

        this.bookingManager = bookingManager;
        this.roomManager = roomManager;
        this.guestManager = guestManager;
        button1.setText(resourceBundle.getString("main.bm"));
        button2.setText(resourceBundle.getString("main.rm"));
        button3.setText(resourceBundle.getString("main.gm"));
        labelHotel.setText(resourceBundle.getString("main.title"));
        labelLang.setText(resourceBundle.getString("main.language"));
        add(panel1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame[] frames = JFrame.getFrames();
                for(Frame frame1 : frames){
                    frame1.setVisible(false);
                }
                BookingFrame bookingFrame = new BookingFrame(bookingManager,roomManager,guestManager);
                bookingFrame.pack();
                bookingFrame.setVisible(true);
            }
        });

        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame[] frames = JFrame.getFrames();
                for(Frame frame1 : frames){
                    frame1.setVisible(false);
                }
                RoomFrame roomFrame = new RoomFrame(roomManager);
                roomFrame.pack();
                roomFrame.setVisible(true);
            }
        });

        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame[] frames = JFrame.getFrames();
                for(Frame frame1 : frames){
                    frame1.setVisible(false);
                }
                GuestFrame guestFrame = new GuestFrame(guestManager);
                guestFrame.pack();
                guestFrame.setVisible(true);
            }
        });
        langBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                locale = new Locale(langBox.getSelectedItem().toString(),langBox.getSelectedItem().toString().toUpperCase());
                resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);
                System.out.println(langBox.getSelectedItem().toString());

            }
        });
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        BookingManager bookingManager = ctx.getBean(BookingManager.class);
        GuestManager guestManager = ctx.getBean(GuestManager.class);
        RoomManager roomManager = ctx.getBean(RoomManager.class);
        EventQueue.invokeLater( ()-> { // zde použito funcionální rozhraní
                    JFrame frame = new MainFrame(bookingManager,roomManager,guestManager);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);


                }
        );
    }
}
