package Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import org.json.JSONObject;

public class TranslateService {

    public static String translateText(String textToTranslate) {
        try {
            String encodedText = URLEncoder.encode(textToTranslate, "UTF-8");

            // On ajoute un email fictif pour que la requête soit acceptée
            String urlStr = "https://api.mymemory.translated.net/get?q=" +
                    encodedText + "&langpair=fr|en&de=vitalink.project@example.com";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            // Analyse du JSON
            JSONObject json = new JSONObject(responseContent.toString());
            return json.getJSONObject("responseData").getString("translatedText");

        } catch (Exception e) {
            System.err.println("❌ Erreur de traduction : " + e.getMessage());
            return "Erreur de traduction";
        }
    }
}
