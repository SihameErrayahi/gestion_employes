package controller;

import model.Employe;
import service.EmployeService;
import service.EmployeService.ResultatService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;

public class EmployeController {

    // ─── TABLE ──────────────────────────────────────────────────────────────
    @FXML private TableView<Employe>               tableEmployes;
    @FXML private TableColumn<Employe, Integer>    colId;
    @FXML private TableColumn<Employe, String>     colNom;
    @FXML private TableColumn<Employe, String>     colPrenom;
    @FXML private TableColumn<Employe, String>     colPoste;
    @FXML private TableColumn<Employe, String>     colDepartement;
    @FXML private TableColumn<Employe, String>     colEmail;
    @FXML private TableColumn<Employe, String>     colTelephone;
    @FXML private TableColumn<Employe, String>     colDateEmbauche;
    @FXML private TableColumn<Employe, Double>     colSalaireBase;
    @FXML private TableColumn<Employe, String>     colStatut;
    @FXML private TableColumn<Employe, String>     colAnciennete;

    // ─── FORMULAIRE ─────────────────────────────────────────────────────────
    @FXML private TextField    fieldNom;
    @FXML private TextField    fieldPrenom;
    @FXML private TextField    fieldEmail;
    @FXML private TextField    fieldTelephone;
    @FXML private TextField    fieldPoste;
    @FXML private TextField    fieldDepartement;
    @FXML private DatePicker   fieldDateEmbauche;
    @FXML private TextField    fieldSalaireBase;
    @FXML private TextField    fieldCin;
    @FXML private TextField    fieldAdresse;
    @FXML private ComboBox<Employe.Statut> comboStatut;

    // ─── RECHERCHE & MESSAGES ───────────────────────────────────────────────
    @FXML private TextField    fieldRecherche;
    @FXML private Label        labelMessage;
    @FXML private Label        labelNbEmployes;

    private final EmployeService employeService = new EmployeService();
    private ObservableList<Employe> listeEmployes;
    private FilteredList<Employe>   listeFiltree;

    // ════════════════════════ INIT ════════════════════════════════════════════
    @FXML
    public void initialize() {

        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateEmbauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        colSalaireBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));

        // Colonne statut avec couleur
        colStatut.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStatut().name()));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "ACTIF"    -> setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                    case "INACTIF"  -> setStyle("-fx-text-fill: #94A3B8;");
                    case "SUSPENDU" -> setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                    default         -> setStyle("");
                }
            }
        });

        // Colonne ancienneté
        colAnciennete.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getAnciennete() + " ans"));

        // ComboBox statut
        comboStatut.setItems(FXCollections.observableArrayList(Employe.Statut.values()));
        comboStatut.setValue(Employe.Statut.ACTIF);

        // Data
        listeEmployes = FXCollections.observableArrayList();
        listeFiltree  = new FilteredList<>(listeEmployes, p -> true);
        tableEmployes.setItems(listeFiltree);

        // Recherche en temps réel
        fieldRecherche.textProperty().addListener((obs, ancienne, nouvelle) -> filtrer(nouvelle));

        // Sélection → remplir formulaire
        tableEmployes.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancien, nouveau) -> remplirFormulaire(nouveau));

        chargerEmployes();
    }

    // ════════════════════════ CHARGEMENT ══════════════════════════════════════
    private void chargerEmployes() {
        listeEmployes.setAll(employeService.getTousLesEmployes());
        majCompteur();
    }

    private void majCompteur() {
        int total = listeEmployes.size();
        long actifs = listeEmployes.stream()
            .filter(e -> e.getStatut() == Employe.Statut.ACTIF).count();
        labelNbEmployes.setText("Total : " + total + " employés  |  Actifs : " + actifs);
    }

    // ════════════════════════ FILTRAGE ════════════════════════════════════════
    private void filtrer(String motCle) {
        if (motCle == null || motCle.isBlank()) {
            listeFiltree.setPredicate(p -> true);
        } else {
            String mc = motCle.toLowerCase();
            listeFiltree.setPredicate(e ->
                e.getNom().toLowerCase().contains(mc) ||
                e.getPrenom().toLowerCase().contains(mc) ||
                e.getPoste().toLowerCase().contains(mc) ||
                e.getDepartement().toLowerCase().contains(mc) ||
                e.getEmail().toLowerCase().contains(mc)
            );
        }
    }

    // ════════════════════════ AJOUTER ═════════════════════════════════════════
    @FXML
    private void ajouterEmploye() {
        Employe e = construireDepuisForm(0);
        if (e == null) return; // erreur de parsing (salaire non numérique)

        ResultatService resultat = employeService.ajouterEmploye(e);
        if (resultat.succes) {
            chargerEmployes();
            viderChamps();
            succes(resultat.message);
        } else {
            erreur(resultat.message);
        }
    }

    // ════════════════════════ MODIFIER ════════════════════════════════════════
    @FXML
    private void modifierEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) {
            erreur("⚠ Veuillez sélectionner un employé à modifier.");
            return;
        }

        Employe e = construireDepuisForm(selectionne.getId());
        if (e == null) return; // erreur de parsing

        ResultatService resultat = employeService.modifierEmploye(e);
        if (resultat.succes) {
            chargerEmployes();
            succes(resultat.message);
        } else {
            erreur(resultat.message);
        }
    }

    // ════════════════════════ SUPPRIMER ═══════════════════════════════════════
    @FXML
    private void supprimerEmploye() {
        Employe selectionne = tableEmployes.getSelectionModel().getSelectedItem();
        if (selectionne == null) {
            erreur("⚠ Veuillez sélectionner un employé à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Confirmer la suppression de " + selectionne.getNomComplet() + " ?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                ResultatService resultat = employeService.supprimerEmploye(selectionne.getId());
                if (resultat.succes) {
                    chargerEmployes();
                    viderChamps();
                    succes(resultat.message);
                } else {
                    erreur(resultat.message);
                }
            }
        });
    }

    // ════════════════════════ HELPERS FORMULAIRE ══════════════════════════════

    /**
     * Construit un objet Employe depuis les champs du formulaire.
     * Retourne null si le salaire n'est pas un nombre valide (affiche une erreur).
     */
    private Employe construireDepuisForm(int id) {
        double salaire = 0;
        String salaireText = fieldSalaireBase.getText().trim();
        if (!salaireText.isEmpty()) {
            try {
                salaire = Double.parseDouble(salaireText.replace(",", "."));
            } catch (NumberFormatException ex) {
                erreur("❌ Le salaire de base doit être un nombre (ex : 5000 ou 5000.50).");
                return null;
            }
        }

        return new Employe(
            id,
            fieldNom.getText().trim(),
            fieldPrenom.getText().trim(),
            fieldEmail.getText().trim(),
            fieldTelephone.getText().trim(),
            fieldPoste.getText().trim(),
            fieldDepartement.getText().trim(),
            fieldDateEmbauche.getValue() != null ? fieldDateEmbauche.getValue().toString() : "",
            salaire,
            fieldCin.getText().trim(),
            fieldAdresse.getText().trim(),
            comboStatut.getValue() != null ? comboStatut.getValue() : Employe.Statut.ACTIF
        );
    }

    private void remplirFormulaire(Employe e) {
        if (e == null) return;
        fieldNom.setText(e.getNom());
        fieldPrenom.setText(e.getPrenom());
        fieldEmail.setText(e.getEmail());
        fieldTelephone.setText(e.getTelephone());
        fieldPoste.setText(e.getPoste());
        fieldDepartement.setText(e.getDepartement());
        try { fieldDateEmbauche.setValue(LocalDate.parse(e.getDateEmbauche())); }
        catch (Exception ignored) { fieldDateEmbauche.setValue(null); }
        fieldSalaireBase.setText(String.valueOf(e.getSalaireBase()));
        fieldCin.setText(e.getCin());
        fieldAdresse.setText(e.getAdresse());
        comboStatut.setValue(e.getStatut());
        labelMessage.setText("");
    }

    @FXML
    private void viderChamps() {
        fieldNom.clear();
        fieldPrenom.clear();
        fieldEmail.clear();
        fieldTelephone.clear();
        fieldPoste.clear();
        fieldDepartement.clear();
        fieldDateEmbauche.setValue(null);
        fieldSalaireBase.clear();
        fieldCin.clear();
        fieldAdresse.clear();
        comboStatut.setValue(Employe.Statut.ACTIF);
        fieldRecherche.clear();
        tableEmployes.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }

    // ════════════════════════ MESSAGES ════════════════════════════════════════
    private void succes(String msg) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        labelMessage.setText(msg);
    }

    private void erreur(String msg) {
        labelMessage.setStyle("-fx-text-fill: #EF4444;");
        labelMessage.setText(msg);
    }
}