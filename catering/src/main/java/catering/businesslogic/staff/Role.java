package catering.businesslogic.staff;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import catering.persistence.PersistenceManager;

/**
 * Rappresenta un ruolo del personale nel sistema di gestione CatERing.
 * Contiene informazioni come l'id, il nome del ruolo e lo stato "started".
 * Fornisce metodi per caricare ruoli predefiniti dal database e per associare ruoli a utenti.
 */
public class Role {
    private int id;
    private String role;
    private boolean started;

    // Ruoli predefiniti statici
    private static final Map<String, Role> predefinedRoles = new HashMap<>();

    /**
     * Costruttore vuoto.
     * Lo stato "started" è inizializzato a false.
     */
    public Role() {
        this.started = false;
    }

    /**
     * Restituisce l'identificatore unico del ruolo.
     * @return id del ruolo
     */
    public int getId() {
        return id;
    }

    /**
     * Indica se il ruolo è stato "avviato" (started).
     * @return true se avviato, false altrimenti
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Imposta lo stato "started" del ruolo.
     * @param s nuovo stato
     * @return il valore impostato (true o false)
     */
    public boolean setStarted(boolean s){
        return started = s;
    }

    /**
     * Restituisce il nome del ruolo.
     * @return nome del ruolo
     */
    public String getRole() {
        return role;
    }

    /**
     * Carica dal database e memorizza i ruoli predefiniti ("CUOCO", "CHEF", "ORGANIZZATORE").
     * Deve essere chiamato una sola volta all'avvio per inizializzare i ruoli.
     */
    public static void initPredefinedRoles() {
        loadAndStoreRole("CUOCO");
        loadAndStoreRole("CHEF");
        loadAndStoreRole("ORGANIZZATORE");
    }

    /**
     * Metodo helper privato per caricare un ruolo dal database e memorizzarlo nella mappa.
     * @param roleName nome del ruolo da caricare
     */
    private static void loadAndStoreRole(String roleName) {
        Role r = new Role();
        String query = "SELECT * FROM Role WHERE role = ?";
        PersistenceManager.executeQuery(query, (ResultSet rs) -> {
            r.id = rs.getInt("id");
            r.role = rs.getString("role");
            r.started = rs.getBoolean("started");
        }, roleName);
        predefinedRoles.put(roleName.toLowerCase(), r);
    }

    /**
     * Restituisce il ruolo predefinito con il nome specificato.
     * @param name nome del ruolo (case insensitive)
     * @return oggetto Role corrispondente, o null se non trovato
     */
    public static Role getPredefined(String name) {
        return predefinedRoles.get(name.toLowerCase());
    }

    // Metodi di utilità diretti
    public static Role getCook() {
        return getPredefined("CUOCO");
    }

    public static Role getChef() {
        return getPredefined("CHEF");
    }

    public static Role getOrganizer() {
        return getPredefined("ORGANIZZATORE");
    }

    // equals() e hashCode() per confronto basato su id
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Role)) return false;
        Role other = (Role) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    //metodi di modifica del DB

    /**
     * Associa un ruolo a un utente nel database.
     * @param userId id dell'utente
     * @param roleId id del ruolo
     */
    public void saveRole(int userId, int roleId) {
        String query = "INSERT INTO UserRoles (user_id, role_id) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, userId, roleId);
    }

    /**
     * Rimuove l'associazione tra un ruolo e un utente nel database.
     * @param userId id dell'utente
     * @param roleId id del ruolo
     */
    public void deleteRole(int userId, int roleId) {
        String query = "DELETE FROM UserRoles WHERE user_id = ? AND role_id = ?";
        PersistenceManager.executeUpdate(query, userId, roleId);
    }
}
