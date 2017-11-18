package com.github.enviableyapper0;

import com.github.enviableyapper0.beans.FoodItem;
import com.github.enviableyapper0.beans.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.util.ArrayList;

public class Dashboard {
    private class SpaceListener implements KeyListener {
        private final static int spaceKeyCode = 32;

        @Override
        public void keyTyped(KeyEvent e) { }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == spaceKeyCode) {
                Dashboard.this.deleteOrder();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }
    }

    final static int fiveSecond = 5000;

    private OrderDashboardDAO dao;

    private JFrame root;
    private JTable table;
    private JPanel quickView;
    private JScrollPane scrollPane;
    private Timer timer;

    public Dashboard(URI baseURI) {
        this.dao = new OrderDashboardDAO(baseURI);

        root = new JFrame("EasyOrder Dashboard");
        root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        root.setPreferredSize(new Dimension(1366, 768));
        root.setMinimumSize(new Dimension(1366, 768));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        root.setLocation(dim.width/2-root.getSize().width/2, dim.height/2-root.getSize().height/2);

        table = new JTable();
        table.setFont(new Font("Tahoma", Font.PLAIN, 24));
        table.setRowHeight(table.getRowHeight() + 12);

        tableDecoration();
        setTableColumnHeader();
        updateTableModel();
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        table.addKeyListener(new SpaceListener());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
        table.getColumnModel().getColumn(2).setCellRenderer( centerRenderer );
        table.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );

        quickView = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quickView.setBackground(new Color(0, 150, 136));
        quickView.setSize(1366, 500);
        JLabel servingText = new JLabel("[Preparing Table]     ");
        servingText.setFont(new Font("Verdana", Font.BOLD,42));
        servingText.setForeground(Color.white);

        quickView.add(servingText, BorderLayout.WEST);
        Dimension buttonSize = new Dimension(100, 100);

        ArrayList<String> preparingList = new ArrayList<>();
        for (Order order: dao.getAllOrder()){
            preparingList.add(Integer.toString(order.getTableNum()));
            //quickView.add(new JButton(Integer.toString(order.getTableNum())));
        }

        JLabel leftoverTable = new JLabel();
        for (String s: preparingList){
            JLabel b = new JLabel(s + "  ");
            b.setFont(new Font("Verdana", Font.BOLD,100));
            b.setForeground(Color.yellow);
            quickView.add(b, BorderLayout.WEST);
        }
        //quickView.add(clickmeButton);


        root.add(scrollPane);
        root.add(quickView,BorderLayout.NORTH);
        root.pack();

        timer = new Timer(fiveSecond, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dashboard.this.updateTableModel();
            }
        });
        timer.start();

    }

    private void setTableColumnHeader() {
        TableColumnModel tableColumnModel = table.getColumnModel();
        DefaultTableModel model = ((DefaultTableModel)table.getModel());

        model.setColumnCount(4);
        model.setColumnIdentifiers(new Object[]{"Order ID", "Food", "Quantity", "Table Number"});
    }

    private void updateTableModel() {
        DefaultTableModel model = ((DefaultTableModel)table.getModel());

        int rowIndex = 0;
        for (Order order : dao.getAllOrder()) {
            for (FoodItem foodItem : order.getFoodItems()) {
                Object[] column = new Object[4];
                column[0] = order.getId();
                column[1] = foodItem.getName();
                column[2] = foodItem.getQuantity();
                column[3] = order.getTableNum();
                model.insertRow(rowIndex++, column);
            }
        }

        for (int i = model.getRowCount() - 1; i >= rowIndex; i--) {
            model.removeRow(i);
        }
    }

    private void tableDecoration(){
        Color teal500 = new Color(0, 150, 136);

        JTableHeader header = table.getTableHeader();
        //header.setBackground(teal500);
        //header.setForeground(Color.white);
        header.setFont(new Font("Verdana", Font.BOLD, 26));
    }

    public void deleteOrder() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (model.getRowCount() == 0) {
            return;
        }

        int idToDelete = (Integer) model.getValueAt(0, 0);
        dao.deleteOrder(idToDelete);
        while (((Integer) model.getValueAt(0,0)) == idToDelete) {
            model.removeRow(0);
        }
    }

    public static void main(String[] args) {
        Dashboard dashboard = new Dashboard(getBaseURI(args));
        dashboard.root.setVisible(true);
    }

    private static URI getBaseURI(String[] args) {
        if (args.length == 0)
            return UriBuilder.fromUri("http://localhost:8080/").build();
        else
            return UriBuilder.fromUri("http://" + args[1] + ":8080/").build();
    }
}
