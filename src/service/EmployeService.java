package service;

import dao.EmployeDAO;
import model.Employe;
import java.time.LocalDate;
import java.util.List;

/**
 * Service métier pour la gestion des employés.
 * Toutes les opérations retournent un {@link ResultatService} contenant :
 *  - un indicateur de succès
 *  - un message explicatif affiché à l'utilisateur
 */
public class EmployeService {

    private final EmployeDAO employeDAO = new EmployeDAO();

    // ══════════════════════════════════════════════════════════════════════════
    //  Classe interne résultat
    // ══════════════════════════════════════════════════════════════════════════
    public static class ResultatService {
        public final boolean succes;
        public final String  message;

        public ResultatService(boolean succes, String message) {
            this.succes  = succes;
            this.message = message;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AJOUTER
    // ══════════════════════════════════════════════════════════════════════════
    public ResultatService ajouterEmploye(Employe e) {

        // 1. Validation des champs
        ResultatService validation = valider(e, 0);
        if (!validation.succes) return validation;

        // 2. Vérification des doublons
        if (employeDAO.emailExiste(e.getEmail(), 0)) {
            return new ResultatService(false,
                "❌ Un employé avec l'email « " + e.getEmail() + " » existe déjà.");
        }
        if (!e.getCin().isEmpty() && employeDAO.cinExiste(e.getCin(), 0)) {
            return new ResultatService(false,
                "❌ Un employé avec le CIN « " + e.getCin() + " » existe déjà.");
        }
        if (!e.getTelephone().isEmpty() && employeDAO.telephoneExiste(e.getTelephone(), 0)) {
            return new ResultatService(false,
                "❌ Un employé avec le téléphone « " + e.getTelephone() + " » existe déjà.");
        }

        // 3. Insertion
        boolean ok = employeDAO.ajouter(e);
        return ok
            ? new ResultatService(true,  "✔ Employé ajouté avec succès.")
            : new ResultatService(false, "❌ Échec de l'ajout. Vérifiez la connexion à la base de données.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODIFIER
    // ══════════════════════════════════════════════════════════════════════════
    public ResultatService modifierEmploye(Employe e) {

        // 1. Validation des champs
        ResultatService validation = valider(e, e.getId());
        if (!validation.succes) return validation;

        // 2. Vérification des doublons (en excluant l'employé lui-même)
        if (employeDAO.emailExiste(e.getEmail(), e.getId())) {
            return new ResultatService(false,
                "❌ L'email « " + e.getEmail() + " » est déjà utilisé par un autre employé.");
        }
        if (!e.getCin().isEmpty() && employeDAO.cinExiste(e.getCin(), e.getId())) {
            return new ResultatService(false,
                "❌ Le CIN « " + e.getCin() + " » est déjà utilisé par un autre employé.");
        }
        if (!e.getTelephone().isEmpty() && employeDAO.telephoneExiste(e.getTelephone(), e.getId())) {
            return new ResultatService(false,
                "❌ Le téléphone « " + e.getTelephone() + " » est déjà utilisé par un autre employé.");
        }

        // 3. Mise à jour
        boolean ok = employeDAO.modifier(e);
        return ok
            ? new ResultatService(true,  "✔ Employé modifié avec succès.")
            : new ResultatService(false, "❌ Échec de la modification. Vérifiez la connexion à la base de données.");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SUPPRIMER
    // ══════════════════════════════════════════════════════════════════════════
    public ResultatService supprimerEmploye(int id) {
        boolean ok = employeDAO.supprimer(id);
        return ok
            ? new ResultatService(true,  "✔ Employé supprimé avec succès.")
            : new ResultatService(false, "❌ Suppression impossible (l'employé est peut-être lié à des salaires ou des congés).");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LECTURE (inchangé)
    // ══════════════════════════════════════════════════════════════════════════
    public List<Employe> getTousLesEmployes() {
        return employeDAO.getTous();
    }

    public Employe getEmployeParId(int id) {
        return employeDAO.getParId(id);
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

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION INTERNE
    // ══════════════════════════════════════════════════════════════════════════
    private ResultatService valider(Employe e, int excludeId) {
        StringBuilder erreurs = new StringBuilder();

        // Nom
        if (e.getNom() == null || e.getNom().trim().isEmpty()) {
            erreurs.append("• Le nom est obligatoire.\n");
        } else if (e.getNom().trim().length() < 2) {
            erreurs.append("• Le nom doit contenir au moins 2 caractères.\n");
        } else if (!e.getNom().trim().matches("[\\p{L}\\s'\\-]+")) {
            erreurs.append("• Le nom ne doit contenir que des lettres, espaces, apostrophes ou tirets.\n");
        }

        // Prénom
        if (e.getPrenom() == null || e.getPrenom().trim().isEmpty()) {
            erreurs.append("• Le prénom est obligatoire.\n");
        } else if (e.getPrenom().trim().length() < 2) {
            erreurs.append("• Le prénom doit contenir au moins 2 caractères.\n");
        } else if (!e.getPrenom().trim().matches("[\\p{L}\\s'\\-]+")) {
            erreurs.append("• Le prénom ne doit contenir que des lettres, espaces, apostrophes ou tirets.\n");
        }

        // Email
        if (e.getEmail() == null || e.getEmail().trim().isEmpty()) {
            erreurs.append("• L'email est obligatoire.\n");
        } else if (!e.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            erreurs.append("• L'email est invalide (exemple : prenom.nom@domaine.com).\n");
        }

        // Téléphone (optionnel mais validé si renseigné)
        if (e.getTelephone() != null && !e.getTelephone().trim().isEmpty()) {
            String tel = e.getTelephone().trim().replaceAll("[\\s\\-\\+\\(\\)]", "");
            if (!tel.matches("\\d{8,15}")) {
                erreurs.append("• Le téléphone doit contenir entre 8 et 15 chiffres.\n");
            }
        }

        // Poste
        if (e.getPoste() == null || e.getPoste().trim().isEmpty()) {
            erreurs.append("• Le poste est obligatoire.\n");
        }

        // Département
        if (e.getDepartement() == null || e.getDepartement().trim().isEmpty()) {
            erreurs.append("• Le département est obligatoire.\n");
        }

        // Date d'embauche
        if (e.getDateEmbauche() == null || e.getDateEmbauche().trim().isEmpty()) {
            erreurs.append("• La date d'embauche est obligatoire.\n");
        } else {
            try {
                LocalDate date = LocalDate.parse(e.getDateEmbauche());
                if (date.isAfter(LocalDate.now())) {
                    erreurs.append("• La date d'embauche ne peut pas être dans le futur.\n");
                }
            } catch (Exception ex) {
                erreurs.append("• La date d'embauche est invalide (format attendu : AAAA-MM-JJ).\n");
            }
        }

        // Salaire de base
        if (e.getSalaireBase() <= 0) {
            erreurs.append("• Le salaire de base doit être un nombre strictement positif.\n");
        } else if (e.getSalaireBase() > 1_000_000) {
            erreurs.append("• Le salaire de base semble incorrect (valeur supérieure à 1 000 000).\n");
        }

        // CIN (optionnel mais validé si renseigné)
        if (e.getCin() != null && !e.getCin().trim().isEmpty()) {
            if (!e.getCin().trim().matches("[A-Za-z]{1,2}\\d{5,8}")) {
                erreurs.append("• Le CIN est invalide (exemple : AB123456).\n");
            }
        }

        if (erreurs.length() > 0) {
            return new ResultatService(false, "⚠ Veuillez corriger les erreurs suivantes :\n\n" + erreurs);
        }
        return new ResultatService(true, "");
    }
}