package controller;

import dao.CongeDAO;
import dao.EmployeDAO;
import model.Conge;
import model.Employe;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class CongeController {

    @FXML private TableView<Conge> tableConges;
    @FXML private TableColumn<Conge, Integer> colId;
    @FXML private TableColumn<Conge, String> colEmployeNom;
    @FXML private TableColumn<Conge, String> colDateDebut;
    @FXML private TableColumn<Conge, String> colDateFin;
    @FXML private TableColumn<Conge, Integer> colJours;
    @FXML private TableColumn<Conge, String> colType;
    @FXML private TableColumn<Conge, String> colStatut;
    @FXML private TableColumn<Conge, String> colMotif;

    @FXML private ComboBox<Employe> comboEmploye;
    @FXML private ComboBox<Conge.Type> comboType;
    @FXML private DatePicker fieldDateDebut;
    @FXML private DatePicker fieldDateFin;
    @FXML private TextField fieldMotif;
    @FXML private TextField fieldRecherche;
    @FXML private Label labelNbJours;
    @FXML private Label labelMessage;
    @FXML private Label labelStats;
    @FXML private ComboBox<String> comboFiltreStatut;

    private final CongeDAO congeDAO = new CongeDAO();
    private final EmployeDAO employeDAO = new EmployeDAO();

    private ObservableList<Conge> listeConges;
    private FilteredList<Conge> listeFiltree;
    private SortedList<Conge> listeTriee;
    private final DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {

        configurerDatePicker(fieldDateDebut);
        configurerDatePicker(fieldDateFin);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colEmployeNom.setCellValueFactory(c -> {
            Employe e = employeDAO.getParId(c.getValue().getEmployeId());
            return new SimpleStringProperty(
                    e != null ? e.getNomComplet() : "Employé ID " + c.getValue().getEmployeId()
            );
        });

        colDateDebut.setCellValueFactory(c ->
                new SimpleStringProperty(formatterDateDepuisBD(c.getValue().getDateDebut()))
        );

        colDateFin.setCellValueFactory(c ->
                new SimpleStringProperty(formatterDateDepuisBD(c.getValue().getDateFin()))
        );

        colJours.setCellValueFactory(new PropertyValueFactory<>("nombreJours"));

        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getType().name())
        );

        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        colStatut.setCellValueFactory(c -> {
            String statut = c.getValue().getStatut().name();
            if (statut.equals("APPROUVE")) statut = "ACCEPTE";
            return new SimpleStringProperty(statut);
        });
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (item.equals("EN_ATTENTE")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (item.equals("APPROUVE")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (item.equals("REFUSE")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item.equals("PASSE")) {
                        setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                    }
                }
            }
        });

        appliquerStyleRecherche(colEmployeNom);
        
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                setText(item);
                setGraphic(null);

                if (item.equals("ACCEPTE")) {
                    setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                } else if (item.equals("REFUSE")) {
                    setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                } else if (item.equals("EN_ATTENTE")) {
                    setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });

        comboEmploye.setItems(FXCollections.observableArrayList(employeDAO.getTous()));
        comboType.setItems(FXCollections.observableArrayList(Conge.Type.values()));

        comboFiltreStatut.setItems(FXCollections.observableArrayList(
                "Tous", "EN_ATTENTE", "ACCEPTE", "REFUSE", "PASSE"
        ));
        comboFiltreStatut.setValue("Tous");

        comboFiltreStatut.valueProperty().addListener(
                (obs, oldVal, newVal) -> appliquerFiltres()
        );

        fieldRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            appliquerFiltres();
            tableConges.refresh();
        });

        fieldDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fieldDateFin.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(empty || date.isBefore(newVal));
                    }
                });

                if (fieldDateFin.getValue() != null && fieldDateFin.getValue().isBefore(newVal)) {
                    fieldDateFin.setValue(null);
                }
            }
            calculerJours();
        });

        fieldDateFin.valueProperty().addListener(
                (obs, oldVal, newVal) -> calculerJours()
        );

        listeConges = FXCollections.observableArrayList();
        listeFiltree = new FilteredList<>(listeConges, p -> true);
        listeTriee = new SortedList<>(listeFiltree);
        tableConges.setItems(listeTriee);
        tableConges.setRowFactory(tv -> new TableRow<Conge>() {
            @Override
            protected void updateItem(Conge item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else if (item.getStatut() == Conge.Statut.PASSE) {
                    setStyle("-fx-background-color: #E5E7EB;");
                } else {
                    setStyle("");
                }
            }
        });
        tableConges.getSelectionModel().selectedItemProperty().addListener(
                (obs, ancien, nouveau) -> remplirFormulaire(nouveau)
        );
        tableConges.setRowFactory(tv -> new TableRow<Conge>() {
            @Override
            protected void updateItem(Conge item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else if (item.getStatut() == Conge.Statut.PASSE) {
                    setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #6B7280;");
                } else {
                    setStyle("");
                }
            }
        });
        chargerConges();
        
        
    }

    private void configurerDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date == null) return "";
                return formatDate.format(date);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.trim().isEmpty()) return null;
                return LocalDate.parse(text, formatDate);
            }
        });
    }

    private String formatterDateDepuisBD(String dateBD) {
        try {
            return LocalDate.parse(dateBD).format(formatDate);
        } catch (Exception e) {
            return dateBD;
        }
    }

    private void appliquerStyleRecherche(TableColumn<Conge, String> colonne) {
        colonne.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                setText(null);
                setGraphic(creerTexteAvecLettresJaunes(item));
                setStyle("");
            }
        });
    }

    private TextFlow creerTexteAvecLettresJaunes(String texte) {
        TextFlow flow = new TextFlow();

        String recherche = fieldRecherche == null ? "" : fieldRecherche.getText();

        if (recherche == null || recherche.trim().isEmpty()) {
            flow.getChildren().add(new Text(texte));
            return flow;
        }

        String r = recherche.toLowerCase().trim();
        String t = texte.toLowerCase();

        int i = 0;

        while (i < texte.length()) {
            int pos = t.indexOf(r, i);

            if (pos == -1) {
                flow.getChildren().add(new Text(texte.substring(i)));
                break;
            }

            if (pos > i) {
                flow.getChildren().add(new Text(texte.substring(i, pos)));
            }

            Text trouve = new Text(texte.substring(pos, pos + r.length()));
            trouve.setStyle("-fx-fill: #F59E0B; -fx-font-weight: bold;");

            flow.getChildren().add(trouve);

            i = pos + r.length();
        }

        return flow;
    }

    private void chargerConges() {

        // 🔥 AJOUT ICI (IMPORTANT)
        congeDAO.marquerCongesPasses();

        listeConges.setAll(congeDAO.getTous());

        majStats();
        appliquerFiltres();
        tableConges.refresh();
    }

    private void majStats() {
        long attente = listeConges.stream()
                .filter(c -> c.getStatut() == Conge.Statut.EN_ATTENTE)
                .count();

        long accepte = listeConges.stream()
                .filter(c -> c.getStatut() == Conge.Statut.APPROUVE)
                .count();

        long refuse = listeConges.stream()
                .filter(c -> c.getStatut() == Conge.Statut.REFUSE)
                .count();

        labelStats.setText("Total : " + listeConges.size()
                + "  |  En attente : " + attente
                + "  |  Acceptés : " + accepte
                + "  |  Refusés : " + refuse);
    }

    private void appliquerFiltres() {
        String statutFiltre = comboFiltreStatut.getValue();
        String recherche = fieldRecherche.getText();

        listeFiltree.setPredicate(c -> {

            boolean statutOK = true;

            if (statutFiltre != null && !statutFiltre.equals("Tous")) {
                if (statutFiltre.equals("ACCEPTE")) {
                    statutOK = c.getStatut().name().equals("APPROUVE");
                } else {
                    statutOK = c.getStatut().name().equals(statutFiltre);
                }
            }

            boolean rechercheOK = true;

            if (recherche != null && !recherche.trim().isEmpty()) {
                String r = recherche.toLowerCase().trim();

                Employe e = employeDAO.getParId(c.getEmployeId());
                String nomEmploye = e != null ? e.getNomComplet().toLowerCase() : "";

                rechercheOK = nomEmploye.contains(r);
            }

            return statutOK && rechercheOK;
        });
        trierParPositionRecherche();
    }

    private void calculerJours() {
        LocalDate debut = fieldDateDebut.getValue();
        LocalDate fin = fieldDateFin.getValue();

        if (debut != null && fin != null && !fin.isBefore(debut)) {
            long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
            labelNbJours.setText(jours + " jour(s)");
        } else {
            labelNbJours.setText("— jours");
        }
    }

    private void trierParPositionRecherche() {

        String recherche = fieldRecherche.getText();

        if (recherche == null || recherche.trim().isEmpty()) {
            listeTriee.setComparator(null);
            return;
        }

        String r = recherche.toLowerCase().trim();

        listeTriee.setComparator((c1, c2) -> {

            Employe e1 = employeDAO.getParId(c1.getEmployeId());
            Employe e2 = employeDAO.getParId(c2.getEmployeId());

            String nom1 = e1 != null ? e1.getNomComplet().toLowerCase() : "";
            String nom2 = e2 != null ? e2.getNomComplet().toLowerCase() : "";

            int pos1 = nom1.indexOf(r);
            int pos2 = nom2.indexOf(r);

            if (pos1 != pos2) {
                return Integer.compare(pos1, pos2);
            }

            return nom1.compareTo(nom2);
        });
    }
    
    private void remplirFormulaire(Conge c) {
        if (c == null) return;

        Employe emp = employeDAO.getParId(c.getEmployeId());
        comboEmploye.setValue(emp);
        comboType.setValue(c.getType());

        fieldDateDebut.setValue(LocalDate.parse(c.getDateDebut()));
        fieldDateFin.setValue(LocalDate.parse(c.getDateFin()));

        fieldMotif.setText(c.getMotif() != null ? c.getMotif() : "");
        calculerJours();
        labelMessage.setText("");
    }

    @FXML
    private void enregistrerConge() {

        if (!valider()) return;

        int employeId = comboEmploye.getValue().getId();
        int annee = fieldDateDebut.getValue().getYear();

        if (comboType.getValue() == Conge.Type.ANNUEL) {

            boolean aMaternite = congeDAO.getTous().stream()
                    .anyMatch(conge ->
                            conge.getEmployeId() == employeId &&
                            conge.getType() == Conge.Type.MATERNITE &&
                            LocalDate.parse(conge.getDateDebut()).getYear() == annee
                    );

            if (aMaternite) {
                erreur("⚠ Impossible : congé annuel non autorisé car maternité déjà prise cette année.");
                return;
            }
        }
        LocalDate debut = fieldDateDebut.getValue();
        LocalDate fin = fieldDateFin.getValue();

        int jours = (int) java.time.temporal.ChronoUnit.DAYS.between(debut, fin) + 1;

        

        // 🔥 CONGÉ ANNUEL (30 jours)
        if (comboType.getValue() == Conge.Type.ANNUEL) {

            int dejaPris = congeDAO.getJoursPrisAnnuel(employeId, annee);

            if (dejaPris + jours > 30) {
                erreur("⚠ Limite 30 jours de congé annuel atteinte.");
                return;
            }
        }

        // 🔥 MATERNITÉ (90 jours)
        if (comboType.getValue() == Conge.Type.MATERNITE) {

            int dejaPris = congeDAO.getJoursPrisMaternite(employeId, annee);

            if (dejaPris + jours > 90) {
                erreur("⚠ Limite 90 jours de congé maternité atteinte.");
                return;
            }
        }

        Conge c = new Conge(
                0,
                employeId,
                debut.toString(),
                fin.toString(),
                jours,
                comboType.getValue(),
                fieldMotif.getText().trim()
        );

        congeDAO.ajouter(c);
        chargerConges();
        viderFormulaire();
        succes("✔ Congé ajouté avec succès.");
        
        
    }
    @FXML
    private void modifierConge() {
        Conge selected = tableConges.getSelectionModel().getSelectedItem();

        if (selected == null) {
            erreur("⚠ Sélectionnez un congé à modifier.");
            return;
        }
        
        if (selected.getStatut() == Conge.Statut.PASSE) {
            erreur("⚠ Impossible de modifier un congé passé.");
            return;
        }

        if (selected.getStatut() == Conge.Statut.APPROUVE) {
            erreur("⚠ Impossible de modifier un congé déjà accepté.");
            return;
        }

        if (!valider()) return;

        int employeId = comboEmploye.getValue().getId();

        if (congeDAO.existeCongePourEmploye(employeId, selected.getId())) {
            erreur("⚠ Cette personne a déjà pris sa vacance. Impossible d'avoir deux congés.");
            return;
        }

        LocalDate debut = fieldDateDebut.getValue();
        LocalDate fin = fieldDateFin.getValue();

        int jours = (int) ChronoUnit.DAYS.between(debut, fin) + 1;

        Conge c = new Conge(
                selected.getId(),
                employeId,
                debut.toString(),
                fin.toString(),
                jours,
                comboType.getValue(),
                fieldMotif.getText().trim()
        );

        congeDAO.modifier(c);
        chargerConges();
        viderFormulaire();
        succes("✔ Congé modifié avec succès.");
    }

    @FXML
    private void accepterConge() {
        Conge selected = tableConges.getSelectionModel().getSelectedItem();

        if (selected == null) {
            erreur("⚠ Sélectionnez un congé à accepter.");
            return;
        }
        
        if (selected.getStatut() == Conge.Statut.PASSE) {
            erreur("⚠ Impossible d'accepter un congé passé.");
            return;
        }

        congeDAO.changerStatut(selected.getId(), Conge.Statut.APPROUVE);
        chargerConges();
        succes("✔ Congé accepté.");
    }

    @FXML
    private void refuserConge() {
        Conge selected = tableConges.getSelectionModel().getSelectedItem();

        if (selected == null) {
            erreur("⚠ Sélectionnez un congé à refuser.");
            return;
        }

        if (selected.getStatut() == Conge.Statut.PASSE) {
            erreur("⚠ Impossible de refuser un congé passé.");
            return;
        } 
        
        congeDAO.changerStatut(selected.getId(), Conge.Statut.REFUSE);
        chargerConges();
        succes("✔ Congé refusé.");
    }

    @FXML
    private void supprimerConge() {
        Conge selected = tableConges.getSelectionModel().getSelectedItem();

        if (selected == null) {
            erreur("⚠ Sélectionnez un congé à supprimer.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer ce congé ?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(rep -> {
            if (rep == ButtonType.YES) {
                congeDAO.supprimer(selected.getId());
                chargerConges();
                viderFormulaire();
                succes("✔ Congé supprimé.");
            }
        });
    }

    @FXML
    private void viderChamps() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        comboEmploye.getSelectionModel().clearSelection();
        comboType.getSelectionModel().clearSelection();
        fieldDateDebut.setValue(null);
        fieldDateFin.setValue(null);
        fieldMotif.clear();
        labelNbJours.setText("— jours");
        labelMessage.setText("");
        tableConges.getSelectionModel().clearSelection();
    }

    private boolean valider() {
        if (comboEmploye.getValue() == null) {
            erreur("⚠ Sélectionnez un employé.");
            return false;
        }

        if (comboType.getValue() == null) {
            erreur("⚠ Choisissez le type de congé.");
            return false;
        }

        if (fieldDateDebut.getValue() == null) {
            erreur("⚠ Choisissez la date de début.");
            return false;
        }

        if (fieldDateFin.getValue() == null) {
            erreur("⚠ Choisissez la date de fin.");
            return false;
        }

        if (fieldDateFin.getValue().isBefore(fieldDateDebut.getValue())) {
            erreur("⚠ La date de fin doit être après la date de début.");
            return false;
        }

        return true;
    }

    private void succes(String message) {
        labelMessage.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        labelMessage.setText(message);
    }

    private void erreur(String message) {
        labelMessage.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
        labelMessage.setText(message);
    }
}