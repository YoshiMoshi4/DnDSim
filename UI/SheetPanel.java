package UI;

import EntityRes.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SheetPanel extends JPanel implements ActionListener {

    private CharSheet sheet;

    private final JLabel NAME_LBL = new JLabel("Name:");
    private JTextField name;
    private final JButton updateNameBtn = new JButton("Update");

    private final JLabel CURRENT_HP_LBL = new JLabel("Current HP:");
    private JLabel hpBar;
    private JTextField hpRatio;
    private final JButton updateHPBtn = new JButton("Update");

    private final JLabel STATUS_LBL = new JLabel("Status:");
    private JTextField status;
    private final JButton updateStatusBtn = new JButton("Update");

    private final JLabel COLOR_LBL = new JLabel("Color:");
    private JComboBox<String> colorDropdown;
    private final JButton updateColorBtn = new JButton("Update");

    private final JLabel PRIMARY_LBL = new JLabel("Primary:");
    private final JLabel SECONDARY_LBL = new JLabel("Secondary:");
    private JComboBox<String> primary;
    private JComboBox<String> secondary;
    private final JButton updatePrimaryBtn = new JButton("Update");
    private final JButton updateSecondaryBtn = new JButton("Update");

    private final JLabel ARMOR_LBL = new JLabel("Armor:");
    private final JLabel HEAD_LBL = new JLabel("Head:");
    private final JLabel TORSO_LBL = new JLabel("Torso:");
    private final JLabel LEGS_LBL = new JLabel("Legs:");
    private JComboBox<String> head;
    private JComboBox<String> torso;
    private JComboBox<String> legs;
    private final JButton updateHeadBtn = new JButton("Update");
    private final JButton updateTorsoBtn = new JButton("Update");
    private final JButton updateLegsBtn = new JButton("Update");

    private final JLabel INVENTORY_LBL = new JLabel("Inventory:");
    private JTextArea inventory;

    // Attribute display labels and fields
    private final JLabel STR_LBL = new JLabel("STR");
    private final JLabel DEX_LBL = new JLabel("DEX");
    private final JLabel ITV_LBL = new JLabel("ITV");
    private final JLabel MOB_LBL = new JLabel("MOB");
    
    private JLabel strBase;
    private JLabel strTemp;
    private JLabel strTotal;
    private JLabel dexBase;
    private JLabel dexTemp;
    private JLabel dexTotal;
    private JLabel itvBase;
    private JLabel itvTemp;
    private JLabel itvTotal;
    private JLabel mobBase;
    private JLabel mobTemp;
    private JLabel mobTotal;

    private final JButton updateAllBtn = new JButton("Update All");
    private final JButton resetEnemyBtn = new JButton("Reset Enemy");

    public SheetPanel(CharSheet sheet) {
        this.sheet = sheet;
        setBounds(300, 0, 700, 700);
        setVisible(true);
        this.setLayout(new BorderLayout());

        // Create inner panel with null layout for absolute positioning
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(null);
        innerPanel.setPreferredSize(new Dimension(680, 900));
        innerPanel.setBackground(getBackground());

        // ===== BASIC INFO SECTION (top) =====
        NAME_LBL.setBounds(10, 10, 80, 25);
        innerPanel.add(NAME_LBL);
        name = new JTextField();
        name.setBounds(100, 10, 100, 25);
        innerPanel.add(name);
        updateNameBtn.setBounds(210, 10, 80, 25);
        updateNameBtn.addActionListener(this);
        innerPanel.add(updateNameBtn);

        CURRENT_HP_LBL.setBounds(10, 45, 80, 25);
        innerPanel.add(CURRENT_HP_LBL);
        hpBar = new JLabel("//////////");
        hpBar.setBounds(160, 45, 40, 25);
        innerPanel.add(hpBar);
        hpRatio = new JTextField();
        hpRatio.setBounds(100, 45, 50, 25);
        innerPanel.add(hpRatio);
        updateHPBtn.setBounds(210, 45, 80, 25);
        updateHPBtn.addActionListener(this);
        innerPanel.add(updateHPBtn);

        STATUS_LBL.setBounds(10, 80, 80, 25);
        innerPanel.add(STATUS_LBL);
        status = new JTextField();
        status.setBounds(100, 80, 100, 25);
        innerPanel.add(status);
        updateStatusBtn.setBounds(210, 80, 80, 25);
        updateStatusBtn.addActionListener(this);
        innerPanel.add(updateStatusBtn);

        COLOR_LBL.setBounds(10, 115, 80, 25);
        innerPanel.add(COLOR_LBL);
        colorDropdown = new JComboBox<>(EntityRes.CharSheet.getColorNames());
        colorDropdown.setBounds(100, 115, 100, 25);
        innerPanel.add(colorDropdown);
        updateColorBtn.setBounds(210, 115, 80, 25);
        updateColorBtn.addActionListener(this);
        innerPanel.add(updateColorBtn);

        // ===== BUTTONS (top right) =====
        updateAllBtn.setBounds(500, 10, 100, 30);
        updateAllBtn.addActionListener(this);
        innerPanel.add(updateAllBtn);

        resetEnemyBtn.setBounds(500, 45, 100, 30);
        resetEnemyBtn.addActionListener(this);
        innerPanel.add(resetEnemyBtn);

        // ===== WEAPONS SECTION (left middle) =====
        PRIMARY_LBL.setBounds(10, 160, 80, 25);
        innerPanel.add(PRIMARY_LBL);
        primary = new JComboBox<>(ItemDatabase.getInstance().getAllWeapons().keySet().toArray(new String[0]));
        primary.setBounds(100, 160, 100, 25);
        innerPanel.add(primary);
        updatePrimaryBtn.setBounds(210, 160, 80, 25);
        updatePrimaryBtn.addActionListener(this);
        innerPanel.add(updatePrimaryBtn);
        
        SECONDARY_LBL.setBounds(10, 195, 80, 25);
        innerPanel.add(SECONDARY_LBL);
        secondary = new JComboBox<>(ItemDatabase.getInstance().getAllWeapons().keySet().toArray(new String[0]));
        secondary.setBounds(100, 195, 100, 25);
        innerPanel.add(secondary);
        updateSecondaryBtn.setBounds(210, 195, 80, 25);
        updateSecondaryBtn.addActionListener(this);
        innerPanel.add(updateSecondaryBtn);

        // ===== ARMOR SECTION (middle) =====
        ARMOR_LBL.setBounds(320, 160, 80, 25);
        innerPanel.add(ARMOR_LBL);
        
        HEAD_LBL.setBounds(320, 195, 80, 25);
        innerPanel.add(HEAD_LBL);
        head = new JComboBox<>(ItemDatabase.getInstance().getAllArmors().keySet().toArray(new String[0]));
        head.setBounds(400, 195, 100, 25);
        innerPanel.add(head);
        updateHeadBtn.setBounds(510, 195, 80, 25);
        updateHeadBtn.addActionListener(this);
        innerPanel.add(updateHeadBtn);
        
        TORSO_LBL.setBounds(320, 230, 80, 25);
        innerPanel.add(TORSO_LBL);
        torso = new JComboBox<>(ItemDatabase.getInstance().getAllArmors().keySet().toArray(new String[0]));
        torso.setBounds(400, 230, 100, 25);
        innerPanel.add(torso);
        updateTorsoBtn.setBounds(510, 230, 80, 25);
        updateTorsoBtn.addActionListener(this);
        innerPanel.add(updateTorsoBtn);
        
        LEGS_LBL.setBounds(320, 265, 80, 25);
        innerPanel.add(LEGS_LBL);
        legs = new JComboBox<>(ItemDatabase.getInstance().getAllArmors().keySet().toArray(new String[0]));
        legs.setBounds(400, 265, 100, 25);
        innerPanel.add(legs);
        updateLegsBtn.setBounds(510, 265, 80, 25);
        updateLegsBtn.addActionListener(this);
        innerPanel.add(updateLegsBtn);

        // ===== ATTRIBUTES SECTION (right side, separate from overlapping items) =====
        JLabel ATTRIBUTES_HEADER = new JLabel("=== ATTRIBUTES ===");
        ATTRIBUTES_HEADER.setBounds(10, 310, 200, 25);
        innerPanel.add(ATTRIBUTES_HEADER);
        
        // Headers for attribute columns
        JLabel baseHeader = new JLabel("Base");
        baseHeader.setBounds(40, 340, 50, 20);
        innerPanel.add(baseHeader);
        JLabel tempHeader = new JLabel("Bonus");
        tempHeader.setBounds(100, 340, 50, 20);
        innerPanel.add(tempHeader);
        JLabel totalHeader = new JLabel("Total");
        totalHeader.setBounds(160, 340, 50, 20);
        innerPanel.add(totalHeader);
        
        // STR
        STR_LBL.setBounds(10, 365, 30, 20);
        innerPanel.add(STR_LBL);
        strBase = new JLabel();
        strBase.setBounds(45, 365, 40, 20);
        innerPanel.add(strBase);
        strTemp = new JLabel();
        strTemp.setBounds(100, 365, 50, 20);
        innerPanel.add(strTemp);
        strTotal = new JLabel();
        strTotal.setBounds(160, 365, 50, 20);
        innerPanel.add(strTotal);
        
        // DEX
        DEX_LBL.setBounds(10, 390, 30, 20);
        innerPanel.add(DEX_LBL);
        dexBase = new JLabel();
        dexBase.setBounds(45, 390, 40, 20);
        innerPanel.add(dexBase);
        dexTemp = new JLabel();
        dexTemp.setBounds(100, 390, 50, 20);
        innerPanel.add(dexTemp);
        dexTotal = new JLabel();
        dexTotal.setBounds(160, 390, 50, 20);
        innerPanel.add(dexTotal);
        
        // ITV
        ITV_LBL.setBounds(10, 415, 30, 20);
        innerPanel.add(ITV_LBL);
        itvBase = new JLabel();
        itvBase.setBounds(45, 415, 40, 20);
        innerPanel.add(itvBase);
        itvTemp = new JLabel();
        itvTemp.setBounds(100, 415, 50, 20);
        innerPanel.add(itvTemp);
        itvTotal = new JLabel();
        itvTotal.setBounds(160, 415, 50, 20);
        innerPanel.add(itvTotal);
        
        // MOB
        MOB_LBL.setBounds(10, 440, 30, 20);
        innerPanel.add(MOB_LBL);
        mobBase = new JLabel();
        mobBase.setBounds(45, 440, 40, 20);
        innerPanel.add(mobBase);
        mobTemp = new JLabel();
        mobTemp.setBounds(100, 440, 50, 20);
        innerPanel.add(mobTemp);
        mobTotal = new JLabel();
        mobTotal.setBounds(160, 440, 50, 20);
        innerPanel.add(mobTotal);

        // ===== INVENTORY SECTION (bottom) =====
        INVENTORY_LBL.setBounds(10, 480, 80, 25);
        innerPanel.add(INVENTORY_LBL);
        inventory = new JTextArea();
        inventory.setBounds(10, 510, 650, 350);
        inventory.setEditable(false);
        innerPanel.add(inventory);

        // Add inner panel to scroll pane
        JScrollPane scrollPane = new JScrollPane(innerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events for the Character Sheet here
        if (e.getSource() == updateNameBtn) {
            updateName();
        } else if (e.getSource() == updateHPBtn) {
            updateHP();
        } else if (e.getSource() == updateStatusBtn) {
            updateStatus();
        } else if (e.getSource() == updateColorBtn) {
            updateColor();
        } else if (e.getSource() == updatePrimaryBtn) {
            updatePrimary();
        } else if (e.getSource() == updateSecondaryBtn) {
            updateSecondary();
        } else if (e.getSource() == updateHeadBtn) {
            updateHead();
        } else if (e.getSource() == updateTorsoBtn) {
            updateTorso();
        } else if (e.getSource() == updateLegsBtn) {
            updateLegs();
        } else if (e.getSource() == updateAllBtn) {
            updateAll();
        } else if (e.getSource() == resetEnemyBtn) {
            resetEnemy();
        }
        updateSheet();
        sheet.save();
    }

    public void updateSheet() {
        name.setText(sheet.getName());
        colorDropdown.setSelectedIndex(sheet.getColor());

        hpRatio.setText(sheet.getCurrentHP() + "/" + sheet.getTotalHP());
        String temp = "";
        int numBars = (int) Math.ceil(((1.0 * sheet.getCurrentHP() / sheet.getTotalHP()) * 10));
        for (int i = 0; i < 10; i++) {
            if (i < numBars) {
                temp += "/";
            } else {
                temp += "-";
            }
        }
        hpBar.setText(temp);

        // Populate weapon combo boxes with weapons in inventory
        primary.removeAllItems();
        secondary.removeAllItems();
        for (Item item : sheet.getInventory()) {
            if (item instanceof Weapon) {
                primary.addItem(item.getName());
                secondary.addItem(item.getName());
            }
        }
        // Add currently equipped if not in inventory (shouldn't happen but safety)
        if (sheet.getEquippedWeapon() != null && primary.getItemCount() == 0) {
            primary.addItem(sheet.getEquippedWeapon().getName());
        }
        if (sheet.getEquippedSecondary() != null && secondary.getItemCount() == 0) {
            secondary.addItem(sheet.getEquippedSecondary().getName());
        }

        primary.setSelectedItem(sheet.getEquippedWeapon().getName());
        secondary.setSelectedItem(sheet.getEquippedSecondary().getName());

        // Populate armor combo boxes with armor in inventory
        head.removeAllItems();
        torso.removeAllItems();
        legs.removeAllItems();
        for (Item item : sheet.getInventory()) {
            if (item instanceof Armor) {
                Armor armor = (Armor) item;
                if (armor.getArmorType() == 0) head.addItem(item.getName());
                else if (armor.getArmorType() == 1) torso.addItem(item.getName());
                else if (armor.getArmorType() == 2) legs.addItem(item.getName());
            }
        }
        // Add currently equipped if not in inventory
        if (sheet.getHead() != null && head.getItemCount() == 0) head.addItem(sheet.getHead().getName());
        if (sheet.getTorso() != null && torso.getItemCount() == 0) torso.addItem(sheet.getTorso().getName());
        if (sheet.getLegs() != null && legs.getItemCount() == 0) legs.addItem(sheet.getLegs().getName());

        head.setSelectedItem(sheet.getHead().getName());
        torso.setSelectedItem(sheet.getTorso().getName());
        legs.setSelectedItem(sheet.getLegs().getName());

        // Display attributes: Base | Temp | Total
        strBase.setText(sheet.getAttribute(0) + "");
        strTemp.setText(sheet.getTempAttribute(0) + "");
        strTotal.setText(sheet.getTotalAttribute(0) + "");
        
        dexBase.setText(sheet.getAttribute(1) + "");
        dexTemp.setText(sheet.getTempAttribute(1) + "");
        dexTotal.setText(sheet.getTotalAttribute(1) + "");
        
        itvBase.setText(sheet.getAttribute(2) + "");
        itvTemp.setText(sheet.getTempAttribute(2) + "");
        itvTotal.setText(sheet.getTotalAttribute(2) + "");
        
        mobBase.setText(sheet.getAttribute(3) + "");
        mobTemp.setText(sheet.getTempAttribute(3) + "");
        mobTotal.setText(sheet.getTotalAttribute(3) + "");

        // Update inventory display
        StringBuilder invText = new StringBuilder();
        for (Item item : sheet.getInventory()) {
            invText.append(item.getName()).append(" (").append(item.getQuantity()).append(")\n");
        }
        inventory.setText(invText.toString());
    }

    public void updateName() {
        sheet.setName(name.getText());
    }

    public void updateHP() {
        String ratioString = hpRatio.getText();
        String currentHP = "";
        String totalHP = "";
        boolean isDenominator = false;
        for (char c : ratioString.toCharArray()) {
            if (c == '/') {
                isDenominator = true;
            } else if (!isDenominator) {
                currentHP += c;
            } else {
                totalHP += c;
            }
        }
        sheet.setCurrentHP(Integer.parseInt(currentHP));
        sheet.setTotalHP(Integer.parseInt(totalHP));
    }

    public void updateStatus() {
        sheet.clearStatus();
        String temp = "";
        for (char c : status.getText().toCharArray()) {
            if (c == ',') {
                sheet.addStatus(new Status(temp));
                temp = "";
            } else if (c != ' ') {
                temp += c;
            }
        }
        if (temp.length() > 0) {
            sheet.addStatus(new Status(temp));
        }
    }

    public void updateColor() {
        sheet.setColor(colorDropdown.getSelectedIndex());
    }

    public void updatePrimary() {
        String selectedWeapon = (String) primary.getSelectedItem();
        if (selectedWeapon != null && !sheet.getEquippedWeapon().getName().equals(selectedWeapon)) {
            // Find the weapon in inventory
            for (Item item : sheet.getInventory()) {
                if (item.getName().equals(selectedWeapon) && item instanceof Weapon) {
                    sheet.equipPrimaryWeapon((Weapon) item);
                    break;
                }
            }
        }
    }

    public void updateSecondary() {
        String selectedWeapon = (String) secondary.getSelectedItem();
        if (selectedWeapon != null && !sheet.getEquippedSecondary().getName().equals(selectedWeapon)) {
            // Find the weapon in inventory
            for (Item item : sheet.getInventory()) {
                if (item.getName().equals(selectedWeapon) && item instanceof Weapon) {
                    sheet.equipSecondaryWeapon((Weapon) item);
                    break;
                }
            }
        }
    }

    public void updateHead() {
        String selectedArmor = (String) head.getSelectedItem();
        if (selectedArmor != null && !sheet.getHead().getName().equals(selectedArmor)) {
            // Find the armor in inventory
            for (Item item : sheet.getInventory()) {
                if (item.getName().equals(selectedArmor) && item instanceof Armor) {
                    sheet.equipHead((Armor) item);
                    break;
                }
            }
        }
    }

    public void updateTorso() {
        String selectedArmor = (String) torso.getSelectedItem();
        if (selectedArmor != null && !sheet.getTorso().getName().equals(selectedArmor)) {
            // Find the armor in inventory
            for (Item item : sheet.getInventory()) {
                if (item.getName().equals(selectedArmor) && item instanceof Armor) {
                    sheet.equipTorso((Armor) item);
                    break;
                }
            }
        }
    }

    public void updateLegs() {
        String selectedArmor = (String) legs.getSelectedItem();
        if (selectedArmor != null && !sheet.getLegs().getName().equals(selectedArmor)) {
            // Find the armor in inventory
            for (Item item : sheet.getInventory()) {
                if (item.getName().equals(selectedArmor) && item instanceof Armor) {
                    sheet.equipLegs((Armor) item);
                    break;
                }
            }
        }
    }

    public void updateAll() {
        updateName();
        updateHP();
        updateStatus();
        updateColor();
        updatePrimary();
        updateSecondary();
        updateHead();
        updateTorso();
        updateLegs();
    }

    public void resetEnemy() {
        if (!sheet.getParty()) {
            sheet.setCurrentHP(sheet.getTotalHP());
            sheet.clearStatus();
            // Could reset equipment to defaults, but for now just HP and status
        }
    }

    public CharSheet getCharSheet() {
        return sheet;
    }
}
