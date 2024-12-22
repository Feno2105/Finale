import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
        // Map pour stocker les variables du fichier de configuration
    private static Map<String, String> configMap = new HashMap<>();
    
        public static void readConfigFile() {
                try (BufferedReader br = new BufferedReader(new FileReader("config.txt"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty() || line.startsWith("#")) {
                            continue;
                        }
        
                        // Diviser chaque ligne en clé et valeur 
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            configMap.put(key, value);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public String getConfigValue(String key) {
        ConfigManager.readConfigFile();
        return configMap.get(key);
    }
}
