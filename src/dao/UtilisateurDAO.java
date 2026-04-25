package dao;

import model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    // Connexion / authentification
    public Utilisateur authentifier(String login, String motDePasse) {
        String sql = "SELECT * FROM utilisateur WHERE login = ? AND mot_de_passe = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, motDePasse);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("login"),
                    rs.getString("mot_de_passe"),
                    Utilisateur.Role.valueOf(rs.getString("role")),
                    rs.getInt("employe_id")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Créer un utilisateur RH (utilisé par l'admin)
    public boolean ajouter(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (login, mot_de_passe, role, employe_id) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getLogin());
            ps.setString(2, u.getMotDePasse());
            ps.setString(3, u.getRole().name());
            ps.setInt(4, u.getEmployeId());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Récupérer tous les utilisateurs RH (pas l'admin)
    public List<Utilisateur> getTousRH() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur WHERE role = 'RH'";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("login"),
                    rs.getString("mot_de_passe"),
                    Utilisateur.Role.valueOf(rs.getString("role")),
                    rs.getInt("employe_id")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    // Supprimer un utilisateur RH par ID
    public boolean supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ? AND role = 'RH'";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Modifier le mot de passe d'un utilisateur (admin change son propre mdp)
    public boolean modifierMotDePasse(int id, String ancienMdp, String nouveauMdp) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ? AND mot_de_passe = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nouveauMdp);
            ps.setInt(2, id);
            ps.setString(3, ancienMdp);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Vérifier si un login existe déjà
    public boolean loginExiste(String login) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE login = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Compter le nombre total de RH
    public int compterRH() {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE role = 'RH'";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}