package cz.muni.fi.hotel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *@author kkatanik snagyova
 */
public class BookingFrame extends JFrame {
    JPanel panel1;
    private JTable jTableBookings;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField textField1;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JComboBox comboBox4;
    private JComboBox comboBox5;
    private JComboBox comboBox6;
    private JComboBox comboBox7;
    private JComboBox comboBox8;
    private JButton backButton;
    private JLabel labelPrice;
    private JLabel labelRoom;
    private JLabel labelGuest;
    private JLabel labelArrival;
    private JLabel labelDeparture;
    private JComboBox langBox;
    private JLabel labelLang;
    static MainFrame frame;
    private final static Logger log = LoggerFactory.getLogger(RoomFrame.class);
    private EditSwingWorker editSwingWorker;
    private AddSwingWorker addSwingWorker;
    private DeleteSwingWorker deleteSwingWorker;
    BookingManager bookingManager;
    RoomManager roomManager;
    GuestManager guestManager;
    Locale locale;
    ResourceBundle resourceBundle;


    private class AddSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Booking booking = new Booking();
            booking.setPrice(Integer.parseInt(textField1.getText()));
            Room room = (Room) comboBox1.getSelectedItem();
            Guest guest = (Guest) comboBox2.getSelectedItem();
            booking.setRoom(room);
            booking.setGuest(guest);
            int arrivalDay = (int)comboBox3.getSelectedItem();
            int arrivalMonth = (int)comboBox4.getSelectedItem();
            int arrivalYear = (int)comboBox5.getSelectedItem();
            int departureDay = (int)comboBox6.getSelectedItem();
            int departureMonth = (int)comboBox7.getSelectedItem();
            int departureYear = (int)comboBox8.getSelectedItem();
            booking.setArrivalDate(LocalDate.of(arrivalYear,arrivalMonth,arrivalDay));
            booking.setDepartureDate(LocalDate.of(departureYear,departureMonth,departureDay));
            bookingManager.createBooking(booking);
            log.info("Adding booking");
            return null;
        }
    }

    private class EditSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Booking booking = new Booking();
            booking.setPrice(Integer.parseInt(textField1.getText()));
            Room room = (Room) comboBox1.getSelectedItem();
            Guest guest = (Guest) comboBox2.getSelectedItem();
            booking.setRoom(room);
            booking.setGuest(guest);
            int arrivalDay = (int)comboBox3.getSelectedItem();
            int arrivalMonth = (int)comboBox4.getSelectedItem();
            int arrivalYear = (int)comboBox5.getSelectedItem();
            int departureDay = (int)comboBox6.getSelectedItem();
            int departureMonth = (int)comboBox7.getSelectedItem();
            int departureYear = (int)comboBox8.getSelectedItem();
            booking.setArrivalDate(LocalDate.of(arrivalYear,arrivalMonth,arrivalDay));
            booking.setDepartureDate(LocalDate.of(departureYear,departureMonth,departureDay));
            booking.setId(Long.parseLong((jTableBookings.getModel().getValueAt(jTableBookings.getSelectedRow(),5)).toString()));
            bookingManager.updateBooking(booking);
            log.info("Editing booking");
            return null;
        }
    }

    private class DeleteSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            for(int i = 0; i < jTableBookings.getSelectedRows().length; i++) {
                int row = jTableBookings.getSelectedRows()[i];
                Long id = Long.parseLong((jTableBookings.getModel().getValueAt(row, 5)).toString());
                bookingManager.deleteBooking(bookingManager.getBookingById(id));
                log.info("Deleting booking");
            }
            return null;
        }
    }


    public BookingFrame(BookingManager bookingManager, RoomManager roomManager, GuestManager guestManager) {
        locale = Locale.getDefault();
        resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);

        langBox.addItem("en");
        langBox.addItem("fr");
        langBox.addItem("sk");

        this.bookingManager = bookingManager;
        this.roomManager = roomManager;
        this.guestManager = guestManager;

        jTableBookings.setModel(new BookingFrame.BookingsTableModel());
        add(panel1);

        List<Room> rooms = roomManager.listAllRooms();
        List<Guest> guests = guestManager.findAllGuests();
        for (Room room: rooms) {
            comboBox1.addItem(room);
        }
        for (Guest guest: guests) {
            comboBox2.addItem(guest);
        }
        
        for(int i=1;i<=31;i++)
        {
            comboBox3.addItem(new Integer(i));
            comboBox6.addItem(new Integer(i));
            if(i<13) {
                comboBox4.addItem(new Integer(i));
                comboBox7.addItem(new Integer(i));
            }
        }
        for(int i=2017;i<=2050;i++){
            comboBox5.addItem(new Integer(i));
            comboBox8.addItem(new Integer(i));
        }
        addButton.setText(resourceBundle.getString("main.add"));
        editButton.setText(resourceBundle.getString("main.edit"));
        deleteButton.setText(resourceBundle.getString("main.delete"));
        backButton.setText(resourceBundle.getString("main.back"));
        labelLang.setText(resourceBundle.getString("main.language"));
        labelArrival.setText(resourceBundle.getString("main.adate"));
        labelDeparture.setText(resourceBundle.getString("main.ddate"));
        labelGuest.setText(resourceBundle.getString("main.guest"));
        labelRoom.setText(resourceBundle.getString("main.room"));
        labelPrice.setText(resourceBundle.getString("main.price"));
        pack();
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textField1.getText().equals("")){
                    JOptionPane.showMessageDialog(frame,"You have to fill all fields!");
                    log.debug("form data invalid");
                }else if(!textField1.getText().matches("^[0-9]")){
                    JOptionPane.showMessageDialog(frame,"Price has to be a number!");
                    log.debug("form data invalid");
                }
                int price = Integer.parseInt(textField1.getText());
                if(price <=0){
                    JOptionPane.showMessageDialog(frame,"Price can not be zero or negative!");
                    log.debug("form data invalid");
                }
                int arrivalDay = (int)comboBox3.getSelectedItem();
                int arrivalMonth = (int)comboBox4.getSelectedItem();
                int arrivalYear = (int)comboBox5.getSelectedItem();
                int departureDay = (int)comboBox6.getSelectedItem();
                int departureMonth = (int)comboBox7.getSelectedItem();
                int departureYear = (int)comboBox8.getSelectedItem();
                if(LocalDate.of(departureYear,departureMonth,departureDay).isBefore(LocalDate.of(arrivalYear,arrivalMonth,arrivalDay))){
                    JOptionPane.showMessageDialog(frame,"Departure date can not be before arrival date!");
                    log.debug("form data invalid");
                }else {
                    addSwingWorker = new AddSwingWorker();
                    addSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame, "Succesfully added!");
                    log.info("OKEY");
                    clearFields();

                    jTableBookings.setModel(new BookingFrame.BookingsTableModel());
                }
            }
        });

        jTableBookings.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = jTableBookings.getSelectedRow();
                String price = jTableBookings.getModel().getValueAt(row,0).toString();
                Room room = (Room)jTableBookings.getModel().getValueAt(row,1);
                Guest guest = (Guest)jTableBookings.getModel().getValueAt(row,2);
                String[] date1 = (jTableBookings.getModel().getValueAt(row,3).toString()).split("-");
                int day1 = Integer.parseInt(date1[2]);
                int month1 = Integer.parseInt(date1[1]);
                int year1 = Integer.parseInt(date1[0]);
                String[] date2 = (jTableBookings.getModel().getValueAt(row,4).toString()).split("-");
                int day2 = Integer.parseInt(date2[2]);
                int month2 = Integer.parseInt(date2[1]);
                int year2 = Integer.parseInt(date2[0]);
                textField1.setText(price);
                comboBox1.setSelectedItem(room);
                comboBox2.setSelectedItem(guest);
                comboBox3.setSelectedItem(day1);
                comboBox4.setSelectedItem(month1);
                comboBox5.setSelectedItem(year1);
                comboBox6.setSelectedItem(day2);
                comboBox7.setSelectedItem(month2);
                comboBox8.setSelectedItem(year2);
            }

        });


        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableBookings.getSelectedRow() == 1) {
                    if(textField1.getText().equals("")){
                        JOptionPane.showMessageDialog(frame,"You have to fill all fields!");
                        log.debug("form data invalid");
                    }else if(!textField1.getText().matches("^[0-9]")){
                        JOptionPane.showMessageDialog(frame,"Price has to be a number!");
                        log.debug("form data invalid");
                    }
                    int price = Integer.parseInt(textField1.getText());
                    if(price <=0){
                        JOptionPane.showMessageDialog(frame,"Price can not be zero or negative!");
                        log.debug("form data invalid");
                    }
                    int arrivalDay = (int) comboBox3.getSelectedItem();
                    int arrivalMonth = (int) comboBox4.getSelectedItem();
                    int arrivalYear = (int) comboBox5.getSelectedItem();
                    int departureDay = (int) comboBox6.getSelectedItem();
                    int departureMonth = (int) comboBox7.getSelectedItem();
                    int departureYear = (int) comboBox8.getSelectedItem();
                    if(LocalDate.of(departureYear,departureMonth,departureDay).isBefore(LocalDate.of(arrivalYear,arrivalMonth,arrivalDay))){
                        JOptionPane.showMessageDialog(frame,"Departure date can not be before arrival date!");
                        log.debug("form data invalid");
                    }
                    editSwingWorker = new EditSwingWorker();
                    editSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame,"Succesfully edited!");
                    log.info("OKEY");
                    clearFields();

                    jTableBookings.setModel(new BookingFrame.BookingsTableModel());
                }
            }
        });

        jTableBookings.addComponentListener(new ComponentAdapter() {

        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableBookings.getSelectedRows().length != 0) {
                    deleteSwingWorker = new DeleteSwingWorker();
                    deleteSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame,"Succesfully deleted!");
                    log.info("OKEY");
                    jTableBookings.setModel(new BookingFrame.BookingsTableModel());


                }
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame[] frames = JFrame.getFrames();
                for(Frame frame1 : frames){
                    frame1.setVisible(false);
                }
                frame = new MainFrame(bookingManager,roomManager,guestManager);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public void clearFields(){
        textField1.setText("");
        comboBox1.setSelectedIndex(0);
        comboBox2.setSelectedIndex(0);
        comboBox3.setSelectedIndex(0);
        comboBox4.setSelectedIndex(0);
        comboBox5.setSelectedIndex(0);
        comboBox6.setSelectedIndex(0);
        comboBox7.setSelectedIndex(0);
        comboBox8.setSelectedIndex(0);
    }

    public class BookingsTableModel extends AbstractTableModel {


        private List<Booking> bookings = bookingManager.findAllBookings();

        @Override
        public int getRowCount() {
            return bookings.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Booking booking = bookings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return booking.getPrice();

                case 1:
                    return booking.getRoom();

                case 2:
                    return booking.getGuest();
                case 3:
                    return booking.getArrivalDate();
                case 4:
                    return booking.getDepartureDate();
                case 5:
                    return booking.getId();
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return resourceBundle.getString("main.price");
                case 1:
                    return resourceBundle.getString("main.room");
                case 2:
                    return resourceBundle.getString("main.guest");
                case 3:
                    return resourceBundle.getString("main.adate");
                case 4:
                    return resourceBundle.getString("main.ddate");
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Integer.class;
                case 1:
                    return Room.class;
                case 2:
                    return Guest.class;
                case 3:
                    return LocalDate.class;
                case 4:
                    return LocalDate.class;
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }



    }

    public static void main(String args[]) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        BookingManager bookingManager = ctx.getBean(BookingManager.class);
        GuestManager guestManager = ctx.getBean(GuestManager.class);
        RoomManager roomManager = ctx.getBean(RoomManager.class);
        EventQueue.invokeLater( ()-> { // zde použito funcionální rozhraní
                    JFrame frame = new BookingFrame(bookingManager,roomManager,guestManager);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
        );
    }
}
