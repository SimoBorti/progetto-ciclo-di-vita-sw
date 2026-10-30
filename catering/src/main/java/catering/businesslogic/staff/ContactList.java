package catering.businesslogic.staff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Classe che rappresenta una lista di contatti.
 * Permette di aggiungere, modificare, rimuovere e cercare contatti nella lista.
 */
public class ContactList {
    private final Date creationDate;
    private final List<Contact> contacts;
    
    /**
     * Costruttore che inizializza la lista di contatti e registra la data di creazione.
     */
    public ContactList() {
        this.creationDate = new Date();
        this.contacts = new ArrayList<>();
    }

    /**
     * Aggiunge un contatto alla lista.
     * 
     * @param contact contatto da aggiungere
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    /**
     * Verifica se nella lista è presente un contatto con un dato codice fiscale.
     * 
     * @param fiscal_code codice fiscale da cercare
     * @return true se esiste un contatto con quel codice fiscale, false altrimenti
     */
    public boolean hasContactNamed(String fiscal_code) {
        for (Contact c : contacts) {
            if (c.getFiscalCode().equals(fiscal_code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Modifica le informazioni di un contatto nella lista.
     * Solo i parametri non null vengono modificati.
     * 
     * @param name nuovo nome, se non null
     * @param contact contatto da modificare
     * @param address nuovo indirizzo, se non null
     * @param fiscalCode nuovo codice fiscale, se non null
     * @param phoneNumber nuovo numero di telefono, se non null
     * @param interested nuovo stato di interesse, se non null
     * @return l'oggetto ContactList corrente (per concatenare chiamate)
     */
    public ContactList changeContact(String name, Contact contact, String address, String fiscalCode, String phoneNumber, Boolean interested) {
        if (name != null) contact.setName(name);
        if (address != null) contact.setAddress(address);
        if (fiscalCode != null) contact.setFiscalCode(fiscalCode);
        if (phoneNumber != null) contact.setPhoneNumber(phoneNumber);
        if (interested != null) contact.setInterested(interested);
        return this;
    }

    /**
     * Rimuove un contatto dalla lista.
     * 
     * @param contact contatto da rimuovere
     * @return true se il contatto è stato rimosso con successo, false altrimenti
     */
    public boolean removeContact(Contact contact) {
        return contacts.remove(contact);
    }

    /**
     * Restituisce la lista completa di contatti.
     * 
     * @return lista di oggetti Contact
     */
    public List<Contact> getContacts() {
        return contacts;
    }
    
    /**
     * Restituisce la data di creazione della lista.
     * 
     * @return data di creazione
     */
    public Date getCreationDate() {
        return creationDate;
    }
}
