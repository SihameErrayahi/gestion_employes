package dao;

import model.Employe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeDAO {

    public void ajouter(Employe e) {
        String sql = "INSERT INTO employe (nom, prenom, email, telephone, poste, departement, date_embauche, salaire_base) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getTelephone());
            ps.setString(5, e.getPoste());
            ps.setString(6, e.getDepartement());
            ps.setString(7, e.getDateEmbauche());
            ps.setDouble(8, e.getSalaireBase());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Employe> getTous() {
        List<Employe> liste = new ArrayList<>();
        String sql = "SELECT * FROM employe";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Employe(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("poste"),
                    rs.getString("departement"),
                    rs.getString("date_embauche"),
                    rs.getDouble("salaire_base")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public Employe getParId(int id) {
        String sql = "SELECT * FROM employe WHERE id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Employe(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("poste"),
                    rs.getString("departement"),
                    rs.getString("date_embauche"),
                    rs.getDouble("salaire_base")
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void modifier(Employe e) {
        String sql = "UPDATE employe SET nom=?, prenom=?, email=?, telephone=?, poste=?, departement=?, date_embauche=?, salaire_base=? WHERE id=?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getTelephone());
            ps.setString(5, e.getPoste());
            ps.setString(6, e.getDepartement());
            ps.setString(7, e.getDateEmbauche());
            ps.setDouble(8, e.getSalaireBase());
            ps.setInt(9, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM employe WHERE id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}