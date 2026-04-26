package dao;

import model.Salaire;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaireDAO {

    public boolean ajouter(Salaire s) {
        String sql = "INSERT INTO salaire (employe_id, mois, salaire_base, primes, retenues, salaire_net) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getEmployeId());
            ps.setString(2, s.getMois());
            ps.setDouble(3, s.getSalaireBase());
            ps.setDouble(4, s.getPrimes());
            ps.setDouble(5, s.getRetenues());
            ps.setDouble(6, s.getSalaireNet());

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Salaire déjà existant pour cet employé et ce mois.");
            return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean existe(int employeId, String mois) {
        String sql = "SELECT COUNT(*) FROM salaire WHERE employe_id = ? AND mois = ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeId);
            ps.setString(2, mois);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public List<Salaire> getParEmploye(int employeId) {
        List<Salaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM salaire WHERE employe_id = ? ORDER BY mois DESC";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                liste.add(construire(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return liste;
    }

    public List<Salaire> getTous() {
        List<Salaire> liste = new ArrayList<>();
        String sql = "SELECT * FROM salaire ORDER BY mois DESC";

        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(construire(rs));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return liste;
    }

    public boolean supprimer(int id) {
        String sql = "DELETE FROM salaire WHERE id = ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Salaire s) {
        String sql = "UPDATE salaire SET employe_id=?, mois=?, salaire_base=?, primes=?, retenues=?, salaire_net=? WHERE id=?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, s.getEmployeId());
            ps.setString(2, s.getMois());
            ps.setDouble(3, s.getSalaireBase());
            ps.setDouble(4, s.getPrimes());
            ps.setDouble(5, s.getRetenues());
            ps.setDouble(6, s.getSalaireNet());
            ps.setInt(7, s.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Impossible : salaire déjà existant pour cet employé et ce mois.");
            return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Salaire construire(ResultSet rs) throws SQLException {
        return new Salaire(
                rs.getInt("id"),
                rs.getInt("employe_id"),
                rs.getString("mois"),
                rs.getDouble("salaire_base"),
                rs.getDouble("primes"),
                rs.getDouble("retenues")
        );
    }
}