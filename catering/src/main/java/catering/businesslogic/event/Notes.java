package catering.businesslogic.event;

import catering.persistence.PersistenceManager;

/**
 * Rappresenta una nota associata a un evento.
 * 
 * Ogni oggetto Notes contiene un testo descrittivo, lo stato di "occupato" e il riferimento
 * all'evento a cui è collegata. Fornisce metodi per modificare le proprietà,
 * e metodi per salvare, aggiornare ed eliminare la nota dal database.
 */
public class Notes {
    private int id;
    private String content;
    private boolean occupied;
    private Event event;

    /**
     * Costruttore per creare una nuova nota associata a un evento.
     * 
     * @param content testo della nota
     * @param event evento associato alla nota
     */
    public Notes(String content, Event event) {
        this.content = content;
        this.event = event;
    }

    /**
     * Modifica le proprietà della nota.
     * 
     * @param occupied nuovo stato di occupato (se non null)
     * @param content nuovo testo della nota (se non null)
     * @return l'istanza modificata di Notes
     */
    public Notes changeNotes(Boolean occupied, String content){
        if (occupied != null) this.occupied = occupied;
        if (content != null) this.content = content;
        return this;
    }

    //Getter e setter
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public boolean isOccupied() {
        return occupied;
    }
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
    public Event getEvent() {
        return event;
    }
    public void setEvent(Event event) {
        this.event = event;
    }

    //metodi per modifica DB

    /**
     * Salva la nota nel database.
     * Se la nota non ha ancora un id, viene inserita una nuova riga.
     * L'id generato viene impostato nell'oggetto.
     * 
     * @param note oggetto Notes da salvare
     * @throws RuntimeException in caso di errore SQL
     */
    public void saveNotes(){
        String insertQuery = "INSERT INTO Notes (content, event_id) VALUES (?, ?)";
        PersistenceManager.executeUpdate(insertQuery, this.getContent(), this.getEvent().getId());
    }

    /**
     * Elimina la nota dal database.
     */
    public void deleteNotes(){
        String query = "DELETE FROM Notes WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.getId());
    }

    /**
     * Aggiorna il contenuto e lo stato di occupato della nota nel database.
     */
    public void updateNotes(){
        String query = "UPDATE Notes SET content = ?, occupied = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.getContent(), this.isOccupied(), this.getId());
    }
}
