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
 * Created by User on 9.5.2017.
 */
public class GuestFrame extends JFrame{
    private JTable jTableGuests;
    private javax.swing.JScrollPane scrollPane;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField textField1;
    private JTextField textField3;
    private JLabel phoneNumberLabel;
    private JLabel dateOfBirthLabel;
    private JLabel nameLabel;
    private JPanel panel1;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JComboBox comboBox3;
    private JButton backButton;
    private JComboBox langBox;
    private JLabel labelLang;
    static JFrame frame;
    private EditSwingWorker editSwingWorker;
    private AddSwingWorker addSwingWorker;
    private DeleteSwingWorker deleteSwingWorker;
    private final static Logger log = LoggerFactory.getLogger(RoomFrame.class);
    GuestManager guestManager;
    BookingManager bookingManager;
    RoomManager roomManager;
    Locale locale;
    ResourceBundle resourceBundle;

    private class AddSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Guest guest = new Guest();
            guest.setPhoneNumber(textField3.getText());
            guest.setName(textField1.getText());
            int day = (int)comboBox1.getSelectedItem();
            int month = (int)comboBox2.getSelectedItem();
            int year = (int)comboBox3.getSelectedItem();
            guest.setDateOfBirth(LocalDate.of(year,month,day));
            guestManager.createGuest(guest);
            log.info("Adding guest");
            return null;
        }
    }

    private class EditSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            Guest guest = new Guest();
            guest.setPhoneNumber(textField3.getText());
            guest.setName(textField1.getText());
            int day = (int)comboBox1.getSelectedItem();
            int month = (int)comboBox2.getSelectedItem();
            int year = (int)comboBox3.getSelectedItem();
            guest.setDateOfBirth(LocalDate.of(year,month,day));
            guest.setId(Long.parseLong((jTableGuests.getModel().getValueAt(jTableGuests.getSelectedRow(),3)).toString()));
            guestManager.updateGuestInformation(guest);
            log.info("Editing guest");
            return null;
        }
    }

    private class DeleteSwingWorker extends SwingWorker<Void,Void>{

        @Override
        protected Void doInBackground() throws Exception {
            for(int i = 0; i < jTableGuests.getSelectedRows().length; i++) {
                int row = jTableGuests.getSelectedRows()[i];
                Long id = Long.parseLong((jTableGuests.getModel().getValueAt(row, 3)).toString());
                guestManager.deleteGuest(guestManager.findGuestById(id));
                log.info("Deleting guest");
            }
            return null;
        }
    }



    public GuestFrame(BookingManager bookingManager,RoomManager roomManager,GuestManager guestManager) {


        langBox.addItem("en");
        langBox.addItem("fr");
        langBox.addItem("sk");

        locale = Locale.getDefault();

        resourceBundle = ResourceBundle.getBundle("HotelBundle",locale);
        this.guestManager = guestManager;
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableGuests.setModel(new GuestFrame.GuestsTableModel());
        add(panel1);
        for(int i=1;i<=31;i++)
        {
            comboBox1.addItem(new Integer(i));
            if(i<13) {
                comboBox2.addItem(new Integer(i));
            }
        }
        for(int i=1910;i<=2018;i++){
            comboBox3.addItem(new Integer(i));
        }
        addButton.setText(resourceBundle.getString("main.add"));
        editButton.setText(resourceBundle.getString("main.edit"));
        deleteButton.setText(resourceBundle.getString("main.delete"));
        backButton.setText(resourceBundle.getString("main.back"));
        labelLang.setText(resourceBundle.getString("main.language"));
        dateOfBirthLabel.setText(resourceBundle.getString("main.birth"));
        nameLabel.setText(resourceBundle.getString("main.name"));
        phoneNumberLabel.setText(resourceBundle.getString("main.telnumber"));
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
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(textField1.getText().equals("") || textField3.getText().equals("")){
                    JOptionPane.showMessageDialog(frame,"You have to fill all fields!");
                    log.debug("form data invalid");
                }
                addSwingWorker = new AddSwingWorker();
                addSwingWorker.execute();
                JOptionPane.showMessageDialog(frame,"Succesfully added!");
                log.info("OKEY");
                clearFields();

                jTableGuests.setModel(new GuestFrame.GuestsTableModel());
            }
        });

        jTableGuests.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = jTableGuests.getSelectedRow();
                String name = jTableGuests.getModel().getValueAt(row,0).toString();
                String[] date = (jTableGuests.getModel().getValueAt(row,1).toString()).split("-");
                int day = Integer.parseInt(date[2]);
                int month = Integer.parseInt(date[1]);
                int year = Integer.parseInt(date[0]);
                String phoneNumber = jTableGuests.getModel().getValueAt(row,2).toString();
                textField1.setText(name);
                textField3.setText(phoneNumber);
                comboBox1.setSelectedItem(day);
                comboBox2.setSelectedItem(month);
                comboBox3.setSelectedItem(year);
            }

        });


        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableGuests.getSelectedRow() != 0) {
                    if(textField1.getText().equals("") || textField3.getText().equals("")){
                        JOptionPane.showMessageDialog(frame,"You have to fill all fields!");
                        log.debug("form data invalid");
                    }
                    editSwingWorker = new EditSwingWorker();
                    editSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame,"Succesfully edited!");
                    log.info("OKEY");
                    clearFields();

                    jTableGuests.setModel(new GuestFrame.GuestsTableModel());
                }
            }
        });

        jTableGuests.addComponentListener(new ComponentAdapter() {

        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTableGuests.getSelectedRows().length != 0) {
                    deleteSwingWorker = new DeleteSwingWorker();
                    deleteSwingWorker.execute();
                    JOptionPane.showMessageDialog(frame,"Succesfully deleted!");
                    log.info("OKEY");
                    jTableGuests.setModel(new GuestFrame.GuestsTableModel());


                }
            }
        });
    }

    public void clearFields(){
        textField1.setText("");
        textField3.setText("");
        comboBox1.setSelectedIndex(0);
        comboBox2.setSelectedIndex(0);
        comboBox3.setSelectedIndex(0);
    }


    public class GuestsTableModel extends AbstractTableModel {
        List<Guest> guests = guestManager.findAllGuests();






        @Override
        public int getRowCount() {
            return guests.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Guest guest = guests.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return guest.getName();

                case 1:
                    return guest.getDateOfBirth();

                case 2:
                    return guest.getPhoneNumber();
                case 3:
                    return guest.getId();
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return resourceBundle.getString("main.name");
                case 1:
                    return resourceBundle.getString("main.birth");
                case 2:
                    return resourceBundle.getString("main.telnumber");
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return LocalDate.class;
                case 2:
                    return String.class;
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
                    frame = new GuestFrame(bookingManager,roomManager,guestManager);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
        );
    }
}




