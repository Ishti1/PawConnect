package com.catconnect.controller;

import com.catconnect.model.CatBreed;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class CatLibraryController {

    @FXML
    private BorderPane root;

    @FXML
    private FlowPane breedGrid;

    @FXML
    private TextField searchField;

    @FXML
    private Label resultLabel;

    private final ObservableList<CatBreed> breeds =
            FXCollections.observableArrayList();

    // Cached reference to the library grid center — needed so the Back button
    // can restore it after showBreedDetails() swaps it out.
    private javafx.scene.Node libraryCenter;

    @FXML
    public void initialize() {

        loadBreeds();

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterBreeds(newValue)
        );

        showBreedCards(breeds);

        // Must be saved AFTER showBreedCards so the grid is populated.
        libraryCenter = root.getCenter();
    }


    // =========================================================
    // BREED DATA
    // =========================================================

    private void loadBreeds() {

        breeds.addAll(

                new CatBreed(
                        "Persian",
                        "Calm • Affectionate • Relaxed",

                        "Persian cats are often known for their gentle personalities "
                                + "and luxurious coats. Many enjoy a peaceful indoor lifestyle "
                                + "and quiet companionship.",

                        "Calm, affectionate and people-oriented",

                        "High",
                        "Low–Medium",
                        "Low",

                        "The Persian is one of the oldest recognised cat breeds.",

                        "/images/library/persian.jpg"
                ),


                new CatBreed(
                        "Siamese",
                        "Social • Intelligent • Vocal",

                        "Siamese cats are highly social and often form strong bonds "
                                + "with their people. They are known for their distinctive "
                                + "colouring and expressive personalities.",

                        "Social, curious and very communicative",

                        "Medium",
                        "High",
                        "High",

                        "Siamese cats are famous for their distinctive blue eyes.",

                        "/images/library/siamese.jpg"
                ),


                new CatBreed(
                        "Maine Coon",
                        "Gentle • Playful • Friendly",

                        "Maine Coons are large cats with long coats and a reputation "
                                + "for being gentle and friendly. They are often playful "
                                + "well into adulthood.",

                        "Gentle, friendly and playful",

                        "Medium–High",
                        "Medium–High",
                        "Medium",

                        "The Maine Coon is one of the largest domestic cat breeds.",

                        "/images/library/maine_coon.jpg"
                ),


                new CatBreed(
                        "Bengal",
                        "Energetic • Curious • Playful",

                        "Bengals are active and curious cats with striking coats. "
                                + "They tend to enjoy climbing, exploring and interactive play.",

                        "Energetic, curious and confident",

                        "Low–Medium",
                        "High",
                        "Medium",

                        "The Bengal's spotted or marbled coat gives it a wild appearance.",

                        "/images/library/bengal.jpg"
                ),


                new CatBreed(
                        "British Shorthair",
                        "Calm • Independent • Sweet",

                        "British Shorthairs are generally calm and easygoing. "
                                + "They often enjoy being near their people without demanding "
                                + "constant attention.",

                        "Calm, independent and affectionate",

                        "Low",
                        "Low–Medium",
                        "Low",

                        "The British Shorthair is known for its dense, plush coat.",

                        "/images/library/british_shorthair.jpg"
                ),


                new CatBreed(
                        "Ragdoll",
                        "Gentle • Affectionate • Easygoing",

                        "Ragdolls are known for their gentle temperament and strong "
                                + "attachment to people. They often enjoy relaxed interaction "
                                + "and companionship.",

                        "Gentle, affectionate and relaxed",

                        "Medium",
                        "Low–Medium",
                        "Low–Medium",

                        "Ragdolls are known for their striking blue eyes.",

                        "/images/library/ragdoll.jpg"
                ),


                new CatBreed(
                        "Scottish Fold",
                        "Sweet • Quiet • Adaptable",

                        "Scottish Folds are known for their distinctive folded ears "
                                + "and generally sweet personalities. Individual cats can "
                                + "vary considerably in temperament.",

                        "Sweet, adaptable and usually quiet",

                        "Medium",
                        "Medium",
                        "Low–Medium",

                        "Their characteristic folded ears originated from a natural mutation.",

                        "/images/library/scottish_fold.jpg"
                ),


                new CatBreed(
                        "Turkish Angora",
                        "Active • Intelligent • Curious",

                        "Turkish Angoras are elegant, active cats that are often "
                                + "described as intelligent and playful. They tend to enjoy "
                                + "exploration and interaction.",

                        "Intelligent, active and playful",

                        "Medium",
                        "High",
                        "Medium",

                        "The Turkish Angora originated in the Ankara region of Turkey.",

                        "/images/library/turkish_angora.jpg"
                ),


                new CatBreed(
                        "Norwegian Forest Cat",
                        "Independent • Affectionate • Calm",

                        "The Norwegian Forest Cat, nicknamed the \"Wegie,\" is a majestic breed "
                                + "known for its thick, double coat and impressive size. They are slow "
                                + "to mature, taking up to five years to reach full stature.",

                        "Independent yet affectionate; playful and gentle with loved ones, "
                                + "reserved with new visitors",

                        "High",
                        "Medium",
                        "Low",

                        "The Norwegian Forest Cat can take up to five years to reach full size.",

                        "/images/library/norwegian_forest_cat.jpg"
                )
        );
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void filterBreeds(String searchText) {

        String query = searchText == null
                ? ""
                : searchText.trim().toLowerCase();

        if (query.isEmpty()) {

            showBreedCards(breeds);
            return;
        }

        List<CatBreed> filtered = new ArrayList<>();

        for (CatBreed breed : breeds) {

            if (breed.getName().toLowerCase().contains(query)
                    || breed.getTagline().toLowerCase().contains(query)
                    || breed.getPersonality().toLowerCase().contains(query)) {

                filtered.add(breed);
            }
        }

        showBreedCards(filtered);
    }


    // =========================================================
    // CARD GRID
    // =========================================================

    private void showBreedCards(List<CatBreed> breedList) {

        // Restore the original library center if we came from the detail view.
        if (libraryCenter != null) {
            root.setCenter(libraryCenter);
        }

        breedGrid.getChildren().clear();

        for (CatBreed breed : breedList) {

            breedGrid.getChildren().add(
                    createBreedCard(breed)
            );
        }

        resultLabel.setText(
                breedList.size() + " breeds"
        );
    }


    // =========================================================
    // BREED CARD
    // =========================================================

    private Node createBreedCard(CatBreed breed) {

        VBox card = new VBox();

        card.getStyleClass().add("breed-card");

        card.setPrefWidth(260);
        card.setMaxWidth(260);

        ImageView imageView = createImageView(
                breed.getImageUrl(),
                260,
                190
        );

        StackPane imageContainer = new StackPane(imageView);

        imageContainer.getStyleClass().add("breed-image-container");

        Label name = new Label(
                breed.getName()
        );

        name.getStyleClass().add("breed-card-name");

        Label tagline = new Label(
                breed.getTagline()
        );

        tagline.getStyleClass().add("breed-card-tagline");

        tagline.setWrapText(true);

        Label description = new Label(
                breed.getDescription()
        );

        description.getStyleClass().add(
                "breed-card-description"
        );

        description.setWrapText(true);

        Button learnMore = new Button(
                "Learn more  →"
        );

        learnMore.getStyleClass().add(
                "breed-learn-button"
        );

        learnMore.setOnAction(
                event -> showBreedDetails(breed)
        );

        VBox information = new VBox(
                7,
                name,
                tagline,
                description,
                learnMore
        );

        information.getStyleClass().add(
                "breed-card-information"
        );

        information.setPadding(
                new Insets(16, 17, 17, 17)
        );

        VBox.setVgrow(description, Priority.ALWAYS);

        card.getChildren().addAll(
                imageContainer,
                information
        );

        return card;
    }


    // =========================================================
    // DETAIL VIEW
    // =========================================================

    private void showBreedDetails(CatBreed breed) {

        VBox details = new VBox();

        details.getStyleClass().add(
                "breed-details"
        );

        details.setSpacing(0);

        Button backButton = new Button(
                "←  Back to Cat Library"
        );

        backButton.getStyleClass().add(
                "breed-back-button"
        );

        backButton.setOnAction(
                event -> showBreedCards(
                        getVisibleBreeds()
                )
        );


        ImageView imageView = createImageView(
                breed.getImageUrl(),
                520,
                350
        );

        StackPane heroImage =
                new StackPane(imageView);

        heroImage.getStyleClass().add(
                "breed-detail-image"
        );


        Label name = new Label(
                breed.getName()
        );

        name.getStyleClass().add(
                "breed-detail-name"
        );


        Label tagline = new Label(
                breed.getTagline()
        );

        tagline.getStyleClass().add(
                "breed-detail-tagline"
        );


        Label descriptionTitle =
                new Label("ABOUT");

        descriptionTitle.getStyleClass().add(
                "detail-section-title"
        );


        Label description =
                new Label(breed.getDescription());

        description.getStyleClass().add(
                "breed-detail-description"
        );

        description.setWrapText(true);


        Label personalityTitle =
                new Label("PERSONALITY");

        personalityTitle.getStyleClass().add(
                "detail-section-title"
        );


        Label personality =
                new Label(breed.getPersonality());

        personality.getStyleClass().add(
                "detail-section-text"
        );


        HBox careRow = new HBox(
                12,
                createCareBox(
                        "Grooming",
                        breed.getGrooming()
                ),
                createCareBox(
                        "Activity",
                        breed.getActivity()
                ),
                createCareBox(
                        "Vocality",
                        breed.getVocality()
                )
        );

        careRow.getStyleClass().add(
                "care-row"
        );


        Label careTitle =
                new Label("CARE");

        careTitle.getStyleClass().add(
                "detail-section-title"
        );


        Label funTitle =
                new Label("FUN FACT");

        funTitle.getStyleClass().add(
                "detail-section-title"
        );


        Label funFact =
                new Label(breed.getFunFact());

        funFact.getStyleClass().add(
                "fun-fact"
        );

        funFact.setWrapText(true);


        VBox content = new VBox(
                10,
                backButton,
                heroImage,
                name,
                tagline,
                descriptionTitle,
                description,
                personalityTitle,
                personality,
                careTitle,
                careRow,
                funTitle,
                funFact
        );

        content.getStyleClass().add(
                "breed-detail-content"
        );

        content.setMaxWidth(760);


        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.getStyleClass().add(
                "breed-detail-scroll"
        );


        root.setCenter(scrollPane);
    }


    // =========================================================
    // CARE BOX
    // =========================================================

    private VBox createCareBox(
            String title,
            String value
    ) {

        Label titleLabel =
                new Label(title);

        titleLabel.getStyleClass().add(
                "care-title"
        );


        Label valueLabel =
                new Label(value);

        valueLabel.getStyleClass().add(
                "care-value"
        );

        valueLabel.setWrapText(true);


        VBox box = new VBox(
                5,
                titleLabel,
                valueLabel
        );

        box.getStyleClass().add(
                "care-box"
        );

        HBox.setHgrow(box, Priority.ALWAYS);

        return box;
    }


    // =========================================================
    // IMAGE
    // =========================================================

    private ImageView createImageView(
            String url,
            double width,
            double height
    ) {
        Image image;

        // Local classpath resource (e.g. "/images/library/persian.jpg")
        if (url.startsWith("/")) {
            java.io.InputStream stream =
                    getClass().getResourceAsStream(url);
            if (stream != null) {
                image = new Image(stream, width, height, false, true);
            } else {
                // Fallback: empty placeholder so the card still renders
                image = new Image(
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=",
                        width, height, false, true
                );
            }
        } else {
            // Remote URL — load asynchronously
            image = new Image(url, width, height, false, true, true);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        return imageView;
    }


    // =========================================================
    // CURRENTLY VISIBLE BREEDS
    // =========================================================

    private List<CatBreed> getVisibleBreeds() {

        String query =
                searchField.getText();

        if (query == null
                || query.trim().isEmpty()) {

            return new ArrayList<>(breeds);
        }

        String lower =
                query.trim().toLowerCase();

        List<CatBreed> result =
                new ArrayList<>();

        for (CatBreed breed : breeds) {

            if (breed.getName()
                    .toLowerCase()
                    .contains(lower)) {

                result.add(breed);
            }
        }

        return result;
    }
}
