package UI;

import EntityRes.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class SheetPanel extends JPanel implements ActionListener
{
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

    private final JLabel WEAPONS_LBL = new JLabel("Weapons:");
    private final JLabel PRIMARY_LBL = new JLabel("Primary:");
    private final JLabel SECONDARY_LBL = new JLabel("Secondary:");
    private JTextField primary;
    private JTextField secondary;
    private final JButton updatePrimaryBtn = new JButton("Update");
    private final JButton updateSecondaryBtn = new JButton("Update");

    private final JLabel ARMOR_LBL = new JLabel("Armor:");
    private final JLabel HEAD_LBL = new JLabel("Head:");
    private final JLabel TORSO_LBL = new JLabel("Torso:");
    private final JLabel LEGS_LBL = new JLabel("Legs:");
    private JTextField head;
    private JTextField torso;
    private JTextField legs;
    private final JButton updateHeadBtn = new JButton("Update");
    private final JButton updateTorsoBtn = new JButton("Update");
    private final JButton updateLegsBtn = new JButton("Update");

    private final JLabel INVENTORY_LBL = new JLabel("Inventory:");
    private JTextArea inventory;

    private final JLabel STR_LBL = new JLabel("STR");
    private final JLabel DEX_LBL = new JLabel("DEX");
    private final JLabel ITV_LBL = new JLabel("ITV");
    private final JLabel MOB_LBL = new JLabel("MOB");
    private JTextField str;
    private JTextField dex;
    private JTextField itv;
    private JTextField mob;
    private final JButton updateStrBtn = new JButton();
    private final JButton updateDexBtn = new JButton();
    private final JButton updateItvBtn = new JButton();
    private final JButton updateMobBtn = new JButton();

    private final JButton updateAllBtn = new JButton("Update All");
    
    public SheetPanel(CharSheet sheet)
    {
        this.sheet = sheet;
        setBounds(300, 0, 600, 500);
        setVisible(true);
        this.setLayout(null);

        NAME_LBL.setBounds(10,80,80,25);
        this.add(NAME_LBL);
        name = new JTextField();
        name.setBounds(100,80,100,25);
        this.add(name);
        updateNameBtn.setBounds(210,80,80,25);
        updateNameBtn.addActionListener(this);
        this.add(updateNameBtn);
        

        CURRENT_HP_LBL.setBounds(10,110,80,25);
        this.add(CURRENT_HP_LBL);
        hpBar = new JLabel("//////////");
        hpBar.setBounds(160,110,40,25);
        this.add(hpBar);
        hpRatio = new JTextField();
        hpRatio.setBounds(100,110,50,25);
        this.add(hpRatio);
        updateHPBtn.setBounds(210,110,80,25);
        updateHPBtn.addActionListener(this);
        this.add(updateHPBtn);

        STATUS_LBL.setBounds(10,140,80,25);
        this.add(STATUS_LBL);
        status = new JTextField();
        status.setBounds(100,140,100,25);
        this.add(status);
        updateStatusBtn.setBounds(210,140,80,25);
        updateStatusBtn.addActionListener(this);
        this.add(updateStatusBtn);

        WEAPONS_LBL.setBounds(10,170,80,25);
        this.add(WEAPONS_LBL);
        PRIMARY_LBL.setBounds(10,200,80,25);
        this.add(PRIMARY_LBL);
        primary = new JTextField();
        primary.setBounds(100, 200, 100, 25);
        this.add(primary);
        updatePrimaryBtn.setBounds(210,200,80,25);
        updatePrimaryBtn.addActionListener(this);
        this.add(updatePrimaryBtn);
        SECONDARY_LBL.setBounds(10,230,80,25);
        this.add(SECONDARY_LBL);
        secondary = new JTextField();
        secondary.setBounds(100, 230, 100, 25);
        this.add(secondary);
        updateSecondaryBtn.setBounds(210,230,80,25);
        updateSecondaryBtn.addActionListener(this);
        this.add(updateSecondaryBtn);

        ARMOR_LBL.setBounds(300,170,80,25);
        this.add(ARMOR_LBL);
        HEAD_LBL.setBounds(300,200,80,25);
        this.add(HEAD_LBL);
        head = new JTextField();
        head.setBounds(350,200,100,25);
        this.add(head);
        updateHeadBtn.setBounds(460,200,80,25);
        updateHeadBtn.addActionListener(this);
        this.add(updateHeadBtn);
        TORSO_LBL.setBounds(300,230,80,25);
        this.add(TORSO_LBL);
        torso = new JTextField();
        torso.setBounds(350,230,100,25);
        this.add(torso);
        updateTorsoBtn.setBounds(460,230,80,25);
        updateTorsoBtn.addActionListener(this);
        this.add(updateTorsoBtn);
        LEGS_LBL.setBounds(300,260,80,25);
        this.add(LEGS_LBL);
        legs = new JTextField();
        legs.setBounds(350,260,100,25);
        this.add(legs);
        updateLegsBtn.setBounds(460,260,80,25);
        updateLegsBtn.addActionListener(this);
        this.add(updateLegsBtn);

        INVENTORY_LBL.setBounds(10,290,80,25);
        this.add(INVENTORY_LBL);
        inventory = new JTextArea();
        inventory.setBounds(100,290,440,150);
        this.add(inventory);

        STR_LBL.setBounds(350,80,25,25);
        this.add(STR_LBL);
        str = new JTextField();
        str.setBounds(350,110,25,25);
        this.add(str);
        updateStrBtn.setBounds(350,140,25,25);
        updateStrBtn.addActionListener(this);
        this.add(updateStrBtn);
        DEX_LBL.setBounds(390,80,25,25);
        this.add(DEX_LBL);
        dex = new JTextField();
        dex.setBounds(390,110,25,25);
        this.add(dex);
        updateDexBtn.setBounds(390,140,25,25);
        updateDexBtn.addActionListener(this);
        this.add(updateDexBtn);
        ITV_LBL.setBounds(432,80,25,25);
        this.add(ITV_LBL);
        itv = new JTextField();
        itv.setBounds(430,110,25,25);
        this.add(itv);
        updateItvBtn.setBounds(430,140,25,25);
        updateItvBtn.addActionListener(this);
        this.add(updateItvBtn);
        MOB_LBL.setBounds(468,80,30,25);
        this.add(MOB_LBL);
        mob = new JTextField();
        mob.setBounds(470,110,25,25);
        this.add(mob);
        updateMobBtn.setBounds(470,140,25,25);
        updateMobBtn.addActionListener(this);
        this.add(updateMobBtn);

        updateAllBtn.setBounds(100,10,100,30);
        updateAllBtn.addActionListener(this);
        this.add(updateAllBtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events for the Character Sheet here
        if (e.getSource() == updateNameBtn)
        {
            updateName();
        }
        else if (e.getSource() == updateHPBtn)
        {
            updateHP();
        }
        else if (e.getSource() == updateStatusBtn)
        {
            updateStatus();
        }
        else if (e.getSource() == updatePrimaryBtn)
        {
            updatePrimary();
        }
        else if (e.getSource() == updateSecondaryBtn)
        {
            updateSecondary();
        }
        else if (e.getSource() == updateHeadBtn)
        {
            updateHead();
        }
        else if (e.getSource() == updateTorsoBtn)
        {
            updateTorso();
        }
        else if (e.getSource() == updateLegsBtn)
        {
            updateLegs();
        }
        else if (e.getSource() == updateStrBtn)
        {
            updateStr();
        }
        else if (e.getSource() == updateDexBtn)
        {
            updateDex();
        }
        else if (e.getSource() == updateItvBtn)
        {
            updateItv();
        }
        else if (e.getSource() == updateMobBtn)
        {
            updateMob();
        }
        else if (e.getSource() == updateAllBtn)
        {
            updateAll();
        }
        updateSheet();
    }

    public void updateSheet()
    {
        name.setText(sheet.getName());

        hpRatio.setText(sheet.getCurrentHP() + "/" + sheet.getTotalHP());
        String temp = "";
        int numBars = (int) Math.ceil(((1.0*sheet.getCurrentHP()/sheet.getTotalHP())*10));
        for (int i = 0; i < 10; i++)
        {
            if (i < numBars)
            {
                temp += "/";
            }
            else
            {
                temp += "-";
            }
        }
        hpBar.setText(temp);

        primary.setText(sheet.getEquippedWeapon().getName());
        secondary.setText(sheet.getEquippedSecondary().getName());
        head.setText(sheet.getHead().getName());
        torso.setText(sheet.getTorso().getName());
        legs.setText(sheet.getLegs().getName());
        str.setText(sheet.getAttribute(0)+"");
        dex.setText(sheet.getAttribute(1)+"");
        itv.setText(sheet.getAttribute(2)+"");
        mob.setText(sheet.getAttribute(3)+"");
    }

    public void updateName()
    {
        sheet.setName(name.getText());
    }
    
    public void updateHP()
    {
        String ratioString = hpRatio.getText();
        String currentHP = "";
        String totalHP = "";
        boolean isDenominator = false;
        for (char c:ratioString.toCharArray())
        {
            if (c == '/')
            {
                isDenominator = true;
            }
            else if (!isDenominator)
            {
                currentHP += c;
            }
            else
            {
                totalHP += c;
            }
        }
        sheet.setCurrentHP(Integer.parseInt(currentHP));
        sheet.setTotalHP(Integer.parseInt(totalHP));
    }

    public void updateStatus()
    {
        sheet.clearStatus();
        String temp = "";
        for (char c:status.getText().toCharArray())
        {
            if (c == ',')
            {
                sheet.addStatus(new Status(temp));
                temp = "";
            }
            else if (c != ' ')
            {
                temp += c;
            }
        }
        if (temp.length() > 0)
            sheet.addStatus(new Status(temp));
    }

    public void updatePrimary()
    {
        String itemType = "Weapon";
        int dmg = 1;
        int[] mods = {0,0,0,0};
        if (!sheet.getEquippedWeapon().getName().equals(primary.getText()))
            sheet.equipPrimaryWeapon(new Weapon(primary.getText(), itemType, dmg, mods));
    }

    public void updateSecondary()
    {
        String itemType = "Weapon";
        int dmg = 1;
        int[] mods = {0,0,0,0};
        if (!sheet.getEquippedSecondary().getName().equals(secondary.getText()))
            sheet.equipSecondaryWeapon(new Weapon(secondary.getText(), itemType, dmg, mods));
    }

    public void updateHead()
    {
        String itemType = "Armor";
        int def = 1;
        int[] mods = {0,0,0,0};
        if (!sheet.getHead().getName().equals(head.getText()))
            sheet.equipHead(new Armor(head.getText(), itemType, 0, def, mods));
    }

    public void updateTorso()
    {
        String itemType = "Armor";
        int def = 1;
        int[] mods = {0,0,0,0};
        if (!sheet.getTorso().getName().equals(torso.getText()))
            sheet.equipTorso(new Armor(torso.getText(), itemType, 1, def, mods));
    }

    public void updateLegs()
    {
        String itemType = "Armor";
        int def = 1;
        int[] mods = {0,0,0,0};
        if (!sheet.getLegs().getName().equals(legs.getText()))
            sheet.equipLegs(new Armor(legs.getText(), itemType, 2, def, mods));
    }

    public void updateStr()
    {
        sheet.setAttribute(0,Integer.parseInt(str.getText()));
    }

    public void updateDex()
    {
        sheet.setAttribute(1,Integer.parseInt(dex.getText()));
    }

    public void updateItv()
    {
        sheet.setAttribute(2,Integer.parseInt(itv.getText()));
    }

    public void updateMob()
    {
        sheet.setAttribute(3,Integer.parseInt(mob.getText()));
    }

    public void updateAll()
    {
        updateName();
        updateHP();
        updateStatus();
        updatePrimary();
        updateSecondary();
        updateHead();
        updateTorso();
        updateLegs();
        updateStr();
        updateDex();
        updateItv();
        updateMob();
    }
}
