package UI;

import EntityRes.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class ItemManager extends JFrame implements ActionListener {

    private JButton backBtn;
    private JButton addWeaponBtn;
    private JButton addArmorBtn;
    private JButton addConsumableBtn;
    private JButton addAmmoBtn;
    private JButton addCraftingBtn;
    private JButton addKeyItemBtn;
    private JTabbedPane tabbedPane;
    private ArrayList<Weapon> weapons;
    private ArrayList<Armor> armors;
    private ArrayList<Consumable> consumables;
    private ArrayList<Ammunition> ammunition;
    private ArrayList<CraftingItem> craftingItems;
    private ArrayList<KeyItem> keyItems;
    private static final String[] ATTRIBUTE_NAMES = {"STR", "DEX", "ITV", "MOB"};
    private static final String[] ARMOR_TYPES = {"Head", "Torso", "Legs"};
    private static final String[] CRAFTING_CATEGORIES = {"Material", "Component", "Reagent", "Miscellaneous"};

    public ItemManager() {
        setTitle("Item Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        weapons = new ArrayList<>();
        armors = new ArrayList<>();
        consumables = new ArrayList<>();
        ammunition = new ArrayList<>();
        craftingItems = new ArrayList<>();
        keyItems = new ArrayList<>();
        loadItems();

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        backBtn = new JButton("Back to Main Menu");
        backBtn.addActionListener(this);
        topPanel.add(backBtn);

        addWeaponBtn = new JButton("Add Weapon");
        addWeaponBtn.addActionListener(this);
        topPanel.add(addWeaponBtn);

        addArmorBtn = new JButton("Add Armor");
        addArmorBtn.addActionListener(this);
        topPanel.add(addArmorBtn);

        addConsumableBtn = new JButton("Add Consumable");
        addConsumableBtn.addActionListener(this);
        topPanel.add(addConsumableBtn);

        addAmmoBtn = new JButton("Add Ammo");
        addAmmoBtn.addActionListener(this);
        topPanel.add(addAmmoBtn);

        addCraftingBtn = new JButton("Add Crafting");
        addCraftingBtn.addActionListener(this);
        topPanel.add(addCraftingBtn);

        addKeyItemBtn = new JButton("Add Key Item");
        addKeyItemBtn.addActionListener(this);
        topPanel.add(addKeyItemBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Weapons", createWeaponsPanel());
        tabbedPane.addTab("Armor", createArmorsPanel());
        tabbedPane.addTab("Consumables", createConsumablesPanel());
        tabbedPane.addTab("Ammunition", createAmmunitionPanel());
        tabbedPane.addTab("Crafting", createCraftingPanel());
        tabbedPane.addTab("Key Items", createKeyItemsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private void loadItems() {
        ItemDatabase db = ItemDatabase.getInstance();
        weapons.addAll(db.getAllWeapons().values());
        armors.addAll(db.getAllArmors().values());
        consumables.addAll(db.getAllConsumables().values());
        ammunition.addAll(db.getAllAmmunition().values());
        craftingItems.addAll(db.getAllCraftingItems().values());
        keyItems.addAll(db.getAllKeyItems().values());
    }

    private JPanel createWeaponsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        for (Weapon weapon : weapons) {
            panel.add(createWeaponCard(weapon));
        }
        return new JScrollPane(panel).getViewport().getView() != null ? 
               (JPanel) new JScrollPane(panel).getViewport().getView() : panel;
    }

    private JPanel createArmorsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        for (Armor armor : armors) {
            panel.add(createArmorCard(armor));
        }
        return panel;
    }

    private JPanel createConsumablesPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        for (Consumable item : consumables) {
            panel.add(createConsumableCard(item));
        }
        return panel;
    }

    private JPanel createAmmunitionPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        for (Ammunition item : ammunition) {
            panel.add(createAmmunitionCard(item));
        }
        return panel;
    }

    private JPanel createCraftingPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        for (CraftingItem item : craftingItems) {
            panel.add(createCraftingCard(item));
        }
        return panel;
    }

    private JPanel createKeyItemsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        for (KeyItem item : keyItems) {
            panel.add(createKeyItemCard(item));
        }
        return panel;
    }

    private JPanel createWeaponCard(Weapon weapon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(weapon.getName()));
        card.setBackground(weapon.getDisplayColor());

        card.add(new JLabel("Damage: " + weapon.getDamage()));
        int[] attrs = weapon.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            card.add(new JLabel(ATTRIBUTE_NAMES[i] + ": " + attrs[i]));
        }

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editWeapon(weapon));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(weapon, weapons));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private JPanel createArmorCard(Armor armor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(armor.getName()));
        card.setBackground(armor.getDisplayColor());

        card.add(new JLabel("Type: " + ARMOR_TYPES[armor.getArmorType()]));
        card.add(new JLabel("Defense: " + armor.getDefense()));
        int[] attrs = armor.getModifiedAttributes();
        for (int i = 0; i < attrs.length && i < ATTRIBUTE_NAMES.length; i++) {
            card.add(new JLabel(ATTRIBUTE_NAMES[i] + ": " + attrs[i]));
        }

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editArmor(armor));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(armor, armors));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private JPanel createConsumableCard(Consumable item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(item.getName()));
        card.setBackground(item.getDisplayColor());

        card.add(new JLabel("Type: " + item.getType()));
        card.add(new JLabel("Heal Amount: " + item.getHealAmount()));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editConsumable(item));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(item, consumables));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private JPanel createAmmunitionCard(Ammunition item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(item.getName()));
        card.setBackground(item.getDisplayColor());

        card.add(new JLabel("Type: Ammunition"));
        card.add(new JLabel("Damage Bonus: " + item.getDamageBonus()));
        card.add(new JLabel("Compatible: " + item.getCompatibleWeaponType()));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editAmmunition(item));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(item, ammunition));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private JPanel createCraftingCard(CraftingItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(item.getName()));
        card.setBackground(item.getDisplayColor());

        card.add(new JLabel("Category: " + item.getCraftingCategory()));
        card.add(new JLabel("Desc: " + (item.getDescription() != null ? item.getDescription() : "")));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editCraftingItem(item));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(item, craftingItems));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private JPanel createKeyItemCard(KeyItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createTitledBorder(item.getName()));
        card.setBackground(item.getDisplayColor());

        card.add(new JLabel("Quest: " + (item.isQuestRelated() ? "Yes" : "No")));
        card.add(new JLabel("Desc: " + (item.getDescription() != null ? item.getDescription() : "")));

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> editKeyItem(item));
        btnPanel.add(editBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteItem(item, keyItems));
        btnPanel.add(deleteBtn);

        card.add(btnPanel);
        return card;
    }

    private void addNewWeapon() {
        JDialog dialog = new JDialog(this, "Add New Weapon", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField damageField = new JTextField("1");
        JTextField strField = new JTextField("0");
        JTextField dexField = new JTextField("0");
        JTextField itvField = new JTextField("0");
        JTextField mobField = new JTextField("0");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(4); // Default red

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Damage:"));
        dialog.add(damageField);
        dialog.add(new JLabel("STR Modifier:"));
        dialog.add(strField);
        dialog.add(new JLabel("DEX Modifier:"));
        dialog.add(dexField);
        dialog.add(new JLabel("ITV Modifier:"));
        dialog.add(itvField);
        dialog.add(new JLabel("MOB Modifier:"));
        dialog.add(mobField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                int damage = Integer.parseInt(damageField.getText());
                int[] attrs = new int[]{
                    Integer.parseInt(strField.getText()),
                    Integer.parseInt(dexField.getText()),
                    Integer.parseInt(itvField.getText()),
                    Integer.parseInt(mobField.getText())
                };
                
                if (!name.isEmpty()) {
                    Weapon weapon = new Weapon(name, "Weapon", damage, attrs);
                    weapon.setColor(colorDropdown.getSelectedIndex());
                    ItemDatabase.getInstance().saveItem(weapon);
                    dialog.dispose();
                    refreshDisplay();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void addNewArmor() {
        JDialog dialog = new JDialog(this, "Add New Armor", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));

        JTextField nameField = new JTextField();
        JComboBox<String> armorTypeBox = new JComboBox<>(ARMOR_TYPES);
        JTextField defenseField = new JTextField("1");
        JTextField strField = new JTextField("0");
        JTextField dexField = new JTextField("0");
        JTextField itvField = new JTextField("0");
        JTextField mobField = new JTextField("0");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(9); // Default blue

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Armor Type:"));
        dialog.add(armorTypeBox);
        dialog.add(new JLabel("Defense:"));
        dialog.add(defenseField);
        dialog.add(new JLabel("STR Modifier:"));
        dialog.add(strField);
        dialog.add(new JLabel("DEX Modifier:"));
        dialog.add(dexField);
        dialog.add(new JLabel("ITV Modifier:"));
        dialog.add(itvField);
        dialog.add(new JLabel("MOB Modifier:"));
        dialog.add(mobField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                int armorType = armorTypeBox.getSelectedIndex();
                int defense = Integer.parseInt(defenseField.getText());
                int[] attrs = new int[]{
                    Integer.parseInt(strField.getText()),
                    Integer.parseInt(dexField.getText()),
                    Integer.parseInt(itvField.getText()),
                    Integer.parseInt(mobField.getText())
                };
                
                if (!name.isEmpty()) {
                    Armor armor = new Armor(name, "Armor", armorType, defense, attrs);
                    armor.setColor(colorDropdown.getSelectedIndex());
                    ItemDatabase.getInstance().saveItem(armor);
                    dialog.dispose();
                    refreshDisplay();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void addNewConsumable() {
        JDialog dialog = new JDialog(this, "Add New Consumable", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField healField = new JTextField("10");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(7); // Default lime

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Heal Amount:"));
        dialog.add(healField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                int healAmount = Integer.parseInt(healField.getText());
                if (!name.isEmpty()) {
                    Consumable item = new Consumable(name, "Consumable", colorDropdown.getSelectedIndex(), healAmount, null);
                    ItemDatabase.getInstance().saveItem(item);
                    dialog.dispose();
                    refreshDisplay();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void addNewAmmunition() {
        JDialog dialog = new JDialog(this, "Add New Ammunition", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField damageBonusField = new JTextField("0");
        JTextField compatibleField = new JTextField("Any");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(5); // Default orange

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Damage Bonus:"));
        dialog.add(damageBonusField);
        dialog.add(new JLabel("Compatible Weapon:"));
        dialog.add(compatibleField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                int damageBonus = Integer.parseInt(damageBonusField.getText());
                String compatible = compatibleField.getText();
                if (!name.isEmpty()) {
                    Ammunition item = new Ammunition(name, "Ammunition", colorDropdown.getSelectedIndex(), damageBonus, compatible);
                    ItemDatabase.getInstance().saveItem(item);
                    dialog.dispose();
                    refreshDisplay();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void addNewCraftingItem() {
        JDialog dialog = new JDialog(this, "Add New Crafting Item", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(CRAFTING_CATEGORIES);
        JTextField descField = new JTextField();
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(14); // Default beige

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryBox);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                CraftingItem item = new CraftingItem(name, "Crafting", colorDropdown.getSelectedIndex(), 
                        (String) categoryBox.getSelectedItem(), descField.getText());
                ItemDatabase.getInstance().saveItem(item);
                dialog.dispose();
                refreshDisplay();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void addNewKeyItem() {
        JDialog dialog = new JDialog(this, "Add New Key Item", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JCheckBox questCheck = new JCheckBox("Quest Related");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(6); // Default yellow

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);
        dialog.add(new JLabel(""));
        dialog.add(questCheck);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            String name = nameField.getText();
            if (!name.isEmpty()) {
                KeyItem item = new KeyItem(name, "Key Item", colorDropdown.getSelectedIndex(), 
                        descField.getText(), questCheck.isSelected());
                ItemDatabase.getInstance().saveItem(item);
                dialog.dispose();
                refreshDisplay();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editWeapon(Weapon weapon) {
        JDialog dialog = new JDialog(this, "Edit Weapon", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(8, 2, 10, 10));

        JTextField nameField = new JTextField(weapon.getName());
        JTextField damageField = new JTextField(String.valueOf(weapon.getDamage()));
        int[] attrs = weapon.getModifiedAttributes();
        JTextField strField = new JTextField(String.valueOf(attrs[0]));
        JTextField dexField = new JTextField(String.valueOf(attrs[1]));
        JTextField itvField = new JTextField(String.valueOf(attrs[2]));
        JTextField mobField = new JTextField(String.valueOf(attrs[3]));
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(weapon.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Damage:"));
        dialog.add(damageField);
        dialog.add(new JLabel("STR Modifier:"));
        dialog.add(strField);
        dialog.add(new JLabel("DEX Modifier:"));
        dialog.add(dexField);
        dialog.add(new JLabel("ITV Modifier:"));
        dialog.add(itvField);
        dialog.add(new JLabel("MOB Modifier:"));
        dialog.add(mobField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                weapon.setName(nameField.getText());
                weapon.setDamage(Integer.parseInt(damageField.getText()));
                weapon.setModifiedAttributes(new int[]{
                    Integer.parseInt(strField.getText()),
                    Integer.parseInt(dexField.getText()),
                    Integer.parseInt(itvField.getText()),
                    Integer.parseInt(mobField.getText())
                });
                weapon.setColor(colorDropdown.getSelectedIndex());
                ItemDatabase.getInstance().saveItem(weapon);
                dialog.dispose();
                refreshDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editArmor(Armor armor) {
        JDialog dialog = new JDialog(this, "Edit Armor", true);
        dialog.setSize(400, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));

        JTextField nameField = new JTextField(armor.getName());
        JComboBox<String> armorTypeBox = new JComboBox<>(ARMOR_TYPES);
        armorTypeBox.setSelectedIndex(armor.getArmorType());
        JTextField defenseField = new JTextField(String.valueOf(armor.getDefense()));
        int[] attrs = armor.getModifiedAttributes();
        JTextField strField = new JTextField(String.valueOf(attrs[0]));
        JTextField dexField = new JTextField(String.valueOf(attrs[1]));
        JTextField itvField = new JTextField(String.valueOf(attrs[2]));
        JTextField mobField = new JTextField(String.valueOf(attrs[3]));
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(armor.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Armor Type:"));
        dialog.add(armorTypeBox);
        dialog.add(new JLabel("Defense:"));
        dialog.add(defenseField);
        dialog.add(new JLabel("STR Modifier:"));
        dialog.add(strField);
        dialog.add(new JLabel("DEX Modifier:"));
        dialog.add(dexField);
        dialog.add(new JLabel("ITV Modifier:"));
        dialog.add(itvField);
        dialog.add(new JLabel("MOB Modifier:"));
        dialog.add(mobField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                armor.setName(nameField.getText());
                armor.setArmorType(armorTypeBox.getSelectedIndex());
                armor.setDefense(Integer.parseInt(defenseField.getText()));
                armor.setModifiedAttributes(new int[]{
                    Integer.parseInt(strField.getText()),
                    Integer.parseInt(dexField.getText()),
                    Integer.parseInt(itvField.getText()),
                    Integer.parseInt(mobField.getText())
                });
                armor.setColor(colorDropdown.getSelectedIndex());
                ItemDatabase.getInstance().saveItem(armor);
                dialog.dispose();
                refreshDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editConsumable(Consumable item) {
        JDialog dialog = new JDialog(this, "Edit Consumable", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        JTextField nameField = new JTextField(item.getName());
        JTextField healField = new JTextField(String.valueOf(item.getHealAmount()));
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(item.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Heal Amount:"));
        dialog.add(healField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                item.setName(nameField.getText());
                item.setHealAmount(Integer.parseInt(healField.getText()));
                item.setColor(colorDropdown.getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                dialog.dispose();
                refreshDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editAmmunition(Ammunition item) {
        JDialog dialog = new JDialog(this, "Edit Ammunition", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField(item.getName());
        JTextField damageBonusField = new JTextField(String.valueOf(item.getDamageBonus()));
        JTextField compatibleField = new JTextField(item.getCompatibleWeaponType());
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(item.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Damage Bonus:"));
        dialog.add(damageBonusField);
        dialog.add(new JLabel("Compatible Weapon:"));
        dialog.add(compatibleField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            try {
                item.setName(nameField.getText());
                item.setDamageBonus(Integer.parseInt(damageBonusField.getText()));
                item.setCompatibleWeaponType(compatibleField.getText());
                item.setColor(colorDropdown.getSelectedIndex());
                ItemDatabase.getInstance().saveItem(item);
                dialog.dispose();
                refreshDisplay();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editCraftingItem(CraftingItem item) {
        JDialog dialog = new JDialog(this, "Edit Crafting Item", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField(item.getName());
        JComboBox<String> categoryBox = new JComboBox<>(CRAFTING_CATEGORIES);
        categoryBox.setSelectedItem(item.getCraftingCategory());
        JTextField descField = new JTextField(item.getDescription() != null ? item.getDescription() : "");
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(item.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryBox);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            item.setName(nameField.getText());
            item.setCraftingCategory((String) categoryBox.getSelectedItem());
            item.setDescription(descField.getText());
            item.setColor(colorDropdown.getSelectedIndex());
            ItemDatabase.getInstance().saveItem(item);
            dialog.dispose();
            refreshDisplay();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void editKeyItem(KeyItem item) {
        JDialog dialog = new JDialog(this, "Edit Key Item", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JTextField nameField = new JTextField(item.getName());
        JTextField descField = new JTextField(item.getDescription() != null ? item.getDescription() : "");
        JCheckBox questCheck = new JCheckBox("Quest Related", item.isQuestRelated());
        JComboBox<String> colorDropdown = new JComboBox<>(CharSheet.getColorNames());
        colorDropdown.setSelectedIndex(item.getColor());

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);
        dialog.add(new JLabel(""));
        dialog.add(questCheck);
        dialog.add(new JLabel("Color:"));
        dialog.add(colorDropdown);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            item.setName(nameField.getText());
            item.setDescription(descField.getText());
            item.setQuestRelated(questCheck.isSelected());
            item.setColor(colorDropdown.getSelectedIndex());
            ItemDatabase.getInstance().saveItem(item);
            dialog.dispose();
            refreshDisplay();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(okBtn);
        dialog.add(cancelBtn);
        dialog.setVisible(true);
    }

    private void deleteItem(Item item, ArrayList<?> list) {
        int result = JOptionPane.showConfirmDialog(this, "Delete " + item.getName() + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            ItemDatabase.getInstance().deleteItem(item);
            refreshDisplay();
        }
    }

    private void refreshDisplay() {
        dispose();
        new ItemManager().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backBtn) {
            new MainMenu();
            dispose();
        } else if (e.getSource() == addWeaponBtn) {
            addNewWeapon();
        } else if (e.getSource() == addArmorBtn) {
            addNewArmor();
        } else if (e.getSource() == addConsumableBtn) {
            addNewConsumable();
        } else if (e.getSource() == addAmmoBtn) {
            addNewAmmunition();
        } else if (e.getSource() == addCraftingBtn) {
            addNewCraftingItem();
        } else if (e.getSource() == addKeyItemBtn) {
            addNewKeyItem();
        }
    }
}