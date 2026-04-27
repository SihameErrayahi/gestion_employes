package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import dao.SalaireDAO;
import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AdminDashboardController {

    @FXML private Label totalEmployesLabel;
    @FXML private Label totalRHLabel;
    @FXML private Label totalCongesLabel;
    @FXML private Label masseSalarialeLabel;

    private final EmployeDAO     employeDAO     = new EmployeDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final CongeDAO       congeDAO       = new CongeDAO();
    private final SalaireDAO     salaireDAO     = new SalaireDAO();

    @FXML
    public void initialize() {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        int nbEmployes = employeDAO.getTous().size();
        if (totalEmployesLabel != null)
            totalEmployesLabel.setText(String.valueOf(nbEmployes));

        int nbRH = utilisateurDAO.compterRH();
        if (totalRHLabel != null)
            totalRHLabel.setText(String.valueOf(nbRH));

        int nbConges = congeDAO.getTous().size();
        if (totalCongesLabel != null)
            totalCongesLabel.setText(String.valueOf(nbConges));

        double masse = salaireDAO.getTous().stream()
                .mapToDouble(s -> s.getSalaireNet())
                .sum();
        if (masseSalarialeLabel != null)
            masseSalarialeLabel.setText(String.format("%.1fK", masse / 1000));
    }

    // *** BOUTONS DASHBOARD — naviguent via le parent AdminMainController ***

    @FXML
    public void allerGestionRH() {
        naviguerVers("/view/gestion_rh.fxml");
    }

    @FXML
    public void allerEmployes() {
        naviguerVers("/view/admin_employes.fxml");
    }

    @FXML
    public void allerParametres() {
        // Paramètres nécessite l'objet admin — on passe par AdminMainController
        try {
            StackPane contentArea = (StackPane) totalEmployesLabel.getScene()
                    .lookup("#adminContentArea");
            if (contentArea != null) {
                Parent vue = FXMLLoader.load(getClass().getResource("/view/admin_parametres.fxml"));
                contentArea.getChildren().clear();
                contentArea.getChildren().add(vue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void naviguerVers(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            StackPane contentArea = (StackPane) totalEmployesLabel.getScene()
                    .lookup("#adminContentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(vue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}