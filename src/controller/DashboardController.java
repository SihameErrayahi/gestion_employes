package controller;
 
import dao.CongeDAO;
import dao.EmployeDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Employe;
import model.Conge;
 
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
 
    @FXML private TableView<String[]> tableDepts;
    @FXML private TableColumn<String[], String> colDept;
    @FXML private TableColumn<String[], Integer> colNbEmps;
    @FXML private TableColumn<String[], Double> colMasseDept;
 
    @FXML
    public void initialize() {
        try {
            labelDate.setText("Aujourd'hui : " +
                    LocalDate.now().format(
                            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)
                    )
            );
 
            EmployeDAO employeDAO = new EmployeDAO();
            CongeDAO congeDAO = new CongeDAO();
 
            List<Employe> tous = employeDAO.getTous();
 
            int nb = tous.size();
 
            long actifs = tous.stream()
                    .filter(e -> e.getStatut() == Employe.Statut.ACTIF)
                    .count();
 
            totalEmployes.setText(String.valueOf(nb));
            totalActifs.setText(actifs + " actifs / " + (nb - actifs) + " inactifs");
 
            long attente = congeDAO.getTous().stream()
                    .filter(c -> c.getStatut() == Conge.Statut.EN_ATTENTE)
                    .count();
 
            congesEnAttente.setText(String.valueOf(attente));
 
            double masse = tous.stream()
                    .mapToDouble(Employe::getSalaireBase)
                    .sum();
 
            masseSalariale.setText(String.format("%,.0f", masse));
 
            Map<String, List<Employe>> parDept = new LinkedHashMap<>();
 
            for (Employe e : tous) {
                String d = e.getDepartement();
 
                if (d == null || d.isBlank()) {
                    d = "(Non défini)";
                }
 
                parDept.computeIfAbsent(d, k -> new ArrayList<>()).add(e);
            }
 
            nbDepartements.setText(String.valueOf(parDept.size()));
 
            colDept.setCellValueFactory(cd ->
                    new SimpleStringProperty(cd.getValue()[0])
            );
 
            colNbEmps.setCellValueFactory(cd ->
                    new SimpleIntegerProperty(Integer.parseInt(cd.getValue()[1])).asObject()
            );
 
            colMasseDept.setCellValueFactory(cd ->
                    new SimpleDoubleProperty(Double.parseDouble(cd.getValue()[2])).asObject()
            );
 
            ObservableList<String[]> rows = FXCollections.observableArrayList();
 
            for (Map.Entry<String, List<Employe>> entry : parDept.entrySet()) {
                String dept = entry.getKey();
                List<Employe> emps = entry.getValue();
 
                double m = emps.stream()
                        .mapToDouble(Employe::getSalaireBase)
                        .sum();
 
                rows.add(new String[]{
                        dept,
                        String.valueOf(emps.size()),
                        String.valueOf(m)
                });
            }
 
            tableDepts.setItems(rows);
 
        } catch (Exception e) {
            e.printStackTrace();
 
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Dashboard");
            alert.setHeaderText("Erreur lors du chargement du dashboard");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
 
    @FXML
    private void nouvelEmploye(ActionEvent e) {
        naviguerVers("/view/employe.fxml");
    }
 
    @FXML
    private void nouveauConge(ActionEvent e) {
        naviguerVers("/view/conge.fxml");
    }
 
    @FXML
    private void genererSalaires(ActionEvent e) {
        naviguerVers("/view/salaire.fxml");
    }
 
    /**
     * Charge la vue dans le contentArea du MainController (même fenêtre),
     * exactement comme la barre latérale.
     */
    private void naviguerVers(String chemin) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(chemin));
 
            // Remonter jusqu'au StackPane "contentArea" du MainController
            javafx.scene.Node node = labelDate; // n'importe quel nœud de la scène
            javafx.scene.layout.StackPane contentArea =
                (javafx.scene.layout.StackPane) node.getScene().lookup("#contentArea");
 
            if (contentArea != null) {
                contentArea.getChildren().setAll(vue);
            } else {
                // Fallback : ouvrir dans la même scène (racine)
                node.getScene().setRoot(vue);
            }
 
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir : " + chemin);
            alert.showAndWait();
        }
    }
}