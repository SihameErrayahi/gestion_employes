package dao;

import model.Employe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeDAO {

    // ─── AJOUTER ────────────────────────────────────────────────────────────
    public boolean ajouter(Employe e) {
        String sql = "INSERT INTO employe (nom, prenom, email, telephone, poste, departement, " +
                     "date_embauche, salaire_base, cin, adresse, statut) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getTelephone());
            ps.setString(5, e.getPoste());
            ps.setString(6, e.getDepartement());
            ps.setDate(7, java.sql.Date.valueOf(e.getDateEmbauche()));
            ps.setDouble(8, e.getSalaireBase());
            ps.setString(9, e.getCin());
            ps.setString(10, e.getAdresse());
            ps.setString(11, e.getStatut().name());
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ─── MODIFIER ───────────────────────────────────────────────────────────
    public boolean modifier(Employe e) {
        String sql = "UPDATE employe SET nom=?, prenom=?, email=?, telephone=?, poste=?, " +
                     "departement=?, date_embauche=?, salaire_base=?, cin=?, adresse=?, statut=? WHERE id=?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getPrenom());
            ps.setString(3, e.getEmail());
            ps.setString(4, e.getTelephone());
            ps.setString(5, e.getPoste());
            ps.setString(6, e.getDepartement());
            ps.setDate(7, java.sql.Date.valueOf(e.getDateEmbauche()));
            ps.setDouble(8, e.getSalaireBase());
            ps.setString(9, e.getCin());
            ps.setString(10, e.getAdresse());
            ps.setString(11, e.getStatut().name());
            ps.setInt(12, e.getId());
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ─── SUPPRIMER ──────────────────────────────────────────────────────────
    public boolean supprimer(int id) {
        String sql = "DELETE FROM employe WHERE id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ─── GET TOUS ───────────────────────────────────────────────────────────
    public List<Employe> getTous() {
        List<Employe> liste = new ArrayList<>();
        String sql = "SELECT * FROM employe ORDER BY nom, prenom";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(construire(rs));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    // ─── GET PAR ID ─────────────────────────────────────────────────────────
    public Employe getParId(int id) {
        String sql = "SELECT * FROM employe WHERE id = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return construire(rs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ─── RECHERCHE ──────────────────────────────────────────────────────────
    public List<Employe> rechercher(String motCle) {
        List<Employe> liste = new ArrayList<>();
        String sql = "SELECT * FROM employe WHERE " +
                     "LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR " +
                     "LOWER(poste) LIKE ? OR LOWER(departement) LIKE ? OR LOWER(email) LIKE ? " +
                     "ORDER BY nom, prenom";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String p = "%" + motCle.toLowerCase() + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(construire(rs));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    // ─── STATISTIQUES ───────────────────────────────────────────────────────
    public int compterParDepartement(String dept) {
        String sql = "SELECT COUNT(*) FROM employe WHERE departement = ?";
        try (Connection con = ConnexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dept);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        return 0;
    }

    public List<String> getDepartements() {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT DISTINCT departement FROM employe WHERE departement IS NOT NULL AND departement != '' ORDER BY departement";
        try (Connection con = ConnexionDB.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(rs.getString(1));
        } catch (Exception ex) { ex.printStackTrace(); }
        return liste;
    }

    // ─── HELPER ─────────────────────────────────────────────────────────────
    private Employe construire(ResultSet rs) throws SQLException {
        Employe.Statut statut = Employe.Statut.ACTIF;
        try {
            String s = rs.getString("statut");
            if (s != null) statut = Employe.Statut.valueOf(s);
        } catch (Exception ignored) {}

        String cin = "";
        String adresse = "";
        try { cin = rs.getString("cin"); } catch (Exception ignored) {}
        try { adresse = rs.getString("adresse"); } catch (Exception ignored) {}

        return new Employe(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("email"),
            rs.getString("telephone"),
            rs.getString("poste"),
            rs.getString("departement"),
            rs.getString("date_embauche"),
            rs.getDouble("salaire_base"),
            cin != null ? cin : "",
            adresse != null ? adresse : "",
            statut
        );
    }
}
