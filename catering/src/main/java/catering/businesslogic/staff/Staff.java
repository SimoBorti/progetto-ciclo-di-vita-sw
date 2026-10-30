package catering.businesslogic.staff;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import catering.businesslogic.CatERing;
import catering.businesslogic.holidays.Holidays;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;

/*
 * Classe che rappresenta lo staff con tutti i suoi attributi e metodi.
 */
public class Staff {
    private int id;
    private String name;
    private boolean activity;
    private boolean available;
    private boolean collaborator;
    private boolean contact;
    private User organizer;

    private final List<Role> roles;
    private final CollaboratorsList collaboratorsList;
    private final ContactList contactList;
    private final Map<Integer, List<Holidays>> holidaysList;

    public Staff(String name) {
        this.name = name;
        this.activity = false;
        this.available = true;
        this.collaborator = false;
        this.contact = false;
        this.roles = new ArrayList<>();
        this.collaboratorsList = new CollaboratorsList();
        this.contactList = new ContactList();
        this.holidaysList = new HashMap<>();
    }

    /**
     * Factory method per creare un nuovo membro dello staff associato all'utente organizzatore corrente.
     *
     * @param name nome dello staff
     * @return nuovo oggetto Staff creato
     */
    public static Staff create(String name) {
        Staff st = new Staff(name);
        st.organizer = CatERing.getInstance().getUserManager().getCurrentUser();
        return st;
    }

    /**
     * Modifica i dati principali dello staff, aggiornando solo i campi non nulli.
     *
     * @param name nuovo nome, oppure null per non modificarlo
     * @param activity nuovo stato di attività, oppure null per non modificarlo
     * @param available nuova disponibilità, oppure null per non modificarla
     * @return l'istanza modificata dello staff
     */
    public Staff changeStaff(String name, Boolean activity, Boolean available) {
        if (name != null) this.name = name;
        if (activity != null) this.activity = activity;
        if (available != null) this.available = available;
        return this;
    }

    /**
     * Verifica se questo staff ha il ruolo specificato.
     *
     * @param rol ruolo da verificare
     * @return true se il ruolo è presente nella lista dei ruoli, false altrimenti
     */
    public boolean isIncluded(Role rol) {
        return roles.contains(rol);
    }

    /**
     * Aggiunge un ruolo allo staff.
     *
     * @param rol ruolo da aggiungere
     */
    public void addRole(Role rol){
        roles.add(rol);
    }

    /**
     * Rimuove un ruolo dallo staff.
     *
     * @param rol ruolo da rimuovere
     */
    public void removeRole(Role rol){
        roles.remove(rol);
    }

    /**
     * Aggiunge un collaboratore alla lista dei collaboratori dello staff.
     *
     * @param col collaboratore da aggiungere
     */
    public void joinCollaborator(Collaborator col) {
        collaboratorsList.addCollaborator(col);
    }

    /**
     * Rimuove un collaboratore dalla lista dei collaboratori.
     *
     * @param col collaboratore da rimuovere
     */
    public void removeCollaborator(Collaborator col) {
        collaboratorsList.removeCollaborator(col);
    }

    /**
     * Aggiunge un contatto alla lista dei contatti associati allo staff.
     *
     * @param contact contatto da aggiungere
     * @return il contatto aggiunto
     */
    public Contact addContact(Contact contact) {
        contactList.addContact(contact);
        return contact;
    }

    /**
     * Aggiunge un periodo di ferie per uno staff specificato.
     *
     * @param staffId ID dello staff a cui associare le ferie
     * @param holidays oggetto Holidays da aggiungere
     */
    public void addHolidays(int staffId, Holidays holidays) {
        holidays.setStaffId(staffId);
        holidaysList.computeIfAbsent(staffId, k -> new ArrayList<>()).add(holidays);
    }

    /**
     * Modifica le date di ferie per uno staff specifico.
     *
     * @param staff_id ID dello staff
     * @param holidays oggetto Holidays da modificare
     * @param startDate nuova data di inizio ferie, o null per non modificarla
     * @param endDate nuova data di fine ferie, o null per non modificarla
     * @return l'oggetto Holidays modificato
     */
    public Holidays changeHolidays(int staff_id, Holidays holidays, LocalDate startDate, LocalDate endDate) {
        holidays.setStaffId(staff_id);
        if(startDate != null) holidays.setStartDate(startDate);
        if(endDate != null) holidays.setEndDate(endDate);
        return holidays;
    }

    /**
     * Rimuove un periodo di ferie dallo staff.
     *
     * @param holidays oggetto Holidays da rimuovere
     */
    public void removeHolidays(Holidays holidays) {
        List<Holidays> list = holidaysList.get(holidays.getStaffId());
        if (list != null) {
            list.remove(holidays);
        }
    }

    /**
     * Carica un membro dello staff dal database in base al nome.
     *
     * @param name nome dello staff da caricare
     * @return l'oggetto Staff caricato, o null se non trovato
     * @throws SQLException in caso di errore nella query
     */
    public static Staff loadByName(String name) throws SQLException {
        final Staff[] result = new Staff[1];
        String query = "SELECT * FROM Staff WHERE name = ?";
        PersistenceManager.executeQuery(query, rs -> {
            Staff s = new Staff(rs.getString("name"));
            s.setId(rs.getInt("id"));
            s.setActivity(rs.getBoolean("activity"));
            s.setAvailable(rs.getBoolean("available"));
            s.setCollaborator(rs.getBoolean("collaborator"));
            s.setContact(rs.getBoolean("contact"));
            result[0] = s;
        }, name);
        return result[0];
    }

    /**
     * Carica un membro dello staff dal database in base all'ID.
     *
     * @param id ID dello staff da caricare
     * @return l'oggetto Staff caricato, o null se non trovato
     * @throws SQLException in caso di errore nella query
     */
    public static Staff loadById(int id) throws SQLException {
        final Staff[] result = new Staff[1];
        String query = "SELECT * FROM Staff WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            Staff s = new Staff(rs.getString("name"));  // usa il costruttore con nome
            s.setId(rs.getInt("id"));
            s.setActivity(rs.getBoolean("activity"));
            s.setAvailable(rs.getBoolean("available"));
            s.setCollaborator(rs.getBoolean("collaborator"));
            s.setContact(rs.getBoolean("contact"));
            result[0] = s;
        }, id);
        return result[0];
    }

    // Getter e setter

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActivity() {
        return activity;
    }
    
    public void setActivity(boolean activity) {
        this.activity = activity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isCollaborator() {
        return collaborator;
    }

    public boolean isContact(){
        return contact;
    }

    public void setCollaborator(boolean collaborator) {
        this.collaborator = collaborator;
    }

    public void setContact(boolean contact) {
        this.contact = contact;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public CollaboratorsList getCollaboratorsList() {
        return collaboratorsList;
    }

    public ContactList getContactList() {
        return contactList;
    }

    public List<Holidays> getHolidaysList(int staffId) {
        return holidaysList.getOrDefault(staffId, Collections.emptyList());
    }

    public User getOrganizer() {
        return this.organizer;
    }

    @Override
    public String toString() {
    return "Staff{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", activity=" + activity +
            ", available=" + available +
            ", collaborator=" + collaborator +  // aggiunto nel toString
            ", organizer=" + (organizer != null ? organizer.getUserName() : "null") +
            ", roles=" + roles.size() +
            ", collaborators=" + collaboratorsList +
            ", contacts list=" + contactList +
            ", holidays=" + holidaysList.size() +
            '}';
    }

    //metodi di modifica del DB

    /**
     * Salva un nuovo membro dello staff nel database.
     *
     * @param st oggetto Staff da salvare
     * @throws RuntimeException in caso di errore SQL durante l'inserimento
     */
    public void saveStaff() {
        String sql = "INSERT INTO Staff (name, activity, available) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = PersistenceManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, this.getName());
            stmt.setBoolean(2, this.isActivity());
            stmt.setBoolean(3, this.isAvailable());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    this.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dello staff", e);
        }
    }

    /**
     * Aggiorna i dati di uno staff esistente nel database.
     *
     */
    public void updateStaff() {
        String query = "UPDATE Staff SET name = ?, activity = ?, available = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query,
                this.getName(),
                this.isActivity(),
                this.isAvailable(),
                this.getId()
        );
    }

    /**
     * Elimina uno staff dal database.
     *
     * @return true se l'eliminazione ha avuto successo, false altrimenti
     */
    public boolean deleteStaff() {
        String query = "DELETE FROM Staff WHERE id = ?";
        return PersistenceManager.executeUpdate(query, this.getId()) > 0;
    }
}
