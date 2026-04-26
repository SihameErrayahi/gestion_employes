package model;

import java.time.LocalDate;
import java.time.Period;

public class Employe {

    public enum Statut { ACTIF, INACTIF, SUSPENDU }

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String poste;
    private String departement;
    private String dateEmbauche;
    private double salaireBase;
    private String cin;
    private String adresse;
    private Statut statut;

    // Constructeur complet
    public Employe(int id, String nom, String prenom, String email,
                   String telephone, String poste, String departement,
                   String dateEmbauche, double salaireBase,
                   String cin, String adresse, Statut statut) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.poste = poste;
        this.departement = departement;
        this.dateEmbauche = dateEmbauche;
        this.salaireBase = salaireBase;
        this.cin = cin;
        this.adresse = adresse;
        this.statut = statut != null ? statut : Statut.ACTIF;
    }

    // Constructeur rétrocompatible (ancien projet)
    public Employe(int id, String nom, String prenom, String email,
                   String telephone, String poste, String departement,
                   String dateEmbauche, double salaireBase) {
        this(id, nom, prenom, email, telephone, poste, departement,
             dateEmbauche, salaireBase, "", "", Statut.ACTIF);
    }

    // Calcul ancienneté en années
    public int getAnciennete() {
        try {
            LocalDate embauche = LocalDate.parse(dateEmbauche);
            return Period.between(embauche, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getNomComplet() { return prenom + " " + nom; }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDateEmbauche() { return dateEmbauche; }
    public double getSalaireBase() { return salaireBase; }
    public String getCin() { return cin; }
    public String getAdresse() { return adresse; }
    public Statut getStatut() { return statut; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setPoste(String poste) { this.poste = poste; }
    public void setDepartement(String departement) { this.departement = departement; }
    public void setDateEmbauche(String dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public void setSalaireBase(double salaireBase) { this.salaireBase = salaireBase; }
    public void setCin(String cin) { this.cin = cin; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setStatut(Statut statut) { this.statut = statut; }

    @Override
    public String toString() {
        return prenom + " " + nom + " - " + poste;
    }
}
