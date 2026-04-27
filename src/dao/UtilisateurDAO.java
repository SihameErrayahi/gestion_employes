package dao;

import model.Utilisateur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

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

    public boolean supprimer(int id) {
        String sql = "DELETE FROM utilisateur WHERE id = ? AND role = 'RH'";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Modifier login et/ou mot de passe d'un RH
    // Si nouveauMdp est null, on ne change pas le mot de passe
    public boolean modifierRH(int id, String nouveauLogin, String nouveauMdp) {
        String sql;
        if (nouveauMdp != null && !nouveauMdp.isEmpty()) {
            sql = "UPDATE utilisateur SET login = ?, mot_de_passe = ? WHERE id = ? AND role = 'RH'";
        } else {
            sql = "UPDATE utilisateur SET login = ? WHERE id = ? AND role = 'RH'";
        }
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nouveauLogin);
            if (nouveauMdp != null && !nouveauMdp.isEmpty()) {
                ps.setInt(2, id);
                // 3e paramètre pas nécessaire, juste 2 params
                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE utilisateur SET login = ?, mot_de_passe = ? WHERE id = ? AND role = 'RH'");
                ps2.setString(1, nouveauLogin);
                ps2.setString(2, nouveauMdp);
                ps2.setInt(3, id);
                return ps2.executeUpdate() > 0;
            } else {
                PreparedStatement ps2 = con.prepareStatement(
                    "UPDATE utilisateur SET login = ? WHERE id = ? AND role = 'RH'");
                ps2.setString(1, nouveauLogin);
                ps2.setInt(2, id);
                return ps2.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean modifierMotDePasse(int id, String ancienMdp, String nouveauMdp) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE id = ? AND mot_de_passe = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nouveauMdp);
            ps.setInt(2, id);
            ps.setString(3, ancienMdp);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

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