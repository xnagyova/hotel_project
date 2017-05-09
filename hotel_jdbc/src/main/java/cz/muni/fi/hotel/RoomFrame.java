package cz.muni.fi.hotel;

import javafx.scene.control.TableColumn;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author katanik nagyova
 */
public class RoomFrame extends javax.swing.JFrame {


    private JTable jTableRooms;
    private javax.swing.JScrollPane scrollPane;
    private JPanel panel1;
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
    RoomManager roomManager = ctx.getBean(RoomManager.class);


    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        JButton button1 = new JButton();
        JButton buttonDelete = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableRooms.setModel(new RoomsTableModel());
        scrollPane.setViewportView(jTableRooms);


        button1.setText("Add room");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });


        buttonDelete.setText("Delete room");
        buttonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeleteActionPerformed(evt);
            }
        });


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(13, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(50, 50, Short.MAX_VALUE)
                                        .addComponent(button1)
                                        .addGap(40, 300, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(40, 300, Short.MAX_VALUE)
                                        .addComponent(buttonDelete)
                                        .addGap(50, 50, Short.MAX_VALUE)))

        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(201, 201, 201)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 137, Short.MAX_VALUE)
                                        .addComponent(button1)
                                        .addGap(0, 138, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(137, 150, Short.MAX_VALUE)
                                        .addComponent(buttonDelete)
                                        .addGap(0, 140, Short.MAX_VALUE)))

        );

        pack();
    }



    public RoomFrame(){
        initComponents();

        RoomsTableModel model= (RoomsTableModel) jTableRooms.getModel();

        model.addRoom(new Room(1L,7,6,false));


    }


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new RoomFrame();
                frame.setVisible(true);
            }
        });
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
                case 0:
                    return room.getCapacity();

                case 1:
                    return room.getFloorNumber();

                case 2:
                    return room.isBalcony();
                default:
                    throw new IllegalArgumentException("columnIndex");
            }
        }
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Capacity";
                case 1:
                    return "Floor number";
                case 2:
                    return "Balcony";
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

        public void addRoom(Room room) {
            roomManager.buildRoom(room);
            int lastRow = rooms.size() - 1;
            fireTableRowsInserted(lastRow, lastRow);
            /*

            rooms.add(room);
            int lastRow = rooms.size() - 1;
            fireTableRowsInserted(lastRow, lastRow);
            */
        }

        public void deleteRoom (Room room){
            roomManager.deleteRoom(room);

        }
    }
    private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        RoomsTableModel model = (RoomsTableModel) jTableRooms.getModel();
        model.addRoom(new Room(1l,4,5,true));
    }

    private void buttonDeleteActionPerformed(java.awt.event.ActionEvent evt){
        RoomsTableModel model = (RoomsTableModel) jTableRooms.getModel();



    }



}
