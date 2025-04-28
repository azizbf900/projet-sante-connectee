package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.json.JSONObject;
import org.json.JSONArray;

public class ChatbotController {
    @FXML private VBox chatBox;
    @FXML private TextField userInput;
    @FXML private Button sendBtn;
    @FXML private ScrollPane chatScroll;

    private static final String OPENROUTER_API_URL;
    private static final String OPENROUTER_API_KEY;
    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Failed to load config");
        }
        OPENROUTER_API_URL = props.getProperty("OPENROUTER_API_URL", "");
        OPENROUTER_API_KEY = props.getProperty("OPENROUTER_API_KEY", "");
    }

    @FXML
    public void initialize() {
        sendBtn.setOnAction(e -> sendMessage());
        userInput.setOnAction(e -> sendMessage());
    }

    private void sendMessage() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;
        addMessage("Vous", message, "#1976d2");
        userInput.clear();
        callOpenRouter(message);
    }

    private void addMessage(String sender, String text, String color) {
        Label msg = new Label(sender + ": " + text);
        msg.setWrapText(true);
        msg.setStyle("-fx-background-color: #f0f4ff; -fx-padding: 8; -fx-background-radius: 10; -fx-text-fill: " + color + ";");
        chatBox.getChildren().add(msg);
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void addBotMessage(String text) {
        Label msg = new Label("Chatbot: " + text);
        msg.setWrapText(true);
        msg.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 8; -fx-background-radius: 10; -fx-text-fill: #333;");
        chatBox.getChildren().add(msg);
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void callOpenRouter(String userMessage) {
        if (OPENROUTER_API_URL == null || OPENROUTER_API_URL.isEmpty()) {
            addBotMessage("[Erreur] URL de l'API OpenRouter manquante dans config.properties.");
            return;
        }
        // Use a default model, fallback to others if needed
        String[] models = {"openchat/openchat-3.5-0106", "mistralai/mistral-7b-instruct", "meta-llama/Meta-Llama-3-8B-Instruct"};
        callOpenRouterWithModel(userMessage, models, 0);
    }

    private void callOpenRouterWithModel(String userMessage, String[] models, int idx) {
        if (idx >= models.length) {
            Platform.runLater(() -> addBotMessage("[Erreur] Aucun modèle OpenRouter n'a répondu."));
            return;
        }
        String model = models[idx];
        String body = "{\"model\":\"" + model + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + userMessage.replace("\"", "\\\"") + "\"}]}";
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(OPENROUTER_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + OPENROUTER_API_KEY)
                .header("HTTP-Referer", "https://openrouter.ai")
                .header("X-Title", "PdevUser Chatbot");
        HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int statusCode = response.statusCode();
                    if (statusCode == 404) {
                        // Try next model
                        callOpenRouterWithModel(userMessage, models, idx + 1);
                    } else if (statusCode != 200) {
                        String reply = "[Erreur OpenRouter] Code HTTP: " + statusCode + ". Réponse: " + response.body();
                        Platform.runLater(() -> addBotMessage(reply));
                    } else {
                        String reply = parseOpenRouterResponse(response.body());
                        Platform.runLater(() -> addBotMessage(reply));
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> addBotMessage("[Erreur de connexion à l'API OpenRouter]"));
                    return null;
                });
    }

    private String parseOpenRouterResponse(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("choices")) {
                JSONArray arr = obj.getJSONArray("choices");
                if (arr.length() > 0) {
                    JSONObject choice = arr.getJSONObject(0);
                    JSONObject msg = choice.getJSONObject("message");
                    String content = msg.optString("content", "");
                    return content.isEmpty() ? "[Réponse vide]" : content;
                }
            }
            return "[Réponse non reconnue]";
        } catch (Exception e) {
            return "[Erreur de parsing de la réponse OpenRouter]";
        }
    }
}