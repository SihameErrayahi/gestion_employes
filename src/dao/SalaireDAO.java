package dao;

import model.Salaire;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaireDAO {

    public void ajouter(Salaire s) {
        String sql = "INSERT INTO salaire (employe_id, mois, salaire_base, primes, retenues, salaire_net) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.getEmployeId());
            ps.setString(2, s.getMois());
            ps.setDouble(3, s.getSalaireBase());
            ps.setDouble(4, s.getPrimes());
            ps.setDouble(5, s.getRetenues());
            ps.setDouble(6, s.getSalaireNet());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Salaire> getParEmploye(int employeId) {
        List<Salaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM salaire WHERE employe_id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Salaire(
                    rs.getInt("id"),
                    rs.getInt("employe_id"),
                    rs.getString("mois"),
                    rs.getDouble("salaire_base"),
                    rs.getDouble("primes"),
                    rs.getDouble("retenues")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public List<Salaire> getTous() {
        List<Salaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM salaire";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Salaire(
                    rs.getInt("id"),
                    rs.getInt("employe_id"),
                    rs.getString("mois"),
                    rs.getDouble("salaire_base"),
                    rs.getDouble("primes"),
                    rs.getDouble("retenues")
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM salaire WHERE id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}