package controller;
//test
import model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label     utilisateurLabel;

    
    public void setUtilisateur(Utilisateur u) {
        utilisateurLabel.setText(u.getLogin() + " (" + u.getRole() + ")");
        showDashboard();
    }

    @FXML
    public void initialize() {
        // la vue sera chargée par setUtilisateur() après login
    }

    @FXML public void showDashboard() { chargerVue("/view/dashboard.fxml"); }
    @FXML public void showEmploye()   { chargerVue("/view/employe.fxml");   }
    @FXML public void showSalaire()   { chargerVue("/view/salaire.fxml");   }
    @FXML public void showConge()     { chargerVue("/view/conge.fxml");     }

    @FXML
    public void seDeconnecter() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Voulez-vous vous déconnecter ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
                    Stage stage = (Stage) contentArea.getScene().getWindow();
                    stage.setScene(new Scene(root, 400, 400));
                    stage.setTitle("Connexion");
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void chargerVue(String fxmlPath) {
        try {
            Parent vue = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(vue);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible de charger : " + fxmlPath);
            alert.showAndWait();
        }
    }
}
