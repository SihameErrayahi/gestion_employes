package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import dao.SalaireDAO;
import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label totalEmployesLabel;
    @FXML private Label totalRHLabel;
    @FXML private Label totalCongesLabel;
    @FXML private Label masseSalarialeLabel;

    private final EmployeDAO    employeDAO    = new EmployeDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final CongeDAO      congeDAO      = new CongeDAO();
    private final SalaireDAO    salaireDAO    = new SalaireDAO();

    @FXML
    public void initialize() {
        chargerStatistiques();
    }

    private void chargerStatistiques() {
        // Nombre total d'employés
        int nbEmployes = employeDAO.getTous().size();
        if (totalEmployesLabel != null) totalEmployesLabel.setText(String.valueOf(nbEmployes));

        // Nombre de comptes RH actifs
        int nbRH = utilisateurDAO.compterRH();
        if (totalRHLabel != null) totalRHLabel.setText(String.valueOf(nbRH));

        // Congés en cours (utiliser vos DAO existants)
        int nbConges = congeDAO.getTous().size();
        if (totalCongesLabel != null) totalCongesLabel.setText(String.valueOf(nbConges));

        // Masse salariale (somme des salaires de base)
        double masseSalariale = salaireDAO.getTous().stream()
        	.mapToDouble(s -> s.getSalaireNet())
            .sum();
        if (masseSalarialeLabel != null) {
            masseSalarialeLabel.setText(String.format("%.1fK", masseSalariale / 1000));
        }
    }

    // Actions rapides du dashboard (boutons)
    @FXML
    public void allerGestionRH() {
        // Naviguer vers gestion RH via le parent AdminMainController
        // Cette navigation est gérée par AdminMainController
    }

    @FXML
    public void allerParametres() {
        // Naviguer vers paramètres via le parent AdminMainController
    }
}