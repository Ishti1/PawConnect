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
    @FXML private TextField titleField, phoneField, goalField, imageUrlField, accountNameField, accountNumberField, bankNameField, mobileBankingField;
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
        body.put("contactPhone", phoneField.getText().trim());
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
    @Override protected boolean ignoreLocationFilter() { return true; }
    @Override protected VBox buildCard(JsonNode item) {
        long id = item.path("id").asLong();
        double goal = item.path("goalAmount").asDouble();
        String title = item.path("title").asText("Campaign");
        String phone = item.path("contactPhone").asText("No phone");
        String posterName = item.path("senderName").asText("Anonymous");
        String description = item.path("description").asText();

        String detail = "👤 Posted by: " + posterName + "\n📞 Contact: " + phone + "\n\n" + description + "\n\n💰 Goal: " + goal;

        VBox card = buildCatalogCard(item, id, title, detail, "/donations/" + id + "/react", () -> beginEdit(item));

        javafx.scene.layout.HBox actionButtons = new javafx.scene.layout.HBox(10);

        Button accountBtn = new Button("💳 Account Info");
        accountBtn.getStyleClass().add("secondary-button");
        accountBtn.setOnAction(e -> showAccountInfo(item));
        actionButtons.getChildren().add(accountBtn);

        if (!isOwn(item)) {
            Button messageBtn = new Button("💬 Message Poster");
            messageBtn.getStyleClass().add("primary-button");
            messageBtn.setOnAction(e -> {
                long targetUserId = item.path("userId").asLong(-1);
                if (targetUserId != -1) {
                    if (MainController.getInstance() != null) {
                        MainController.getInstance().openDirectChat(targetUserId, posterName);
                    } else {
                        com.catconnect.util.UiHelper.showError("Navigation error: Main screen not found.");
                    }
                } else {
                    com.catconnect.util.UiHelper.showError("Cannot message this user.");
                }
            });
            actionButtons.getChildren().add(messageBtn);
        }

        card.getChildren().add(actionButtons);
        return card;
    }

    private void beginEdit(JsonNode item) {
        editingId = item.path("id").asLong(); existingImageUrl = imageUrlFrom(item);
        titleField.setText(item.path("title").asText(""));
        phoneField.setText(item.path("contactPhone").asText(""));
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
            titleField.clear(); phoneField.clear(); descField.clear(); goalField.clear();
            accountNameField.clear(); accountNumberField.clear(); bankNameField.clear();
            mobileBankingField.clear(); paymentInstructionsField.clear();
            clearSelectedImage(photoPreview, photoLabel, imageUrlField);
            editingId = null; existingImageUrl = null;
        }
    }
}
