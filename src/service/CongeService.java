package service;

import dao.CongeDAO;
import model.Conge;
import java.util.List;

public class CongeService {

    private CongeDAO congeDAO = new CongeDAO();

    public void enregistrerConge(Conge c) {
        congeDAO.ajouter(c);
    }

    public void modifierConge(Conge c) {
        congeDAO.modifier(c);
    }

    public List<Conge> getTousLesConges() {
        return congeDAO.getTous();
    }

    public void accepterConge(int id) {
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

    public long getNombreCongesAcceptes() {
        return congeDAO.getTous().stream()
            .filter(c -> c.getStatut() == Conge.Statut.APPROUVE)
            .count();
    }

    public long getNombreCongesRefuses() {
        return congeDAO.getTous().stream()
            .filter(c -> c.getStatut() == Conge.Statut.REFUSE)
            .count();
    }
}