package model;

public class Conge {

	public enum Statut {
	    EN_ATTENTE, APPROUVE, REFUSE, PASSE
	}

    public enum Type {
        ANNUEL,
        MALADIE,
        MATERNITE,
        PATERNITE,
        SANS_SOLDE,
        VACANCE,        
        MARIAGE,
        DECES,
        FORMATION,
        EXCEPTIONNEL,
        RECUPERATION
    }

    private int id;
    private int employeId;
    private String dateDebut;
    private String dateFin;
    private int nombreJours;
    private Type type;
    private Statut statut;
    private String motif;

    public Conge(int id, int employeId, String dateDebut, String dateFin,
                 int nombreJours, Type type, String motif) {
        this.id = id;
        this.employeId = employeId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreJours = nombreJours;
        this.type = type;
        this.motif = motif;
        this.statut = Statut.EN_ATTENTE;
    }

    public int getId() { return id; }
    public int getEmployeId() { return employeId; }
    public String getDateDebut() { return dateDebut; }
    public String getDateFin() { return dateFin; }
    public int getNombreJours() { return nombreJours; }
    public Type getType() { return type; }
    public Statut getStatut() { return statut; }
    public String getMotif() { return motif; }

    public void setStatut(Statut statut) { this.statut = statut; }
    public void setMotif(String motif) { this.motif = motif; }
}