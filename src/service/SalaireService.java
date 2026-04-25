package service;

import dao.SalaireDAO;
import model.Salaire;
import java.util.List;

public class SalaireService {

    private SalaireDAO salaireDAO = new SalaireDAO();

    public void ajouterSalaire(Salaire s) {
        salaireDAO.ajouter(s);
    }

    public List<Salaire> getSalairesParEmploye(int employeId) {
        return salaireDAO.getParEmploye(employeId);
    }

    public List<Salaire> getTousLesSalaires() {
        return salaireDAO.getTous();
    }

    public void supprimerSalaire(int id) {
        salaireDAO.supprimer(id);
    }

    public double getTotalSalairesParMois(String mois) {
        double total = 0;
        for (Salaire s : salaireDAO.getTous()) {
            if (s.getMois().equals(mois)) {
                total += s.getSalaireNet();
            }
        }
        return total;
    }
}