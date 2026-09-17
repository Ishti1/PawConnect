package com.catconnect.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.*;
import java.util.prefs.Preferences;

public class MemoryMatchController {

    public enum Difficulty {
        EASY(4, 4, 8, "Easy (4x4)"),
        MEDIUM(5, 4, 10, "Medium (5x4)"),
        HARD(6, 4, 12, "Hard (6x4)");

        final int cols;
        final int rows;
        final int pairs;
        final String displayName;

        Difficulty(int cols, int rows, int pairs, String displayName) {
            this.cols = cols;
            this.rows = rows;
            this.pairs = pairs;
            this.displayName = displayName;
        }
    }

    private static final String[] ALL_CAT_TYPES = {
            "cat_fish", "cat_box", "cat_bar", "cat_black",
            "cat_calico", "cat_siamese", "cat_groom", "cat_upside",
            "cat_wink", "cat_wave", "cat_collar", "cat_white"
    };

    @FXML private VBox memoryRoot;
    @FXML private GridPane cardGrid;
    @FXML private StackPane gameAreaStack;
    @FXML private Pane overlayPane;
    @FXML private Label timerLabel;
    @FXML private Label movesLabel;
    @FXML private Label matchesLabel;
    @FXML private Label scoreLabel;
    @FXML private Label bestScoreLabel;
    @FXML private Label bestTimeLabel;
    @FXML private Button btnEasy;
    @FXML private Button btnMedium;
    @FXML private Button btnHard;

    private final Map<String, Image> catImageCache = new HashMap<>();
    private Image cardBackImage;

    private Difficulty currentDifficulty = Difficulty.EASY;
    private final List<Card> cards = new ArrayList<>();
    private Card firstSelectedCard = null;
    private Card secondSelectedCard = null;
    private boolean checkingPair = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;

    private int movesCount = 0;
    private int matchesCount = 0;
    private int score = 0;
    private int consecutiveMatches = 0;
    private int elapsedSeconds = 0;

    private Timeline gameTimer;
    private Preferences prefs;

    @FXML
    public void initialize() {
        prefs = Preferences.userNodeForPackage(MemoryMatchController.class);
        loadImages();
        setupTimer();
        setDifficulty(Difficulty.EASY);
    }

    private void loadImages() {
        try {
            cardBackImage = new Image(getClass().getResourceAsStream("/images/memory/card_back.png"));
        } catch (Exception e) {
            cardBackImage = null;
        }

        for (String type : ALL_CAT_TYPES) {
            try {
                Image img = new Image(getClass().getResourceAsStream("/images/memory/" + type + ".png"));
                catImageCache.put(type, img);
            } catch (Exception ignored) {}
        }
    }

    private void setupTimer() {
        gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedSeconds++;
            updateTimerDisplay();
        }));
        gameTimer.setCycleCount(Animation.INDEFINITE);
    }

    private void updateTimerDisplay() {
        int mins = elapsedSeconds / 60;
        int secs = elapsedSeconds % 60;
        if (timerLabel != null) {
            timerLabel.setText(String.format("%02d:%02d", mins, secs));
        }
    }

    @FXML
    public void onEasySelected() {
        setDifficulty(Difficulty.EASY);
    }

    @FXML
    public void onMediumSelected() {
        setDifficulty(Difficulty.MEDIUM);
    }

    @FXML
    public void onHardSelected() {
        setDifficulty(Difficulty.HARD);
    }

    @FXML
    public void onNewGame() {
        startNewGame();
    }

    private void setDifficulty(Difficulty diff) {
        this.currentDifficulty = diff;
        updateDifficultyButtons();
        startNewGame();
    }

    private void updateDifficultyButtons() {
        if (btnEasy != null) {
            btnEasy.getStyleClass().remove("diff-btn-active");
            if (currentDifficulty == Difficulty.EASY) btnEasy.getStyleClass().add("diff-btn-active");
        }
        if (btnMedium != null) {
            btnMedium.getStyleClass().remove("diff-btn-active");
            if (currentDifficulty == Difficulty.MEDIUM) btnMedium.getStyleClass().add("diff-btn-active");
        }
        if (btnHard != null) {
            btnHard.getStyleClass().remove("diff-btn-active");
            if (currentDifficulty == Difficulty.HARD) btnHard.getStyleClass().add("diff-btn-active");
        }
    }

    private void startNewGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }

        movesCount = 0;
        matchesCount = 0;
        score = 0;
        consecutiveMatches = 0;
        elapsedSeconds = 0;
        gameStarted = false;
        gameOver = false;
        firstSelectedCard = null;
        secondSelectedCard = null;
        checkingPair = false;

        updateStatsDisplay();
        loadBestRecords();

        buildCards();
    }

    private void updateStatsDisplay() {
        if (timerLabel != null) timerLabel.setText("00:00");
        if (movesLabel != null) movesLabel.setText(String.valueOf(movesCount));
        if (matchesLabel != null) matchesLabel.setText(matchesCount + " / " + currentDifficulty.pairs);
        if (scoreLabel != null) scoreLabel.setText(String.valueOf(score));
    }

    private void loadBestRecords() {
        int bestScore = prefs.getInt("best_score_" + currentDifficulty.name(), 0);
        int bestTime = prefs.getInt("best_time_" + currentDifficulty.name(), 0);

        if (bestScoreLabel != null) {
            bestScoreLabel.setText(bestScore > 0 ? String.valueOf(bestScore) : "--");
        }
        if (bestTimeLabel != null) {
            if (bestTime > 0) {
                int mins = bestTime / 60;
                int secs = bestTime % 60;
                bestTimeLabel.setText(String.format("%02d:%02d", mins, secs));
            } else {
                bestTimeLabel.setText("--");
            }
        }
    }

    private void saveBestRecords(int finalScore, int totalSeconds) {
        int bestScore = prefs.getInt("best_score_" + currentDifficulty.name(), 0);
        int bestTime = prefs.getInt("best_time_" + currentDifficulty.name(), 0);

        boolean isNewScore = finalScore > bestScore;
        boolean isNewTime = bestTime == 0 || totalSeconds < bestTime;

        if (isNewScore) {
            prefs.putInt("best_score_" + currentDifficulty.name(), finalScore);
        }
        if (isNewTime) {
            prefs.putInt("best_time_" + currentDifficulty.name(), totalSeconds);
        }
        loadBestRecords();
    }

    private void buildCards() {
        cards.clear();
        cardGrid.getChildren().clear();

        // Calculate responsive card size based on difficulty
        double cardSize = 100;
        if (currentDifficulty == Difficulty.EASY) {
            cardSize = 105;
        } else if (currentDifficulty == Difficulty.MEDIUM) {
            cardSize = 95;
        } else {
            cardSize = 85;
        }

        // Pick pair types
        List<String> pool = new ArrayList<>(Arrays.asList(ALL_CAT_TYPES));
        Collections.shuffle(pool);
        List<String> selectedTypes = pool.subList(0, currentDifficulty.pairs);

        List<String> gameDeck = new ArrayList<>();
        for (String type : selectedTypes) {
            gameDeck.add(type);
            gameDeck.add(type);
        }
        Collections.shuffle(gameDeck);

        int totalCards = currentDifficulty.cols * currentDifficulty.rows;
        for (int i = 0; i < totalCards; i++) {
            String type = gameDeck.get(i);
            Card card = new Card(i, type, catImageCache.get(type), cardBackImage, cardSize);
            cards.add(card);

            int col = i % currentDifficulty.cols;
            int row = i / currentDifficulty.cols;
            cardGrid.add(card.getNode(), col, row);

            card.getNode().setOnMouseClicked(e -> handleCardClick(card));
        }
    }

    private void handleCardClick(Card card) {
        if (checkingPair || gameOver || card.isFlipped() || card.isMatched() || card.isFlipping() || card == firstSelectedCard) {
            return;
        }

        if (!gameStarted) {
            gameStarted = true;
            gameTimer.play();
        }

        card.setFlipping(true);

        if (firstSelectedCard == null) {
            firstSelectedCard = card;
            flipCard(card, true, () -> card.setFlipping(false));
        } else if (secondSelectedCard == null) {
            secondSelectedCard = card;
            movesCount++;
            if (movesLabel != null) movesLabel.setText(String.valueOf(movesCount));
            checkingPair = true;
            flipCard(card, true, () -> {
                card.setFlipping(false);
                checkMatch();
            });
        }
    }

    private void checkMatch() {
        if (firstSelectedCard == null || secondSelectedCard == null) {
            checkingPair = false;
            return;
        }

        if (firstSelectedCard.getCatType().equals(secondSelectedCard.getCatType())) {
            // MATCH!
            matchesCount++;
            consecutiveMatches++;

            int points = 100 + (consecutiveMatches > 1 ? (consecutiveMatches - 1) * 25 : 0);
            score += points;

            firstSelectedCard.setMatched(true);
            secondSelectedCard.setMatched(true);

            firstSelectedCard.playMatchEffect();
            secondSelectedCard.playMatchEffect();

            showFloatingScore(secondSelectedCard.getNode(), "+" + points + " ✨");

            updateStatsDisplay();

            firstSelectedCard = null;
            secondSelectedCard = null;
            checkingPair = false;

            if (matchesCount == currentDifficulty.pairs) {
                handleVictory();
            }
        } else {
            // NO MATCH
            consecutiveMatches = 0;
            firstSelectedCard.playShakeEffect();
            secondSelectedCard.playShakeEffect();

            PauseTransition pause = new PauseTransition(Duration.millis(850));
            pause.setOnFinished(e -> {
                if (firstSelectedCard != null && secondSelectedCard != null) {
                    flipCard(firstSelectedCard, false, null);
                    flipCard(secondSelectedCard, false, () -> {
                        firstSelectedCard = null;
                        secondSelectedCard = null;
                        checkingPair = false;
                    });
                } else {
                    firstSelectedCard = null;
                    secondSelectedCard = null;
                    checkingPair = false;
                }
            });
            pause.play();
        }
    }

    private void flipCard(Card card, boolean showFront, Runnable onFinished) {
        StackPane node = card.getNode();
        ScaleTransition st1 = new ScaleTransition(Duration.millis(110), node);
        st1.setFromX(1.0);
        st1.setToX(0.0);
        st1.setInterpolator(Interpolator.EASE_IN);

        st1.setOnFinished(e -> {
            card.setFlipped(showFront);
            ScaleTransition st2 = new ScaleTransition(Duration.millis(110), node);
            st2.setFromX(0.0);
            st2.setToX(1.0);
            st2.setInterpolator(Interpolator.EASE_OUT);
            st2.setOnFinished(e2 -> {
                if (onFinished != null) onFinished.run();
            });
            st2.play();
        });
        st1.play();
    }

    private void showFloatingScore(Node targetNode, String text) {
        if (overlayPane == null) return;

        Label label = new Label(text);
        label.getStyleClass().add("floating-score-label");

        // Position relative to target node within grid
        javafx.geometry.Bounds bounds = targetNode.localToScene(targetNode.getBoundsInLocal());
        javafx.geometry.Bounds paneBounds = overlayPane.localToScene(overlayPane.getBoundsInLocal());

        if (bounds != null && paneBounds != null) {
            double x = bounds.getMinX() - paneBounds.getMinX() + (bounds.getWidth() / 2) - 30;
            double y = bounds.getMinY() - paneBounds.getMinY() - 10;
            label.setLayoutX(Math.max(10, x));
            label.setLayoutY(Math.max(10, y));
        }

        overlayPane.getChildren().add(label);

        TranslateTransition tt = new TranslateTransition(Duration.millis(800), label);
        tt.setByY(-40);

        FadeTransition ft = new FadeTransition(Duration.millis(800), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.setOnFinished(e -> overlayPane.getChildren().remove(label));
        pt.play();
    }

    private void handleVictory() {
        gameOver = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }

        // Calculate bonuses
        int timeBonus = Math.max(0, 300 - (elapsedSeconds * 2));
        int movesBonus = Math.max(0, (currentDifficulty.pairs * 3 - movesCount) * 15);
        int finalScore = score + timeBonus + movesBonus;
        score = finalScore;
        if (scoreLabel != null) {
            scoreLabel.setText(String.valueOf(score));
        }

        saveBestRecords(finalScore, elapsedSeconds);

        Platform.runLater(() -> showWinDialog(finalScore));
    }

    private void showWinDialog(int finalScore) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("dialog-card");
        box.setStyle("-fx-min-width: 380px; -fx-padding: 30 35;");

        Label badge = new Label("🎉 PURR-FECT!");
        badge.getStyleClass().add("dialog-badge");

        Label title = new Label("You Matched Every Cat!");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label("Paw-some memory skills! 🐾");
        subtitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        // Stats Box
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(10);
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setStyle("-fx-background-color: #fff5f5; -fx-padding: 16 20; -fx-background-radius: 12;");

        Label movesKey = new Label("Moves:");
        movesKey.setStyle("-fx-text-fill: #636e72; -fx-font-weight: bold;");
        Label movesVal = new Label(movesCount + " moves");
        movesVal.setStyle("-fx-text-fill: #2d3436; -fx-font-weight: bold;");

        Label timeKey = new Label("Time:");
        timeKey.setStyle("-fx-text-fill: #636e72; -fx-font-weight: bold;");
        int mins = elapsedSeconds / 60;
        int secs = elapsedSeconds % 60;
        Label timeVal = new Label(String.format("%02d:%02d", mins, secs));
        timeVal.setStyle("-fx-text-fill: #2d3436; -fx-font-weight: bold;");

        Label scoreKey = new Label("Total Score:");
        scoreKey.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        Label scoreVal = new Label(finalScore + " pts");
        scoreVal.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 16px;");

        statsGrid.add(movesKey, 0, 0);
        statsGrid.add(movesVal, 1, 0);
        statsGrid.add(timeKey, 0, 1);
        statsGrid.add(timeVal, 1, 1);
        statsGrid.add(scoreKey, 0, 2);
        statsGrid.add(scoreVal, 1, 2);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER);

        Button playBtn = new Button("Play Again 🐾");
        playBtn.getStyleClass().addAll("dialog-btn", "dialog-btn-win");
        playBtn.setOnAction(e -> {
            dialog.close();
            startNewGame();
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().addAll("dialog-btn", "dialog-btn-close");
        closeBtn.setOnAction(e -> dialog.close());

        btnBox.getChildren().addAll(playBtn, closeBtn);

        box.getChildren().addAll(badge, title, subtitle, statsGrid, btnBox);

        Scene scene = new Scene(box);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        } catch (Exception ignored) {}

        dialog.setScene(scene);
        dialog.show();
    }

    // ── Card Class ──────────────────────────────────────────

    public static class Card {
        private final int id;
        private final String catType;
        private final StackPane node;
        private final ImageView frontImageView;
        private final ImageView backImageView;
        private boolean flipped = false;
        private boolean matched = false;
        private boolean flipping = false;

        public Card(int id, String catType, Image catImage, Image backImage, double size) {
            this.id = id;
            this.catType = catType;

            node = new StackPane();
            node.setPrefSize(size, size);
            node.setMinSize(size, size);
            node.setMaxSize(size, size);
            node.getStyleClass().add("memory-card");

            backImageView = new ImageView();
            if (backImage != null) {
                backImageView.setImage(backImage);
            }
            backImageView.setFitWidth(size - 6);
            backImageView.setFitHeight(size - 6);
            backImageView.setPreserveRatio(true);

            frontImageView = new ImageView();
            if (catImage != null) {
                frontImageView.setImage(catImage);
            }
            frontImageView.setFitWidth(size - 6);
            frontImageView.setFitHeight(size - 6);
            frontImageView.setPreserveRatio(true);
            frontImageView.setVisible(false);

            node.getChildren().addAll(backImageView, frontImageView);
        }

        public int getId() { return id; }
        public String getCatType() { return catType; }
        public StackPane getNode() { return node; }
        public boolean isFlipped() { return flipped; }
        public boolean isMatched() { return matched; }
        public boolean isFlipping() { return flipping; }
        public void setFlipping(boolean flipping) { this.flipping = flipping; }

        public void setFlipped(boolean flipped) {
            this.flipped = flipped;
            if (flipped) {
                backImageView.setVisible(false);
                frontImageView.setVisible(true);
                node.getStyleClass().add("memory-card-flipped");
            } else {
                backImageView.setVisible(true);
                frontImageView.setVisible(false);
                node.getStyleClass().remove("memory-card-flipped");
            }
        }

        public void setMatched(boolean matched) {
            this.matched = matched;
            if (matched) {
                node.getStyleClass().add("memory-card-matched");
            }
        }

        public void playMatchEffect() {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), node);
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(1.15);
            st.setToY(1.15);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            st.play();
        }

        public void playShakeEffect() {
            TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
            tt.setByX(7);
            tt.setAutoReverse(true);
            tt.setCycleCount(6);
            tt.setOnFinished(e -> node.setTranslateX(0));
            tt.play();
        }
    }
}
