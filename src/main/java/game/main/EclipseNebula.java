package game.main;

import edu.monash.fit2099.engine.actions.ActionList;
import edu.monash.fit2099.engine.displays.Display;
import edu.monash.fit2099.engine.positions.DefaultGroundCreator;
import edu.monash.fit2099.engine.positions.GameMap;
import edu.monash.fit2099.engine.positions.Location;
import edu.monash.fit2099.engine.positions.World;
import game.actors.ContractedWorker;
import game.actors.Parasite;
import game.flora.DeprecatedSprout;
import game.grounds.*;
import edu.monash.fit2099.engine.actions.Action;
import game.interfaces.Spawner;
import game.inventory.WeightLimitedInventory;
import game.items.*;
import game.systems.MultiSpawner;
import game.systems.SlimeSpawner;
import game.systems.SnatcherSpawner;
import game.systems.UndeadSpawner;
import edu.monash.fit2099.engine.actions.DoNothingAction;
import edu.monash.fit2099.engine.actors.Actor;

import java.util.Arrays;
import java.util.List;
import edu.monash.fit2099.engine.actors.Actor;
import java.util.ArrayList;

/**
 * This class handles the miracle of creation for Moon 99-Deprecated.
 * Focusing on REQ1 (Economy) and REQ2 (Scrap & Items).
 */
public class EclipseNebula extends World {

    // Stores the number of workers selected by the website player.
    private final int numberOfPlayers;
    // Stores the names entered by website players.
    private final List<String> playerNames;

    // Keeps the original terminal game using five workers.
    public EclipseNebula(Display display) {
        this(display, 5);
    }

    // Allows the website to provide custom player names.
    public EclipseNebula(Display display, List<String> playerNames) {
        super(display);

        // Validate the number of player names.
        if (playerNames == null || playerNames.size() < 1 || playerNames.size() > 5) {
            throw new IllegalArgumentException(
                    "Number of players must be between 1 and 5."
            );
        }

        // Store the selected number of players.
        this.numberOfPlayers = playerNames.size();

        // Make a separate copy of the player names.
        this.playerNames = new ArrayList<>(playerNames);
    }

    // Allows the original game to use its default player names.
    public EclipseNebula(Display display, int numberOfPlayers) {
        super(display);

        // Validate the selected number of workers.
        if (numberOfPlayers < 1 || numberOfPlayers > 5) {
            throw new IllegalArgumentException(
                    "Number of players must be between 1 and 5."
            );
        }

        this.numberOfPlayers = numberOfPlayers;

        // Keep the original default names for the terminal game.
        this.playerNames = Arrays.asList(
                "Bob", "Tom", "Sarah", "Julie", "Rick"
        ).subList(0, numberOfPlayers);
    }

    /**
     * Initialise maps, actors, items, and grounds of the game world.
     */
    public void initialise() throws Exception {
        DefaultGroundCreator groundCreator = new DefaultGroundCreator();

        groundCreator.registerGround('.', Dirt::new);
        groundCreator.registerGround('#', Wall::new);
        groundCreator.registerGround('~', Puddle::new);
        groundCreator.registerGround('_', Floor::new);

//        groundCreator.registerGround('y', FleshySprout::new);
//        groundCreator.registerGround('w', WarperSapling::new);
//        groundCreator.registerGround('y', DeprecatedSprout::new);


        groundCreator.registerGround('=', AluminiumDoor::new);
        groundCreator.registerGround('N', IronDoor::new);
        groundCreator.registerGround('M', TitaniumDoor::new);
        groundCreator.registerGround('≈', ToxicWaste::new);
        groundCreator.registerGround('≡', Supercomputer::new);

        groundCreator.registerGround('Φ', TeleportationTube::new);

        groundCreator.registerGround('◈', Floor::new);
        groundCreator.registerGround('o', Floor::new);
        groundCreator.registerGround('◎', Floor::new);
        groundCreator.registerGround('V', Floor::new);
        groundCreator.registerGround('O', Floor::new);
        groundCreator.registerGround('⚔', WeaponStore::new);


        List<String> moon99Deprecated = Arrays.asList(
                "....................########################################",
                "...#######..........#__________________#___________________#",
                "...#≡____#..........=__________________=___________________#",
                "...#_____=...~......#__________________#___________________#",
                "...#_____#..~~~.....########=#####=#####___#############___#",
                "...#######.~~~~.....#______#_#_________#___#___________#___#",
                ".........~~~~.......#______#_#_________#####___________#####",
                "....................#______=_#_________#___________________#",
                "......~.............#______#_#_________#___________________#",
                ".....~~~............#______#_###########___#############___#",
                ".....~..............#______#___________#___#___________#___#",
                "....................=______#___________=___=___________=___#",
                "....................#______#############___#############___#",
                ".........~~~~.......#______#___________#####################",
                "........~~~~~~......#______#___________=___________________#",
                ".........~~~~.......#______#___________#___________________#",
                "....................#______#############___#############___#",
                "....................#______#___________#___#___________#___#",
                "..~.................#______=___________=___=___________=___#",
                "....................########################################"
        );

        GameMap moon99DeprecatedMap = new GameMap("99-Deprecated", groundCreator, moon99Deprecated);
        this.addGameMap(moon99DeprecatedMap);

        Moon20Overflow moon20 = new Moon20Overflow(groundCreator);
        this.addGameMap(moon20);

        Location mainTubeLoc = moon99DeprecatedMap.at(5, 3);
        TeleportationTube mainTube = new TeleportationTube();
        mainTubeLoc.setGround(mainTube);

        Location overflowTubeLoc = moon20.at(6, 3);
        TeleportationTube overflowTube = new TeleportationTube();
        overflowTubeLoc.setGround(overflowTube);

        mainTube.addDestination(overflowTubeLoc);
        overflowTube.addDestination(mainTubeLoc);

        moon99DeprecatedMap.at(7, 2).addItem(new AccessCardLevel1());
        moon99DeprecatedMap.at(7, 3).addItem(new FirstAidKit());
        moon99DeprecatedMap.at(35, 2).addItem(new SterilisationBox());

        moon99DeprecatedMap.at(35, 3).addItem(new Apple());
        moon99DeprecatedMap.at(36, 3).addItem(new Cookies());
        moon99DeprecatedMap.at(37, 3).addItem(new Lantern());
        moon99DeprecatedMap.at(34, 3).addItem(new CRTMonitor());
        moon99DeprecatedMap.at(33, 3).addItem(new FloppyDisk());

        // Add TRAP SPAWNS
        // Placing them on Floor tiles inside the facility structure walkway loop
        moon99DeprecatedMap.at(6, 4).addItem(new FireMine());
        moon99DeprecatedMap.at(7, 4).addItem(new GasMine());
        moon99DeprecatedMap.at(8, 4).addItem(new StunMine());

        moon99DeprecatedMap.at(11, 2).setGround(new DeprecatedSprout());

        Spawner deprecatedHoleSpawner = new MultiSpawner(new SlimeSpawner(), new UndeadSpawner());
        moon99DeprecatedMap.at(23, 6).setGround(new Hole(deprecatedHoleSpawner, 8));

        Spawner deprecatedHole1Spawner = new SnatcherSpawner();
        moon99DeprecatedMap.at(23, 7).setGround(new Hole(deprecatedHole1Spawner, 8));

        moon99DeprecatedMap.at(10, 4).setGround(new WeaponStore());

        // Create Bob if at least one worker is selected.
        if (numberOfPlayers >= 1) {
            this.addPlayer(
                    createNewWorker(playerNames.get(0), 'ඞ'),
                    moon99DeprecatedMap.at(6, 2)
            );
        }

// Create Tom if at least two workers are selected.
        if (numberOfPlayers >= 2) {
            this.addPlayer(
                    createNewWorker(playerNames.get(1), 'ඞ'),
                    moon99DeprecatedMap.at(7, 2)
            );
        }

// Create Sarah if at least three workers are selected.
        if (numberOfPlayers >= 3) {
            this.addPlayer(
                    createNewWorker(playerNames.get(2), 'ඞ'),
                    moon99DeprecatedMap.at(8, 2)
            );
        }

// Create Julie if at least four workers are selected.
        if (numberOfPlayers >= 4) {
            this.addPlayer(
                    createNewWorker(playerNames.get(3), 'ඞ'),
                    moon99DeprecatedMap.at(6, 4)
            );
        }

// Create Rick if at least five workers are selected.
        if (numberOfPlayers >= 5) {
            this.addPlayer(
                    createNewWorker(playerNames.get(4), 'ඞ'),
                    moon99DeprecatedMap.at(8, 4)
            );
        }
        moon99DeprecatedMap.at(14, 2).addActor(new Parasite());

    }

    /**
     * Helper to ensure each worker is independent and starts with a Flask.
     */
    private ContractedWorker createNewWorker(String name, char skin) {
        WeightLimitedInventory inv = new WeightLimitedInventory(50);
        inv.add(new Flask());
        return new ContractedWorker(name, skin, 100, inv);
    }

    // Returns all game maps created by this world.
    public java.util.List<GameMap> getGameMaps() {
        return java.util.Collections.unmodifiableList(gameMaps);
    }

    /**
     * Returns the workers in their original turn order.
     */
    public List<ContractedWorker> getWorkers() {

        // Store the workers in their original order.
        List<ContractedWorker> workers = new ArrayList<>();

        // Get every player from the original World class.
        for (Actor player : getPlayers()) {

            // Add the player if it is a contracted worker.
            if (player instanceof ContractedWorker worker) {
                workers.add(worker);
            }
        }

        return workers;
    }

    /**
     * Returns the available actions for a specific worker.
     *
     * @param worker the worker whose available actions are requested
     * @return the actions that the worker can perform
     */
    public ActionList getAvailableActions(ContractedWorker worker) {

        // Check whether the worker is still in the game.
        if (!actorLocations.contains(worker)) {
            return new ActionList();
        }

        // Get the worker's current location.
        Location currentLocation = actorLocations.locationOf(worker);

        // Generate actions using the original game engine.
        return prepareAllowableActions(worker, currentLocation);
    }


    /**
     * Executes an action selected by a worker.
     *
     * @param worker the worker performing the action
     * @param action the selected action
     * @return the result of the action
     */
    public String executeAction(ContractedWorker worker, Action action) {

        // Check whether the worker is still in the game.
        if (!actorLocations.contains(worker)) {
            return "This worker is no longer in the game.";
        }

        // Get the worker's current location.
        Location currentLocation = actorLocations.locationOf(worker);

        // Execute the original game engine action.
        return action.execute(worker, currentLocation.map());
    }

    /**
     * Processes the turns of all NPCs in the game.
     */
    public void processNpcTurns() {

        // Iterate through all actors in the original game order.
        for (Actor actor : actorLocations) {

            // Stop processing if all players have been eliminated.
            if (!stillRunning()) {
                break;
            }

            // Skip human players because they take turns through the website.
            if (players.contains(actor)) {
                continue;
            }

            // Give the NPC an initial previous action if it has none.
            lastActionMap.putIfAbsent(actor, new DoNothingAction());

            // Execute the NPC's original AI behaviour.
            processActorTurn(actor);
        }
    }

    /**
     * Advances time by ticking every game map once.
     */
    public void tickAllMaps() {

        // Tick every map in the game.
        for (GameMap gameMap : gameMaps) {
            gameMap.tick();
        }
    }

}