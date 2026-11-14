package game.grounds;

import edu.monash.fit2099.engine.actions.ActionList;
import edu.monash.fit2099.engine.actors.Actor;
import edu.monash.fit2099.engine.displays.Display;
import edu.monash.fit2099.engine.items.Item;
import edu.monash.fit2099.engine.positions.Exit;
import edu.monash.fit2099.engine.positions.GameMap;
import edu.monash.fit2099.engine.positions.Ground;
import edu.monash.fit2099.engine.positions.Location;
import game.actions.BuyAction;
import game.actions.DepositAction;
import game.actions.SellAction;
import game.enums.Ability;
import game.interfaces.Buyable;
import game.interfaces.Depositable;
import game.interfaces.Sellable;
import game.items.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A specialized Ground type that acts as a central trading hub.
 * The Supercomputer allows actors to purchase essential equipment and sell
 * specific items for credits.
 *
 * @author Tan Jia Hern, Rachel Chiew
 * @version 2.0
 */
public class Supercomputer extends Ground {

    /**
     * A list of items available for purchase at this terminal.
     */
    private final List<Buyable> storePrototypes = new ArrayList<>();

    private int companyRank = 1;
    private int companyCredits = 0;
    private int currentQuota = 100;
    private int timeLimit = 200; //200
    private int turnsPassed = 0;
    private boolean deadlineReachedandFailed = false;

    /**
     * Constructor for the Supercomputer.
     * Initializes the store with a stock of medical kits and access cards.
     */
    public Supercomputer() {
        super('≡', "Supercomputer");
        // Load the shop inventory
        storePrototypes.add(new FirstAidKit());
        storePrototypes.add(new SterilisationBox());
        storePrototypes.add(new AccessCardLevel1());
        storePrototypes.add(new AccessCardLevel2());
        storePrototypes.add(new AccessCardLevel3());

        storePrototypes.add(new PlasmaCutter());
    }

    /**
     * Increases the company credit balance by the specified amount.
     *
     * @param amount The number of company credits to add
     */
    public void addCompanyCredits(int amount){
        this.companyCredits += amount;
    }

    /**
     * Handles quota tracking, promotion logic, and the firing protocol upon deadline expiry
     *
     * @param location The location of the Ground
     */
    @Override
    public void tick(Location location){
        Display display = new Display();

        if (deadlineReachedandFailed){
            fireAdjacentWorkers(location, display);
            return;
        }

        if (!isPlayerOnMap(location.map())){
            return;
        }

        turnsPassed ++;

        display.println(String.format("Supercomputer Quota: %d/%d Company Credits | Deadline in: %d turns",
                companyCredits, currentQuota, (timeLimit - turnsPassed)));

        if (turnsPassed >= timeLimit){
            if (companyCredits >= currentQuota){
                companyCredits = 0;
                turnsPassed = 0;
                companyRank ++;

                currentQuota = (int) Math.ceil(currentQuota * 1.05);
                timeLimit = (int) Math.ceil(timeLimit * 1.10);

                display.println("Quota met! Great job, expendable worker.");
                display.println("Promoted to Rank " + companyRank + "!New quota: " + currentQuota +
                        " |New Deadline: " + timeLimit + " turns.");
            } else{
                deadlineReachedandFailed = true;
                display.println("Quota failed. Termination protocol initiated.");
                fireAdjacentWorkers(location, display);
            }
        }
    }

    /**
     * Identifies workers in adjacent tiles and applies the termination protocol
     *
     * @param location The location of the supercomputer.
     * @param display The display instance used for console messaging.
     */
    private void fireAdjacentWorkers(Location location, Display display){
        for (Exit exit : location.getExits()){
            Location adjacent = exit.getDestination();
            if (adjacent.containsAnActor()){
                Actor target = adjacent.getActor();
                if (target.hasAbility(Ability.WORKER)){
                    display.println("The Supercomputer fires a lethal security beam at " + target + "!");
                    target.unconscious(location.map());
                }
            }
        }
    }

    /**
     * Returns a list of actions that the Supercomputer can perform or allow
     * for an actor standing adjacent to it.
     *
     * @param actor     The actor performing the action.
     * @param location  The current location of the Supercomputer.
     * @param direction The direction of the Supercomputer relative to the actor.
     * @return A list of BuyActions and SellActions available to the actor.
     */

    /*
     * Returns the actions available when an actor interacts with the Supercomputer.
     * The actor must stand directly on the Supercomputer tile.
     */
    @Override
    public ActionList allowableActions(Actor actor, Location location, String direction) {

        // Get the default actions.
        ActionList actions = super.allowableActions(actor, location, direction);

        // Check whether the actor is standing directly on this Supercomputer.
        if (!location.containsAnActor() || location.getActor() != actor) {
            return actions;
        }

        // Stop trading actions if the deadline has failed.
        if (deadlineReachedandFailed) {
            return actions;
        }

        // Add buying actions from the store inventory.
        for (Buyable buyable : storePrototypes) {
            actions.add(new BuyAction(buyable));
        }

        // Check the actor's inventory for sellable and depositable items.
        for (Item item : actor.getInventory().getItems()) {

            // Add selling actions for sellable items.
            if (item.hasAbility(Ability.SELLABLE)) {

                // Retrieve the Sellable capability.
                Sellable sellableItem = item.asCapability(Sellable.class).orElse(null);

                // Add the selling action if the item is sellable.
                if (sellableItem != null) {
                    actions.add(new SellAction(sellableItem));
                }
            }

            // Add depositing actions for depositable items.
            if (item.hasAbility(Ability.DEPOSITABLE)) {

                // Retrieve the Depositable capability.
                Depositable depositableItem = item.asCapability(Depositable.class).orElse(null);

                // Add the depositing action if the item is depositable.
                if (depositableItem != null) {
                    actions.add(new DepositAction(depositableItem, this));
                }
            }
        }

        // Return all available actions.
        return actions;
    }


    /**
     * Checks the entire map to see if there is at least one active worker.
     *
     * @param map The current game map
     * @return true if a worker is found, false otherwise.
     */
    private boolean isPlayerOnMap(GameMap map){
        for (int x : map.getXRange()){
            for (int y : map.getYRange()){
                Location loc = map.at(x, y);
                if (loc.containsAnActor() && loc.getActor().hasAbility(Ability.WORKER)){
                    return true;
                }
            }
        }

        return false;
    }
}