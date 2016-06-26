package org.anieanie.cardgame.environment;

import java.util.HashMap;

public class GameEnvironment extends HashMap<String, String> {

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
