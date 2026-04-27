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

public class AdminEmployesController {

    @FXML private TableView<Employe>              tableEmployes;
    @FXML private TableColumn<Employe, Integer>   colId;
    @FXML private TableColumn<Employe, String>    colNom;
    @FXML private TableColumn<Employe, String>    colPrenom;
    @FXML private TableColumn<Employe, String>    colPoste;
    @FXML private TableColumn<Employe, String>    colDepartement;
    @FXML private TableColumn<Employe, String>    colEmail;
    @FXML private TableColumn<Employe, String>    colTelephone;
    @FXML private TableColumn<Employe, String>    colDateEmbauche;
    @FXML private TableColumn<Employe, Double>    colSalaireBase;

    @FXML private TextField rechercheField;
    @FXML private Label     totalLabel;
    @FXML private Label     detailLabel;

    private final EmployeDAO employeDAO = new EmployeDAO();
    private ObservableList<Employe> tousLesEmployes;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departement"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateEmbauche.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        colSalaireBase.setCellValueFactory(new PropertyValueFactory<>("salaireBase"));

        chargerEmployes();

        // Afficher le détail quand on clique sur un employé
        tableEmployes.getSelectionModel().selectedItemProperty().addListener((obs, ancien, e) -> {
            if (e != null) {
                detailLabel.setText(
                    e.getPrenom() + " " + e.getNom()
                    + "  |  Poste : " + e.getPoste()
                    + "  |  Département : " + e.getDepartement()
                    + "  |  Embauché le : " + e.getDateEmbauche()
                    + "  |  Salaire base : " + e.getSalaireBase() + " DH"
                );
            }
        });
    }

    private void chargerEmployes() {
        List<Employe> liste = employeDAO.getTous();
        tousLesEmployes = FXCollections.observableArrayList(liste);
        tableEmployes.setItems(tousLesEmployes);
        if (totalLabel != null)
            totalLabel.setText("Total employes : " + liste.size());
    }

    // *** RECHERCHE EN TEMPS RÉEL ***

    @FXML
    public void rechercherEmploye() {
        String terme = rechercheField.getText().trim().toLowerCase();
        if (terme.isEmpty()) {
            tableEmployes.setItems(tousLesEmployes);
            return;
        }
        List<Employe> filtres = tousLesEmployes.stream()
            .filter(e ->
                e.getNom().toLowerCase().contains(terme)
                || e.getPrenom().toLowerCase().contains(terme)
                || e.getPoste().toLowerCase().contains(terme)
                || e.getDepartement().toLowerCase().contains(terme)
                || e.getEmail().toLowerCase().contains(terme)
            )
            .collect(Collectors.toList());

        tableEmployes.setItems(FXCollections.observableArrayList(filtres));
        if (totalLabel != null)
            totalLabel.setText("Résultats : " + filtres.size() + " employé(s)");
    }

    @FXML
    public void reinitialiserRecherche() {
        rechercheField.clear();
        tableEmployes.setItems(tousLesEmployes);
        if (totalLabel != null)
            totalLabel.setText("Total employés : " + tousLesEmployes.size());
        detailLabel.setText("");
    }
}