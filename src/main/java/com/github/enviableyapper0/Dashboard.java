package com.github.enviableyapper0;

import com.github.enviableyapper0.beans.FoodItem;
import com.github.enviableyapper0.beans.Order;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.*;
import javax.ws.rs.core.UriBuilder;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class Dashboard {
    private class SpaceListener implements KeyListener {
        private final static int spaceKeyCode = 32;
        private final static int deleteKeyCode = 46;

        @Override
        public void keyTyped(KeyEvent e) { }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == spaceKeyCode) {
                Dashboard.this.deleteIndividualFoodItem();
            }
            if (e.getKeyCode() == deleteKeyCode) {
                Dashboard.this.deleteOrder();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { }
    }

    private final static int fiveSecond = 5000;
    private OrderDashboardDAO dao;

    private JFrame root;
    private JTable table;
    private JPanel remainingHeaderPanel;
    private JPanel statusBarPanel;
    private JLabel remainingTableLabel = new JLabel();
    private JScrollPane scrollPane;
    private Timer timer;
    private Color teal500 = new Color(0, 150, 136);

    private ArrayList<Integer> remainingID = new ArrayList<>();
    private ArrayList<String> remainingList = new ArrayList<String>(){
        @Override
        public String toString() {
            String output = "";
            for (String s : this) output += s + "  ";
            return output;
        }
    };
    private ArrayList<ArrayList<Integer>> foodIndexArray = new ArrayList<>();

    public Dashboard(URI baseURI) {
        this.dao = new OrderDashboardDAO(baseURI);

        root = new JFrame("EasyOrder Dashboard");
        root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        root.setPreferredSize(new Dimension(1366, 768));
        root.setMinimumSize(new Dimension(1366, 768));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        root.setLocation(dim.width/2-root.getSize().width/2, dim.height/2-root.getSize().height/2);

        ImageIcon img = new ImageIcon("C:\\Users\\Coregame\\IdeaProjects\\EasyOrderDashboard\\src\\main\\res\\dashboard.png");
        root.setIconImage(img.getImage());

        // Status Bar
        statusBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBarDecoration();

        // Table
        table = new JTable();
        tableDecoration();
        setTableColumnHeader();
        updateTableModel();
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        table.addKeyListener(new SpaceListener());




        // Remaining Header Panel
        remainingHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remainingHeaderPanelDecoration();
        updateRemainingHeaderPanel();
        remainingTableLabel.setFont(new Font("Verdana", Font.BOLD, 100));
        remainingTableLabel.setForeground(Color.yellow);
        remainingHeaderPanel.add(remainingTableLabel, BorderLayout.WEST);

        /*for (Order order: dao.getAllOrder()){
            remainingList.add(Integer.toString(order.getTableNum()));
        }*/

        root.add(statusBarPanel, BorderLayout.SOUTH);
        root.add(scrollPane);
        root.add(remainingHeaderPanel,BorderLayout.NORTH);
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
        DefaultTableModel model = ((DefaultTableModel) table.getModel());
        model.setColumnCount(4);
        model.setColumnIdentifiers(new Object[]{"Order ID", "Table Number", "Food", "Quantity"});

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        table.getColumnModel().getColumn(0).setCellRenderer( centerRenderer );
        table.getColumnModel().getColumn(1).setCellRenderer( centerRenderer );
        table.getColumnModel().getColumn(3).setCellRenderer( centerRenderer );
    }

    private void updateTableModel() {
        DefaultTableModel model = ((DefaultTableModel) table.getModel());
        int rowIndex = 0;
        int count = 1;
        ArrayList<Integer> foodItemsIndexList = new ArrayList<>();

        for (Order order : dao.getAllOrder()) {
            for (FoodItem foodItem : order.getFoodItems()) {
                Object[] column = new Object[4];
                column[0] = order.getId();
                column[1] = order.getTableNum();
                column[2] = count + ". " + foodItem.getName();
                column[3] = "x " + foodItem.getQuantity();
                model.insertRow(rowIndex++, column);

                foodItemsIndexList.add(count - 1);
                count++;
            }

            // If current table number is not exist in remaining list
            String currentTableNum = Integer.toString(order.getTableNum());
            if (!remainingList.contains(currentTableNum)){
                remainingList.add(currentTableNum);
                remainingID.add(order.getId());
                foodIndexArray.add(foodItemsIndexList);
            }

            // Reset counter
            count = 1;
            foodItemsIndexList = new ArrayList<>();

        }
        System.out.println("TableNum: " + remainingList);
        System.out.println("Order ID: " + remainingID);
        System.out.println("Food Index: " + foodIndexArray);

        System.out.println("==========");


        updateRemainingHeaderPanel();

        for (int i = model.getRowCount() - 1; i >= rowIndex; i--) {
            model.removeRow(i);
        }

    }

    private void tableDecoration(){
        table.setFont(new Font("Tahoma", Font.PLAIN, 24));
        table.setRowHeight(table.getRowHeight() + 12);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Verdana", Font.BOLD, 26));
    }

    private void statusBarDecoration(){
        statusBarPanel.setBorder(new CompoundBorder(new LineBorder
                (Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
        final JLabel status = new JLabel();
        status.setText("<html><b>Status:</b> <font color='green'>Ready</font></html>");
        statusBarPanel.add(status);
        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                status.setText(root.getWidth() + "x" + root.getHeight());
            }
        });
    }

    private void remainingHeaderPanelDecoration(){
        remainingHeaderPanel.setBackground(teal500);
        remainingHeaderPanel.setSize(1366, 500);
        JLabel servingText = new JLabel("[Preparing Table]     ");
        servingText.setFont(new Font("Verdana", Font.BOLD,42));
        servingText.setForeground(Color.white);
        remainingHeaderPanel.add(servingText, BorderLayout.WEST);
    }

    private void updateRemainingHeaderPanel(){
        if(remainingList.size() == 0){
            remainingTableLabel.setText("No order left.");
        } else {
            remainingTableLabel.setText(remainingList.toString());
        }
    }

    private void deleteOrder() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (model.getRowCount() == 0) {
            return;
        }

        final int idToDelete = (Integer) model.getValueAt(table.getSelectedRow(), 0);
        int indexToRemove = remainingID.indexOf(idToDelete);

        dao.deleteOrder(idToDelete);

        remainingList.remove(indexToRemove);
        remainingID.remove(indexToRemove);
        foodIndexArray.remove(indexToRemove);

        updateRemainingHeaderPanel();

        while (((Integer) model.getValueAt(table.getSelectedRow(),0)) == idToDelete) {
            model.removeRow(table.getSelectedRow());
        }
    }

    private void deleteIndividualFoodItem(){
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (model.getRowCount() == 0) {
            return;
        }

        final int idToDelete = (Integer) model.getValueAt(table.getSelectedRow(), 0);
        int indexToRemove = remainingID.indexOf(idToDelete);

        ArrayList<Integer> allItems = new ArrayList<>();
        for (ArrayList<Integer> arr: foodIndexArray){
            for (Integer i: arr){
                allItems.add(i);
            }
        }

        int count = 0;
        for (int i = 0; i < indexToRemove; i++){
            count += foodIndexArray.get(i).size();
        }

        int foodIndex = allItems.get(count);
        dao.deleteIndividualFoodItem(idToDelete, foodIndex);

        foodIndexArray.get(indexToRemove).remove(foodIndex);

        if (foodIndexArray.get(indexToRemove).isEmpty()){
            remainingList.remove(indexToRemove);
            remainingID.remove(indexToRemove);
            foodIndexArray.remove(indexToRemove);

            updateRemainingHeaderPanel();
        }

        updateFoodIndexArray(indexToRemove);
        updateTableModel();

        while (((Integer) model.getValueAt(0,0)) == idToDelete) {
            model.removeRow(0);
        }
    }

    // Helper
    private void updateFoodIndexArray(int OuterArrayIndex){
        int limit = foodIndexArray.get(OuterArrayIndex).size();
        if (limit >= 1){
            for (int i = 0; i < limit; i++){
                foodIndexArray.get(OuterArrayIndex).set(i, i);
            }
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
