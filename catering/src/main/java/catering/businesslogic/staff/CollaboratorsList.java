package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import catering.persistence.PersistenceManager;

/**
 * Classe che rappresenta la lista dei collaboratori.
 * Tiene traccia della data di creazione e della lista stessa.
 */
public class CollaboratorsList {
    private final Date creationDate;
    private final List<Collaborator> collaborators;

    /**
     * Costruttore che inizializza la lista e imposta la data di creazione a quella attuale.
     */
    public CollaboratorsList() {
        this.creationDate = new Date();
        this.collaborators = new ArrayList<>();
    }

    /**
     * Aggiunge un collaboratore alla lista.
     * 
     * @param collaborator il collaboratore da aggiungere
     */
    public void addCollaborator(Collaborator collaborator) {
        collaborators.add(collaborator);
    }

    /**
     * Rimuove un collaboratore dalla lista.
     * 
     * @param collaborator il collaboratore da rimuovere
     * @return true se il collaboratore è stato rimosso, false altrimenti
     */
    public boolean removeCollaborator(Collaborator collaborator) {
        return collaborators.remove(collaborator);
    }

    /**
     * Imposta il collaboratore come accettato, se presente nella lista.
     * 
     * @param collaborator il collaboratore da accettare
     * @return true se il collaboratore è stato accettato, false se non era presente
     */
    public boolean acceptCollaborator(Collaborator collaborator) {
        if (collaborators.contains(collaborator)) {
            collaborator.accept();
            return true;
        }
        return false;
    }

    /**
     * Modifica i dettagli di un collaboratore nella lista in base ai parametri forniti.
     * 
     * @param list lista di collaboratori (non usata nel metodo ma passata)
     * @param col collaboratore da modificare
     * @param name nuovo nome, se non null
     * @param phoneNumber nuovo numero di telefono, se non null
     * @param accepted nuovo stato di accettazione, se non null
     * @param disposable nuovo stato disposable, se non null
     * @return l'istanza corrente di CollaboratorsList (this)
     */
    public CollaboratorsList changeCollaboratorsList(CollaboratorsList list, Collaborator col, String name, String phoneNumber, Boolean accepted, Boolean disposable){
        if(name!=null) col.setName(name);
        if(phoneNumber!=null) col.setPhoneNumber(phoneNumber);
        if(accepted!=null) col.setAccepted(accepted);
        if(disposable!=null) col.setDisposable(true);
        return this;
    }

    /**
     * Controlla se un collaboratore con il nome specificato è presente nella lista.
     * 
     * @param name nome del collaboratore da cercare
     * @return true se presente, false altrimenti
     */
    public boolean containsByName(String name) {
        for (Collaborator c : collaborators) {
            if (c.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Controlla se un collaboratore è già presente nella lista.
     * 
     * @param collaborator collaboratore da controllare
     * @return true se presente, false altrimenti
     */
    public boolean isCollaboratorAdded(Collaborator collaborator) {
        return collaborators.contains(collaborator);
    }

    /**
     * Restituisce la lista completa dei collaboratori.
     * 
     * @return lista di collaboratori
     */
    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    /**
     * Restituisce la data di creazione della lista.
     * 
     * @return data di creazione
     */
    public Date getCreationDate() {
        return creationDate;
    }

    //metodi di modifica del DB

    /**
     * Aggiorna i dati di un collaboratore nel database.
     * 
     * @param col collaboratore da aggiornare
     */
    public void updateCollaboratorsList(Collaborator col){
        String query = "UPDATE CollaboratorsList SET name = ?, phone_number = ?, accepted = ?, disposable = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, 
        col.getName(),
        col.getPhoneNumber(),
        col.isAccepted(),
        col.isDisposable(),
        col.getId()
        );
    }

    /**
     * Elimina un collaboratore dalla tabella CollaboratorsList nel database.
     * 
     * @param col collaboratore da eliminare
     */
    public void deleteCollaboratorFromList(Collaborator col){
        String query = "DELETE FROM CollaboratorsList WHERE id = ?";
        PersistenceManager.executeUpdate(query, col.getId());
    }
}