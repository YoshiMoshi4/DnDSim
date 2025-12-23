package UI;

import javax.swing.*;

public class SheetButton extends JButton
{
    private SheetPanel sheet;

    public SheetButton(SheetPanel sheetPanel, String name)
    {
        super(name);
        this.sheet = sheetPanel;
    }

    public SheetPanel getSheet()
    {
        return sheet;
    }

    public void setSheet(SheetPanel sheet)
    {
        this.sheet = sheet;
    }
}
