
package org.example.wastelandsurvival.dto;

/**
 * Stores an action that can be displayed on the website.
 */
public class ActionDTO {

    // Stores the unique ID of the action.
    private String actionId;

    // Stores the action description shown to the player.
    private String description;

    /**
     * Creates an action DTO.
     *
     * @param actionId the unique action ID
     * @param description the action description
     */
    public ActionDTO(String actionId, String description) {

        // Store the action ID.
        this.actionId = actionId;

        // Store the action description.
        this.description = description;
    }

    /**
     * Returns the action ID.
     */
    public String getActionId() {
        return actionId;
    }

    /**
     * Returns the action description.
     */
    public String getDescription() {
        return description;
    }
}
