package catering.persistence;

import catering.businesslogic.event.Notes;
import catering.businesslogic.holidays.Holidays;
import catering.businesslogic.staff.Collaborator;
import catering.businesslogic.staff.CollaboratorsList;
import catering.businesslogic.staff.Contact;
import catering.businesslogic.staff.Role;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.staff.StaffEventReceiver;
import catering.businesslogic.user.User;

/**
 * Implementazione di {@link StaffEventReceiver} che si occupa di
 * sincronizzare le modifiche degli oggetti del dominio Staff con il database.
 * 
 * Questa classe funge da "listener" o "adapter" tra gli eventi generati
 * dalle entità di business (Staff, Collaborator, Role, Contact, Holidays, Notes)
 * e le operazioni CRUD eseguite tramite il {@link PersistenceManager}.
 * 
 * Ogni metodo corrisponde a un evento specifico di modifica dello stato di un'entità,
 * e richiama il metodo appropriato sull'oggetto coinvolto per aggiornare il database.
 */
public class StaffPersistence implements StaffEventReceiver {

    /**
     * Gestisce la creazione di uno staff nel database.
     * @param st oggetto Staff appena creato
     */
    @Override
    public void updateStaffCreated(Staff st){
        st.saveStaff();
    }

    /**
     * Gestisce la modifica di uno staff esistente nel database.
     * @param st oggetto Staff modificato
     */
    @Override
    public void updateStaffChanged(Staff st){
        st.updateStaff();
    }

    /**
     * Gestisce la cancellazione di uno staff dal database.
     * @param st oggetto Staff da eliminare
     */
    @Override
    public void updateStaffDeleted(Staff st){
        st.deleteStaff(); 
    }

    /**
     * Associa un ruolo a un utente nel database.
     * @param user utente a cui è stato assegnato un ruolo
     * @param rol ruolo aggiunto all'utente
     */
    @Override
    public void updateRoleAdded(User user, Role rol){
        rol.saveRole(user.getId(), rol.getId());
    }

    /**
     * Rimuove l'associazione di un ruolo da un utente nel database.
     * @param user utente a cui è stato rimosso un ruolo
     * @param rol ruolo rimosso dall'utente
     */
    @Override
    public void updateRoleDeleted(User user, Role rol){
        rol.deleteRole(user.getId(), rol.getId());  
    }

    /**
     * Salva un nuovo contatto contrassegnato nel database.
     * @param contact contatto da salvare
     */
    @Override
    public void updateContactMarked(Contact contact){
        contact.saveContactMarked();
    }

    /**
     * Aggiorna un contatto esistente nel database.
     * @param contact contatto da aggiornare
     */
    @Override
    public void updateContactModify(Contact contact){
        contact.updateContact();
    }

    /**
     * Elimina un contatto dal database.
     * @param contact contatto da eliminare
     */
    @Override
    public void updateContactDeleted(Contact contact){
        contact.deleteContact(contact.getId());
    }

    /**
     * Aggiunge un collaboratore allo staff nel database.
     * @param col collaboratore da aggiungere
     */
    @Override
    public void updateCollaboratorAdded(Collaborator col){
        col.addCollaboratorToStaff(col.getName());
    }

    /**
     * Elimina un collaboratore dallo staff nel database.
     * @param col collaboratore da eliminare
     */
    @Override
    public void updateCollaboratorDeleted(Collaborator col){
        col.deleteCollaboratorFromStaff(col.getName());
    }

    /**
     * Aggiorna i dati di un collaboratore all'interno della lista collaboratori nel database.
     * @param list lista collaboratori modificata
     * @param col collaboratore aggiornato
     */
    @Override
    public void updateCollaboratorsListModified(CollaboratorsList list, Collaborator col){
        list.updateCollaboratorsList(col);
    }

    /**
     * Rimuove un collaboratore dalla lista collaboratori nel database.
     * @param list lista collaboratori modificata
     * @param col collaboratore da rimuovere
     */
    @Override
    public void updateCollaboratorDeletedFromList(CollaboratorsList list, Collaborator col){
        list.deleteCollaboratorFromList(col);
    }

    /**
     * Aggiunge un nuovo contatto al database.
     * @param contact contatto da aggiungere
     */
    @Override
    public void updateContactAdded(Contact contact){
        contact.saveContact();
    }

    /**
     * Salva un nuovo periodo di ferie nel database.
     * @param holidays ferie da salvare
     */
    @Override
    public void updateHolidaysAdded(Holidays holidays){
        holidays.saveHolidays();
    }

    /**
     * Modifica un periodo di ferie esistente nel database.
     * @param holidays ferie modificate
     */
    @Override
    public void updateHolidaysModified(Holidays holidays){
        holidays.updateHolidays();
    }

    /**
     * Elimina un periodo di ferie dal database.
     * @param holidays ferie da eliminare
     */
    @Override
    public void updateHolidaysDeleted(Holidays holidays){
        holidays.deleteHolidays();
    }

    /**
     * Salva una nuova nota nel database.
     * @param notes nota da salvare
     */
    @Override
    public void updateNotesAdded(Notes notes){
        notes.saveNotes();
    }

    /**
     * Elimina una nota dal database.
     * @param notes nota da eliminare
     */
    @Override
    public void updateNotesDeleted(Notes notes){
        notes.deleteNotes();
    }

    /**
     * Modifica una nota esistente nel database.
     * @param notes nota da modificare
     */
    @Override
    public void updateNotesModified(Notes notes){
        notes.updateNotes();
    }
}