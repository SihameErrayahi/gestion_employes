package controller;

import dao.EmployeDAO;
import model.Employe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField posteField;
    @FXML private TextField departementField;
    @FXML private TextField dateEmbaucheField;
    @FXML private TextField salaireBaseField;
    @FXML private TextField rechercheField;
    @FXML private Label messageLabel;

    @FXML private TableView<Employe> employeTable;
    @FXML private TableColumn<Employe, Integer> colId;
    @FXML private TableColumn<Employe, String>  colNom;
    @FXML private TableColumn<Employe, String>  colPrenom;
    @FXML private TableColumn<Employe, String>  colEmail;
    @FXML private TableColumn<Employe, String>  colTelephone;
    @FXML private TableColumn<Employe, String>  colPoste;
    @FXML private TableColumn<Employe, String>  colDepartement;
    @FXML private TableColumn<Employe, Double>  colSalaire;

    private EmployeDAO employeDAO = new EmployeDAO();
    private ObservableList<Employe> listeEmployes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));

        employeTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> { if (newVal != null) remplirFormulaire(newVal); }
        );

        chargerDonnees();
    }

    @FXML
    public void chargerDonnees() {
        listeEmployes.setAll(employeDAO.getTous());
        employeTable.setItems(listeEmployes);
        messageLabel.setText("");
    }

    @FXML
    public void ajouterEmploye() {
        if (!validerFormulaire()) return;
        try {
            Employe e = new Employe(0,
                nomField.getText().trim(), prenomField.getText().trim(),
                emailField.getText().trim(), telephoneField.getText().trim(),
                posteField.getText().trim(), departementField.getText().trim(),
                dateEmbaucheField.getText().trim(),
                Double.parseDouble(salaireBaseField.getText().trim()));
            employeDAO.ajouter(e);
            afficherSucces("✅ Employé ajouté avec succès !");
            annuler();
            chargerDonnees();
        } catch (NumberFormatException ex) {
            afficherErreur("❌ Salaire invalide. Entrez un nombre.");
        }
    }

    @FXML
    public void modifierEmploye() {
        Employe sel = employeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un employé à modifier."); return; }
        if (!validerFormulaire()) return;
        try {
            sel.setNom(nomField.getText().trim());
            sel.setPrenom(prenomField.getText().trim());
            sel.setEmail(emailField.getText().trim());
            sel.setTelephone(telephoneField.getText().trim());
            sel.setPoste(posteField.getText().trim());
            sel.setDepartement(departementField.getText().trim());
            sel.setDateEmbauche(dateEmbaucheField.getText().trim());
            sel.setSalaireBase(Double.parseDouble(salaireBaseField.getText().trim()));
            employeDAO.modifier(sel);
            afficherSucces("✅ Employé modifié avec succès !");
            annuler();
            chargerDonnees();
        } catch (NumberFormatException ex) {
            afficherErreur("❌ Salaire invalide. Entrez un nombre.");
        }
    }

    @FXML
    public void supprimerEmploye() {
        Employe sel = employeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un employé à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'employé ?");
        confirm.setContentText("Supprimer " + sel.getPrenom() + " " + sel.getNom() + " ?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                employeDAO.supprimer(sel.getId());
                afficherSucces("✅ Employé supprimé.");
                chargerDonnees();
            }
        });
    }

    @FXML
    public void rechercherEmploye() {
        String terme = rechercheField.getText().toLowerCase();
        List<Employe> filtres = employeDAO.getTous().stream()
            .filter(e -> e.getNom().toLowerCase().contains(terme)
                      || e.getPrenom().toLowerCase().contains(terme)
                      || e.getPoste().toLowerCase().contains(terme)
                      || e.getEmail().toLowerCase().contains(terme))
            .collect(Collectors.toList());
        listeEmployes.setAll(filtres);
        employeTable.setItems(listeEmployes);
    }

    @FXML
    public void annuler() {
        nomField.clear(); prenomField.clear(); emailField.clear();
        telephoneField.clear(); posteField.clear(); departementField.clear();
        dateEmbaucheField.clear(); salaireBaseField.clear();
        employeTable.getSelectionModel().clearSelection();
    }

    private void remplirFormulaire(Employe e) {
        nomField.setText(e.getNom());
        prenomField.setText(e.getPrenom());
        emailField.setText(e.getEmail());
        telephoneField.setText(e.getTelephone());
        posteField.setText(e.getPoste());
        departementField.setText(e.getDepartement());
        dateEmbaucheField.setText(e.getDateEmbauche());
        salaireBaseField.setText(String.valueOf(e.getSalaireBase()));
    }

    private boolean validerFormulaire() {
        if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty()) {
            afficherErreur("❌ Nom et Prénom sont obligatoires.");
            return false;
        }
        return true;
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