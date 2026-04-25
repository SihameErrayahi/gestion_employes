package model;

public class Salaire {

    private int id;
    private int employeId;
    private String mois;       // format : "yyyy-MM"
    private double salaireBase;
    private double primes;
    private double retenues;
    private double salaireNet;

    public Salaire(int id, int employeId, String mois,
                   double salaireBase, double primes, double retenues) {
        this.id = id;
        this.employeId = employeId;
        this.mois = mois;
        this.salaireBase = salaireBase;
        this.primes = primes;
        this.retenues = retenues;
        this.salaireNet = salaireBase + primes - retenues;
    }

    public int getId() { return id; }
    public int getEmployeId() { return employeId; }
    public String getMois() { return mois; }
    public double getSalaireBase() { return salaireBase; }
    public double getPrimes() { return primes; }
    public double getRetenues() { return retenues; }
    public double getSalaireNet() { return salaireNet; }

    public void setPrimes(double primes) {
        this.primes = primes;
        this.salaireNet = this.salaireBase + this.primes - this.retenues;
    }

    public void setRetenues(double retenues) {
        this.retenues = retenues;
        this.salaireNet = this.salaireBase + this.primes - this.retenues;
    }

    @Override
    public String toString() {
        return "Salaire [" + mois + "] Employé#" + employeId + " - Net: " + salaireNet;
    }
}