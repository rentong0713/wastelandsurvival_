
package org.example.wastelandsurvival.dto;

/**
 * Stores the separate visual layers of one game map tile.
 */
public class TileDTO {

    // Stores the ground symbol.
    private String ground;

    // Stores the item symbol.
    private String item;

    // Stores the actor symbol.
    private String actor;

    /**
     * Creates a tile with its ground, item and actor.
     */
    public TileDTO(String ground, String item, String actor) {
        this.ground = ground;
        this.item = item;
        this.actor = actor;
    }

    // Returns the ground symbol.
    public String getGround() {
        return ground;
    }

    // Returns the item symbol.
    public String getItem() {
        return item;
    }

    // Returns the actor symbol.
    public String getActor() {
        return actor;
    }
}
