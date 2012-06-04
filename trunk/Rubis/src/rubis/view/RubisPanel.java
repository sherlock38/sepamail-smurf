package rubis.view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.table.TableCellRenderer;

/**
 * The RubisPanel class defines the UI that is used by the user to gather, generate and send requests of payments
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RubisPanel extends javax.swing.JPanel {

    /**
     * Creates new form RubisPanel
     */
    public RubisPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titlePanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        stepsPanel = new javax.swing.JPanel();
        firstStepToggleButton = new javax.swing.JToggleButton();
        centerPanel = new javax.swing.JPanel();
        secondStepToggleButton = new javax.swing.JToggleButton();
        thirdStepToggleButton = new javax.swing.JToggleButton();
        gridControlsPanel = new javax.swing.JPanel();
        gridScrollPane = new javax.swing.JScrollPane();
        gridTable = new javax.swing.JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                Color backgroundColor;
                if (rowIndex % 2 == 0) {
                    backgroundColor = Color.WHITE;
                } else {
                    backgroundColor = new Color(243, 246, 250);
                }
                if (isCellSelected(rowIndex, vColIndex)) {
                    c.setBackground(new Color(83,150,227));
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(backgroundColor);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        pagerControlsPanel = new javax.swing.JPanel();
        pagerButtonsPanel = new javax.swing.JPanel();
        firstButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        lastButton = new javax.swing.JButton();
        pagerLabelContainerPanel = new javax.swing.JPanel();
        pagerLabel = new javax.swing.JLabel();
        pagesComboBox = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new java.awt.BorderLayout());

        titlePanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        titleLabel.setText("Demandes de règlement SEPAmail RUBIS");
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 15, 0));
        titlePanel.add(titleLabel, java.awt.BorderLayout.CENTER);

        add(titlePanel, java.awt.BorderLayout.NORTH);

        stepsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));
        stepsPanel.setPreferredSize(new java.awt.Dimension(138, 531));
        stepsPanel.setSize(new java.awt.Dimension(138, 138));
        stepsPanel.setLayout(new java.awt.BorderLayout());

        firstStepToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_1_def.png"))); // NOI18N
        firstStepToggleButton.setActionCommand("RECUPERATION");
        firstStepToggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        firstStepToggleButton.setBorderPainted(false);
        firstStepToggleButton.setContentAreaFilled(false);
        firstStepToggleButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        firstStepToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        firstStepToggleButton.setIconTextGap(5);
        firstStepToggleButton.setLabel("Récupération");
        firstStepToggleButton.setMaximumSize(new java.awt.Dimension(128, 128));
        firstStepToggleButton.setMinimumSize(new java.awt.Dimension(128, 128));
        firstStepToggleButton.setPreferredSize(new java.awt.Dimension(128, 128));
        firstStepToggleButton.setRequestFocusEnabled(false);
        firstStepToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_1.png"))); // NOI18N
        firstStepToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stepsPanel.add(firstStepToggleButton, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.GridBagLayout());

        secondStepToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_2_def.png"))); // NOI18N
        secondStepToggleButton.setActionCommand("GENERATION");
        secondStepToggleButton.setBorderPainted(false);
        secondStepToggleButton.setContentAreaFilled(false);
        secondStepToggleButton.setEnabled(false);
        secondStepToggleButton.setFocusPainted(false);
        secondStepToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        secondStepToggleButton.setIconTextGap(5);
        secondStepToggleButton.setLabel("Génération");
        secondStepToggleButton.setMaximumSize(new java.awt.Dimension(128, 128));
        secondStepToggleButton.setMinimumSize(new java.awt.Dimension(128, 128));
        secondStepToggleButton.setPreferredSize(new java.awt.Dimension(128, 128));
        secondStepToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_2.png"))); // NOI18N
        secondStepToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        centerPanel.add(secondStepToggleButton, new java.awt.GridBagConstraints());

        stepsPanel.add(centerPanel, java.awt.BorderLayout.CENTER);

        thirdStepToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_3_def.png"))); // NOI18N
        thirdStepToggleButton.setActionCommand("ENVOI");
        thirdStepToggleButton.setBorderPainted(false);
        thirdStepToggleButton.setContentAreaFilled(false);
        thirdStepToggleButton.setEnabled(false);
        thirdStepToggleButton.setFocusPainted(false);
        thirdStepToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        thirdStepToggleButton.setIconTextGap(5);
        thirdStepToggleButton.setLabel("Envoi");
        thirdStepToggleButton.setMaximumSize(new java.awt.Dimension(128, 128));
        thirdStepToggleButton.setMinimumSize(new java.awt.Dimension(128, 128));
        thirdStepToggleButton.setPreferredSize(new java.awt.Dimension(128, 128));
        thirdStepToggleButton.setRequestFocusEnabled(false);
        thirdStepToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/step_3.png"))); // NOI18N
        thirdStepToggleButton.setSize(new java.awt.Dimension(128, 128));
        thirdStepToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        stepsPanel.add(thirdStepToggleButton, java.awt.BorderLayout.SOUTH);

        add(stepsPanel, java.awt.BorderLayout.LINE_END);

        gridControlsPanel.setLayout(new java.awt.BorderLayout());

        gridTable.setGridColor(new java.awt.Color(187, 187, 187));
        gridTable.setRowHeight(26);
        gridTable.setSelectionBackground(new java.awt.Color(83, 150, 227));
        gridTable.setShowHorizontalLines(false);
        gridTable.setShowVerticalLines(false);
        gridScrollPane.setViewportView(gridTable);

        gridControlsPanel.add(gridScrollPane, java.awt.BorderLayout.CENTER);

        pagerControlsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        pagerControlsPanel.setMaximumSize(new java.awt.Dimension(2147483647, 40));
        pagerControlsPanel.setMinimumSize(new java.awt.Dimension(40, 40));
        pagerControlsPanel.setPreferredSize(new java.awt.Dimension(703, 40));
        pagerControlsPanel.setSize(new java.awt.Dimension(40, 40));
        pagerControlsPanel.setLayout(new java.awt.BorderLayout());

        pagerButtonsPanel.setMaximumSize(new java.awt.Dimension(160, 30));
        pagerButtonsPanel.setMinimumSize(new java.awt.Dimension(160, 30));
        pagerButtonsPanel.setPreferredSize(new java.awt.Dimension(160, 30));
        pagerButtonsPanel.setLayout(new java.awt.GridLayout(1, 4, 4, 0));

        firstButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/first.png"))); // NOI18N
        firstButton.setActionCommand("FIRST_PAGE");
        firstButton.setContentAreaFilled(false);
        firstButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/first_dis.png"))); // NOI18N
        firstButton.setEnabled(false);
        pagerButtonsPanel.add(firstButton);

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/previous.png"))); // NOI18N
        previousButton.setActionCommand("PREVIOUS_PAGE");
        previousButton.setContentAreaFilled(false);
        previousButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/previous_dis.png"))); // NOI18N
        previousButton.setEnabled(false);
        pagerButtonsPanel.add(previousButton);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/next.png"))); // NOI18N
        nextButton.setActionCommand("NEXT_PAGE");
        nextButton.setContentAreaFilled(false);
        nextButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/next_dis.png"))); // NOI18N
        nextButton.setEnabled(false);
        pagerButtonsPanel.add(nextButton);

        lastButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/last.png"))); // NOI18N
        lastButton.setActionCommand("LAST_PAGE");
        lastButton.setContentAreaFilled(false);
        lastButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/rubis/view/last_dis.png"))); // NOI18N
        lastButton.setEnabled(false);
        pagerButtonsPanel.add(lastButton);

        pagerControlsPanel.add(pagerButtonsPanel, java.awt.BorderLayout.WEST);

        pagerLabelContainerPanel.setLayout(new java.awt.GridBagLayout());
        pagerLabelContainerPanel.add(pagerLabel, new java.awt.GridBagConstraints());

        pagerControlsPanel.add(pagerLabelContainerPanel, java.awt.BorderLayout.CENTER);

        pagesComboBox.setActionCommand("PAGE_CHANGE");
        pagesComboBox.setEnabled(false);
        pagesComboBox.setMaximumSize(new java.awt.Dimension(32767, 27));
        pagesComboBox.setMinimumSize(new java.awt.Dimension(110, 27));
        pagesComboBox.setPreferredSize(new java.awt.Dimension(110, 27));
        pagerControlsPanel.add(pagesComboBox, java.awt.BorderLayout.EAST);

        gridControlsPanel.add(pagerControlsPanel, java.awt.BorderLayout.PAGE_END);

        add(gridControlsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    public javax.swing.JButton firstButton;
    public javax.swing.JToggleButton firstStepToggleButton;
    private javax.swing.JPanel gridControlsPanel;
    public javax.swing.JScrollPane gridScrollPane;
    public javax.swing.JTable gridTable;
    public javax.swing.JButton lastButton;
    public javax.swing.JButton nextButton;
    private javax.swing.JPanel pagerButtonsPanel;
    private javax.swing.JPanel pagerControlsPanel;
    public javax.swing.JLabel pagerLabel;
    private javax.swing.JPanel pagerLabelContainerPanel;
    public javax.swing.JComboBox pagesComboBox;
    public javax.swing.JButton previousButton;
    public javax.swing.JToggleButton secondStepToggleButton;
    private javax.swing.JPanel stepsPanel;
    public javax.swing.JToggleButton thirdStepToggleButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel titlePanel;
    // End of variables declaration//GEN-END:variables
}