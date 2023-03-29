import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlueAllianceAPI {

  public static void main(String[] args) {
    String apiKey =
      "zTao3xBd8F22l2mr0w9Cp7eara3baub1H0XEjFXTjq2UT87fpYdP8cPp3XzGOMcY";
    String eventKey = "254"; // replace with the key for the desired event

    String endpoint =
      "https://www.thebluealliance.com/api/v3/team/frc" +
      eventKey +
      "?X-TBA-Auth-Key=" +
      apiKey;

    try {
      URL url = new URL(endpoint);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();

      int responseCode = conn.getResponseCode();
      if (responseCode != 200) {
        System.out.println("Error: Response code " + responseCode);
      } else {
        BufferedReader in = new BufferedReader(
          new InputStreamReader(conn.getInputStream())
        );
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        Gson gson = new Gson();
        Event event = gson.fromJson(response.toString(), Event.class);

        System.out.println("Event name: " + event.getName());
        System.out.println("Event start date: " + event.getStartDate());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  class Event {

    private String name;
    private String startDate;

    public String getName() {
      return name;
    }

    public String getStartDate() {
      return startDate;
    }
    // and so on for other properties
  }
}
