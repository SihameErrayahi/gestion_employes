package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import model.Conge;
import model.Employe;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CongeController {

    @FXML private TableView<Conge>            tableConges;
    @FXML private TableColumn<Conge, Integer> colId;
    @FXML private TableColumn<Conge, String>  colEmployeNom;
    @FXML private TableColumn<Conge, String>  colDateDebut;
    @FXML private TableColumn<Conge, String>  colDateFin;
    @FXML private TableColumn<Conge, Integer> colJours;
    @FXML private TableColumn<Conge, String>  colType;
    @FXML private TableColumn<Conge, String>  colStatut;
    @FXML private TableColumn<Conge, String>  colMotif;

    @FXML private ComboBox<Employe>      comboEmploye;
    @FXML private ComboBox<Conge.Type>   comboType;
    @FXML private DatePicker             fieldDateDebut;
    @FXML private DatePicker             fieldDateFin;
    @FXML private TextField              fieldMotif;
    @FXML private Label                  labelNbJours;
    @FXML private Label                  labelMessage;
    @FXML private Label                  labelStats;
    @FXML private ComboBox<String>       comboFiltreStatut;

    private final CongeDAO   congeDAO   = new CongeDAO();
    private final EmployeDAO employeDAO = new EmployeDAO();

    private ObservableList<Conge> listeConges;
    private FilteredList<Conge>   listeFiltree;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Nom employé au lieu de l'ID
        colEmployeNom.setCellValueFactory(c -> {
            Employe e = employeDAO.getParId(c.getValue().getEmployeId());
            return new SimpleStringProperty(e != null ? e.getNomComplet() : "ID:" + c.getValue().getEmployeId());
        });

        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colJours.setCellValueFactory(new PropertyValueFactory<>("nombreJours"));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        // Colonne statut avec couleur
        colStatut.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatut().name()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "APPROUVE"  -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    case "REFUSE"    -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    case "EN_ATTENTE"-> setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                    default          -> setStyle("");
                }
            }
        });

        comboEmploye.setItems(FXCollections.observableArrayList(employeDAO.getTous()));
        comboType.setItems(FXCollections.observableArrayList(Conge.Type.values()));

        // Filtre statut
        comboFiltreStatut.setItems(FXCollections.observableArrayList(
            "Tous", "EN_ATTENTE", "APPROUVE", "REFUSE"));
        comboFiltreStatut.setValue("Tous");
        comboFiltreStatut.valueProperty().addListener((obs, a, b) -> filtrerParStatut(b));

        // Calcul auto nb jours
        fieldDateDebut.valueProperty().addListener((o, a, b) -> calculerJours());
        fieldDateFin.valueProperty().addListener((o, a, b) -> calculerJours());

        listeConges  = FXCollections.observableArrayList();
        listeFiltree = new FilteredList<>(listeConges, p -> true);
        tableConges.setItems(listeFiltree);

        tableConges.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancien, nouveau) -> remplirFormulaire(nouveau));

        chargerConges();
    }

    private void chargerConges() {
        listeConges.setAll(congeDAO.getTous());
        majStats();
    }

    private void majStats() {
        long att = listeConges.stream().filter(c -> c.getStatut() == Conge.Statut.EN_ATTENTE).count();
        long app = listeConges.stream().filter(c -> c.getStatut() == Conge.Statut.APPROUVE).count();
        labelStats.setText("Total : " + listeConges.size() + "  |  ⏳ En attente : " + att + "  |  ✅ Approuvés : " + app);
    }

    private void calculerJours() {
        try {
            LocalDate debut = fieldDateDebut.getValue();
            LocalDate fin   = fieldDateFin.getValue();
            if (debut != null && fin != null && !fin.isBefore(debut)) {
                long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
                labelNbJours.setText(jours + " jour(s)");
            } else {
                labelNbJours.setText("— jours");
            }
        } catch (Exception e) {
            labelNbJours.setText("— jours");
        }
    }

    private void filtrerParStatut(String statut) {
        if (statut == null || statut.equals("Tous")) {
            listeFiltree.setPredicate(p -> true);
        } else {
            listeFiltree.setPredicate(c -> c.getStatut().name().equals(statut));
        }
    }

    private void remplirFormulaire(Conge c) {
        if (c == null) return;
        Employe emp = employeDAO.getParId(c.getEmployeId());
        comboEmploye.setValue(emp);
        comboType.setValue(c.getType());
        try { fieldDateDebut.setValue(LocalDate.parse(c.getDateDebut())); } catch (Exception ignored) {}
        try { fieldDateFin.setValue(LocalDate.parse(c.getDateFin())); } catch (Exception ignored) {}
        fieldMotif.setText(c.getMotif() != null ? c.getMotif() : "");
        labelMessage.setText("");
    }

    private void viderFormulaire() {
        comboEmploye.getSelectionModel().clearSelection();
        comboType.getSelectionModel().clearSelection();
        fieldDateDebut.setValue(null);
        fieldDateFin.setValue(null);
        fieldMotif.clear();
        labelNbJours.setText("— jours");
        tableConges.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }

    @FXML
    private void demanderConge() {
        if (!valider()) return;
        try {
            LocalDate debut = fieldDateDebut.getValue();
            LocalDate fin   = fieldDateFin.getValue();
            int jours = (int)(ChronoUnit.DAYS.between(debut, fin) + 1);

            Conge c = new Conge(0,
                comboEmploye.getValue().getId(),
                debut.toString(), fin.toString(),
                jours, comboType.getValue(),
                fieldMotif.getText().trim()
            );
            congeDAO.ajouter(c);
            chargerConges();
            viderFormulaire();
            succes("✔ Demande de congé soumise avec succès.");
        } catch (Exception ex) {
            erreur("❌ Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void approuverConge() {
        Conge sel = tableConges.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un congé."); return; }
        congeDAO.changerStatut(sel.getId(), Conge.Statut.APPROUVE);
        chargerConges();
        succes("✔ Congé approuvé.");
    }

    @FXML
    private void refuserConge() {
        Conge sel = tableConges.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un congé."); return; }
        congeDAO.changerStatut(sel.getId(), Conge.Statut.REFUSE);
        chargerConges();
        erreur("✔ Congé refusé.");
    }

    @FXML
    private void supprimerConge() {
        Conge sel = tableConges.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un congé."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer cette demande de congé ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                congeDAO.supprimer(sel.getId());
                chargerConges();
                viderFormulaire();
                succes("✔ Congé supprimé.");
            }
        });
    }

    @FXML private void viderChamps() { viderFormulaire(); }

    private boolean valider() {
        if (comboEmploye.getValue() == null) { erreur("⚠ Sélectionnez un employé."); return false; }
        if (comboType.getValue() == null)    { erreur("⚠ Choisissez le type de congé."); return false; }
        if (fieldDateDebut.getValue() == null) { erreur("⚠ Date de début requise."); return false; }
        if (fieldDateFin.getValue() == null)   { erreur("⚠ Date de fin requise."); return false; }
        if (fieldDateFin.getValue().isBefore(fieldDateDebut.getValue())) {
            erreur("⚠ La date de fin doit être après la date de début."); return false;
        }
        return true;
    }

    private void succes(String msg) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        labelMessage.setText(msg);
    }
    private void erreur(String msg) {
        labelMessage.setStyle("-fx-text-fill: #EF4444;");
        labelMessage.setText(msg);
    }
}
