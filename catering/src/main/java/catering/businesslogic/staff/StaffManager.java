package catering.businesslogic.staff;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.event.Notes;
import catering.businesslogic.holidays.Holidays;
import catering.businesslogic.user.User;

/**
 * La classe {@code StaffManager} rappresenta il controller principale per la gestione
 * del personale nello scenario applicativo. Si occupa di ricevere le istruzioni 
 * dall'interfaccia utente o da altri componenti, delegandole alla logica di dominio.
 * <p>
 * Inoltre, gestisce un sistema di osservatori per notificare i cambiamenti di stato
 * relativi a staff, contatti e ruoli.
 * <p>
 * I metodi {@code notifyX} gestiscono la comunicazione tra il manager e i receiver registrati,
 * secondo il pattern Observer, e non richiedono documentazione individuale.
 */
public class StaffManager {
    private Staff currentStaff;
    private final List<StaffEventReceiver> receivers;
    private Event currentEvent;

    /**
     * Imposta lo staff attualmente in uso.
     *
     * @param staff lo staff da associare come staff corrente
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }

    /**
     * Imposta l'evento attualmente in uso.
     *
     * @param e l'evento da associare come evento corrente
     */
    public void setCurrentEvent(Event e) {
        currentEvent = e;
    }

    /**
     * Costruisce un nuovo {@code StaffManager}, inizializzando la lista dei receiver
     * e la persistenza del personale.
     */
    public StaffManager() {
        receivers = new ArrayList<>();
    }

    /**
     * Registra un nuovo {@code StaffEventReceiver} per ricevere notifiche sugli eventi
     * relativi al personale.
     *
     * @param staffEr il receiver da aggiungere
     */
    public void addReceiver(StaffEventReceiver staffEr) {
        if (!receivers.contains(staffEr)) {
            receivers.add(staffEr);
        }
    }

    /**
     * Rimuove un {@code StaffEventReceiver} precedentemente registrato.
     *
     * @param staffEr il receiver da rimuovere
     */
    public void removeReceiver(StaffEventReceiver staffEr) {
        receivers.remove(staffEr);
    }

    // Metodi di notifica ai receiver

    private void notifyStaffCreated() {
        for (StaffEventReceiver r : receivers) {
            r.updateStaffCreated(currentStaff);
        }
    }

    private void notifyStaffChanged(Staff st) {
        for (StaffEventReceiver r : receivers) {
            r.updateStaffChanged(st);
        }
    }

    private void notifyStaffDeleted() {
        for (StaffEventReceiver r : receivers) {
            r.updateStaffDeleted(currentStaff);
        }
    }

    private void notifyContactModify(Contact contact) {
        for (StaffEventReceiver r : receivers) {
            r.updateContactModify(contact);
        }
    }

    private void notifyContactMarked(Contact contact) {
        for (StaffEventReceiver r : receivers) {
            r.updateContactMarked(contact);
        }
    }

    private void notifyContactAdded(Contact contact) {
        for (StaffEventReceiver r : receivers) {
            r.updateContactAdded(contact);
        }
    }

    private void notifyRoleAdded(User user, Role rol) {
        for (StaffEventReceiver r : receivers) {
            r.updateRoleAdded(user, rol);
        }
    }

    private void notifyRoleDeleted(User user, Role rol) {
        for (StaffEventReceiver r : receivers) {
            r.updateRoleDeleted(user, rol);
        }
    }

    private void notifyCollaboratorsListModified(CollaboratorsList list, Collaborator col) {
        for (StaffEventReceiver r : receivers) {
            r.updateCollaboratorsListModified(list, col);
        }
    }

    private void notifyCollaboratorDeletedFromList(CollaboratorsList list, Collaborator col) {
        for (StaffEventReceiver r : receivers) {
            r.updateCollaboratorDeletedFromList(list, col);
        }
    }

    private void notifyCollaboratorAdded(Collaborator col) {
        for (StaffEventReceiver r : receivers) {
            r.updateCollaboratorAdded(col);
        }
    }

    private void notifyCollaboratorDeleted(Collaborator col) {
        for (StaffEventReceiver r : receivers) {
            r.updateCollaboratorDeleted(col);
        }
    }

    private void notifyContactDeleted(Contact contact) {
        for (StaffEventReceiver r : receivers) {
            r.updateContactDeleted(contact);
        }
    }

    private void notifyHolidaysAdded(Holidays holidays) {
        for (StaffEventReceiver r : receivers) {
            r.updateHolidaysAdded(holidays);
        }
    }

    private void notifyHolidaysModified(Holidays holidays) {
        for (StaffEventReceiver r : receivers) {
            r.updateHolidaysModified(holidays);
        }
    }

    private void notifyHolidaysDeleted(Holidays holidays) {
        for (StaffEventReceiver r : receivers) {
            r.updateHolidaysDeleted(holidays);
        }
    }

    private void notifyNotesAdded(Notes notes){
        for (StaffEventReceiver r : receivers) {
            r.updateNotesAdded(notes);
        }
    }

    private void notifyNotesDeleted(Notes notes){
        for (StaffEventReceiver r : receivers) {
            r.updateNotesDeleted(notes);
        }
    }

    private void notifyNotesModified(Notes notes){
        for (StaffEventReceiver r : receivers) {
            r.updateNotesModified(notes);
        }
    }



    // Metodi operativi

    /**
     * Crea un nuovo staff con il nome specificato e lo salva nel sistema.
     * Solo un utente con ruolo di organizzatore può eseguire questa operazione.
     *
     * @param name il nome del nuovo staff
     * @return l'istanza di {@code Staff} appena creata
     * @throws UseCaseLogicException se l'utente corrente non ha i permessi
     */
    public Staff arrangeStaff(String name) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!user.isOrganizer()) {
            throw new UseCaseLogicException("L'utente non ha i permessi per creare lo staff.");
        }

        Staff st = Staff.create(name);
        currentStaff = st;
        notifyStaffCreated();
        return st;
    }

    /**
     * Verifica che lo staff specificato esista.
     *
     * @param staff lo staff da verificare
     * @return lo stesso oggetto {@code Staff} se valido
     * @throws UseCaseLogicException se lo staff è nullo
     */
    public Staff verifyStaff() throws UseCaseLogicException{
        if(currentStaff == null) {
            throw new UseCaseLogicException("Lo staff non esiste.");
        }
        return currentStaff;
    }

    /**
     * Modifica i dati di uno staff esistente, se non è attivo.
     *
     * @param name il nuovo nome dello staff
     * @param activity lo stato di attività
     * @param available la disponibilità dello staff
     * @return l'oggetto {@code Staff} aggiornato
     * @throws UseCaseLogicException se lo staff è attivo e quindi non modificabile
     */
    public Staff modifyStaff(String name, Boolean activity, Boolean available) throws UseCaseLogicException {
        if(currentStaff.isActivity()) {
            throw new UseCaseLogicException("Il personale non può essere modificato.");
        }
        Staff st = currentStaff.changeStaff(name, activity, available);
        notifyStaffChanged(st);
        return st;
    }

    /**
     * Elimina lo staff specificato dal sistema.
     * Solo un organizzatore può eseguire questa operazione, e solo se lo staff è attivo.
     *
     * @throws UseCaseLogicException se l'utente non ha i permessi o lo staff non è attivo
     */
    public void deleteStaff() throws UseCaseLogicException{
        User user = CatERing.getInstance().getUserManager().getCurrentUser();

        if (!user.isOrganizer() || !currentStaff.isActivity()) {
            throw new UseCaseLogicException("L'utente non ha i permessi per eliminare lo staff.");
        }
        notifyStaffDeleted();
    }

    /**
     * Assegna un ruolo (solo "Cuoco" o "Chef") a un utente, se non lo ha già.
     * Solo un organizzatore può eseguire questa operazione.
     *
     * @param user l'utente a cui assegnare il ruolo
     * @param role il ruolo da assegnare
     * @throws UseCaseLogicException se l'utente non è un organizzatore o il ruolo non è ammesso
     * @throws SQLException se si verifica un errore durante il salvataggio nel database
     */
    public void divideRoles(User user, Role role) throws UseCaseLogicException, SQLException {
        User current = CatERing.getInstance().getUserManager().getCurrentUser();

        if (current == null || !current.isOrganizer()) {
            throw new UseCaseLogicException("Solo un organizzatore può assegnare ruoli.");
        }

        String ruoloName = role.getRole(); 
        if (!ruoloName.equalsIgnoreCase("Cuoco") && !ruoloName.equalsIgnoreCase("Chef")) {
            throw new UseCaseLogicException("Non è consentito assegnare il ruolo: " + ruoloName);
        }

        if (!user.hasRole(role)) {
            role.setStarted(false);
            user.addRole(role);
            notifyRoleAdded(user, role);
        }
       
    }

    /**
     * Rimuove un ruolo (solo "Cuoco" o "Chef") da un utente, se lo possiede.
     * Solo un organizzatore può eseguire questa operazione.
     *
     * @param user l'utente da cui rimuovere il ruolo
     * @param role il ruolo da rimuovere
     * @throws UseCaseLogicException se l'utente non è un organizzatore o il ruolo non è ammesso
     * @throws SQLException se si verifica un errore durante l'eliminazione nel database
     */
    public void deleteRoles(User user, Role role) throws UseCaseLogicException, SQLException {
        User current = CatERing.getInstance().getUserManager().getCurrentUser();

        if (current == null || !current.isOrganizer()) {
            throw new UseCaseLogicException("Solo un organizzatore può rimuovere ruoli.");
        }

        String ruoloName = role.getRole();
        if (!ruoloName.equalsIgnoreCase("Cuoco") && !ruoloName.equalsIgnoreCase("Chef")) {
            throw new UseCaseLogicException("Non è consentito rimuovere il ruolo: " + ruoloName);
        }

        if (user.hasRole(role)) {
            user.removeRole(role);
            notifyRoleDeleted(user, role);
        }
    }

    /**
     * Aggiunge un collaboratore allo staff specificato e lo rimuove dalla lista dei collaboratori.
     * Il collaboratore viene marcato come non accettato e non disponibile.
     *
     * @param col il collaboratore da aggiungere
     * @param list la lista dei collaboratori da cui rimuovere il collaboratore
     * @throws UseCaseLogicException se il collaboratore è già parte dello staff
     */
    public void addCollaboratorToStaff(Collaborator col, CollaboratorsList list) throws UseCaseLogicException {
        if(col.isPartOf(currentStaff, col.getName())){
            throw new UseCaseLogicException("Il collaboratore è già parte dello staff.");
        }
        currentStaff.joinCollaborator(col);
        list.removeCollaborator(col);
        col.setAccepted(false);
        col.setDisposable(false);
        notifyCollaboratorAdded(col);
    }

    /**
     * Rimuove un collaboratore dallo staff specificato, se è parte dello staff ed è stato accettato.
     *
     * @param col il collaboratore da rimuovere
     * @throws UseCaseLogicException se il collaboratore non è parte dello staff o non è accettato
     * @throws SQLException se si verifica un errore nella persistenza
     */
    public void deleteCollaboratorFromStaff(Collaborator col) throws  UseCaseLogicException, SQLException{
        if(!col.isPartOf(currentStaff, col.getName()) || !col.isAccepted()){
            throw new UseCaseLogicException("Il collaboratore non è parte dello staff.");
        }
        currentStaff.removeCollaborator(col);
        notifyCollaboratorDeleted(col);
    }

    /**
     * Modifica le informazioni di un collaboratore nella lista specificata.
     *
     * @param list la lista dei collaboratori
     * @param col il collaboratore da modificare
     * @param name il nuovo nome del collaboratore
     * @param phoneNumber il nuovo numero di telefono
     * @param accepted stato di accettazione del collaboratore
     * @param disposable indicazione se il collaboratore è rimovibile
     * @throws UseCaseLogicException se il collaboratore non è presente nella lista
     */
    public void modifyCollaboratorsList(CollaboratorsList list, Collaborator col, String name, String phoneNumber, Boolean accepted, Boolean disposable) throws  UseCaseLogicException{
        if(!col.isInList(list)){
            throw new UseCaseLogicException("Il collaboratore non è presente nella lista.");
        }
        list.changeCollaboratorsList(list, col, name, phoneNumber, accepted, disposable);
        notifyCollaboratorsListModified(list, col);
    }
    
    /**
     * Rimuove un collaboratore dalla lista se è marcato come rimovibile.
     *
     * @param list la lista dei collaboratori
     * @param col il collaboratore da rimuovere
     * @throws UseCaseLogicException se il collaboratore non è nella lista o non è rimovibile
     */
    public void removeCollaboratorFromList(CollaboratorsList list, Collaborator col) throws  UseCaseLogicException{
        if(!col.isDisposable() || !col.isInList(list)){
            throw new UseCaseLogicException("Il collaboratore non è presente nella lista.");
        }
        list.removeCollaborator(col);
        notifyCollaboratorDeletedFromList(list, col);
    }

    /**
     * Aggiunge un contatto a una lista di contatti, marcandolo come interessato.
     *
     * @param contact il contatto da aggiungere
     * @param list la lista dei contatti a cui aggiungere il contatto
     * @param date la data di interesse (attualmente non usata direttamente)
     * @throws UseCaseLogicException se il contatto è già presente nella lista
     * @throws SQLException se si verifica un errore nella persistenza
     */
    public void markContact(Contact contact, ContactList list, Date date) throws UseCaseLogicException, SQLException{
        if (contact.isAdded(list)) {
            throw new UseCaseLogicException("Il contatto è già presente nella lista.");
        }
        list.addContact(contact);
        contact.setInterested(true);
        notifyContactMarked(contact);
    }

    /**
     * Modifica le informazioni di un contatto esistente nella lista.
     *
     * @param contact il contatto da modificare
     * @param list la lista che contiene il contatto
     * @param name il nuovo nome
     * @param address il nuovo indirizzo
     * @param fiscalCode il nuovo codice fiscale
     * @param phoneNumber il nuovo numero di telefono
     * @param interested indica se il contatto è interessato
     * @throws UseCaseLogicException se il contatto non è presente nella lista
     */
    public void modifyContact(Contact contact, ContactList list, String name, String address, String fiscalCode, String phoneNumber, boolean interested) throws UseCaseLogicException {
        if (!contact.isAdded(list)) {
            throw new UseCaseLogicException("Il contatto non è presente nella lista.");
        }
        list.changeContact(name, contact, address, fiscalCode, phoneNumber, interested);
        notifyContactModify(contact);
    }

    /**
     * Rimuove un contatto da una lista esistente.
     *
     * @param contact il contatto da rimuovere
     * @param list la lista da cui rimuovere il contatto
     * @throws UseCaseLogicException se il contatto non è presente nella lista
     * @throws SQLException se si verifica un errore nella persistenza
     */
    public void deleteContact(Contact contact, ContactList list) throws  UseCaseLogicException, SQLException{
        if (!contact.isAdded(list)) {
            throw new UseCaseLogicException("Il contatto non è presente nella lista.");
        }
        list.removeContact(contact);
        notifyContactDeleted(contact);
    }

    /**
     * Aggiunge un contatto allo staff se non è già presente e se è segnato come interessato.
     *
     * @param contact il contatto da aggiungere
     * @throws UseCaseLogicException se il contatto è già parte dello staff o non è interessato
     * @throws SQLException se si verifica un errore nella persistenza
     */
    public void addContactToStaff(Contact contact) throws UseCaseLogicException, SQLException {
        if (contact.isPartOf(currentStaff, contact.getFiscalCode()) || !contact.isInterested()){
            throw new UseCaseLogicException("Il contatto non può essere aggiunto allo staff.");
        }
        currentStaff.addContact(contact);
        notifyContactAdded(contact);
    }

    /**
     * Assegna le ferie a un membro dello staff, se disponibile e non già assegnate.
     *
     * @param holidays le ferie da assegnare
     * @throws UseCaseLogicException se lo staff non è disponibile o le ferie sono già assegnate
     */
    public void grantHolidays(Holidays holidays) throws UseCaseLogicException {
        if (!currentStaff.isAvailable() || holidays.isAssigned(currentStaff)) {
            throw new UseCaseLogicException("Non è possibile assegnare le ferie.");
        }
        currentStaff.addHolidays(currentStaff.getId(), holidays);
        notifyHolidaysAdded(holidays);
    }

    /**
     * Modifica l'intervallo di date di ferie già assegnate a un membro dello staff.
     *
     * @param holidays le ferie da modificare
     * @param startDate la nuova data di inizio
     * @param endDate la nuova data di fine
     * @throws UseCaseLogicException se lo staff non è disponibile o le ferie non sono assegnate
     */
    public void modifyHolidays(Holidays holidays, LocalDate startDate, LocalDate endDate) throws UseCaseLogicException {
        if(!currentStaff.isAvailable() || !holidays.isAssigned(currentStaff)){
            throw new UseCaseLogicException("Non è possibile modificare le ferie.");
        }
        currentStaff.changeHolidays(currentStaff.getId(), holidays, startDate, endDate);
        notifyHolidaysModified(holidays);
    }

    /**
     * Rimuove le ferie assegnate a un membro dello staff.
     *
     * @param holidays le ferie da rimuovere
     * @throws UseCaseLogicException se lo staff non è disponibile o le ferie non sono assegnate
     */
    public void deleteHolidays(Holidays holidays) throws UseCaseLogicException {
        if(!currentStaff.isAvailable() || !holidays.isAssigned(currentStaff)){
            throw new UseCaseLogicException("Non è possibile cancellare le ferie.");
        }
        currentStaff.removeHolidays(holidays);
        holidays.removeAssignment();
        notifyHolidaysDeleted(holidays);
    }

    /**
     * Aggiunge delle note all'evento attualmente in corso, se non sono già presenti.
     *
     * @param notes le note da aggiungere all'evento corrente
     * @throws UseCaseLogicException se le note sono già presenti nell'evento
     */
    public void addNotesToCurrentEvent(Notes notes) throws UseCaseLogicException {
        if (currentEvent.hasNotes(notes.getContent())) {
            throw new UseCaseLogicException("Queste note sono già presenti.");
        }
        currentEvent.addNotes(notes);
        notes.setOccupied(false);
        notifyNotesAdded(notes);
    }

    /**
     * Rimuove delle note dall'evento attualmente in corso, se presenti e non occupate.
     *
     * @param notes le note da rimuovere
     * @throws UseCaseLogicException se le note non sono presenti o sono occupate
     */
    public void deleteNotesFromEvent(Notes notes)throws UseCaseLogicException {
        if (!currentEvent.hasNotes(notes.getContent()) || notes.isOccupied()) {
            throw new UseCaseLogicException("Non si possono eliminare queste note.");
        }
        currentEvent.removeNotes(notes);
        notifyNotesDeleted(notes);
    }
        
    /**
     * Modifica il contenuto di note associate all'evento corrente, se non occupate.
     *
     * @param notes le note da modificare
     * @throws UseCaseLogicException se le note non sono presenti o sono occupate
     */
    public void modifyNotesOfEvent(Notes notes)throws UseCaseLogicException{
        if (!currentEvent.hasNotes(notes.getContent()) || notes.isOccupied()) {
            throw new UseCaseLogicException("Non si possono modificare queste note.");
        }
        notes.changeNotes(notes.isOccupied(), notes.getContent());
        notifyNotesModified(notes);
    }
}