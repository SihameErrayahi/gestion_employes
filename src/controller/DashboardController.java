package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import model.Employe;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController {

    @FXML private Label totalEmployes;
    @FXML private Label totalActifs;
    @FXML private Label congesEnAttente;
    @FXML private Label masseSalariale;
    @FXML private Label nbDepartements;
    @FXML private Label labelDate;

    @FXML private TableView<String[]>             tableDepts;
    @FXML private TableColumn<String[], String>   colDept;
    @FXML private TableColumn<String[], Integer>  colNbEmps;
    @FXML private TableColumn<String[], Double>   colMasseDept;

    @FXML
    public void initialize() {
        // Date du jour
        labelDate.setText("Aujourd'hui : " +
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy",
            		Locale.of("fr"))));

        EmployeDAO employeDAO = new EmployeDAO();
        CongeDAO   congeDAO   = new CongeDAO();

        List<Employe> tous = employeDAO.getTous();

        // Stats globales
        int nb = tous.size();
        long actifs = tous.stream().filter(e -> e.getStatut() == Employe.Statut.ACTIF).count();
        totalEmployes.setText(String.valueOf(nb));
        totalActifs.setText(actifs + " actifs / " + (nb - actifs) + " inactifs");

        long attente = congeDAO.getTous().stream()
            .filter(c -> c.getStatut() == model.Conge.Statut.EN_ATTENTE).count();
        congesEnAttente.setText(String.valueOf(attente));

        double masse = tous.stream().mapToDouble(Employe::getSalaireBase).sum();
        masseSalariale.setText(String.format("%,.0f", masse));

        // Départements
        Map<String, List<Employe>> parDept = new LinkedHashMap<>();
        for (Employe e : tous) {
            String d = (e.getDepartement() == null || e.getDepartement().isBlank())
                       ? "(Non défini)" : e.getDepartement();
            parDept.computeIfAbsent(d, k -> new ArrayList<>()).add(e);
        }
        nbDepartements.setText(String.valueOf(parDept.size()));

        // Table départements
        colDept.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue()[0]));
        colNbEmps.setCellValueFactory(cd ->
            new SimpleIntegerProperty(Integer.parseInt(cd.getValue()[1])).asObject());
        colMasseDept.setCellValueFactory(cd ->
            new SimpleDoubleProperty(Double.parseDouble(cd.getValue()[2])).asObject());

        ObservableList<String[]> rows = FXCollections.observableArrayList();
        parDept.forEach((dept, emps) -> {
            double m = emps.stream().mapToDouble(Employe::getSalaireBase).sum();
            rows.add(new String[]{ dept, String.valueOf(emps.size()), String.format("%.2f", m) });
        });
        tableDepts.setItems(rows);
    }

    @FXML private void nouvelEmploye(ActionEvent e)    { ouvrirPage("/view/employe.fxml"); }
    @FXML private void nouveauConge(ActionEvent e)     { ouvrirPage("/view/conge.fxml"); }
    @FXML private void genererSalaires(ActionEvent e)  { ouvrirPage("/view/salaire.fxml"); }

    private void ouvrirPage(String chemin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(chemin));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Employés");
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
