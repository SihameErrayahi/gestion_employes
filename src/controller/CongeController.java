package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import model.Conge;
import model.Employe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CongeController {

    @FXML private ComboBox<Employe>    employeCombo;
    @FXML private ComboBox<Conge.Type> typeCombo;
    @FXML private DatePicker           dateDebutPicker;
    @FXML private DatePicker           dateFinPicker;
    @FXML private TextField            nombreJoursField;
    @FXML private TextField            motifField;
    @FXML private Label                messageLabel;

    @FXML private TableView<Conge>              congeTable;
    @FXML private TableColumn<Conge, Integer>   colId;
    @FXML private TableColumn<Conge, Integer>   colEmploye;
    @FXML private TableColumn<Conge, String>    colType;
    @FXML private TableColumn<Conge, String>    colDateDebut;
    @FXML private TableColumn<Conge, String>    colDateFin;
    @FXML private TableColumn<Conge, Integer>   colJours;
    @FXML private TableColumn<Conge, String>    colStatut;
    @FXML private TableColumn<Conge, String>    colMotif;

    private CongeDAO   congeDAO   = new CongeDAO();
    private EmployeDAO employeDAO = new EmployeDAO();
    private ObservableList<Conge> listeConges = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        employeCombo.setItems(FXCollections.observableArrayList(employeDAO.getTous()));
        typeCombo.setItems(FXCollections.observableArrayList(Conge.Type.values()));

        // Calcul automatique des jours
        dateDebutPicker.valueProperty().addListener((obs, o, n) -> calculerJours());
        dateFinPicker.valueProperty().addListener((obs, o, n) -> calculerJours());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmploye.setCellValueFactory(new PropertyValueFactory<>("employeId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colJours.setCellValueFactory(new PropertyValueFactory<>("nombreJours"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        // Coloration du statut
        colStatut.setCellFactory(col -> new TableCell<Conge, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.equals("APPROUVE"))
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                else if (item.equals("REFUSE"))
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
            }
        });

        chargerDonnees();
    }

    private void calculerJours() {
        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin   = dateFinPicker.getValue();
        if (debut != null && fin != null && !fin.isBefore(debut)) {
            nombreJoursField.setText(String.valueOf(ChronoUnit.DAYS.between(debut, fin) + 1));
        }
    }

    @FXML
    public void chargerDonnees() {
        listeConges.setAll(congeDAO.getTous());
        congeTable.setItems(listeConges);
        messageLabel.setText("");
    }

    @FXML
    public void ajouterConge() {
        if (employeCombo.getValue() == null || typeCombo.getValue() == null
                || dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            afficherErreur("❌ Veuillez remplir tous les champs obligatoires."); return;
        }
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            afficherErreur("❌ La date de fin doit être après la date de début."); return;
        }
        try {
            int jours = Integer.parseInt(nombreJoursField.getText().trim());
            Conge c = new Conge(0,
                employeCombo.getValue().getId(),
                dateDebutPicker.getValue().toString(),
                dateFinPicker.getValue().toString(),
                jours, typeCombo.getValue(),
                motifField.getText().trim());
            congeDAO.ajouter(c);
            afficherSucces("✅ Demande de congé enregistrée !");
            annuler();
            chargerDonnees();
        } catch (NumberFormatException ex) {
            afficherErreur("❌ Nombre de jours invalide.");
        }
    }

    @FXML
    public void approuverConge() {
        Conge sel = congeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un congé."); return; }
        congeDAO.changerStatut(sel.getId(), Conge.Statut.APPROUVE);
        afficherSucces("✅ Congé approuvé !");
        chargerDonnees();
    }

    @FXML
    public void refuserConge() {
        Conge sel = congeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un congé."); return; }
        congeDAO.changerStatut(sel.getId(), Conge.Statut.REFUSE);
        afficherSucces("⛔ Congé refusé.");
        chargerDonnees();
    }

    @FXML
    public void supprimerConge() {
        Conge sel = congeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un congé à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce congé ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                congeDAO.supprimer(sel.getId());
                afficherSucces("✅ Congé supprimé.");
                chargerDonnees();
            }
        });
    }

    @FXML
    public void annuler() {
        employeCombo.setValue(null); typeCombo.setValue(null);
        dateDebutPicker.setValue(null); dateFinPicker.setValue(null);
        nombreJoursField.clear(); motifField.clear();
        congeTable.getSelectionModel().clearSelection();
    }

    private void afficherSucces(String msg) {
        messageLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
    private void afficherErreur(String msg) {
        messageLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
}