import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class BlueAllianceAPI {
    public static void main(String[] args) {
        String apiKey = "your_API_key_here";
        String eventKey = "2022casj"; // replace with the key for the desired event

        String endpoint = "https://www.thebluealliance.com/api/v3/event/" + eventKey + "?X-TBA-Auth-Key=" + apiKey;

        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error: Response code " + responseCode);
            } else {
                Scanner scanner = new Scanner(url.openStream());
                String responseBody = scanner.useDelimiter("\\A").next();
                scanner.close();
                System.out.println(responseBody); // print the JSON response to the console
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
