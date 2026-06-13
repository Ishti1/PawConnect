package com.catconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DonationsController extends BaseListController {

    @FXML private VBox addForm;
    @FXML private TextField titleField, goalField, imageUrlField, accountNameField, accountNumberField, bankNameField, mobileBankingField;
    @FXML private TextArea descField, paymentInstructionsField;
    @FXML private ImageView photoPreview;
    @FXML private Label photoLabel;
    private Long editingId;
    private String existingImageUrl;

    @FXML public void initialize() { loadData(); }
    @FXML private void onAdd() { showForm(true); }
    @FXML private void onRefresh() { loadData(); }
    @FXML private void onCancelAdd() { showForm(false); }
    @FXML private void onChooseImage() { chooseImage(photoPreview, photoLabel); }
    @FXML private void onClearImage() { clearSelectedImage(photoPreview, photoLabel, imageUrlField); }

    @FXML private void onSave() {
        if (titleField.getText().isBlank()) {
            com.catconnect.util.UiHelper.showError("Title is required."); return;
        }
        BigDecimal goal = null;
        try {
            if (!goalField.getText().isBlank()) {
                goal = new BigDecimal(goalField.getText().trim());
            }
        } catch (NumberFormatException ignored) {
        }
        if ((goal == null || goal.compareTo(BigDecimal.ZERO) <= 0) && editingId == null) {
            com.catconnect.util.UiHelper.showError("Goal amount is required.");
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("title", titleField.getText().trim());
        body.put("description", descField.getText().trim());
        if (goal != null && goal.compareTo(BigDecimal.ZERO) > 0) {
            body.put("goalAmount", goal);
        }
        body.put("accountName", accountNameField.getText().trim());
        body.put("accountNumber", accountNumberField.getText().trim());
        body.put("bankName", bankNameField.getText().trim());
        body.put("mobileBanking", mobileBankingField.getText().trim());
        body.put("paymentInstructions", paymentInstructionsField.getText().trim());
        File img = selectedImageFile; String url = readImageUrlField(imageUrlField);
        if (editingId != null && img == null && url.isBlank() && existingImageUrl != null) url = existingImageUrl;
        Long id = editingId; showForm(false);
        if (id != null) updateWithImage("/donations/" + id, body, img, url);
        else postWithImage("/donations", body, img, url);
    }

    @Override protected String getApiPath() { return "/donations"; }
    @Override protected String getDeletePath() { return "/donations/"; }
    @Override protected String getUploadCategory() { return "donations"; }
    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        double goal = item.path("goalAmount").asDouble();
        double raised = item.path("raisedAmount").asDouble();
        int pct = goal > 0 ? (int) ((raised / goal) * 100) : 0;
        String title = item.path("title").asText("Campaign");
        String detail = item.path("description").asText() + "\nRaised: " + raised + " / " + goal + " (" + pct + "%)";
        VBox card = buildCatalogCard(item, id, title, detail, "/donations/" + id + "/react", () -> beginEdit(item));
        Button accountBtn = new Button("Account Info");
        accountBtn.getStyleClass().add("secondary-button");
        accountBtn.setOnAction(e -> showAccountInfo(item));
        card.getChildren().add(accountBtn);
        return card;
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        titleField.setText(item.path("title").asText(""));
        descField.setText(item.path("description").asText(""));
        goalField.setText(item.path("goalAmount").asText(""));
        accountNameField.setText(item.path("accountName").asText(""));
        accountNumberField.setText(item.path("accountNumber").asText(""));
        bankNameField.setText(item.path("bankName").asText(""));
        mobileBankingField.setText(item.path("mobileBanking").asText(""));
        paymentInstructionsField.setText(item.path("paymentInstructions").asText(""));
        imageUrlField.setText(existingImageUrl); addForm.setVisible(true); addForm.setManaged(true);
    }

    private void showAccountInfo(JsonNode item) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Donation Account Details");
        a.setHeaderText(item.path("title").asText("Campaign"));
        String txt = "Account Name: " + item.path("accountName").asText("-")
                + "\nAccount Number: " + item.path("accountNumber").asText("-")
                + "\nBank Name: " + item.path("bankName").asText("-")
                + "\nMobile Banking: " + item.path("mobileBanking").asText("-")
                + "\n\nInstructions:\n" + item.path("paymentInstructions").asText("-");
        a.setContentText(txt);
        a.showAndWait();
    }

    private void showForm(boolean show) {
        addForm.setVisible(show); addForm.setManaged(show);
        if (!show) {
            titleField.clear(); descField.clear(); goalField.clear();
            accountNameField.clear(); accountNumberField.clear(); bankNameField.clear();
            mobileBankingField.clear(); paymentInstructionsField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; existingImageUrl = null;
        }
    }
}
