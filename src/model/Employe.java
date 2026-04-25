package model;

public class Employe {

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String poste;
    private String departement;
    private String dateEmbauche;  // format : "yyyy-MM-dd"
    private double salaireBase;

    public Employe(int id, String nom, String prenom, String email,
                   String telephone, String poste, String departement,
                   String dateEmbauche, double salaireBase) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.poste = poste;
        this.departement = departement;
        this.dateEmbauche = dateEmbauche;
        this.salaireBase = salaireBase;
    }

    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDateEmbauche() { return dateEmbauche; }
    public double getSalaireBase() { return salaireBase; }

    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPoste(String poste) { this.poste = poste; }
    public void setDepartement(String departement) { this.departement = departement; }
    public void setDateEmbauche(String dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public void setSalaireBase(double salaireBase) { this.salaireBase = salaireBase; }

    @Override
    public String toString() {
        return prenom + " " + nom + " - " + poste;
    }
}