
/*
 * Wait until the HTML page has finished loading.
 */
document.addEventListener("DOMContentLoaded", function () {

    /*
     * PART 1: PLAYER SELECTION
     */

    // Store the number of workers selected by the player.
    let selectedPlayers = 0;

    // Store the previous health and balance values.
    let previousHealth = null;
    let previousCredits = null;
    // Store the previous inventory items.
    let previousInventory = null;

    // Get all player selection buttons.
    const playerButtons = document.querySelectorAll(".player-option");

    // Get the start game button.
    const startGameButton = document.getElementById("start-game-button");

    // Get the container for player name input fields.
    const playerNameInputs = document.getElementById("player-name-inputs");
    // Check whether the selection buttons exist.
    console.log("Player buttons found:", playerButtons.length);

    // Allow the player to select the number of workers.
    playerButtons.forEach(button => {
        button.addEventListener("click", function () {

            // Get the selected number of workers.
            selectedPlayers = Number(this.dataset.players);

            // Remove the previous selection.
            playerButtons.forEach(option => {
                option.classList.remove("selected");
            });

            // Highlight the selected button.
            this.classList.add("selected");

            // Enable the start game button.
            startGameButton.disabled = false;

            // Display the selected number in the console.
            console.log("Selected players:", selectedPlayers);

            // Clear the previous player name fields.
            playerNameInputs.innerHTML = "";

// Create one name input for each selected player.
            for (let i = 1; i <= selectedPlayers; i++) {

                // Create a new input field.
                const input = document.createElement("input");

                // Configure the input field.
                input.type = "text";
                input.id = "player-name-" + i;
                input.className = "player-name-input";
                input.placeholder = "Enter Player " + i + " name";
                input.required = true;
                input.maxLength = 20;

                // Add the input field to the page.
                playerNameInputs.appendChild(input);
            }
        });
    });


    /*
     * PART 2: START THE GAME
     */

    // Start a new game using the selected player count.
    startGameButton.addEventListener("click", async function () {

        // Prevent starting without selecting players.
        if (selectedPlayers < 1 || selectedPlayers > 5) {
            console.error("Please select between 1 and 5 players.");
            return;
        }

        try {

            // Send the selected player count to Java.
            // Collect the names entered for each player.
            const playerNames = [];

            for (let i = 1; i <= selectedPlayers; i++) {

                // Get the name from the corresponding input field.
                const playerName = document.getElementById("player-name-" + i).value.trim();

                // Check whether the name is empty.
                if (playerName === "") {
                    alert("Please enter a name for Player " + i + ".");
                    return;
                }

                // Add the name to the player name list.
                playerNames.push(playerName);
            }

// Send the player names to Java.
            const response = await fetch("/api/game/start", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    playerNames: playerNames
                })
            });

            // Check whether the request succeeded.
            if (!response.ok) {
                throw new Error("Unable to start the game. Status: " + response.status);
            }

            console.log("Java game started successfully.");

            // Remember that a game has been started.
            localStorage.setItem("gameStarted", "true");


            // Hide the player selection screen.
            document.getElementById("start-screen").style.display = "none";

            // Show the game screen.
            const gameScreen = document.getElementById("game-screen");
            gameScreen.hidden = false;
            gameScreen.style.display = "block";

            console.log("Game screen displayed.");

            // Load the newly created game map.
            await loadGameMap();

            // Load the current worker's turn information.
            await loadCurrentTurn();

            // Load the current worker's available actions.
            await loadAvailableActions();
            await loadPlayerStatus();

            // Display a confirmation in the console.
            console.log("Game started with", selectedPlayers, "workers.");

        } catch (error) {

            // Display any errors.
            console.error("Game start error:", error);

            // Inform the player that the game could not start.
            alert("Unable to start the game. Please check the browser console.");
        }
    });


    /*
     * PART 3: IMAGE MAPPING
     */

    // Match Java game symbols with image files.
    const imageMapping = {

        // Ground images.
        ".": "/images/ground/dirt.png",
        "#": "/images/ground/wall.png",
        "_": "/images/ground/floor.png",

        // Actor images.
        "x": "/images/NPC/parasite.png",
        "ඞ": "/images/actors/worker.png",
        "u": "/images/items/flask.png",
        "▤": "/images/items/card_lvl1.png",
        "=": "/images/ground/door_lvl1_v3.png",
        "+": "/images/items/first_aid_kit_v2.png",
        "▣": "/images/items/st_box_v2.png",
        "ó": "/images/items/apple.png",
        "◍": "/images/items/cookies.png",
        "&": "/images/items/lantern.png",
        "⊟": "/images/items/floppy_disk.png",
        "◙": "/images/items/crt_monitor.png",
        "~": "/images/ground/puddle.png",
        "O": "/images/ground/holes.png",
        "Ѫ": "/images/actors/undead.png",
        "⍾": "/images/actors/slime.png",
        "≡": "/images/ground/supercomputer.jpg",
        "α": "/images/ground/card_lvl2.png",
        "◐": "/images/ground/card_lvl3.png",
        "Φ": "/images/ground/teleport_tube.gif",
        "◈": "/images/items/alien_cube.png",
        "≈": "/images/ground/toxic_waste_v4.png",
        "◎": "/images/ground/magic_circle.png",
        "N": "/images/ground/iron_door.png",
        "M": "/images/ground/titanium_door.png",
        "y": "/images/ground/sprout.png",
        "v": "/images/ground/fsapling.png",
        "Y": "/images/ground/fmature.png",
        "w": "/images/ground/wsapling.png",
        "W": "/images/ground/wmature.png",
        "V": "/images/ground/vent.png",
        "^": "/images/ground/fire.png",
        ">": "/images/items/plasma_cutter.png",
        "%": "/images/items/aluminium_scrap.png",
        "@": "/images/items/industrial_fan.png",
        "?": "/images/items/alien_artifact .png",
        "s": "/images/actors/scrap_snatcher.png",
        "H": "/images/ground/monolith.png"
    };


    /*
     * MAP CAMERA
     */

    // Store the current camera mode.
    let cameraMode = "follow";

    // Store the latest worker position and map dimensions.
    let latestPlayerX = null;
    let latestPlayerY = null;
    let latestMapRows = 0;
    let latestMapColumns = 0;

    // Set the zoom level when following a worker.
    const workerZoom = 2.5;

    // Update the map camera position and zoom.
    function updateMapCamera(playerX, playerY, mapRows, mapColumns) {

        // Get the map and its visible container.
        const gameMap = document.getElementById("game-map");
        const gameContainer = document.getElementById("game-container");

        // Stop if the map elements do not exist.
        if (!gameMap || !gameContainer) {
            return;
        }

        // Store the latest worker position and map dimensions.
        latestPlayerX = playerX;
        latestPlayerY = playerY;
        latestMapRows = mapRows;
        latestMapColumns = mapColumns;

        // Get the original map dimensions.
        const mapWidth = gameMap.offsetWidth;
        const mapHeight = gameMap.offsetHeight;

        // Get the visible container dimensions.
        const viewportWidth = gameContainer.clientWidth;
        const viewportHeight = gameContainer.clientHeight;

        // Stop if the map has no dimensions yet.
        if (mapWidth === 0 || mapHeight === 0 ||
            viewportWidth === 0 || viewportHeight === 0) {
            return;
        }

        // Store the camera zoom and position.
        let zoom;
        let translateX;
        let translateY;

        // Show the entire map.
        if (cameraMode === "overview") {

            // Calculate the zoom needed to fit the whole map.
            zoom = Math.min(
                viewportWidth / mapWidth,
                viewportHeight / mapHeight,
                1
            );

            // Centre the entire map inside the container.
            translateX = (viewportWidth - mapWidth * zoom) / 2;
            translateY = (viewportHeight - mapHeight * zoom) / 2;

        } else {

            // Zoom in to follow the active worker.
            zoom = workerZoom;

            // Calculate the centre position of the worker.
            const workerCentreX =
                (playerX + 0.5) * (mapWidth / mapColumns);

            const workerCentreY =
                (playerY + 0.5) * (mapHeight / mapRows);

            // Centre the worker inside the visible container.
            translateX = viewportWidth / 2 - workerCentreX * zoom;
            translateY = viewportHeight / 2 - workerCentreY * zoom;

            // Prevent empty space from appearing around the map.
            translateX = Math.max(
                viewportWidth - mapWidth * zoom,
                Math.min(0, translateX)
            );

            translateY = Math.max(
                viewportHeight - mapHeight * zoom,
                Math.min(0, translateY)
            );
        }

        // Set the map transformation origin.
        gameMap.style.transformOrigin = "0 0";

        // Apply the camera position and zoom.
        gameMap.style.transform =
            `translate(${translateX}px, ${translateY}px) scale(${zoom})`;
    }

    // Make the Follow Worker button work.
    document.getElementById("follow-worker-button")
        .addEventListener("click", function () {

            // Change the camera mode.
            cameraMode = "follow";

            // Centre the camera on the active worker.
            if (latestPlayerX !== null) {
                updateMapCamera(
                    latestPlayerX,
                    latestPlayerY,
                    latestMapRows,
                    latestMapColumns
                );
            }
        });

    // Make the View Entire Map button work.
    document.getElementById("overview-map-button")
        .addEventListener("click", function () {

            // Change the camera mode.
            cameraMode = "overview";

            // Show the entire map.
            if (latestPlayerX !== null) {
                updateMapCamera(
                    latestPlayerX,
                    latestPlayerY,
                    latestMapRows,
                    latestMapColumns
                );
            }
        });

    // Recalculate the camera when the browser window is resized.
    window.addEventListener("resize", function () {

        // Update the camera using the latest worker position.
        if (latestPlayerX !== null) {
            updateMapCamera(
                latestPlayerX,
                latestPlayerY,
                latestMapRows,
                latestMapColumns
            );
        }
    });


    /*
     * PART 4: CREATE TILE IMAGES
     */

    // Create an image element for a game tile.
    function createTileImage(imagePath, className, symbol) {

        // Create a new image element.
        const image = document.createElement("img");

        // Set the image source.
        image.src = imagePath;

        // Set alternative text.
        image.alt = symbol;

        // Apply the CSS class.
        image.classList.add(className);

        // Display an error if the image cannot load.
        image.onerror = function () {
            console.error("Image not found:", imagePath);
        };

        return image;
    }


    /*
     * PART 5: LOAD CURRENT WORKER'S TURN
     */


// Load the current worker's turn information.
    async function loadCurrentTurn() {

        try {

            // Request the current turn from Java.
            const response = await fetch("/api/game/current-turn");

            // Check whether the request succeeded.
            if (!response.ok) {
                throw new Error("Unable to load current turn.");
            }

            // Convert the response into JSON.
            const turnData = await response.json();

            // Get the original player name from Java.
            const originalName = turnData.playerName;

            // Display the current player's name.
            document.getElementById("current-player-name").textContent =
                originalName + "'s Turn";

            // Display the current player number.
            document.getElementById("current-player-number").textContent =
                "Player " + turnData.playerNumber;

            // Display the player's original name in the console.
            console.log("Original name:", originalName);

        } catch (error) {

            // Display any errors.
            console.error("Current turn error:", error);
        }
    }


// Load the current player's health, credits and inventory.
    async function loadPlayerStatus() {

        try {

            // Request the current player's status from Java.
            const response = await fetch("/api/game/current-player-status");

            // Check whether the request was successful.
            if (!response.ok) {
                throw new Error("Failed to load player status.");
            }

            // Convert the response into JavaScript data.
            const status = await response.json();


            /*
             * PART 1: UPDATE PLAYER HEALTH
             */

            // Get the current and maximum health.
            const currentHealth = status.currentHealth;
            const maximumHealth = status.maximumHealth;

            // Calculate the health percentage.
            const healthPercentage = maximumHealth > 0
                ? Math.max(0, Math.min(100, (currentHealth / maximumHealth) * 100))
                : 0;

            // Get the health display element.
            const healthDisplay = document.getElementById("player-health");

            // Animate the health text if its value has changed.
            animateStatus(healthDisplay, currentHealth, previousHealth);

            // Display the current health.
            healthDisplay.textContent = `${currentHealth} / ${maximumHealth}`;

            // Get the health bar container.
            const healthBarContainer = document.querySelector(".health-bar");

            // Animate the actual health bar container.
            animateStatus(healthBarContainer, currentHealth, previousHealth);

            // Get the health bar fill.
            const healthBar = document.getElementById("health-bar-fill");

            // Update the health bar width.
            healthBar.style.width = `${healthPercentage}%`;

            // Change the health bar colour based on remaining health.
            if (healthPercentage <= 25) {
                healthBar.style.backgroundColor = "#e53935";
            } else if (healthPercentage <= 50) {
                healthBar.style.backgroundColor = "#ff9800";
            } else {
                healthBar.style.backgroundColor = "#4caf50";
            }

            // Store the current health for the next comparison.
            previousHealth = currentHealth;


            /*
             * PART 2: UPDATE PLAYER BALANCE
             */

            // Set the maximum credit balance.
            const maximumCredits = 1000;

            // Get the current credit balance.
            const currentCredits = status.credits;

            // Calculate the credit percentage.
            const creditPercentage = Math.max(
                0,
                Math.min(100, (currentCredits / maximumCredits) * 100)
            );

            // Get the balance display element.
            const creditsDisplay = document.getElementById("player-credits");

            // Animate the balance text if its value has changed.
            animateStatus(creditsDisplay, currentCredits, previousCredits);

            // Display the current and maximum credits.
            creditsDisplay.textContent = `${currentCredits} / ${maximumCredits}`;

            // Get the balance bar container.
            const creditBarContainer = document.querySelector(".credit-bar");

            // Animate the actual balance bar container.
            animateStatus(creditBarContainer, currentCredits, previousCredits);

            // Get the balance bar fill.
            const creditBar = document.getElementById("credit-bar-fill");

            // Update the balance bar width.
            creditBar.style.width = `${creditPercentage}%`;

            // Store the current balance for the next comparison.
            previousCredits = currentCredits;


            /*
             * PART 3: UPDATE PLAYER INVENTORY
             */

            // Get the inventory items from the backend.
            const inventoryItems = status.inventory;

            // Calculate the total weight of all inventory items.
            const currentWeight = inventoryItems.reduce(function (total, item) {
                return total + item.weight;
            }, 0);

            // Set the maximum inventory capacity.
            const maximumWeight = 50;

            // Calculate the remaining inventory capacity.
            const remainingWeight = maximumWeight - currentWeight;

            // Update the remaining weight in the hover popup.
            document.getElementById("inventory-weight").textContent =
                `${remainingWeight} / ${maximumWeight} weight remaining`;

// Update the remaining weight displayed on the inventory button.
            document.getElementById("inventory-weight-button").textContent =
                `${remainingWeight} / ${maximumWeight}`;

            // Update the remaining weight display.
            document.getElementById("inventory-weight").textContent =
                `${remainingWeight} / ${maximumWeight} weight remaining`;

            // Animate the inventory if an item was picked up or dropped.
            animateInventory(inventoryItems, previousInventory);

            // Store the current inventory for the next comparison.
            previousInventory = [...inventoryItems];

            // Update the inventory item count.
            document.getElementById("inventory-count").textContent =
                `${inventoryItems.length} item${inventoryItems.length === 1 ? "" : "s"}`;

            /*
             * PART 4: UPDATE INVENTORY MODAL
             */

            // Get the inventory list element.
            const inventoryList = document.getElementById("player-inventory");

            // Clear the previous inventory items.
            inventoryList.innerHTML = "";

            // Check whether the inventory is empty.
            if (inventoryItems.length === 0) {

                // Display a message when the inventory is empty.
                const emptyItem = document.createElement("li");
                emptyItem.textContent = "No items";
                inventoryList.appendChild(emptyItem);

            } else {

                // Display each inventory item's name and weight.
                inventoryItems.forEach(function (item) {

                    // Create a list item.
                    const listItem = document.createElement("li");

                    // Display the item's name and weight.
                    listItem.textContent = `${item.name} - ${item.weight} weight`;

                    // Add the item to the inventory list.
                    inventoryList.appendChild(listItem);
                });
            }


            /*
             * PART 5: UPDATE INVENTORY HOVER POPUP
             */

            // Get the inventory hover popup list.
            const hoverInventoryList = document.getElementById("player-inventory-hover");

            // Clear the previous hover popup items.
            hoverInventoryList.innerHTML = "";

            // Check whether the inventory is empty.
            if (inventoryItems.length === 0) {

                // Display a message when the inventory is empty.
                const emptyItem = document.createElement("li");
                emptyItem.textContent = "No items";
                hoverInventoryList.appendChild(emptyItem);

            } else {

                // Display each item's name and weight.
                inventoryItems.forEach(function (item) {

                    // Create a list item.
                    const listItem = document.createElement("li");

                    // Display the item name and weight.
                    listItem.textContent = `${item.name} - ${item.weight} weight`;

                    // Add the item to the hover popup.
                    hoverInventoryList.appendChild(listItem);
                });
            }

        } catch (error) {

            // Display an error if the status cannot be loaded.
            console.error("Error loading player status:", error);
        }
    }

    // Animate a status element when its value changes.
    function animateStatus(element, newValue, previousValue) {

        // Do nothing if this is the first value or the value has not changed.
        if (previousValue === null || newValue === previousValue) {
            return;
        }

        // Remove previous animation classes.
        element.classList.remove("status-increase", "status-decrease");

        // Restart the animation.
        void element.offsetWidth;

        // Apply the correct animation based on the value change.
        if (newValue > previousValue) {
            element.classList.add("status-increase");
        } else {
            element.classList.add("status-decrease");
        }
    }

    // Animate the inventory when an item is picked up or dropped.
    function animateInventory(newInventory, oldInventory) {

        // Do nothing when the inventory is loaded for the first time.
        if (oldInventory === null) {
            return;
        }

        // Do nothing if the inventory has not changed.
        if (JSON.stringify(newInventory) === JSON.stringify(oldInventory)) {
            return;
        }

        // Determine whether an item was picked up or dropped.
        let animationClass;

        if (newInventory.length > oldInventory.length) {
            animationClass = "inventory-increase";
        } else if (newInventory.length < oldInventory.length) {
            animationClass = "inventory-decrease";
        } else {
            animationClass = "inventory-change";
        }

        // Get the inventory button and item list.
        const inventoryButton = document.getElementById("inventory-button");
        const inventoryList = document.getElementById("player-inventory");

        // Remove previous animation classes.
        const animationClasses = [
            "inventory-increase",
            "inventory-decrease",
            "inventory-change"
        ];

        inventoryButton.classList.remove(...animationClasses);
        inventoryList.classList.remove(...animationClasses);

        // Restart the animation.
        void inventoryButton.offsetWidth;
        void inventoryList.offsetWidth;

        // Apply the animation to both inventory elements.
        inventoryButton.classList.add(animationClass);
        inventoryList.classList.add(animationClass);
    }

// Open or close the inventory popup when the button is clicked.
    // Get the inventory modal.
    const inventoryPopup = document.getElementById("inventory-popup");

    // Get the Game Over popup.
    const gameOverPopup = document.getElementById("game-over-popup");

// Get the restart button.
    const restartGameButton = document.getElementById("restart-game-button");

// Display the Game Over popup.
    function showGameOverPopup() {
        gameOverPopup.hidden = false;
    }

// Restart the game and return to player selection.
    restartGameButton.addEventListener("click", function () {

        // Remove the saved game indicator.
        localStorage.removeItem("gameStarted");

        // Hide the Game Over popup.
        gameOverPopup.hidden = true;

        // Hide the inventory popup.
        inventoryPopup.hidden = true;

        // Hide the game screen.
        document.getElementById("game-screen").hidden = true;
        document.getElementById("game-screen").style.display = "none";

        // Show the player selection screen.
        document.getElementById("start-screen").style.display = "block";

        // Reset the selected player count.
        selectedPlayers = 0;

        // Disable the start button until players are selected again.
        startGameButton.disabled = true;

        // Remove the previous player selection.
        playerButtons.forEach(function (button) {
            button.classList.remove("selected");
        });

        // Clear the previous player name inputs.
        playerNameInputs.innerHTML = "";

        // Reset previous health and balance values.
        previousHealth = null;
        previousCredits = null;
        // Reset previous inventory.
        previousInventory = null;

    });

// Open the inventory when the backpack button is clicked.
    document.getElementById("inventory-button").addEventListener("click", function () {

        // Show the inventory modal.
        inventoryPopup.hidden = false;
    });

// Close the inventory when the close button is clicked.
    document.getElementById("close-inventory-button").addEventListener("click", function () {

        // Hide the inventory modal.
        inventoryPopup.hidden = true;
    });

// Close the inventory when the dark background is clicked.
    document.querySelector(".inventory-overlay").addEventListener("click", function () {

        // Hide the inventory modal.
        inventoryPopup.hidden = true;
    });

// Close the inventory when the Escape key is pressed.
    document.addEventListener("keydown", function (event) {

        // Check whether the Escape key was pressed.
        if (event.key === "Escape") {

            // Hide the inventory modal.
            inventoryPopup.hidden = true;
        }
    });


    /*
     * PART 6: LOAD AVAILABLE ACTIONS
     */

    // Load and display the available actions for the current player.
    async function loadAvailableActions() {

        try {

            // Request available actions from Java.
            const response = await fetch("/api/game/actions");

            // Check whether the request succeeded.
            if (!response.ok) {
                throw new Error("Unable to load available actions.");
            }

            // Convert the response into JSON.
            const actions = await response.json();

            // Get the action list container.
            const actionList = document.getElementById("action-list");

            // Clear previously displayed actions.
            actionList.innerHTML = "";

            // Store movement actions by their keypad positions.
            const movementActions = {};

            // Store other actions separately.
            const otherActions = [];

            // Match movement directions with keypad numbers.
            const directionPositions = {
                "north-west": 7,
                "north": 8,
                "north-east": 9,
                "west": 4,
                "east": 6,
                "south-west": 1,
                "south": 2,
                "south-east": 3
            };

            // Separate movement actions from other actions.
            actions.forEach(function (action) {

                // Convert the description to lowercase.
                const description = action.description.toLowerCase();

                // Check whether the action is a movement action.
                const movementMatch = description.match(/moves\s+([a-z-]+)/);

                if (movementMatch) {

                    // Get the movement direction.
                    const direction = movementMatch[1];

                    // Find the corresponding keypad position.
                    const position = directionPositions[direction];

                    // Store the movement action.
                    if (position !== undefined) {
                        movementActions[position] = action;
                    } else {
                        otherActions.push(action);
                    }

                } else if (description.includes("does nothing")) {

                    // Place the do-nothing action in the centre.
                    movementActions[5] = action;

                } else {

                    // Keep Consume, Attack, Drop and other actions normal.
                    otherActions.push(action);
                }
            });

            // Define the keypad layout.
            const keypadPositions = [
                { number: 7, arrow: "↖" },
                { number: 8, arrow: "↑" },
                { number: 9, arrow: "↗" },
                { number: 4, arrow: "←" },
                { number: 5, arrow: "•" },
                { number: 6, arrow: "→" },
                { number: 1, arrow: "↙" },
                { number: 2, arrow: "↓" },
                { number: 3, arrow: "↘" }
            ];

            // Check whether movement actions are available.
            if (Object.keys(movementActions).length > 0) {

                // Create the movement grid.
                const movementGrid = document.createElement("div");

                // Apply the movement grid CSS class.
                movementGrid.className = "movement-grid";

                // Create the nine keypad positions.
                keypadPositions.forEach(function (position) {

                    // Get the action for this position.
                    const action = movementActions[position.number];

                    if (action) {

                        // Create the movement button.
                        const button = createActionButton(action);

                        // Apply the movement button CSS class.
                        button.classList.add("movement-button");

                        // Display the number and arrow.
                        button.textContent =
                            position.number + " " + position.arrow;

                        // Show the full action description on hover.
                        button.title = action.description;

                        // Add the movement button to the grid.
                        movementGrid.appendChild(button);

                    } else {

                        // Create an empty position.
                        const emptyPosition = document.createElement("div");

                        // Apply the empty position CSS class.
                        emptyPosition.className = "movement-empty";

                        // Add the empty position to the grid.
                        movementGrid.appendChild(emptyPosition);
                    }
                });

                // Add the movement grid to the action list.
                actionList.appendChild(movementGrid);
            }

            // Display other actions as normal buttons.
            otherActions.forEach(function (action) {

                // Create a normal action button.
                const button = createActionButton(action);

                // Add the button to the action list.
                actionList.appendChild(button);
            });

        } catch (error) {

            // Display any loading errors.
            console.error("Available actions error:", error);

            // Show the error in the game message panel.
            document.getElementById("message-text").textContent =
                error.message;
        }
    }


    /*
     * PART 7: CREATE ACTION BUTTONS
     */

    // Create a button for a Java game action.
    function createActionButton(action) {

        // Create a new button element.
        const button = document.createElement("button");

        // Set the button type.
        button.type = "button";

        // Display the action description.
        button.textContent = action.description;

        // Store the action ID.
        button.dataset.actionId = action.actionId;

        // Apply the action button CSS class.
        button.className = "action-button";

        // Execute the selected action when clicked.
        button.addEventListener("click", async function () {

            try {

                // Prevent repeated clicks while the action is executing.
                button.disabled = true;

                // Send the selected action to Java.
                const actionResponse = await fetch(
                    "/api/game/action?actionId=" +
                    encodeURIComponent(action.actionId),
                    {
                        method: "POST"
                    }
                );

                // Check whether the action request succeeded.
                if (!actionResponse.ok) {
                    throw new Error("Unable to execute the selected action.");
                }

                // Convert the result into JSON.
                const result = await actionResponse.json();

// Display the action result in the game message panel.
                document.getElementById("message-text").textContent =
                    result.result;

// Check whether all workers have died.
                if (result.gameOver === true) {

                    // Display the Game Over popup.
                    showGameOverPopup();

                    // Stop refreshing the game.
                    return;
                }

                // Refresh the game map and active player glow.
                await loadGameMap();

                // Refresh the current player's information.
                await loadCurrentTurn();

                // Load the next player's available actions.
                await loadAvailableActions();

                await loadPlayerStatus();

            } catch (error) {

                // Display any action errors inside the message panel.
                document.getElementById("message-text").textContent =
                    error.message;

                // Log the error.
                console.error("Action error:", error);
            }
        });

        // Return the completed button.
        return button;
    }


    /*
     * PART 8: LOAD THE GAME MAP
     */

    // Load the game map with separate visual layers.
    // Load the game map with separate visual layers.
    async function loadGameMap() {

        try {

            // Request the layered map from Java.
            const mapResponse = await fetch("/api/game/layered-map");

            // Check whether the map request succeeded.
            if (!mapResponse.ok) {
                throw new Error("Unable to load the game map.");
            }

            // Convert the map response into JavaScript data.
            const mapRows = await mapResponse.json();


            // Request the current player's position from Java.
            const turnResponse = await fetch("/api/game/current-turn");

            // Check whether the current turn request succeeded.
            if (!turnResponse.ok) {
                throw new Error("Unable to load the current turn.");
            }

            // Convert the current turn response into JavaScript data.
            const currentTurn = await turnResponse.json();

            // Request the current player's teleport destinations.
            const teleportResponse = await fetch("/api/game/teleport-destinations");

            // Check whether the teleport destinations request succeeded.
            if (!teleportResponse.ok) {
                throw new Error("Unable to load teleport destinations.");
            }

            // Convert the teleport destinations response into JavaScript data.
            const teleportDestinations = await teleportResponse.json();

            // Display all teleport destination coordinates.
            console.log(
                "Teleport destinations:",
                JSON.stringify(teleportDestinations, null, 2)
            );

// Display the map dimensions.
            console.log("Map rows:", mapRows.length);
            console.log("Map columns:", mapRows[0].length);


            // Get the HTML element that contains the game map.
            const gameMap = document.getElementById("game-map");

            // Remove the previous map tiles before drawing the new map.
            gameMap.innerHTML = "";

            // Set the number of columns based on the map width.
            gameMap.style.gridTemplateColumns =
                `repeat(${mapRows[0].length}, minmax(0, 1fr))`;

// Set every map row to 24 pixels high.
            gameMap.style.gridTemplateRows =
                `repeat(${mapRows.length}, 24px)`;


            // Loop through every row of the game map.
            mapRows.forEach((row, rowIndex) => {

                // Loop through every tile in the current row.
                row.forEach((tileData, columnIndex) => {

                    // Get the tile's map coordinates.
                    const x = columnIndex;
                    const y = rowIndex;

                    // Create a new HTML element for this tile.
                    const tile = document.createElement("div");

                    // Add the tile CSS class.
                    tile.classList.add("tile");


                    // Check whether this tile contains the current player.
                    if (x === currentTurn.playerX && y === currentTurn.playerY) {

                        // Highlight the current player's tile.
                        tile.classList.add("active-player-tile");
                    }


                    // Check whether this tile is a teleport destination.
                    const isTeleportDestination = teleportDestinations.some(
                        destination => destination.x === x && destination.y === y
                    );

                    // Check whether this tile is a teleport destination.
                    if (isTeleportDestination) {

                        // Add the teleport destination CSS class.
                        tile.classList.add("teleport-destination-tile");

                        // Display the matched map coordinates.
                        console.log("Matched teleport tile:", x, y);
                    }


                    // Get the ground symbol of this tile.
                    const groundSymbol = tileData.ground;

                    // Get the item symbol of this tile.
                    const itemSymbol = tileData.item;

                    // Get the actor symbol of this tile.
                    const actorSymbol = tileData.actor;


                    // Display the ground image if one exists.
                    if (groundSymbol && imageMapping[groundSymbol]) {

                        // Create the ground image element.
                        const groundImage = document.createElement("img");

                        // Set the ground image source.
                        groundImage.src = imageMapping[groundSymbol];

                        // Add the ground image CSS class.
                        groundImage.classList.add("ground-image");

                        // Add the ground image to the tile.
                        tile.appendChild(groundImage);
                    }


                    // Display the item image if one exists.
                    if (itemSymbol && imageMapping[itemSymbol]) {

                        // Create the item image element.
                        const itemImage = document.createElement("img");

                        // Set the item image source.
                        itemImage.src = imageMapping[itemSymbol];

                        // Add the item image CSS class.
                        itemImage.classList.add("item-image");

                        // Add the item image to the tile.
                        tile.appendChild(itemImage);
                    }


                    // Display the actor image if one exists.
                    if (actorSymbol && imageMapping[actorSymbol]) {

                        // Create the actor image element.
                        const actorImage = document.createElement("img");

                        // Set the actor image source.
                        actorImage.src = imageMapping[actorSymbol];

                        // Add the actor image CSS class.
                        actorImage.classList.add("actor-image");

                        // Add the actor image to the tile.
                        tile.appendChild(actorImage);
                    }


                    // Add the completed tile to the game map.
                    gameMap.appendChild(tile);

                });

            });

            // Count the teleport destination tiles created on the website.
            console.log(
                "Total teleport markers:",
                document.querySelectorAll(".teleport-destination-tile").length
            );

            // Update the camera after all map tiles have been created.
            updateMapCamera(
                currentTurn.playerX,
                currentTurn.playerY,
                mapRows.length,
                mapRows[0].length
            );

        } catch (error) {

            // Display the error in the browser console.
            console.error("Error loading game map:", error);
        }



    }

    /*
 * PART 9: SHOW PLAYER SELECTION AFTER PAGE RELOAD
 */

// Show the player selection screen.
    document.getElementById("start-screen").style.display = "block";

// Hide the game screen.
    const gameScreen = document.getElementById("game-screen");
    gameScreen.hidden = true;
    gameScreen.style.display = "none";

// Clear the previous game status.
    localStorage.removeItem("gameStarted");



// Get the game guide elements.
    const gameGuideButton = document.getElementById("game-guide-button");
    const gameGuideModal = document.getElementById("game-guide-modal");
    const closeGameGuideButton = document.getElementById("close-game-guide");

// Open the game guide when its button is clicked.
    gameGuideButton.addEventListener("click", function () {
        gameGuideModal.classList.add("active");
    });

// Close the game guide when the close button is clicked.
    closeGameGuideButton.addEventListener("click", function () {
        gameGuideModal.classList.remove("active");
    });

// Close the game guide when clicking outside its content.
    gameGuideModal.addEventListener("click", function (event) {
        if (event.target === gameGuideModal) {
            gameGuideModal.classList.remove("active");
        }
    });

// Close the game guide when Escape is pressed.
    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            gameGuideModal.classList.remove("active");
        }
    });

});
