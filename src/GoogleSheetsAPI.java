import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSheetsAPI {

  private static final String APPLICATION_NAME = "FRCScoutingGoogleSheets";
  private static final String SPREADSHEET_ID =
    "1AHh-srtUqq6G6ELz--Nf801SlL0mawZvR-9ksJN7nmk";
  private static final String CREDENTIALS_FILE_PATH =
    "tba-api-integration-35f07261eef8.json";

  public static void main(String[] args) {
    try {
      // Load credentials from the specified file
      InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
      GoogleCredentials credentials = GoogleCredentials
        .fromStream(in)
        .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      GsonFactory factory = GsonFactory.getDefaultInstance();

      // Create the Sheets API client
      Sheets sheets = new Sheets.Builder(
        transport,
        factory,
        new HttpCredentialsAdapter(credentials)
      )
        .setApplicationName(APPLICATION_NAME)
        .build();

      // Get the spreadsheet and sheet we want to write to
      String sheetName = "Sheet1";
      ValueRange response = sheets
        .spreadsheets()
        .values()
        .get(SPREADSHEET_ID, sheetName)
        .execute();
      List<List<Object>> sheetValues = response.getValues();

      // Get the range of cells we want to write to
      String writeRange = "A1:E1";
      int rowIndex = sheetValues != null ? sheetValues.size() + 1 : 1;

      // Get the data we want to write
      List<Object> rowData = Arrays.asList(
        "Team Number",
        "Event Name",
        "Event Date",
        "Event Rank",
        "Event Record"
      );

      // Create the value range object and write the data to the sheet
      ValueRange valueRange = new ValueRange()
        .setValues(Arrays.asList(rowData));
      sheets
        .spreadsheets()
        .values()
        .update(SPREADSHEET_ID, writeRange, valueRange)
        .setValueInputOption("USER_ENTERED")
        .execute();

      // Loop through each FRC team and get their most recent event data
      URL url = new URL("https://api.statbotics.io/v0/teams");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setRequestProperty("Authorization", "Bearer your_api_key_here");

      BufferedReader in2 = new BufferedReader(
        new InputStreamReader(con.getInputStream())
      );
      String inputLine;
      StringBuffer response2 = new StringBuffer();

      while ((inputLine = in2.readLine()) != null) {
        response2.append(inputLine);
      }

      in2.close();

      JSONArray teams = new JSONArray(response2.toString());

      for (int i = 0; i < teams.length(); i++) {
        JSONObject team = teams.getJSONObject(i);
        String teamNumber = team.getString("team_number");

        url =
          new URL(
            "https://api.statbotics.io/v0/teams/" + teamNumber + "/events"
          );
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer your_api_key_here");

        in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        response2 = new StringBuffer();

        while ((inputLine = in2.readLine()) != null) {
          response2.append(inputLine);
        }

        in2.close();

        JSONArray events = new JSONArray(response2.toString());
        JSONObject mostRecentEvent = events.getJSONObject(events.length() - 1);
        String eventName = mostRecentEvent.getString("name");
        String eventDate = mostRecentEvent.getString("start_date");

        url =
          new URL(
            "https://api.statbotics.io/v0/events/" +
            mostRecentEvent.getString("key")
          );
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer your_api_key_here");

        in2 = new BufferedReader(new InputStreamReader(con.getInputStream()));
        response2 = new StringBuffer();

        while ((inputLine = in2.readLine()) != null) {
          response2.append(inputLine);
        }

        in2.close();

        JSONObject event = new JSONObject(response2.toString());
        JSONArray rankings = event.getJSONArray("rankings");

        for (int j = 0; j < rankings.length(); j++) {
          JSONObject ranking = rankings.getJSONObject(j);

          if (ranking.getString("team_key").equals(teamNumber)) {
            String eventRank = ranking.getString("rank");
            String eventRecord = ranking.getString("record");
            rowData =
              Arrays.asList(
                teamNumber,
                eventName,
                eventDate,
                eventRank,
                eventRecord
              );
            valueRange = new ValueRange().setValues(Arrays.asList(rowData));
            writeRange = "A" + rowIndex + ":E" + rowIndex;
            sheets
              .spreadsheets()
              .values()
              .update(SPREADSHEET_ID, writeRange, valueRange)
              .setValueInputOption("USER_ENTERED")
              .execute();
            rowIndex++;
            break;
          }
        }
      }

      System.out.println("Successfully wrote data to the Google Sheet.");
    } catch (IOException | GeneralSecurityException e) {
      System.err.println("Exception occurred: " + e.getMessage());
      e.printStackTrace();
    } catch (JSONException e) {
      System.err.println("Exception occurred: " + e.getLocalizedMessage());
      e.printStackTrace();
    }
  }
}
