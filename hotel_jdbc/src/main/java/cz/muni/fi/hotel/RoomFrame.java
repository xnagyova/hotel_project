package cz.muni.fi.hotel;

import javafx.scene.control.TableColumn;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import cz.muni.fi.hotel.Room;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * @author katanik nagyova
 */
public class RoomFrame extends javax.swing.JFrame {
    JPanel panel2;
    private JTable jTableRooms;
    private JButton addRoomButton;
    private JButton editRoomButton;
    private JButton deleteRoomButton;
    private JTextField textField1;
    private JTextField textField2;
    private JRadioButton hasBalconyRadioButton;
    private JButton backButton;
    private JComboBox langBox;
    private JLabel labelFloorNumber;
    private JLabel labelCapacity;
    private JLabel labelBalcony;
    private JLabel labelLang;
    public ResourceBundle resourceBundle;
    public Locale locale;
    RoomManager roomManager;
    BookingManager bookingManager;
    GuestManager guestManager;




    public RoomFrame(RoomManager roomManager) {
        //locale = Locale.getDefault();
        locale = new Locale("sk","SK");
        resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);

        langBox.addItem("en");
        langBox.addItem("fr");
        langBox.addItem("sk");

        this.roomManager = roomManager;
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableRooms.setModel(new RoomFrame.RoomsTableModel());
        //scrollPane.setViewportView(jTableGuests);
        add(panel2);

        addRoomButton.setText(resourceBundle.getString("main.add"));
        editRoomButton.setText(resourceBundle.getString("main.edit"));
        deleteRoomButton.setText(resourceBundle.getString("main.delete"));
        labelBalcony.setText("");
        labelCapacity.setText(resourceBundle.getString("main.capacity"));
        labelFloorNumber.setText(resourceBundle.getString("main.floorNumber"));
        labelLang.setText(resourceBundle.getString("main.language"));
        backButton.setText(resourceBundle.getString("main.back"));
        hasBalconyRadioButton.setText(resourceBundle.getString("main.balcony"));

        pack();
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Frame[] frames = JFrame.getFrames();
                for(Frame frame1 : frames){
                    frame1.setVisible(false);
                }
                MainFrame frame = new MainFrame(bookingManager,roomManager,guestManager);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
        addRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int floorNumber = Integer.parseInt(textField1.getText());
                int capacity = Integer.parseInt(textField2.getText());
                Boolean balcony = hasBalconyRadioButton.isSelected();
                roomManager.buildRoom(new Room(null,floorNumber,capacity,balcony));
                clearFields();
                jTableRooms.setModel(new RoomFrame.RoomsTableModel());
            }
        });

        jTableRooms.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = jTableRooms.getSelectedRow();
                String floorNumber = jTableRooms.getModel().getValueAt(row,0).toString();
                String capacity = jTableRooms.getModel().getValueAt(row,1).toString();
                boolean balcony = Boolean.parseBoolean(jTableRooms.getModel().getValueAt(row,2).toString());
                textField1.setText(floorNumber);
                textField2.setText(capacity);
                hasBalconyRadioButton.setSelected(balcony);
            }

        });


        editRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableRooms.getSelectedRow() != -1) {

                    int floorNumber  = Integer.parseInt(textField1.getText());
                    int capacity  = Integer.parseInt(textField2.getText());
                    boolean balcony = hasBalconyRadioButton.isSelected();
                    int row = jTableRooms.getSelectedRow();
                    Long id = Long.parseLong((jTableRooms.getModel().getValueAt(row,3)).toString());
                    Room room = new Room(id,floorNumber,capacity,balcony);
                    roomManager.updateRoomInformation(room);
                    clearFields();

                    jTableRooms.setModel(new RoomFrame.RoomsTableModel());

                }
            }
        });

        jTableRooms.addComponentListener(new ComponentAdapter() {

        });
        deleteRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableRooms.getSelectedRows().length != 0) {
                    for(int i = 0; i < jTableRooms.getSelectedRows().length; i++) {
                        int row = jTableRooms.getSelectedRows()[i];
                        Long id = Long.parseLong((jTableRooms.getModel().getValueAt(row, 3)).toString());
                        roomManager.deleteRoom(roomManager.findRoomById(id));
                    }
                    jTableRooms.setModel(new RoomFrame.RoomsTableModel());
                }
            }
        });
        langBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                locale = new Locale(langBox.getSelectedItem().toString(),langBox.getSelectedItem().toString().toUpperCase());
                resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);
            }
        });
    }


    public void clearFields(){
        textField1.setText("");
        textField2.setText("");
    }


    public class RoomsTableModel extends AbstractTableModel {


        private List<Room> rooms = roomManager.listAllRooms();

        @Override
        public int getRowCount() {
            return rooms.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Room room = rooms.get(rowIndex);
            switch (columnIndex) {
                case 1:
                    return room.getCapacity();

                case 0:
                    return room.getFloorNumber();

                case 2:
                    return room.isBalcony();
                case 3:
                    return room.getId();
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return resourceBundle.getString("main.floorNumber");
                case 1:
                    return resourceBundle.getString("main.capacity");
                case 2:
                    return resourceBundle.getString("main.balcony");
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
                    return Integer.class;
                case 2:
                    return Boolean.class;
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
                    JFrame frame = new RoomFrame(roomManager);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
        );
    }



}
