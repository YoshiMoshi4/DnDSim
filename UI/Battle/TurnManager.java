package UI.Battle;

import Objects.*;
import EntityRes.CharSheet;
import java.util.*;
import java.util.function.Consumer;

public class TurnManager {

    private final List<GridObject> turnOrder;
    private int currentIndex = 0;
    private int round = 1;
    private boolean battleStarted = false;
    private boolean surpriseRound = false;
    
    // Initiative tracking
    private final Map<GridObject, Integer> initiativeRolls = new HashMap<>();
    private final Map<GridObject, int[]> rollBreakdown = new HashMap<>(); // [d20, dexMod]
    private final List<List<GridObject>> pendingTieResolutions = new ArrayList<>();
    
    // Callback for when player ties need resolution
    private Consumer<List<List<GridObject>>> tieResolutionHandler;
    
    // Random for dice rolls
    private final Random random = new Random();

    public TurnManager(List<Entity> entities) {
        this.turnOrder = new ArrayList<>(entities);
    }

    /**
     * Roll initiative for all combatants: d20 + DEX modifier.
     * Handles surprise rounds (party goes first), identifies ties,
     * resolves enemy ties via coin flip, and queues player ties for resolution.
     * 
     * @return List of roll result strings for combat log
     */
    public List<String> calculateInitiativeOrder() {
        List<String> logMessages = new ArrayList<>();
        initiativeRolls.clear();
        rollBreakdown.clear();
        pendingTieResolutions.clear();
        
        if (surpriseRound) {
            logMessages.add("Surprise Round! All party members act first.");
        }
        
        // Roll initiative for everyone
        for (GridObject obj : turnOrder) {
            int d20 = random.nextInt(20) + 1;
            int dexMod = getDexterity(obj);
            int total = d20 + dexMod;
            
            // In surprise round, party members get +100 to ensure they go first
            if (surpriseRound && isPartyMember(obj)) {
                total += 100;
            }
            
            initiativeRolls.put(obj, total);
            rollBreakdown.put(obj, new int[]{d20, dexMod});
            
            String name = getName(obj);
            logMessages.add(name + " rolls initiative: " + (total > 100 ? total - 100 : total) + 
                          " (d20: " + d20 + " + DEX: " + dexMod + ")");
        }
        
        // Sort by initiative (highest first)
        turnOrder.sort((o1, o2) -> Integer.compare(initiativeRolls.get(o2), initiativeRolls.get(o1)));
        
        // Identify and resolve ties
        logMessages.addAll(resolveTies());
        
        currentIndex = 0;
        round = 1;
        
        return logMessages;
    }
    
    /**
     * Identify ties and handle them appropriately:
     * - Enemy vs Enemy/Player: coin flip (resolved immediately)
     * - Player vs Player: queue for player choice each round
     */
    private List<String> resolveTies() {
        List<String> logMessages = new ArrayList<>();
        
        // Group combatants by their initiative roll
        Map<Integer, List<GridObject>> tieGroups = new LinkedHashMap<>();
        for (GridObject obj : turnOrder) {
            int init = initiativeRolls.get(obj);
            tieGroups.computeIfAbsent(init, k -> new ArrayList<>()).add(obj);
        }
        
        // Process each tie group
        for (Map.Entry<Integer, List<GridObject>> entry : tieGroups.entrySet()) {
            List<GridObject> tied = entry.getValue();
            if (tied.size() <= 1) continue;
            
            // Split into party members and enemies
            List<GridObject> partyMembers = new ArrayList<>();
            List<GridObject> enemies = new ArrayList<>();
            for (GridObject obj : tied) {
                if (isPartyMember(obj)) {
                    partyMembers.add(obj);
                } else {
                    enemies.add(obj);
                }
            }
            
            // Resolve enemy-involved ties with coin flips
            List<GridObject> needsCoinFlip = new ArrayList<>();
            needsCoinFlip.addAll(enemies);
            
            // If there are party members tied with enemies, include them in coin flip
            if (!enemies.isEmpty() && !partyMembers.isEmpty()) {
                needsCoinFlip.addAll(partyMembers);
            }
            
            if (needsCoinFlip.size() > 1) {
                // Shuffle using coin flips
                Collections.shuffle(needsCoinFlip, random);
                
                // Log the coin flip results for mixed ties
                if (!enemies.isEmpty() && !partyMembers.isEmpty()) {
                    GridObject winner = needsCoinFlip.get(0);
                    logMessages.add("Tie between " + formatTiedNames(tied) + 
                                  " - coin flip: " + getName(winner) + " goes first");
                } else if (enemies.size() > 1) {
                    GridObject winner = needsCoinFlip.get(0);
                    logMessages.add("Tie between " + formatTiedNames(enemies) + 
                                  " - coin flip: " + getName(winner) + " goes first");
                }
                
                // Update the turn order for these tied combatants
                int startIdx = turnOrder.indexOf(tied.get(0));
                for (int i = 0; i < needsCoinFlip.size(); i++) {
                    int idx = turnOrder.indexOf(needsCoinFlip.get(i));
                    if (idx != startIdx + i) {
                        // Swap to correct position
                        Collections.swap(turnOrder, idx, startIdx + i);
                    }
                }
                
                // If party members were included in coin flip, they're resolved
                partyMembers.clear();
            }
            
            // Queue purely player-vs-player ties for dialog resolution
            if (partyMembers.size() > 1) {
                pendingTieResolutions.add(new ArrayList<>(partyMembers));
            }
        }
        
        return logMessages;
    }
    
    private String formatTiedNames(List<GridObject> objects) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            if (i > 0) {
                sb.append(i == objects.size() - 1 ? " and " : ", ");
            }
            sb.append(getName(objects.get(i)));
        }
        return sb.toString();
    }
    
    /**
     * Get the DEX modifier for a combatant.
     */
    private int getDexterity(GridObject obj) {
        if (obj instanceof Entity e) {
            return e.getCharSheet().getTotalAttribute(CharSheet.DEXTERITY);
        } else if (obj instanceof Enemy enemy) {
            return enemy.getDexterity();
        }
        return 0;
    }
    
    private boolean isPartyMember(GridObject obj) {
        if (obj instanceof Entity e) {
            return e.isParty();
        }
        return false;
    }
    
    private String getName(GridObject obj) {
        if (obj instanceof Entity e) {
            return e.getName();
        } else if (obj instanceof Enemy en) {
            return en.getName();
        }
        return "Unknown";
    }

    /**
     * Apply player's chosen order for tied party members.
     * Called from the tie resolution dialog.
     */
    public void applyTieResolution(List<GridObject> tiedGroup, List<GridObject> chosenOrder) {
        if (tiedGroup.isEmpty() || chosenOrder.isEmpty()) return;
        
        int startIdx = -1;
        // Find where this tie group starts in turn order
        for (GridObject obj : tiedGroup) {
            int idx = turnOrder.indexOf(obj);
            if (startIdx == -1 || idx < startIdx) {
                startIdx = idx;
            }
        }
        
        if (startIdx == -1) return;
        
        // Reorder the tied group according to player's choice
        for (int i = 0; i < chosenOrder.size(); i++) {
            GridObject toPlace = chosenOrder.get(i);
            int currentIdx = turnOrder.indexOf(toPlace);
            if (currentIdx != startIdx + i) {
                Collections.swap(turnOrder, currentIdx, startIdx + i);
            }
        }
        
        // Remove from pending resolutions
        pendingTieResolutions.removeIf(group -> 
            new HashSet<>(group).equals(new HashSet<>(tiedGroup)));
    }
    
    /**
     * Check if there are pending player tie resolutions.
     */
    public boolean hasPendingTieResolutions() {
        return !pendingTieResolutions.isEmpty();
    }
    
    /**
     * Get the pending tie groups that need player resolution.
     */
    public List<List<GridObject>> getPendingTieResolutions() {
        return new ArrayList<>(pendingTieResolutions);
    }
    
    /**
     * Get the initiative roll for a combatant.
     */
    public int getInitiativeRoll(GridObject obj) {
        Integer roll = initiativeRolls.get(obj);
        if (roll == null) return 0;
        // Strip the surprise bonus for display
        return roll > 100 ? roll - 100 : roll;
    }
    
    /**
     * Get the roll breakdown [d20, dexMod] for a combatant.
     */
    public int[] getRollBreakdown(GridObject obj) {
        return rollBreakdown.getOrDefault(obj, new int[]{0, 0});
    }
    
    public void setSurpriseRound(boolean surprise) {
        this.surpriseRound = surprise;
    }
    
    public boolean isSurpriseRound() {
        return surpriseRound;
    }
    
    public void setTieResolutionHandler(Consumer<List<List<GridObject>>> handler) {
        this.tieResolutionHandler = handler;
    }

    public GridObject getCurrentCombatant() {
        if (turnOrder.isEmpty()) {
            return null;
        }
        return turnOrder.get(currentIndex);
    }

    public Entity getCurrent() {
        GridObject current = getCurrentCombatant();
        if (current instanceof Entity e) {
            return e;
        }
        return null;
    }

    public Enemy getCurrentEnemy() {
        GridObject current = getCurrentCombatant();
        if (current instanceof Enemy e) {
            return e;
        }
        return null;
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) {
            return;
        }
        
        currentIndex++;
        if (currentIndex >= turnOrder.size()) {
            currentIndex = 0;
            round++;
            
            // At start of new round, trigger tie resolution if needed
            if (tieResolutionHandler != null && hasPendingTieResolutions()) {
                tieResolutionHandler.accept(getPendingTieResolutions());
            }
        }
        
        // Process status effects for the entity whose turn is starting
        GridObject current = getCurrentCombatant();
        if (current instanceof Entity e) {
            e.getCharSheet().procStatus();
        }
    }

    public void removeDeadCombatants() {
        turnOrder.removeIf(obj -> {
            if (obj instanceof Entity e) return e.isDead();
            if (obj instanceof Enemy en) return en.isDead();
            return false;
        });
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
            round++;
        }
    }

    public boolean isCurrent(GridObject obj) {
        GridObject current = getCurrentCombatant();
        return current != null && current == obj;
    }

    public List<GridObject> getTurnOrder() {
        return new ArrayList<>(turnOrder);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getRound() {
        return round;
    }

    public void addEntity(Entity entity) {
        turnOrder.add(entity);
        if (battleStarted) {
            // Roll initiative for the new entity
            int d20 = random.nextInt(20) + 1;
            int dexMod = entity.getCharSheet().getTotalAttribute(CharSheet.DEXTERITY);
            int total = d20 + dexMod;
            initiativeRolls.put(entity, total);
            rollBreakdown.put(entity, new int[]{d20, dexMod});
            
            // Re-sort
            turnOrder.sort((o1, o2) -> {
                int init1 = initiativeRolls.getOrDefault(o1, 0);
                int init2 = initiativeRolls.getOrDefault(o2, 0);
                return Integer.compare(init2, init1);
            });
        }
    }

    public void removeEntity(Entity entity) {
        int idx = turnOrder.indexOf(entity);
        turnOrder.remove(entity);
        initiativeRolls.remove(entity);
        rollBreakdown.remove(entity);
        if (idx != -1 && idx < currentIndex) {
            currentIndex--;
        }
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
        }
    }

    public void addEnemy(Enemy enemy) {
        turnOrder.add(enemy);
        if (battleStarted) {
            // Roll initiative for the new enemy
            int d20 = random.nextInt(20) + 1;
            int dexMod = enemy.getDexterity();
            int total = d20 + dexMod;
            initiativeRolls.put(enemy, total);
            rollBreakdown.put(enemy, new int[]{d20, dexMod});
            
            // Re-sort
            turnOrder.sort((o1, o2) -> {
                int init1 = initiativeRolls.getOrDefault(o1, 0);
                int init2 = initiativeRolls.getOrDefault(o2, 0);
                return Integer.compare(init2, init1);
            });
        }
    }

    public void removeEnemy(Enemy enemy) {
        int idx = turnOrder.indexOf(enemy);
        turnOrder.remove(enemy);
        initiativeRolls.remove(enemy);
        rollBreakdown.remove(enemy);
        if (idx != -1 && idx < currentIndex) {
            currentIndex--;
        }
        if (currentIndex >= turnOrder.size() && !turnOrder.isEmpty()) {
            currentIndex = 0;
        }
    }

    public void setBattleStarted(boolean started) {
        this.battleStarted = started;
    }

    public boolean isBattleStarted() {
        return battleStarted;
    }
}
