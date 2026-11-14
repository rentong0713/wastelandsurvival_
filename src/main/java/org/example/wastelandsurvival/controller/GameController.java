
package org.example.wastelandsurvival.controller;

import org.example.wastelandsurvival.service.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.wastelandsurvival.dto.TileDTO;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;
import org.example.wastelandsurvival.dto.ActionDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

import java.util.List;

/**
 * Handles requests from the website to retrieve game information.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    // Service that manages the game data.
    private final GameService gameService;

    /**
     * Creates the controller with the game service.
     */
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Returns the first game map as rows of characters.
     */
    @GetMapping("/map")
    public List<String> getMap() {
        return gameService.getMap();
    }

    /**
     * Returns the game map with separate visual layers.
     */
    @GetMapping("/layered-map")
    public List<List<TileDTO>> getLayeredMap() {
        return gameService.getLayeredMap();
    }

    // Starts a new game using the names provided by the website.
    @PostMapping("/start")
    public String startGame(@RequestBody Map<String, Object> request) throws Exception {

        // Get the player names sent by the website.
        List<String> playerNames = (List<String>) request.get("playerNames");

        // Start the game using the custom player names.
        gameService.startNewGame(playerNames);

        // Return a success message.
        return "Game started with " + playerNames.size() + " workers.";
    }

    // Returns information about the worker whose turn is active.
    @GetMapping("/current-turn")
    public Map<String, Object> getCurrentTurn() {

        // Store the current worker's information.
        Map<String, Object> turn = new java.util.HashMap<>();

        // Get the current worker's name.
        turn.put("playerName", gameService.getCurrentPlayerName());

        // Get the current worker's number.
        turn.put("playerNumber", gameService.getCurrentPlayerNumber());

        // Get the current worker's X coordinate.
        turn.put("playerX", gameService.getCurrentPlayerX());

        // Get the current worker's Y coordinate.
        turn.put("playerY", gameService.getCurrentPlayerY());

        return turn;
    }

    // Return the available teleport destinations.
    @GetMapping("/teleport-destinations")
    public List<Map<String, Integer>> getTeleportDestinations() {
        return gameService.getTeleportDestinations();
    }

    /**
     * Returns the available actions for the current player.
     *
     * @return a list of available actions
     */
    @GetMapping("/actions")
    public List<ActionDTO> getAvailableActions() {

        // Get the available actions from GameService.
        return gameService.getAvailableActions();
    }


    /**
     * Executes an action selected by the player.
     *
     * @param actionId the ID of the selected action
     * @return the result of the action
     */
    /*
     * Executes an action selected by the player.
     */
    @PostMapping("/action")
    public Map<String, Object> executeAction(
            @RequestParam String actionId) {

        // Execute the selected action through GameService.
        String result = gameService.executeAction(actionId);

        // Prepare the response for JavaScript.
        Map<String, Object> response = new java.util.HashMap<>();

        // Store the action result.
        response.put("result", result);

        // Tell JavaScript whether the game has ended.
        response.put("gameOver", gameService.isGameOver());

        // Return the response.
        return response;
    }

    // Get the current worker's health, credits and inventory.
    @GetMapping("/current-player-status")
    public Map<String, Object> getCurrentPlayerStatus() {

        // Retrieve the current worker's status from GameService.
        return gameService.getCurrentPlayerStatus();
    }

}
