package alex.beta.portablecinema.gui;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * https://stackoverflow.com/questions/27075493/how-can-to-create-a-rollover-effect-in-a-jtable
 * <p>
 * Add tooltip support
 * Disable reordering of column
 * Set font of header and cell
 */
public class RollOverTable extends JTable {

    //    private Color rollOverBackground = new Color(233,239,248);
    private Color rollOverBackground = new Color(220, 220, 235);
    private Color rollOverForeground = UIManager.getDefaults().getColor("windowText");

    private int rollOverRowIndex = -1;
    private boolean dispatchedEvent = false;
    private RollOverListener lst;
    private JScrollPane scp = null;

    public RollOverTable(TableModel model) {
        super(model);
        lst = new RollOverListener();
        // Customize L&F
        setRowHeight(20);
        setFont(new Font("Serif", Font.PLAIN, 12));
        setDragEnabled(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getColumnModel().setColumnSelectionAllowed(false);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setFont(new Font("Serif", Font.PLAIN, 12));
        getTableHeader().setPreferredSize(new Dimension(1, 24));
        getTableHeader().setBackground(Color.GRAY);
        getTableHeader().setForeground(Color.WHITE);

        addMouseMotionListener(lst);
        addMouseListener(lst);
        addMouseWheelListener(lst);
    }

    public Color getRollOverBackground() {
        return rollOverBackground;
    }

    public void setRollOverBackground(Color value) {
        rollOverBackground = value;
    }

    public Color getRollOverForeground() {
        return rollOverForeground;
    }

    public void setRollOverForeground(Color value) {
        rollOverForeground = value;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        for (Component c = this; c != null; c = c.getParent())
            if (c instanceof JScrollPane) {
                scp = (JScrollPane) c;
                scp.addMouseWheelListener(new RollOverMouseWheelListener(this));
                break;
            }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (isRowSelected(row)) {
            c.setForeground(getSelectionForeground());
            c.setBackground(getSelectionBackground());
        } else if (row == rollOverRowIndex) {
            c.setForeground(getRollOverForeground());
            c.setBackground(getRollOverBackground());
        } else {
            c.setForeground(getForeground());
            c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
        }
        return c;
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return getModel().getColumnName(realIndex);
            }
        };
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        if (e == null) return null;
        String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);

        if (getModel().getColumnClass(colIndex).isAssignableFrom(Icon.class)) {
            return null;
        }
        try {
            tip = getValueAt(rowIndex, colIndex).toString();
        } catch (RuntimeException e1) {
            //catch null pointer exception if mouse is over an empty line
        }
        return tip;
    }

    protected void repaintRow(int row) {
        if (row == -1) return;
        int modelRow = convertRowIndexToModel(row);
        ((AbstractTableModel) getModel()).fireTableRowsUpdated(modelRow, modelRow);
    }

    private class RollOverListener extends MouseInputAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            if (rollOverRowIndex != -1) {
                repaintRow(rollOverRowIndex);
                rollOverRowIndex = -1;
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            int row = rowAtPoint(e.getPoint());
            if (row != rollOverRowIndex) {
                repaintRow(rollOverRowIndex);
                rollOverRowIndex = row;
                repaintRow(rollOverRowIndex);
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (rollOverRowIndex != -1) {
                repaintRow(rollOverRowIndex);
                rollOverRowIndex = -1;
                dispatchedEvent = true;
                scp.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, scp));
            }
        }
    }

    private class RollOverMouseWheelListener extends MouseInputAdapter {

        private final RollOverTable table;

        public RollOverMouseWheelListener(RollOverTable table) {
            this.table = table;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (dispatchedEvent) {
                dispatchedEvent = false;
                Point point = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), table);
                int row = table.rowAtPoint(point);
                if (rollOverRowIndex != row) {
                    rollOverRowIndex = row;
                    table.repaintRow(rollOverRowIndex);
                }
            }
        }
    }

}
