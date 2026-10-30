package catering.businesslogic.holidays;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import catering.businesslogic.staff.Staff;
import catering.persistence.PersistenceManager;

/*
 * Classe che rappresenta le ferie.
 */
public class Holidays {
    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private int staff_id;

    public Holidays(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Carica le ferie assegnate a uno specifico membro dello staff dal database.
     *
     * @param staffId l'ID dello staff per cui caricare le ferie
     * @return un oggetto Holidays associato allo staffId, o null se non trovato
     * @throws SQLException in caso di errore nella query al database
     */
    public static Holidays loadByStaffId(int staffId) throws SQLException {
        final Holidays[] holidays = {null};
        String query = "SELECT * FROM Holidays WHERE staff_id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            LocalDate startDate = rs.getDate("startDate").toLocalDate();
            LocalDate endDate = rs.getDate("endDate").toLocalDate();
            holidays[0] = new Holidays(startDate, endDate);
            holidays[0].setId(rs.getInt("id"));
            holidays[0].setStaffId(staffId);
        }, staffId);
        return holidays[0];
    }

    /**
     * Rimuove l'assegnazione delle ferie dallo staff, impostando l'id dello staff a -1.
     */
    public void removeAssignment() {
        this.staff_id = -1;
    }

    /**
     * Verifica se queste ferie sono assegnate allo staff specificato.
     *
     * @param st oggetto Staff da verificare
     * @return true se le ferie sono assegnate allo staff indicato, false altrimenti
     */
    public boolean isAssigned(Staff st) {
        return this.staff_id == st.getId();
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStartDate(LocalDate newDate) {
        this.startDate = newDate;
    }

    public void setEndDate(LocalDate newDate) {
        this.endDate = newDate;
    }

    public int getStaffId(){
        return staff_id;
    }

    public void setStaffId(int staff_id){
        this.staff_id = staff_id;
    }

        public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //metodi per modifica DB

    /**
     * Salva un nuovo record di ferie nel database, associandolo allo staff.
     *
     * @param holidays l'oggetto Holidays da salvare
     * @throws RuntimeException in caso di errore SQL durante l'inserimento
     */
    public void saveHolidays() {
        String insertQuery = "INSERT INTO Holidays (startDate, endDate, staff_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = PersistenceManager.getConnection().prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, this.getStartDate());
            stmt.setObject(2, this.getEndDate());
            stmt.setInt(3, this.getStaffId());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    this.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio delle ferie", e);
        }
    }

    /**
     * Aggiorna le date di ferie nel database per un record esistente identificato dall'id.
     */
    public void updateHolidays() {
        String query = "UPDATE Holidays SET startDate = ?, endDate = ? WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.getStartDate(), this.getEndDate(), this.getId());
    }

    
    /**
     * Elimina il record delle ferie dal database identificato dall'id.
     */
    public void deleteHolidays(){
        String query = "DELETE FROM Holidays WHERE id = ?";
        PersistenceManager.executeUpdate(query, this.getId());
    }
}
