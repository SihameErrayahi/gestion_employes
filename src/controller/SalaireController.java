package controller;

import dao.EmployeDAO;
import dao.SalaireDAO;
import model.Employe;
import model.Salaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SalaireController {

    @FXML private ComboBox<Employe> employeCombo;
    @FXML private TextField moisField;
    @FXML private TextField salaireBaseField;
    @FXML private TextField primesField;
    @FXML private TextField retenuesField;
    @FXML private TextField salaireNetField;
    @FXML private TextField filtreField;
    @FXML private Label     messageLabel;
    @FXML private Label     totalLabel;

    @FXML private TableView<Salaire>            salaireTable;
    @FXML private TableColumn<Salaire, Integer> colId;
    @FXML private TableColumn<Salaire, Integer> colEmploye;
    @FXML private TableColumn<Salaire, String>  colMois;
    @FXML private TableColumn<Salaire, Double>  colBase;
    @FXML private TableColumn<Salaire, Double>  colPrimes;
    @FXML private TableColumn<Salaire, Double>  colRetenues;
    @FXML private TableColumn<Salaire, Double>  colNet;

    private SalaireDAO salaireDAO = new SalaireDAO();
    private EmployeDAO employeDAO = new EmployeDAO();
    private ObservableList<Salaire> listeSalaires = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        employeCombo.setItems(FXCollections.observableArrayList(employeDAO.getTous()));

        // Pré-remplir salaire de base selon l'employé sélectionné
        employeCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, newVal) -> {
            if (newVal != null) {
                salaireBaseField.setText(String.valueOf(newVal.getSalaireBase()));
                calculerNet();
            }
        });

        // Recalcul automatique
        salaireBaseField.textProperty().addListener((obs, o, n) -> calculerNet());
        primesField.textProperty().addListener((obs, o, n) -> calculerNet());
        retenuesField.textProperty().addListener((obs, o, n) -> calculerNet());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmploye.setCellValueFactory(new PropertyValueFactory<>("employeId"));
        colMois.setCellValueFactory(new PropertyValueFactory<>("mois"));
        colBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));
        colPrimes.setCellValueFactory(new PropertyValueFactory<>("primes"));
        colRetenues.setCellValueFactory(new PropertyValueFactory<>("retenues"));
        colNet.setCellValueFactory(new PropertyValueFactory<>("salaireNet"));

        // Coloration du salaire net
        colNet.setCellFactory(col -> new TableCell<Salaire, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.2f DH", item));
                setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
            }
        });

        chargerDonnees();
    }

    @FXML
    public void calculerNet() {
        try {
            double base     = parseDouble(salaireBaseField.getText());
            double primes   = parseDouble(primesField.getText());
            double retenues = parseDouble(retenuesField.getText());
            salaireNetField.setText(String.format("%.2f DH", base + primes - retenues));
        } catch (Exception e) {
            salaireNetField.setText("—");
        }
    }

    @FXML
    public void ajouterSalaire() {
        if (employeCombo.getValue() == null || moisField.getText().trim().isEmpty()) {
            afficherErreur("❌ Employé et mois sont obligatoires."); return;
        }
        try {
            Salaire s = new Salaire(0,
                employeCombo.getValue().getId(),
                moisField.getText().trim(),
                parseDouble(salaireBaseField.getText()),
                parseDouble(primesField.getText()),
                parseDouble(retenuesField.getText()));
            salaireDAO.ajouter(s);
            afficherSucces("✅ Salaire enregistré avec succès !");
            annuler();
            chargerDonnees();
        } catch (NumberFormatException ex) {
            afficherErreur("❌ Valeurs numériques invalides.");
        }
    }

    @FXML
    public void chargerDonnees() {
        List<Salaire> tous = salaireDAO.getTous();
        listeSalaires.setAll(tous);
        salaireTable.setItems(listeSalaires);
        calculerTotal(tous);
        messageLabel.setText("");
    }

    @FXML
    public void filtrerParMois() {
        String mois = filtreField.getText().trim();
        if (mois.isEmpty()) { chargerDonnees(); return; }
        List<Salaire> filtres = salaireDAO.getTous().stream()
            .filter(s -> s.getMois().contains(mois))
            .collect(Collectors.toList());
        listeSalaires.setAll(filtres);
        salaireTable.setItems(listeSalaires);
        calculerTotal(filtres);
    }

    @FXML
    public void supprimerSalaire() {
        Salaire sel = salaireTable.getSelectionModel().getSelectedItem();
        if (sel == null) { afficherErreur("❌ Sélectionnez un salaire à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce salaire ?");
        confirm.setContentText("Mois : " + sel.getMois() + " — Net : " + sel.getSalaireNet() + " DH");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                salaireDAO.supprimer(sel.getId());
                afficherSucces("✅ Salaire supprimé.");
                chargerDonnees();
            }
        });
    }

    @FXML
    public void annuler() {
        employeCombo.setValue(null);
        moisField.clear(); salaireBaseField.clear();
        primesField.clear(); retenuesField.clear(); salaireNetField.clear();
        salaireTable.getSelectionModel().clearSelection();
    }

    private void calculerTotal(List<Salaire> liste) {
        double total = liste.stream().mapToDouble(Salaire::getSalaireNet).sum();
        totalLabel.setText(String.format("%.2f DH", total));
    }

    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim());
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