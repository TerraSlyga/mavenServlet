package sumdu.edu.ua.config;

import java.util.Map;

public class CustomInfoService {

    private final String appName;
    private final String version;
    private final String welcomeMessage;

    public CustomInfoService(String appName, String version, String welcomeMessage) {
        this.appName = appName;
        this.version = version;
        this.welcomeMessage = welcomeMessage;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersion() {
        return version;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public Map<String, Object> getAppMetadata() {
        return Map.of(
                "app", appName,
                "version", version,
                "welcomeMessage", welcomeMessage,
                "status", "UP"
        );
    }
}
