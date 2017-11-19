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
import java.net.URI;
import java.util.ArrayList;

/*---------------------------------------------------------------------------------

	EasyOrder Dashboard

-----------------------------------------------------------------------------------

	0.	Variables Initialization
	1.	Constructor
	2.  Class Methods
	3.	Decorator Methods
	4.	Helper Methods
	5.	Main

----------------------------------------------------------------------------------- */

public class Dashboard {
    private class SpaceListener implements KeyListener {
        private final static int spaceKeyCode = 32;
        private final static int deleteKeyCode = 46;

        @Override
        public void keyTyped(KeyEvent e) {
        }

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
        public void keyReleased(KeyEvent e) {
        }
    }

    /* -------------------------------------------------------------------------------- */
    /*	0. Variables Initialization
    /* -------------------------------------------------------------------------------- */
    private final static int fiveSecond = 5000;
    private OrderDashboardDAO dao;

    private JFrame root;
    private JTable table;
    private JPanel remainingHeaderPanel;
    private JPanel statusBarPanel;
    private JLabel remainingTableLabel;
    private JLabel statusLabel;
    private Color teal500 = new Color(0, 150, 136);

    private ArrayList<ArrayList<Integer>> foodIndexArray = new ArrayList<>();
    private ArrayList<Integer> remainingIDArray = new ArrayList<>();
    private ArrayList<String> remainingListArray = new ArrayList<String>() {
        @Override
        public String toString() {
            String output = "";
            for (String s : this) output += s + "  ";
            return output;
        }
    };

    /* -------------------------------------------------------------------------------- */
    /*	1.	Constructor
    /* -------------------------------------------------------------------------------- */
    public Dashboard(URI baseURI) {
        this.dao = new OrderDashboardDAO(baseURI);

        // JFrame Root
        root = new JFrame("EasyOrder Dashboard");
        rootDecorator();

        // Status Bar
        statusBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBarDecorator();

        // Remaining Header Panel
        remainingHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remainingHeaderPanelDecorator();
        updateRemainingHeaderPanel();

        // Table
        table = new JTable();
        tableDecorator();
        setTableColumnHeader();
        updateTableModel();

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.setDefaultEditor(Object.class, null);
        table.addKeyListener(new SpaceListener());

        root.add(statusBarPanel, BorderLayout.SOUTH);
        root.add(scrollPane);
        root.add(remainingHeaderPanel, BorderLayout.NORTH);
        root.pack();

        Timer timer = new Timer(fiveSecond, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dashboard.this.updateTableModel();
            }
        });
        timer.start();
    }

    /* -------------------------------------------------------------------------------- */
    /*	2. Class Methods
    /* -------------------------------------------------------------------------------- */
    private void updateRemainingHeaderPanel() {
        if (remainingListArray.size() == 0) {
            remainingTableLabel.setText("No order left");
        } else {
            remainingTableLabel.setText(remainingListArray.toString());
        }
    }

    private void setTableColumnHeader() {
        TableColumnModel tableColumnModel = table.getColumnModel();
        DefaultTableModel model = ((DefaultTableModel) table.getModel());
        model.setColumnCount(4);
        model.setColumnIdentifiers(new Object[]{"Order ID", "Table Number", "Food", "Quantity"});

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableColumnModel.getColumn(0).setCellRenderer(centerRenderer);
        tableColumnModel.getColumn(1).setCellRenderer(centerRenderer);
        tableColumnModel.getColumn(2).setPreferredWidth(300);
        tableColumnModel.getColumn(3).setCellRenderer(centerRenderer);
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
            if (!remainingListArray.contains(currentTableNum)) {
                remainingListArray.add(currentTableNum);
                remainingIDArray.add(order.getId());
                foodIndexArray.add(foodItemsIndexList);
            }

            // Reset counter
            count = 1;
            foodItemsIndexList = new ArrayList<>();
        }

        System.out.println("Refreshing...");
        //consoleDebugLogHelper();
        updateRemainingHeaderPanel();

        for (int i = model.getRowCount() - 1; i >= rowIndex; i--) {
            model.removeRow(i);
        }
    }

    private void deleteOrder() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (model.getRowCount() == 0) {
            return;
        }

        final int idToDelete = (Integer) model.getValueAt(table.getSelectedRow(), 0);
        int indexToRemove = remainingIDArray.indexOf(idToDelete);

        dao.deleteOrder(idToDelete);
        removeHelper(indexToRemove);

        while (((Integer) model.getValueAt(table.getSelectedRow(), 0)) == idToDelete) {
            model.removeRow(table.getSelectedRow());
        }
    }

    private void deleteIndividualFoodItem() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        if (model.getRowCount() == 0) {
            return;
        }

        final int idToDelete = (Integer) model.getValueAt(table.getSelectedRow(), 0);
        int indexToRemove = remainingIDArray.indexOf(idToDelete);

        ArrayList<Integer> allItems = new ArrayList<>();
        for (ArrayList<Integer> arr : foodIndexArray) {
            for (Integer i : arr) {
                allItems.add(i);
            }
        }

        int count = 0;
        for (int i = 0; i < indexToRemove; i++) {
            count += foodIndexArray.get(i).size();
        }

        int foodIndex = allItems.get(count);
        dao.deleteIndividualFoodItem(idToDelete, foodIndex);

        foodIndexArray.get(indexToRemove).remove(foodIndex);

        if (foodIndexArray.get(indexToRemove).isEmpty()) {
            removeHelper(indexToRemove);
        }

        updateFoodIndexArrayHelper(indexToRemove);
        updateTableModel();

        while (((Integer) model.getValueAt(0, 0)) == idToDelete) {
            model.removeRow(0);
        }
    }

    /* -------------------------------------------------------------------------------- */
    /*	3. Decorator Methods
    /* -------------------------------------------------------------------------------- */
    private void rootDecorator() {
        root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        root.setPreferredSize(new Dimension(1366, 768));
        root.setMinimumSize(new Dimension(1366, 768));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        root.setLocation(dim.width / 2 - root.getSize().width / 2, dim.height / 2 - root.getSize().height / 2);

        ImageIcon img = new ImageIcon("C:\\Users\\Coregame\\IdeaProjects\\EasyOrderDashboard\\src\\main\\res\\dashboard.png");
        root.setIconImage(img.getImage());
    }

    private void remainingHeaderPanelDecorator() {
        remainingHeaderPanel.setBackground(teal500);
        remainingHeaderPanel.setSize(1366, 500);

        JLabel servingText = new JLabel("[Preparing Table]     ");
        servingText.setFont(new Font("Verdana", Font.BOLD, 42));
        servingText.setForeground(Color.white);
        remainingHeaderPanel.add(servingText, BorderLayout.WEST);

        remainingTableLabel = new JLabel();
        remainingTableLabel.setFont(new Font("Verdana", Font.BOLD, 100));
        remainingTableLabel.setForeground(Color.yellow);
        remainingHeaderPanel.add(remainingTableLabel, BorderLayout.WEST);
    }

    private void tableDecorator() {
        table.setFont(new Font("Tahoma", Font.PLAIN, 24));
        table.setRowHeight(table.getRowHeight() + 12);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Verdana", Font.BOLD, 26));
    }

    private void statusBarDecorator() {
        statusBarPanel.setBorder(new CompoundBorder(new LineBorder
                (Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));

        statusLabel = new JLabel();
        statusLabel.setText("<html><b>Status:</b> <font color='green'>Ready</font></html>");
        statusBarPanel.add(statusLabel);

        /*root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                statusLabel.setText(root.getWidth() + "x" + root.getHeight());
            }
        });*/
    }

    /* -------------------------------------------------------------------------------- */
    /*	4. Helper Methods
    /* -------------------------------------------------------------------------------- */
    private void removeHelper(int index) {
        remainingListArray.remove(index);
        remainingIDArray.remove(index);
        foodIndexArray.remove(index);

        updateRemainingHeaderPanel();
    }

    private void updateFoodIndexArrayHelper(int OuterArrayIndex) {
        int limit = foodIndexArray.get(OuterArrayIndex).size();
        if (limit >= 1) {
            for (int i = 0; i < limit; i++) {
                foodIndexArray.get(OuterArrayIndex).set(i, i);
            }
        }
    }

    private void consoleDebugLogHelper(){
        System.out.println("TableNum   : " + remainingListArray);
        System.out.println("Order ID   : " + remainingIDArray);
        System.out.println("Food Index : " + foodIndexArray);
        System.out.println("==========");
    }

    /* -------------------------------------------------------------------------------- */
    /*	5. Main
    /* -------------------------------------------------------------------------------- */
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