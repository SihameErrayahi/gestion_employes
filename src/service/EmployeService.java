package service;

import dao.EmployeDAO;
import model.Employe;
import java.util.List;

public class EmployeService {

    private EmployeDAO employeDAO = new EmployeDAO();

    public void ajouterEmploye(Employe e) {
        employeDAO.ajouter(e);
    }

    public List<Employe> getTousLesEmployes() {
        return employeDAO.getTous();
    }

    public Employe getEmployeParId(int id) {
        return employeDAO.getParId(id);
    }

    public void modifierEmploye(Employe e) {
        employeDAO.modifier(e);
    }

    public void supprimerEmploye(int id) {
        employeDAO.supprimer(id);
    }

    public int getNombreTotalEmployes() {
        return employeDAO.getTous().size();
    }

    public double getMasseSalariale() {
        double total = 0;
        for (Employe e : employeDAO.getTous()) {
            total += e.getSalaireBase();
        }
        return total;
    }
}