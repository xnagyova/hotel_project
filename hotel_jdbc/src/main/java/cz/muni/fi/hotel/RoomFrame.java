package cz.muni.fi.hotel;

import cz.muni.fi.hotel.common.ValidationException;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.ExecutionException;

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
    private JLabel labelFloorNumber;
    private JLabel labelCapacity;
    private JLabel labelBalcony;
    private EditSwingWorker editSwingWorker;
    private AddSwingWorker addSwingWorker;
    private DeleteSwingWorker deleteSwingWorker;
    private static JFrame frame;
    private ResourceBundle resourceBundle;
    private Locale locale;
    RoomManager roomManager;
    private static final Logger log = LoggerFactory.getLogger(RoomFrame.class.getName());
    BookingManager bookingManager;
    GuestManager guestManager;

    private class AddSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Room room = new Room();
            room.setBalcony(hasBalconyRadioButton.isSelected());
            room.setCapacity(Integer.parseInt(textField2.getText()));
            room.setFloorNumber(Integer.parseInt(textField1.getText()));
            roomManager.buildRoom(room);
            log.info("Adding room");
            return null;
        }
    }

    private class EditSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Room room = new Room();
            room.setBalcony(hasBalconyRadioButton.isSelected());
            room.setCapacity(Integer.parseInt(textField2.getText()));
            room.setFloorNumber(Integer.parseInt(textField1.getText()));
            room.setId(Long.parseLong((jTableRooms.getModel().getValueAt(jTableRooms.getSelectedRow(),3)).toString()));
            roomManager.updateRoomInformation(room);
            log.info("Editing room");
            return null;
        }
    }

    private class DeleteSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            for(int i = 0; i < jTableRooms.getSelectedRows().length; i++) {
                int row = jTableRooms.getSelectedRows()[i];
                Long id = Long.parseLong((jTableRooms.getModel().getValueAt(row, 3)).toString());
                roomManager.deleteRoom(roomManager.findRoomById(id));
                log.info("deleting room");
            }
            return null;
        }
    }


    public RoomFrame(BookingManager bookingManager,RoomManager roomManager, GuestManager guestManager) {
        //locale = Locale.getDefault();
        //locale = new Locale("sk","SK");
        locale = new Locale("fr","FR");
        resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);

        this.roomManager = roomManager;
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableRooms.setModel(new RoomFrame.RoomsTableModel());
        add(panel2);

        addRoomButton.setText(resourceBundle.getString("main.add"));
        editRoomButton.setText(resourceBundle.getString("main.edit"));
        deleteRoomButton.setText(resourceBundle.getString("main.delete"));
        labelBalcony.setText("");
        labelCapacity.setText(resourceBundle.getString("main.capacity"));
        labelFloorNumber.setText(resourceBundle.getString("main.floorNumber"));
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
                if(textField1.getText().equals("") || textField2.getText().equals("")){
                    JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorFields"));
                    log.debug("form data invalid");
                } else if(!textField1.getText().matches("[0-9]+") || !textField2.getText().matches("[0-9]+")){
                    JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorCapNumber"));
                    log.debug("form data invalid");
                }else if (Integer.parseInt(textField2.getText())<=0){
                    JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorCapNegative"));
                    log.debug("form data invalid");
                }else if (Integer.parseInt(textField1.getText())<0){
                    JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorFNNegative"));
                    log.debug("form data invalid");
                }else {
                    addSwingWorker = new AddSwingWorker();
                    addSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame, resourceBundle.getString("added"));
                    log.info("OKEY");
                    clearFields();
                    jTableRooms.setModel(new RoomFrame.RoomsTableModel());
                }
            }
        });

        jTableRooms.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                log.info("Filling text fields");
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

                    if(textField1.getText().equals("") || textField2.getText().equals("")){
                        JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorFields"));
                        log.debug("form data invalid");
                    } else if(!textField1.getText().matches("[0-9]+") || !textField2.getText().matches("[0-9]+")){
                        JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorCapNumber"));
                        log.debug("form data invalid");
                    }else if (Integer.parseInt(textField2.getText())<=0){
                        JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorCapNegative"));
                        log.debug("form data invalid");
                    }else if (Integer.parseInt(textField1.getText())<0){
                        JOptionPane.showMessageDialog(frame,resourceBundle.getString("errorFNNegative"));
                        log.debug("form data invalid");
                    }else {
                        editSwingWorker = new EditSwingWorker();
                        editSwingWorker.execute();
                        JOptionPane.showMessageDialog(frame, resourceBundle.getString("edited"));
                        log.info("edit done");
                        clearFields();

                        jTableRooms.setModel(new RoomFrame.RoomsTableModel());
                    }

                }
            }
        });

        jTableRooms.addComponentListener(new ComponentAdapter() {

        });
        deleteRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableRooms.getSelectedRows().length != 0) {
                    deleteSwingWorker = new DeleteSwingWorker();
                    deleteSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame,resourceBundle.getString("deleted"));
                    log.info("delete done");
                    jTableRooms.setModel(new RoomFrame.RoomsTableModel());
                }
            }
        });
    }


    public void clearFields(){
        log.info("clearing fields");
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
                    frame = new RoomFrame(bookingManager,roomManager,guestManager);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
        );
    }



}
