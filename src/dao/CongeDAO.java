package dao;

import model.Conge;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CongeDAO {

    public void ajouter(Conge c) {
        String sql = "INSERT INTO conge (employe_id, date_debut, date_fin, nombre_jours, type, statut, motif) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, c.getEmployeId());
            ps.setString(2, c.getDateDebut());
            ps.setString(3, c.getDateFin());
            ps.setInt(4, c.getNombreJours());
            ps.setString(5, c.getType().name());
            ps.setString(6, c.getStatut().name());
            ps.setString(7, c.getMotif());
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void modifier(Conge c) {
        String sql = "UPDATE conge SET employe_id=?, date_debut=?, date_fin=?, nombre_jours=?, type=?, motif=? WHERE id=?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, c.getEmployeId());
            ps.setString(2, c.getDateDebut());
            ps.setString(3, c.getDateFin());
            ps.setInt(4, c.getNombreJours());
            ps.setString(5, c.getType().name());
            ps.setString(6, c.getMotif());
            ps.setInt(7, c.getId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean existeCongePourEmploye(int employeId, int idAExclure) {
        String sql = "SELECT COUNT(*) FROM conge WHERE employe_id = ? AND id <> ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeId);
            ps.setInt(2, idAExclure);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public List<Conge> getTous() {
        List<Conge> liste = new ArrayList<>();
        String sql = "SELECT * FROM conge ORDER BY id DESC";

        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Conge c = new Conge(
                        rs.getInt("id"),
                        rs.getInt("employe_id"),
                        rs.getString("date_debut"),
                        rs.getString("date_fin"),
                        rs.getInt("nombre_jours"),
                        Conge.Type.valueOf(rs.getString("type")),
                        rs.getString("motif")
                );

                c.setStatut(Conge.Statut.valueOf(rs.getString("statut")));
                liste.add(c);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return liste;
    }

    public List<Conge> getParEmploye(int employeId) {
        List<Conge> liste = new ArrayList<>();
        String sql = "SELECT * FROM conge WHERE employe_id = ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, employeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Conge c = new Conge(
                        rs.getInt("id"),
                        rs.getInt("employe_id"),
                        rs.getString("date_debut"),
                        rs.getString("date_fin"),
                        rs.getInt("nombre_jours"),
                        Conge.Type.valueOf(rs.getString("type")),
                        rs.getString("motif")
                );

                c.setStatut(Conge.Statut.valueOf(rs.getString("statut")));
                liste.add(c);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return liste;
    }

    public void changerStatut(int id, Conge.Statut statut) {
        String sql = "UPDATE conge SET statut = ? WHERE id = ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, statut.name());
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM conge WHERE id = ?";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void marquerCongesPasses() {
        String sql = "UPDATE conge SET statut = 'PASSE' WHERE date_fin < CURDATE() AND statut <> 'PASSE'";

        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}