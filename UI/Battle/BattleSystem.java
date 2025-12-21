package UI.Battle;

import EntityRes.*;
import Objects.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class BattleSystem extends JFrame implements ActionListener {

    BattleGrid grid;
    TurnManager turnManager;
    List<Entity> entities;
    List<TerrainObject> terrainObjects;
    List<Pickup> pickups;

    public BattleSystem(int row, int column) {
        setTitle("Battle System");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        entities = List.of(
                new Player(0, 0, new CharSheet("Hero", true, 20, new int[]{5, 5, 5, 5})),
                new Enemy(0, 1, new CharSheet("Goblin", false, 15, new int[]{3, 3, 3, 3}))
        );

        Weapon dagger = new Weapon("Dagger", "Weapon", 2, new int[]{1, 0, 0, 0});
        Weapon sword = new Weapon("Sword", "Weapon", 4, new int[]{1, 0, 0, 0});

        entities.get(0).getCharSheet().pickupItem(sword);
        entities.get(0).getCharSheet().equipPrimaryWeapon(sword);

        entities.get(1).getCharSheet().pickupItem(dagger);
        entities.get(1).getCharSheet().equipPrimaryWeapon(dagger);

        terrainObjects = List.of(
                new Rock(2, 2, 10),
                new Rock(3, 3, 10)
        );

        pickups = List.of(
                new Pickup(1, 1, new Weapon("Sword", "Weapon", 4, new int[]{1, 0, 0})),
                new Pickup(4, 4, new Item("Health Potion", "Consumable"))
        );

        grid = new BattleGrid(row, column, entities, terrainObjects, pickups);
        turnManager = new TurnManager(entities);

        add(new BattleGridPanel(grid, turnManager));
        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

}
