package cz.muni.fi.hotel.common;

import cz.muni.fi.hotel.Room;
import cz.muni.fi.hotel.RoomManagerImpl;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * Created by User on 9.5.2017.
 */
public class RoomFrame {


    private JTable jTableRooms;
    private JButton button1;
    private JPanel panel1;
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents



    public RoomFrame() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("tlačítko zmáčknuto");
            }
        });

    }


    public static void main(String args[]) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Rooms");
                JTable table= new JTable(new RoomsTableModel);

                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setContentPane(new RoomFrame().jTableRooms);
                frame.setPreferredSize(new Dimension(800,600));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }




    public class RoomsTableModel extends AbstractTableModel {

        private List<Room> rooms = new ArrayList<Room>();

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

            rooms.add(room);
            int lastRow = rooms.size() - 1;
            fireTableRowsInserted(lastRow, lastRow);
        }
    }



}
