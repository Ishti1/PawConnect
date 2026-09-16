package com.catconnect.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class CatGameHubController {

    @FXML private StackPane gameContentContainer;

    private Parent memoryGameRoot;

    @FXML
    public void initialize() {
        loadMemoryGame();
    }

    private void loadMemoryGame() {
        if (memoryGameRoot == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/memory_game.fxml"));
                memoryGameRoot = loader.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (memoryGameRoot != null) {
            gameContentContainer.getChildren().setAll(memoryGameRoot);
        }
    }
}
