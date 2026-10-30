package catering.businesslogic.staff;

import java.sql.SQLException;
import java.util.Objects;

import catering.persistence.PersistenceManager;

/**
 * Classe che rappresenta un contatto.
 * Contiene informazioni anagrafiche e di interesse.
 */
public class Contact {
    private int id;
    private String name;
    private String address;
    private String fiscalCode;
    private String phoneNumber;
    private boolean interested;

    /**
     * Costruttore completo per creare un contatto con tutte le informazioni.
     * 
     * @param name nome del contatto
     * @param address indirizzo del contatto
     * @param fiscalCode codice fiscale (identificativo univoco)
     * @param phoneNumber numero di telefono
     * @param interested stato di interesse (true se interessato)
     */
    public Contact(String name, String address, String fiscalCode, String phoneNumber, boolean interested) {
        this.name = name;
        this.address = address;
        this.fiscalCode = fiscalCode;
        this.phoneNumber = phoneNumber;
        this.interested = interested;
    }

    /**
     * Verifica se questo contatto è già presente nella lista fornita.
     * 
     * @param list lista di contatti
     * @return true se il contatto è presente, false altrimenti
     */
    public boolean isAdded(ContactList list) {
        return list.getContacts().contains(this);
    }

    /**
     * Verifica se il contatto fa parte dello staff, controllando il codice fiscale.
     * 
     * @param st oggetto Staff
     * @param fiscal_code codice fiscale da cercare
     * @return true se il contatto è presente nello staff, false altrimenti
     */
    public boolean isPartOf(Staff st, String fiscal_code) {
        return st.getContactList().hasContactNamed(fiscal_code);
    }

    /**
     * Modifica le informazioni del contatto. I parametri null indicano che non si vuole modificare il relativo campo.
     * 
     * @param name nuovo nome, se non null
     * @param contact oggetto contatto da modificare (non usato in questo metodo)
     * @param list lista contatti (non usata nel metodo)
     * @param address nuovo indirizzo, se non null
     * @param fiscalCode nuovo codice fiscale, se non null
     * @param phoneNumber nuovo numero di telefono, se non null
     * @param interested nuovo stato di interesse, se non null
     * @return l'oggetto Contact modificato (this)
     */
    public Contact changeContact(String name, Contact contact, ContactList list, String address, String fiscalCode, String phoneNumber, Boolean interested) {
        if (name != null) this.name = name;        
        if (address != null) this.address = address;
        if (fiscalCode != null) this.fiscalCode = fiscalCode;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        if (interested != null) this.interested = interested;
        return this;
    }

    /**
     * Carica un contatto dal database dato il suo id.
     * 
     * @param id identificatore del contatto
     * @return oggetto Contact se trovato, null altrimenti
     * @throws SQLException in caso di errore SQL
     */
    public static Contact loadById(int id) throws SQLException {
        final Contact[] result = new Contact[1];
        String query = "SELECT * FROM ContactList WHERE id = ?";

        PersistenceManager.executeQuery(query, rs -> {
            // Qui dentro viene chiamato ogni volta che c'è una riga nel ResultSet
            if (result[0] == null) { // prendiamo solo la prima riga, come previsto
                Contact c = new Contact(
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("fiscal_code"),
                    rs.getString("phone_number"),
                    rs.getBoolean("interested")
                );
                c.setId(rs.getInt("id"));
                result[0] = c;
            }
        }, id);

        return result[0];  // Può essere null se non trovato
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return fiscalCode.equals(contact.fiscalCode); // o confronta anche altri campi chiave
    }

    @Override
    public int hashCode() {
        return Objects.hash(fiscalCode); // oppure altri campi chiave
    }


    // Getter e setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getFiscalCode() { return fiscalCode; }
    public void setFiscalCode(String fiscalCode) { this.fiscalCode = fiscalCode; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public boolean isInterested() { return interested; }
    public void setInterested(Boolean interested) { this.interested = interested; } //con la B maiuscola perché accetta la possibilità di essere null e quindi si può fare il controllo sui metodi di modifica


    //metodi di modifica del DB

    /**
     * Inserisce un nuovo contatto nella tabella ContactList.
     */
    public void saveContactMarked(){
        String query = "INSERT INTO ContactList (name, address, fiscal_code, phone_number, interested) VALUES (?, ?, ?, ?, ?)";
        PersistenceManager.executeUpdate(query,
            this.getName(),
            this.getAddress(),
            this.getFiscalCode(),
            this.getPhoneNumber(),
            this.isInterested()
        );
    }

    /**
     * Aggiorna i dati di un contatto esistente nel database.
     * 
     * @param contact contatto con dati aggiornati
     */
    public void updateContact() {
        String query = "UPDATE ContactList SET name = ?, address = ?, fiscal_code = ?, phone_number = ?, interested = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query,
                this.getName(),
                this.getAddress(),
                this.getFiscalCode(),
                this.getPhoneNumber(),
                this.isInterested(),
                this.getId()
        );
    }

    /**
     * Elimina un contatto dal database dato il suo id.
     * 
     * @param contactId id del contatto da eliminare
     */
    public void deleteContact(int contactId) {
        String query = "DELETE FROM ContactList WHERE id = ?";
        PersistenceManager.executeUpdate(query, contactId);
    }
    
    /**
     * Sposta un contatto nella tabella Staff come contatto attivo,
     * eliminandolo dalla lista contatti.
     */
    public void saveContact() {
        String insertQuery = "INSERT INTO Staff (name, contact) VALUES (?, ?)";
        PersistenceManager.executeUpdate(insertQuery, this.getName(), true);

        String deleteQuery = "DELETE FROM ContactList WHERE fiscal_code = ?";
        PersistenceManager.executeUpdate(deleteQuery, this.getFiscalCode());
    }
}
