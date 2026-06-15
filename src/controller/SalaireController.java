package controller;

import dao.EmployeDAO;
import dao.SalaireDAO;
import model.Employe;
import model.Salaire;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class SalaireController {

    @FXML private TableView<Salaire>            tableSalaires;
    @FXML private TableColumn<Salaire, Integer> colId;
    @FXML private TableColumn<Salaire, String>  colEmployeNom;
    @FXML private TableColumn<Salaire, String>  colMois;
    @FXML private TableColumn<Salaire, Double>  colBase;
    @FXML private TableColumn<Salaire, Double>  colPrimes;
    @FXML private TableColumn<Salaire, Double>  colRetenues;
    @FXML private TableColumn<Salaire, Double>  colNet;

    @FXML private ComboBox<Employe> comboEmploye;
    
    @FXML private TextField         fieldMois;
    @FXML private TextField         fieldBase;
    @FXML private TextField         fieldPrimes;
    @FXML private TextField         fieldRetenues;
    @FXML private Label             labelNet;
    @FXML private Label             labelMessage;
    @FXML private Label             labelStats;
    @FXML private TextField fieldRecherche;

    private final SalaireDAO salaireDAO = new SalaireDAO();
    private final EmployeDAO employeDAO = new EmployeDAO();

    private ObservableList<Salaire> listeSalaires;
    private FilteredList<Salaire>   listeFiltree;

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colEmployeNom.setCellValueFactory(c -> {
            Employe e = employeDAO.getParId(c.getValue().getEmployeId());
            return new SimpleStringProperty(e != null ? e.getNomComplet() : "ID:" + c.getValue().getEmployeId());
        });

        colMois.setCellValueFactory(new PropertyValueFactory<>("mois"));
        colBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));
        colPrimes.setCellValueFactory(new PropertyValueFactory<>("primes"));
        colRetenues.setCellValueFactory(new PropertyValueFactory<>("retenues"));
        colNet.setCellValueFactory(new PropertyValueFactory<>("salaireNet"));

        comboEmploye.setItems(FXCollections.observableArrayList(employeDAO.getTous()));

         // Calcul net en temps réel
        fieldBase.textProperty().addListener((o, a, b) -> calculerNet());
        fieldPrimes.textProperty().addListener((o, a, b) -> calculerNet());
        fieldRetenues.textProperty().addListener((o, a, b) -> calculerNet());

        // Remplissage auto salaire de base depuis l'employé sélectionné
        comboEmploye.valueProperty().addListener((obs, ancien, nouveau) -> {
            if (nouveau != null) {
                fieldBase.setText(String.valueOf(nouveau.getSalaireBase()));
            }
        });

        listeSalaires = FXCollections.observableArrayList();
        listeFiltree  = new FilteredList<>(listeSalaires, p -> true);
        tableSalaires.setItems(listeFiltree);
        fieldRecherche.textProperty().addListener((obs, oldValue, newValue) -> {

            String recherche = newValue.toLowerCase().trim();

            listeFiltree.setPredicate(salaire -> {

                if (recherche.isEmpty()) {
                    return true;
                }

                Employe emp = employeDAO.getParId(salaire.getEmployeId());

                if (emp == null) {
                    return false;
                }

                return emp.getNomComplet()
                          .toLowerCase()
                          .contains(recherche);
            });
        });

        tableSalaires.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancien, nouveau) -> remplirFormulaire(nouveau));

        chargerSalaires();
    }

    private void chargerSalaires() {
        listeSalaires.setAll(salaireDAO.getTous());
        majStats();
    }

    private void majStats() {
        double total = listeSalaires.stream().mapToDouble(Salaire::getSalaireNet).sum();
        labelStats.setText("Nb fiches : " + listeSalaires.size() +
            "  |  Total net : " + String.format("%,.0f DH", total));
    }

    private void calculerNet() {
        try {
            double base     = parseD(fieldBase.getText());
            double primes   = parseD(fieldPrimes.getText());
            double retenues = parseD(fieldRetenues.getText());
            double net = base + primes - retenues;
            labelNet.setText(String.format("%,.2f DH", net));
            labelNet.setStyle("-fx-font-size:18px; -fx-font-weight:bold; "
                + (net >= 0 ? "-fx-text-fill:#10B981;" : "-fx-text-fill:#EF4444;"));
        } catch (Exception e) {
            labelNet.setText("— DH");
        }
    }

    private void filtrerParEmploye(String nomComplet) {
        if (nomComplet == null || nomComplet.equals("Tous les employés")) {
            listeFiltree.setPredicate(p -> true);
        } else {
            listeFiltree.setPredicate(s -> {
                Employe e = employeDAO.getParId(s.getEmployeId());
                return e != null && e.getNomComplet().equals(nomComplet);
            });
        }
    }

    private void remplirFormulaire(Salaire s) {
        if (s == null) return;
        Employe e = employeDAO.getParId(s.getEmployeId());
        comboEmploye.setValue(e);
        fieldMois.setText(s.getMois());
        fieldBase.setText(String.valueOf(s.getSalaireBase()));
        fieldPrimes.setText(String.valueOf(s.getPrimes()));
        fieldRetenues.setText(String.valueOf(s.getRetenues()));
        labelMessage.setText("");
    }

    private void viderFormulaire() {
        comboEmploye.getSelectionModel().clearSelection();
        fieldMois.clear(); fieldBase.clear();
        fieldPrimes.clear(); fieldRetenues.clear();
        labelNet.setText("— DH");
        tableSalaires.getSelectionModel().clearSelection();
        labelMessage.setText("");
    }

    @FXML
    private void ajouterSalaire() {
        if (!valider()) return;

        try {
            int empId = comboEmploye.getValue().getId();
            String mois = fieldMois.getText().trim();

            // ✅ AJOUT IMPORTANT
            if (salaireDAO.existe(empId, mois)) {
                erreur("⚠ Cet employé a déjà un salaire pour ce mois !");
                return;
            }

            Salaire s = new Salaire(
                0,
                empId,
                mois,
                parseD(fieldBase.getText()),
                parseD(fieldPrimes.getText()),
                parseD(fieldRetenues.getText())
            );

            boolean ok = salaireDAO.ajouter(s);

            if (ok) {
                chargerSalaires();
                viderFormulaire();
                succes("✔ Salaire ajouté avec succès.");
            } else {
                erreur("❌ Erreur lors de l'ajout.");
            }

        } catch (Exception ex) {
            erreur("❌ Erreur : " + ex.getMessage());
        }
    }

    @FXML
    private void supprimerSalaire() {
        Salaire sel = tableSalaires.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un salaire."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer ce salaire ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                salaireDAO.supprimer(sel.getId());
                chargerSalaires();
                viderFormulaire();
                succes("✔ Salaire supprimé.");
            }
        });
    }

    @FXML private void viderChamps() { viderFormulaire(); }

    private boolean valider() {
        if (comboEmploye.getValue() == null) {
            erreur("⚠ Sélectionnez un employé.");
            return false;
        }

        String mois = fieldMois.getText().trim();

        if (!mois.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            erreur("⚠ Mois invalide. Format : yyyy-MM (ex: 2026-04)");
            return false;
        }

        double base, primes, retenues;

        try {
            base = parseD(fieldBase.getText());
            primes = parseD(fieldPrimes.getText());
            retenues = parseD(fieldRetenues.getText());
        } catch (Exception e) {
            erreur("⚠ Les montants doivent être des nombres.");
            return false;
        }

        if (base <= 0) {
            erreur("⚠ Salaire de base invalide.");
            return false;
        }

        if (primes < 0) {
            erreur("⚠ Primes négatives interdites.");
            return false;
        }

        if (retenues < 0) {
            erreur("⚠ Retenues négatives interdites.");
            return false;
        }

        if (retenues > base + primes) {
            erreur("⚠ Retenues trop grandes.");
            return false;
        }
     // Salaire de base
        if (base < 3000) {
            erreur("⚠ Le salaire de base ne peut pas être inférieur à 3000 DH.");
            return false;
        }

        if (base > 100000) {
            erreur("⚠ Salaire de base trop élevé.");
            return false;
        }
     // Primes
        if (primes > base) {
            erreur("⚠ Les primes ne peuvent pas dépasser le salaire de base.");
            return false;
        }
     // Retenues
        if (retenues > (base + primes) * 0.8) {
            erreur("⚠ Les retenues dépassent 80% du salaire.");
            return false;
        }
     // Salaire net
        double net = base + primes - retenues;

        if (net <= 0) {
            erreur("⚠ Le salaire net doit être positif.");
            return false;
        }

        return true;
    }

    private double parseD(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        return Double.parseDouble(s.trim());
    }

    private void succes(String msg) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        labelMessage.setText(msg);
    }
    private void erreur(String msg) {
        labelMessage.setStyle("-fx-text-fill: #EF4444;");
        labelMessage.setText(msg);
    }
    @FXML
    private void modifierSalaire() {
        Salaire sel = (Salaire) tableSalaires.getSelectionModel().getSelectedItem();

        if (sel == null) {
            erreur("⚠ Sélectionnez un salaire à modifier.");
            return;
        }

        if (!valider()) return;

        try {
            Salaire s = new Salaire(
                    sel.getId(),
                    ((Employe) comboEmploye.getValue()).getId(),
                    fieldMois.getText().trim(),
                    parseD(fieldBase.getText()),
                    parseD(fieldPrimes.getText()),
                    parseD(fieldRetenues.getText())
            );

            boolean ok = salaireDAO.modifier(s);

            if (ok) {
                chargerSalaires();
                viderFormulaire();
                succes("✔ Salaire modifié avec succes.");
            } else {
                erreur("❌ Modification impossible.");
            }

        } catch (Exception ex) {
            erreur("❌ Erreur : " + ex.getMessage());
        }
    }
    
}
