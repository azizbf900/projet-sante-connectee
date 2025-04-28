package Services;

import javafx.scene.control.Alert;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TranslateService {

    private static final String TRANSLATION_API_URL = "https://libretranslate.de/translate"; // ✅ URL plus fiable

    public static String translateText(String text, String sourceLang, String targetLang) {
        try {
            URL url = new URL(TRANSLATION_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            // Construire le body JSON
            JSONObject data = new JSONObject();
            data.put("q", text);
            data.put("source", sourceLang);
            data.put("target", targetLang);
            data.put("format", "text");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("translatedText");

        } catch (IOException e) {
            showError("Erreur de connexion au serveur de traduction.\nVérifiez votre connexion internet ou essayez plus tard.");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur inattendue pendant la traduction.");
            e.printStackTrace();
        }
        return null;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur de Traduction");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
