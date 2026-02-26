package UI;

import UI.Battle.BattleSystem;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;

public class MainMenu extends JFrame implements ActionListener {

    private JButton battleButton;
    private JButton characterButton;
    private JButton itemButton;
    private JButton terrainButton;
    private JPanel mainPanel;
    private CharacterSheetMenu characterSheetMenu;

    public MainMenu() {
        setTitle("Main Menu");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Border outer = BorderFactory.createLineBorder(Color.BLACK);
        Border inner = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border compound = BorderFactory.createCompoundBorder(outer, inner);

        mainPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setPreferredSize(new Dimension(0, 120));
        add(mainPanel, BorderLayout.CENTER);

        battleButton = new JButton("Start Battle System");
        battleButton.setFont(new Font("Arial", Font.BOLD, 30));
        battleButton.setBorder(compound);
        battleButton.setFocusPainted(false);
        battleButton.addActionListener(this);
        mainPanel.add(battleButton);

        characterButton = new JButton("Character Sheets");
        characterButton.setFont(new Font("Arial", Font.BOLD, 30));
        characterButton.setBorder(compound);
        characterButton.setFocusPainted(false);
        characterButton.addActionListener(this);
        mainPanel.add(characterButton);

        itemButton = new JButton("Item Manager");
        itemButton.setFont(new Font("Arial", Font.BOLD, 30));
        itemButton.setBorder(compound);
        itemButton.setFocusPainted(false);
        itemButton.addActionListener(this);
        mainPanel.add(itemButton);

        terrainButton = new JButton("Terrain Manager");
        terrainButton.setFont(new Font("Arial", Font.BOLD, 30));
        terrainButton.setBorder(compound);
        terrainButton.setFocusPainted(false);
        terrainButton.addActionListener(this);
        mainPanel.add(terrainButton);

        characterSheetMenu = new CharacterSheetMenu();
        characterSheetMenu.setVisible(false);

        pack();
        setSize(800, 700);
        mainPanel.revalidate();
        mainPanel.repaint();
        setVisible(true);
    }

    public MainMenu(CharacterSheetMenu c) {
        this();
        this.characterSheetMenu = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == battleButton) {
            handleBattleSystem();
        } else if (e.getSource() == characterButton) {
            handleCharacterSheet();
        } else if (e.getSource() == itemButton) {
            handleItemManager();
        } else if (e.getSource() == terrainButton) {
            handleTerrainManager();
        }
    }

    public void handleBattleSystem() {
        final int[] selectedRows = {0};
        final int[] selectedCols = {0};
        
        // Style constants
        Color bgColor = new Color(45, 45, 48);
        Color cellColor = new Color(70, 70, 75);
        Color hoverColor = new Color(86, 156, 214);
        Color textColor = new Color(220, 220, 220);
        
        JDialog dialog = new JDialog(this, "Select Grid Size", true);
        dialog.setSize(560, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(bgColor);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Title label
        JLabel titleLabel = new JLabel("Hover to select grid size, click to confirm", SwingConstants.CENTER);
        titleLabel.setForeground(textColor);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(titleLabel, BorderLayout.NORTH);
        
        // Size display label
        JLabel sizeLabel = new JLabel("0 x 0", SwingConstants.CENTER);
        sizeLabel.setForeground(hoverColor);
        sizeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sizeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // Grid picker panel - 50x50 fully visible
        final int MAX_DISPLAY = 50;
        final int CELL_SIZE = 10;
        
        JPanel gridPanel = new JPanel() {
            int hoverRow = -1;
            int hoverCol = -1;
            
            {
                setPreferredSize(new Dimension(MAX_DISPLAY * CELL_SIZE + 1, MAX_DISPLAY * CELL_SIZE + 1));
                setBackground(bgColor);
                
                addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(java.awt.event.MouseEvent e) {
                        int col = Math.min(e.getX() / CELL_SIZE, MAX_DISPLAY - 1);
                        int row = Math.min(e.getY() / CELL_SIZE, MAX_DISPLAY - 1);
                        if (col != hoverCol || row != hoverRow) {
                            hoverCol = col;
                            hoverRow = row;
                            selectedRows[0] = row + 1;
                            selectedCols[0] = col + 1;
                            sizeLabel.setText((row + 1) + " x " + (col + 1));
                            repaint();
                        }
                    }
                });
                
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hoverRow = -1;
                        hoverCol = -1;
                        selectedRows[0] = 0;
                        selectedCols[0] = 0;
                        sizeLabel.setText("0 x 0");
                        repaint();
                    }
                    
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (hoverRow >= 0 && hoverCol >= 0) {
                            selectedRows[0] = hoverRow + 1;
                            selectedCols[0] = hoverCol + 1;
                            dialog.dispose();
                        }
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                for (int row = 0; row < MAX_DISPLAY; row++) {
                    for (int col = 0; col < MAX_DISPLAY; col++) {
                        int x = col * CELL_SIZE;
                        int y = row * CELL_SIZE;
                        
                        if (row <= hoverRow && col <= hoverCol) {
                            g2d.setColor(hoverColor);
                        } else {
                            g2d.setColor(cellColor);
                        }
                        g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                        
                        g2d.setColor(bgColor);
                        g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        };
        
        // Wrapper panel to center the grid
        JPanel gridWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        gridWrapper.setBackground(bgColor);
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        gridWrapper.add(gridPanel);
        dialog.add(gridWrapper, BorderLayout.CENTER);
        
        // Bottom panel with size label and cancel button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(bgColor);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        bottomPanel.add(sizeLabel, BorderLayout.CENTER);
        
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.setBackground(cellColor);
        cancelBtn.setForeground(textColor);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelBtn.addActionListener(e -> {
            selectedRows[0] = 0;
            selectedCols[0] = 0;
            dialog.dispose();
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(bgColor);
        btnPanel.add(cancelBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);

        if(selectedRows[0] == 0 || selectedCols[0] == 0) {
            return; // User cancelled or did not enter valid data
        }
        BattleSystem battleSystem = new BattleSystem(selectedRows[0], selectedCols[0], characterSheetMenu);
        this.dispose();
    }

    public void handleCharacterSheet() {
        characterSheetMenu.setVisible(true);
        this.dispose();
    }

    public void handleItemManager() {
        new ItemManager();
        this.dispose();
    }

    public void handleTerrainManager() {
        new TerrainManager();
        this.dispose();
    }

}
