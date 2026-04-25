package service;

import dao.CongeDAO;
import model.Conge;
import java.util.List;

public class CongeService {

    private CongeDAO congeDAO = new CongeDAO();

    public void demanderConge(Conge c) {
        congeDAO.ajouter(c);
    }

    public List<Conge> getCongesParEmploye(int employeId) {
        return congeDAO.getParEmploye(employeId);
    }

    public List<Conge> getTousLesConges() {
        return congeDAO.getTous();
    }

    public void approuverConge(int id) {
        congeDAO.changerStatut(id, Conge.Statut.APPROUVE);
    }

    public void refuserConge(int id) {
        congeDAO.changerStatut(id, Conge.Statut.REFUSE);
    }

    public void supprimerConge(int id) {
        congeDAO.supprimer(id);
    }

    public long getNombreCongesEnAttente() {
        return congeDAO.getTous().stream()
            .filter(c -> c.getStatut() == Conge.Statut.EN_ATTENTE)
            .count();
    }
}