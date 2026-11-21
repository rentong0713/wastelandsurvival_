// testing branch12
package org.example.wastelandsurvival.service;

import game.main.EclipseNebula;

import edu.monash.fit2099.engine.displays.Display;
import edu.monash.fit2099.engine.positions.GameMap;
import game.enums.ItemStatistics;
import edu.monash.fit2099.engine.positions.Location;
import org.example.wastelandsurvival.dto.TileDTO;
import edu.monash.fit2099.engine.actors.Actor;
import edu.monash.fit2099.engine.items.Item;
import edu.monash.fit2099.engine.actions.Action;
import edu.monash.fit2099.engine.actions.ActionList;
import game.actions.TeleportAction;
import game.actors.ContractedWorker;
import org.example.wastelandsurvival.dto.ActionDTO;

import org.springframework.stereotype.Service;

// Import the actor health statistic.
import edu.monash.fit2099.engine.actors.ActorStatistics;

// Import the worker's credit statistic.
import game.enums.WorkerStatistics;

import java.util.ArrayList;
import java.util.List;
// Import the collection classes.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import the item class.
import edu.monash.fit2099.engine.items.Item;

@Service
public class GameService {

    // Stores the current game world.
    private EclipseNebula gameWorld;
    // Stores the index of the worker whose turn is active.
    private int currentPlayerIndex = 0;
    // Stores the actual Java actions available to the current player.
    private List<Action> currentActions = new ArrayList<>();

    // Tracks whether all workers have died.
    private boolean gameOver = false;

    // Starts a default five-worker game when the application launches.
    public GameService() throws Exception {
        startNewGame(5);
    }

    // Find the next living worker starting from the given index.
    private int findNextLivingPlayerIndex(int startIndex) {

        // Get all workers.
        List<ContractedWorker> workers = gameWorld.getWorkers();

        // Check every worker in circular order.
        for (int i = 0; i < workers.size(); i++) {

            // Calculate the worker's index.
            int index = (startIndex + i) % workers.size();

            // Check whether the worker is still alive.
            if (workers.get(index).isConscious()) {
                return index;
            }
        }

        // Return -1 if every worker has died.
        return -1;
    }

    // Remove all unconscious workers from the game map.
    private void removeDeadWorkersFromMap() {

        // Get the first game map.
        GameMap map = gameWorld.getGameMaps().get(0);

        // Check every worker.
        for (ContractedWorker worker : gameWorld.getWorkers()) {

            // Check whether the worker has died.
            if (!worker.isConscious()) {

                // Check whether the worker is still on the map.
                Location location = map.locationOf(worker);

                if (location != null) {

                    // Use the original FIT2099 removal method.
                    worker.unconscious(map);
                }
            }
        }
    }

    // Creates a new game using the selected number of workers.
    public void startNewGame(int numberOfPlayers) throws Exception {

        // Create a display for the game engine.
        Display display = new Display();

        // Create a new game world with the selected workers.
        EclipseNebula newGameWorld = new EclipseNebula(
                display,
                numberOfPlayers
        );

        // Initialise the maps, workers, items and NPCs.
        newGameWorld.initialise();

// Tick all maps before the first player's turn.
        newGameWorld.tickAllMaps();

        // Replace the previous game world.
        this.gameWorld = newGameWorld;

// Clear actions from the previous game.
        this.currentActions.clear();

// Reset the game-over status.
        this.gameOver = false;

// Start with the first living worker.
        this.currentPlayerIndex = findNextLivingPlayerIndex(0);
    }

    // Starts a new game using the names provided by the website.
    public void startNewGame(List<String> playerNames) throws Exception {

        // Create a display for the game.
        Display display = new Display();

        // Create the game world using the custom player names.
        EclipseNebula newGame = new EclipseNebula(display, playerNames);

        // Initialise the game world.
        newGame.initialise();

        // Tick all maps once when the game starts.
        newGame.tickAllMaps();

        // Store the newly created game world.
        this.gameWorld = newGame;

// Clear actions from the previous game.
        this.currentActions.clear();

// Reset the game-over status.
        this.gameOver = false;

// Start with the first living worker.
        this.currentPlayerIndex = findNextLivingPlayerIndex(0);
    }


    /*
     * Finds the current worker's location across all game maps.
     */
    private Location getCurrentPlayerLocation() {

        // Get the current worker.
        ContractedWorker worker = gameWorld.getWorkers().get(currentPlayerIndex);

        // Search through every game map.
        for (GameMap map : gameWorld.getGameMaps()) {

            // Check whether the worker is on this map.
            Location location = map.locationOf(worker);

            // Return the worker's location if found.
            if (location != null) {
                return location;
            }
        }

        // Return null if the worker is not found on any map.
        return null;
    }


    // Return the first map from the original game.
    public List<String> getMap() {

        // Get the first map in EclipseNebula.
        GameMap map = gameWorld.getGameMaps().get(0);

        // Store every row of the map.
        List<String> rows = new ArrayList<>();

        // Read the map from top to bottom.
        for (int y : map.getYRange()) {

            // Build one row of characters.
            StringBuilder row = new StringBuilder();

            // Read each location from left to right.
            for (int x : map.getXRange()) {

                // Get the existing location.
                Location location = map.at(x, y);

                // Get its actual display character.
                row.append(location.getDisplayChar());
            }

            // Save the completed row.
            rows.add(row.toString());
        }

        return rows;
    }



    /*
     * Returns the current worker's map with separate visual layers.
     */
    public List<List<TileDTO>> getLayeredMap() {

        // Find the current worker's actual location.
        Location playerLocation = getCurrentPlayerLocation();

        // Check whether the worker is on a map.
        if (playerLocation == null) {
            return new ArrayList<>();
        }

        // Get the map where the worker is currently located.
        GameMap map = playerLocation.map();

        // Store all map rows.
        List<List<TileDTO>> rows = new ArrayList<>();

        // Go through every row.
        for (int y = map.getYRange().min();
             y <= map.getYRange().max(); y++) {

            // Store the tiles in this row.
            List<TileDTO> row = new ArrayList<>();

            // Go through every column.
            for (int x = map.getXRange().min();
                 x <= map.getXRange().max(); x++) {

                // Get the current location.
                Location location = map.at(x, y);

                // Get the ground symbol.
                String groundSymbol = String.valueOf(
                        location.getGround().getDisplayChar()
                );

                // Get the actor at this location.
                Actor actor = location.getActor();

                // Store the actor symbol.
                String actorSymbol = null;

                if (actor != null) {
                    actorSymbol = String.valueOf(
                            actor.getDisplayChar()
                    );
                }

                // Store the item symbol.
                String itemSymbol = null;

                // Check whether the location contains items.
                if (!location.getItems().isEmpty()) {

                    // Get the most recently added item.
                    Item item = location.getItems().get(
                            location.getItems().size() - 1
                    );

                    // Get the item's display character.
                    itemSymbol = String.valueOf(
                            item.getDisplayChar()
                    );
                }

                // Create the tile with its separate layers.
                TileDTO tile = new TileDTO(
                        groundSymbol,
                        itemSymbol,
                        actorSymbol
                );

                // Add the tile to the current row.
                row.add(tile);
            }

            // Add the completed row.
            rows.add(row);
        }

        // Return the current worker's map.
        return rows;
    }


    /**
     * Returns the current worker's position in the player list.
     */
    public int getCurrentPlayerNumber() {

        // Player numbers start from 1.
        return currentPlayerIndex + 1;
    }


    /*
     * Returns the current worker's X coordinate.
     */
    public int getCurrentPlayerX() {

        // Find the worker's actual location.
        Location location = getCurrentPlayerLocation();

        // Return -1 if the worker cannot be found.
        if (location == null) {
            return -1;
        }

        // Return the horizontal coordinate.
        return location.x();
    }


    // Check whether the game has ended.
    public boolean isGameOver() {
        return gameOver;
    }

    // Get the name of the current player.
    public String getCurrentPlayerName() {
        return gameWorld.getWorkers().get(currentPlayerIndex).getName();
    }

    /*
     * Returns the current worker's Y coordinate.
     */
    public int getCurrentPlayerY() {

        // Find the worker's actual location.
        Location location = getCurrentPlayerLocation();

        // Return -1 if the worker cannot be found.
        if (location == null) {
            return -1;
        }

        // Return the vertical coordinate.
        return location.y();
    }

    /**
     * Returns the available actions for the current player.
     *
     * @return a list of action descriptions for the website
     */

    public List<ActionDTO> getAvailableActions() {

        // Get the current player.
        ContractedWorker worker = gameWorld.getWorkers().get(currentPlayerIndex);

        // Generate actions only if they have not already been generated.
        if (currentActions.isEmpty()) {

            // Get the available actions from the original game engine.
            ActionList actions = gameWorld.getAvailableActions(worker);

            // Store the actual Java actions.
            for (Action action : actions) {
                currentActions.add(action);
            }
        }

        // Store the action information for the website.
        List<ActionDTO> actionDTOs = new ArrayList<>();

        // Go through every stored action.
        for (int i = 0; i < currentActions.size(); i++) {

            // Get the current action.
            Action action = currentActions.get(i);

            // Create an ID for this action.
            String actionId = String.valueOf(i);

            // Get the action description.
            String description = action.menuDescription(worker);

            // Add the action information to the DTO list.
            actionDTOs.add(new ActionDTO(actionId, description));
        }

        // Return the actions to the website.
        return actionDTOs;
    }






    /**
     * Executes an action selected by the current player.
     *
     * @param actionId the ID of the selected action
     * @return the result of the action
     */
    // Execute an action selected by the current player.
    public String executeAction(String actionId) {

        // Prevent actions after the game has ended.
        if (gameOver) {
            return "GAME OVER! All workers have died.";
        }

        // Convert the action ID into an integer.
        int index;

        try {
            index = Integer.parseInt(actionId);
        } catch (NumberFormatException e) {
            return "Invalid action ID.";
        }

        // Check whether the selected action exists.
        if (index < 0 || index >= currentActions.size()) {
            return "Invalid action selection.";
        }

        // Get all workers.
        List<ContractedWorker> workers = gameWorld.getWorkers();

        // Get the current worker.
        ContractedWorker worker = workers.get(currentPlayerIndex);

        // Prevent dead workers from performing actions.
        if (!worker.isConscious()) {
            return "This worker has died and cannot perform actions.";
        }

        // Retrieve the selected Java action.
        Action selectedAction = currentActions.get(index);

        // Execute the action through EclipseNebula.
        String result = gameWorld.executeAction(worker, selectedAction);

        // Remove the current worker if the action caused their death.
        removeDeadWorkersFromMap();

        // Clear previous actions.
        currentActions.clear();

        // Check whether every worker has died.
        int nextPlayerIndex = findNextLivingPlayerIndex(currentPlayerIndex + 1);

        if (nextPlayerIndex == -1) {

            // Mark the game as over.
            gameOver = true;

            return result + " GAME OVER! All workers have died.";
        }

        // Check whether the next living worker is in the next round.
        boolean roundFinished = nextPlayerIndex <= currentPlayerIndex;

        // Process NPC turns and map updates after a complete round.
        if (roundFinished) {

            // Let NPCs take their turns.
            gameWorld.processNpcTurns();

// Update all maps.
            gameWorld.tickAllMaps();

// Remove any workers who died during NPC turns or map updates.
            removeDeadWorkersFromMap();

            // Check whether all workers died during the map updates.
            currentPlayerIndex = findNextLivingPlayerIndex(0);

            if (currentPlayerIndex == -1) {

                // Mark the game as over.
                gameOver = true;

                return result + " GAME OVER! All workers have died.";
            }

        } else {

            // Move to the next living worker.
            currentPlayerIndex = nextPlayerIndex;
        }

        // Return the action result.
        return result;
    }

    // Get the teleport destinations available to the current worker.
    public List<Map<String, Integer>> getTeleportDestinations() {

        // Store the teleport destination coordinates.
        List<Map<String, Integer>> destinations = new ArrayList<>();

        // Find the current worker's location.
        Location playerLocation = getCurrentPlayerLocation();

        // Stop if the worker is not on a map.
        if (playerLocation == null) {
            return destinations;
        }

        // Get the map where the current worker is located.
        GameMap currentMap = playerLocation.map();

        // Check all available actions.
        for (Action action : currentActions) {

            // Check whether the action is a teleport action.
            if (action instanceof TeleportAction) {

                // Get the teleport destination.
                TeleportAction teleportAction = (TeleportAction) action;
                Location destination = teleportAction.getDestination();

                // Only display destinations on the current map.
                if (destination.map() == currentMap) {

                    // Store the destination coordinates.
                    Map<String, Integer> coordinates = new HashMap<>();
                    coordinates.put("x", destination.x());
                    coordinates.put("y", destination.y());

                    // Add the coordinates to the result.
                    destinations.add(coordinates);
                }
            }
        }

        // Return all available teleport destinations.
        return destinations;
    }

    // Return the current worker's health and inventory information.
    public Map<String, Object> getCurrentPlayerStatus() {

        // Get the worker whose turn it currently is.
        ContractedWorker worker = gameWorld.getWorkers().get(currentPlayerIndex);

        // Create a map to store the worker's status information.
        Map<String, Object> status = new HashMap<>();

        // Add the worker's current health.
        status.put("currentHealth",
                worker.getStatistic(ActorStatistics.HEALTH));

        // Add the worker's maximum health.
        status.put("maximumHealth",
                worker.getMaximumStatistic(ActorStatistics.HEALTH));

        // Add the worker's current credits.
        status.put("credits",
                worker.getStatistic(WorkerStatistics.CREDITS));

        // Create a list to store the worker's inventory details.
        List<Map<String, Object>> inventoryItems = new ArrayList<>();

// Go through every item in the worker's inventory.
        for (Item item : worker.getInventory().getItems()) {

            // Create a map to store the item's details.
            Map<String, Object> itemDetails = new HashMap<>();

            // Store the item's name.
            itemDetails.put("name", item.toString());

            // Store the item's weight if it has a weight statistic.
            int itemWeight = 0;

            if (item.hasStatistic(ItemStatistics.WEIGHT)) {
                itemWeight = item.getStatistic(ItemStatistics.WEIGHT);
            }

            itemDetails.put("weight", itemWeight);

            // Add the item details to the inventory list.
            inventoryItems.add(itemDetails);
        }

// Add the inventory list to the status information.
        status.put("inventory", inventoryItems);

        // Return all the collected information.
        return status;
    }

}
