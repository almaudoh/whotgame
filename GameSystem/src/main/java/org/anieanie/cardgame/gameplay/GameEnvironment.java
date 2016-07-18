package org.anieanie.cardgame.gameplay;

import java.util.HashMap;

public class GameEnvironment extends HashMap<String, String> {

    public static final String VAR_CURRENT_PLAYER = "CurrentPlayer";
    public static final String VAR_PLAYERS = "Players";
    public static final String VAR_VIEWERS = "Viewers";
    public static final String VAR_PLAYER_COUNT = "PlayerCount";

    public static GameEnvironment fromCGMPString(String envSpec) {
        GameEnvironment environment = new GameEnvironment();
        String[] envr = envSpec.split(";");
        String[] parts;
        for (String env : envr) {
            parts = env.split(":", 2);
            try {
                environment.put(parts[0].trim(), parts[1].trim());
            }
            catch (IndexOutOfBoundsException e) {
                // Likely an empty string.
            }
        }
        return environment;
    }

    public String toCGMPString() {
        StringBuilder envSpec = new StringBuilder();
        for (String key: keySet()) {
            envSpec.append(String.format("%s: %s; ", key, get(key)));
        }
        return envSpec.toString();
    }

}
