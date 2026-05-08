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
}