package UI.Battle;

import Objects.*;
import java.util.*;

public class BattleGrid {

    private final int rows;
    private final int cols;

    private final List<Entity> entities = new ArrayList<>();
    private final List<TerrainObject> terrainObjects = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();

    public BattleGrid(int rows, int cols, List<Entity> entities,
            List<TerrainObject> terrainObjects, List<Pickup> pickups) {
        this.rows = rows;
        this.cols = cols;
        this.entities.addAll(entities);
        this.terrainObjects.addAll(terrainObjects);
        this.pickups.addAll(pickups);
    }

    public GridObject getObjectAt(int r, int c) {
        for (Entity e : entities) {
            if (e.getRow() == r && e.getCol() == c) {
                return e;
            }
        }

        for (TerrainObject t : terrainObjects) {
            if (!t.isDestroyed() && t.getRow() == r && t.getCol() == c) {
                return t;
            }
        }

        for (Pickup p : pickups) {
            if (p.getRow() == r && p.getCol() == c) {
                return p;
            }
        }

        return null;
    }

    public boolean isBlocked(int r, int c) {
        for (TerrainObject t : terrainObjects) {
            if (t.getRow() == r && t.getCol() == c && !t.isDestroyed()) {
                return t.blocksMovement();
            }
        }

        for (Entity e : entities) {
            if (e.getRow() == r && e.getCol() == c) {
                return true;
            }
        }

        return false;
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public TerrainObject getTerrainAt(int r, int c) {
        for (TerrainObject t : terrainObjects) {
            if (t.getRow() == r && t.getCol() == c && !t.isDestroyed()) {
                return t;
            }
        }
        return null;
    }

    public Pickup getPickupAt(int r, int c) {
        for (Pickup p : pickups) {
            if (p.getRow() == r && p.getCol() == c) {
                return p;
            }
        }
        return null;
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public void removeDestroyedTerrain() {
        terrainObjects.removeIf(TerrainObject::isDestroyed);
    }

    public void removePickup(Pickup p) {
        pickups.remove(p);
    }

    public List<TerrainObject> getTerrainObjects() {
        return terrainObjects;
    }

    public List<Pickup> getPickups() {
        return pickups;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

}
