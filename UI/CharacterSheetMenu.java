package UI;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;

import EntityRes.CharSheet;
import java.util.ArrayList;
import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;
import tools.jackson.jr.ob.comp.ObjectComposer;


public class CharacterSheetMenu extends JFrame implements ActionListener {

    private JPanel mainPanel;
    private JButton backBtn;
    private JButton saveBtn;
    private JButton loadBtn;
    private JButton newBtn;
    private ArrayList<SheetButton> sheets = new ArrayList<SheetButton>();

    public CharacterSheetMenu() {
        setTitle("Character Sheet");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Additional UI components and layout setup can be added here
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 900, 500);
        add(mainPanel);

        backBtn = new JButton("Back");
        backBtn.setBounds(10,10, 80, 30);
        backBtn.addActionListener(this);
        mainPanel.add(backBtn);

        saveBtn = new JButton("Save");
        saveBtn.setBounds(100,10, 80, 30);
        saveBtn.addActionListener(this);
        mainPanel.add(saveBtn);

        loadBtn = new JButton("Load");
        loadBtn.setBounds(190,10, 80, 30);
        loadBtn.addActionListener(this);
        mainPanel.add(loadBtn);

        newBtn = new JButton("New");
        newBtn.setBounds(280,10,80,30);
        newBtn.addActionListener(this);
        mainPanel.add(newBtn);
        
        sheets = new ArrayList<SheetButton>();

        add(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle action events for the Character Sheet here
        if (e.getSource() == backBtn)
        {
            handleBack();
        }
        else if (e.getSource() == saveBtn)
        {
            handleSave();
        }
        else if (e.getSource() == loadBtn)
        {
            handleLoad();
        }
        else if (e.getSource() == newBtn)
        {
            handleNew();
        }
    }

    public void handleBack()
    {
        this.setVisible(false);
        new MainMenu(this);
    }

    public void handleSave()
    {
        System.out.println("save not implemented");
    }

    public void handleLoad()
    {
        System.out.println("load not implemented");
    }

    public void handleNew()
    {
        JDialog newSheetDialog = new JDialog(this,"New Sheet");
        newSheetDialog.setSize(600, 400);
        newSheetDialog.setLocationRelativeTo(this);
        newSheetDialog.setResizable(false);
        newSheetDialog.setLayout(null);
        newSheetDialog.setVisible(true);

        

        JTextField name = new JTextField("Name");
        name.setBounds(10,10,100,25);
        newSheetDialog.add(name);

        JButton isParty = new JButton("Party");
        isParty.setBounds(10,40,100,25);
        isParty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (isParty.getText().equals("Party"))
                {
                    isParty.setText("Not Party");
                }
                else
                {
                    isParty.setText("Party");
                }
            }
        });
        newSheetDialog.add(isParty);

        JTextField totalHP = new JTextField("Total HP");
        totalHP.setBounds(10,70,100,25);
        newSheetDialog.add(totalHP);

        JTextField str = new JTextField("STR");
        str.setBounds(10,100,100,25);
        newSheetDialog.add(str);
        JTextField dex = new JTextField("DEX");
        dex.setBounds(10,130,100,25);
        newSheetDialog.add(dex);
        JTextField itv = new JTextField("ITV");
        itv.setBounds(10,160,100,25);
        newSheetDialog.add(itv);
        JTextField mob = new JTextField("MOB");
        mob.setBounds(10,190,100,25);
        newSheetDialog.add(mob);

        JButton submit = new JButton("Submit");
        submit.setBounds(10,220,100,25);
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean party = false;
                if (isParty.getText().equals("Party"))
                    party = true;
                int totHP = Integer.parseInt(totalHP.getText());
                int[] attr = new int[4];
                attr[0] = Integer.parseInt(str.getText());
                attr[1] = Integer.parseInt(dex.getText());
                attr[2] = Integer.parseInt(itv.getText());
                attr[3] = Integer.parseInt(mob.getText());
            
                SheetPanel panel = new SheetPanel(new CharSheet(name.getText(),party,totHP,attr));
                panel.updateSheet();
                panel.setVisible(false);
                sheets.add(new SheetButton(panel,name.getText()));
                mainPanel.add(panel);
                updateSheetButtons();
                newSheetDialog.dispose();
            }
        });
        newSheetDialog.add(submit);
        JButton cancel = new JButton("Cancel");
        cancel.setBounds(10,250,100,25);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                newSheetDialog.dispose();
            }
        });
        newSheetDialog.add(cancel);


    }

    public void updateSheetButtons()
    {
        for (int i = 0; i < sheets.size(); i++)
        {
            SheetButton currSheet = sheets.get(i);
            currSheet.setBounds(10,(50+(30*i)),100,25);
            currSheet.setVisible(true);
            currSheet.addActionListener(new ActionListener(){
                 @Override
            public void actionPerformed(ActionEvent e)
            {
                for (int i = 0; i < sheets.size(); i++)
                {
                    sheets.get(i).getSheet().setVisible(false);
                }
                currSheet.getSheet().setVisible(true);
            }
            });
            mainPanel.add(currSheet);
            currSheet.setVisible(true);
            currSheet.repaint();
        }
    }

}
