package model;

public class Utilisateur {

    // Ajouter ADMIN dans l'enum
    public enum Role { RH, RESPONSABLE, ADMIN }

    private int id;
    private String login;
    private String motDePasse;
    private Role role;
    private int employeId;

    public Utilisateur(int id, String login, String motDePasse,
                       Role role, int employeId) {
        this.id = id;
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
        this.employeId = employeId;
    }

    public int getId() { return id; }
    public String getLogin() { return login; }
    public String getMotDePasse() { return motDePasse; }
    public Role getRole() { return role; }
    public int getEmployeId() { return employeId; }

    public void setLogin(String login) { this.login = login; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    public void setRole(Role role) { this.role = role; }

    
    

    public boolean isResponsable() {
        return this.role == Role.RESPONSABLE;
    }

    public boolean isRH() {
        return this.role == Role.RH;
    }

    @Override
    public String toString() {
        return login + " (" + role + ")";
    }
}