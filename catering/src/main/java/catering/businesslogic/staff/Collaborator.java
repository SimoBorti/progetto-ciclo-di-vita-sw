package catering.businesslogic.staff;

import catering.persistence.PersistenceManager;

/*
 * Classe che rappresenta un collaboratore.
 */
public class Collaborator {
    private int id;
    private String name;
    private String phoneNumber;
    private boolean accepted;
    private boolean disposable;

    /**
     * Costruttore vuoto, imposta accepted e disposable a false.
     */
    public Collaborator() {
        this.accepted = false;
        this.disposable = false;
    }

    /**
     * Costruttore completo per creare un collaboratore con tutti i campi.
     *
     * @param name nome del collaboratore
     * @param phoneNumber numero di telefono
     * @param accepted stato di accettazione
     * @param disposable indica se è un collaboratore usa e getta
     */
    public Collaborator(String name, String phoneNumber, boolean accepted, boolean disposable) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.accepted = accepted;
        this.disposable = disposable;
    }

    /**
     * Costruttore che imposta solo il nome del collaboratore.
     *
     * @param name nome del collaboratore
     */
    public Collaborator(String name) {
        this.name = name;
    }


    /**
     * Imposta il collaboratore come accettato.
     */
    public void accept() {
        this.accepted = true;
    }

    /**
     * Rende il collaboratore disponibile (usa e getta).
     */
    public void makeDisposable() {
        this.disposable = true;
    }

    /**
     * Verifica se un collaboratore con un certo nome fa parte dello staff specificato.
     *
     * @param st staff di riferimento
     * @param name nome da verificare
     * @return true se il collaboratore è nella lista dei collaboratori dello staff
     */
    public boolean isPartOf(Staff st, String name) {
        return st.getCollaboratorsList().containsByName(name);
    }

    /**
     * Verifica se questo collaboratore è presente nella lista specificata.
     *
     * @param list lista di collaboratori da controllare
     * @return true se è presente nella lista, false altrimenti
     */
    public boolean isInList(CollaboratorsList list) {
        for (Collaborator c : list.getCollaborators()) {
            if (this.equals(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collaborator that = (Collaborator) o;

        // Puoi decidere tu se confrontare solo l'id (più robusto) oppure anche altri campi
        return this.name.equals(that.name) &&
            ((this.phoneNumber == null && that.phoneNumber == null) || 
                (this.phoneNumber != null && this.phoneNumber.equals(that.phoneNumber)));
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
        return result;
    }

    // Getter e Setter
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isDisposable() {
        return disposable;
    }

    public void setDisposable(boolean disposable) {
        this.disposable = disposable;
    }

    //metodi di modifica del DB

    /**
     * Aggiunge un collaboratore come staff nel database,
     * eliminandolo dalla lista collaboratori.
     *
     * @param collaboratorName nome del collaboratore da aggiungere allo staff
     */
    public void addCollaboratorToStaff(String collaboratorName){
        String insertQuery = "INSERT INTO Staff (name, collaborator) VALUES (?, ?)";
        PersistenceManager.executeUpdate(insertQuery, collaboratorName, true);

        String deleteQuery = "DELETE FROM CollaboratorsList WHERE name = ?";
        PersistenceManager.executeUpdate(deleteQuery, collaboratorName);
    }

    /**
     * Rimuove un collaboratore dallo staff e lo reinserisce nella lista collaboratori.
     *
     * @param name nome dello staff da cui rimuovere il collaboratore
     */
    public void deleteCollaboratorFromStaff(String name){
        String insertQuery = "INSERT INTO CollaboratorsList (name, phone_number, accepted, disposable) VALUES (?,?,?,?)";
        PersistenceManager.executeUpdate(insertQuery, 
        this.getName(),
        this.getPhoneNumber(),
        this.isAccepted(),
        this.isDisposable()        
        );

        String query = "DELETE FROM Staff WHERE name = ? AND collaborator = 1";
        PersistenceManager.executeUpdate(query, name);
    }

}