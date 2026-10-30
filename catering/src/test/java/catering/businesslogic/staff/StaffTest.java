package catering.businesslogic.staff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.event.Event;
import catering.businesslogic.event.Notes;
import catering.businesslogic.holidays.Holidays;
import catering.businesslogic.user.User;
import catering.persistence.PersistenceManager;
import catering.util.LogManager;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // così JUnit crea una sola istanza di classe per tutti i test
public class StaffTest {
    private static final Logger LOGGER = LogManager.getLogger(Staff.class);

    private static CatERing app;
    private static User organizer;

    @BeforeAll
    static void init() {
        PersistenceManager.initializeDatabase("database/catering_init_sqlite.sql");
        app = CatERing.getInstance();
    }

    //da commentare se si vuole vedere il risultato dei test nel DB
    @AfterAll
    void reloadInitialStateFromScript() {
        try {
            String sql = Files.readString(Paths.get("database/catering_init_sqlite.sql"));
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    PersistenceManager.executeUpdate(statement.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail("Errore nel ripristino dallo script SQL: " + e.getMessage());
        }
    }

    @BeforeEach
    void setup() {
        try {
            // Carica i ruoli
            Role.initPredefinedRoles();

            // Carica l'organizzatore
            organizer = User.load("Giovanni");
            assertNotNull(organizer, "Organizer user should be loaded");
            assertTrue(organizer.isOrganizer(), "User should have organizer role");

            // Login come organizzatore
            app.getUserManager().fakeLogin(organizer.getUserName());
            assertEquals(organizer, app.getUserManager().getCurrentUser(), "Current user should be the organizer");

        } catch (UseCaseLogicException e) {
            LOGGER.severe(e.getMessage());
            fail("Setup failed: " + e.getMessage());
        }
    }

//////////////////////////////////////////TEST SIGNIFICATIVI////////////////////////////////////////////////
    @Test
    @Order(2)
    void testModifyStaff() {
        try {
            // Carica uno staff esistente
            Staff staff = Staff.loadByName("Ferdinando");
            assertNotNull(staff, "Staff 'Ferdinando' should be loaded");

            // Setta lo staff corrente
            app.getStaffManager().setCurrentStaff(staff);

            // Controlla che il personale caricato non abbia activity=true
            assertFalse(staff.isActivity(), "Il personale attivo non può essere modificato");

            // Crea le modifiche per il membro dello staff
            String newName = "Ferdinando Modificato";
            Boolean newActivity = false;
            Boolean newAvailable = false;

            // Chiama il metodo per modificare lo staff
            Staff modified = app.getStaffManager().modifyStaff(newName, newActivity, newAvailable);

            // Verifica che lo staff sia stato modificato correttamente
            assertEquals(newName, modified.getName());
            assertEquals(newActivity, modified.isActivity());
            assertEquals(newAvailable, modified.isAvailable());

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception in testModifyStaff: " + e.getMessage());
        }
    }    

@Test
    @Order(13)
    void testAddContactToStaff() {
        try {
            // Carica uno staff esistente
            Staff staff = Staff.loadByName("Piero");
            assertNotNull(staff, "Staff 'Piero' should be loaded");

            // Setta lo staff corrente
            app.getStaffManager().setCurrentStaff(staff);

            // Carica un contatto esistente nella ContactList
            final Contact[] contact = {null};
            String selectQuery = "SELECT * FROM ContactList WHERE fiscal_code = ?";
            PersistenceManager.executeQuery(selectQuery, rs -> {
                contact[0] = new Contact(
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("fiscal_code"),
                    rs.getString("phone_number"),
                    rs.getBoolean("interested")
                );
                contact[0].setId(rs.getInt("id"));
            }, "VRFNCL99X01H501Y"); //DA MODIFICARE PER TEST SINGOLO

            assertNotNull(contact[0], "Contact should be loaded from ContactList");

            // Assicuriamoci che il contatto non sia già nello staff
            Staff existingStaff = Staff.loadByName(contact[0].getName());
            assertNull(existingStaff, "Staff with contact's name should not exist yet");

            // Chiamo il metodo da testare
            app.getStaffManager().addContactToStaff(contact[0]);

            // Ora il nuovo membro dello staff con nome del contatto deve esistere nel DB
            Staff promotedStaff = Staff.loadByName(contact[0].getName());
            assertNotNull(promotedStaff, "New staff member with contact's name should be created");

            // Verifica che il flag contact sia true e collaborator false
            assertTrue(promotedStaff.isContact(), "The new staff member should have contact = true");
            assertFalse(promotedStaff.isCollaborator(), "The new staff member should have collaborator = false");

            // Verifica che il nuovo membro abbia il nome esatto del contatto
            assertEquals(contact[0].getName(), promotedStaff.getName(), "Staff name should match contact name");

            // Assicuriamoci che il contatto non sia più presente in ContactList
            final boolean[] stillInList = {false};
            String checkListQuery = "SELECT * FROM ContactList WHERE fiscal_code = ?";
            PersistenceManager.executeQuery(checkListQuery, rs -> {
                stillInList[0] = true;
            }, contact[0].getFiscalCode());
            assertFalse(stillInList[0], "Contact should no longer be present in ContactList");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Exception in testAddContactToStaff: " + e.getMessage());
        }
    }
//////////////////////////////////////////TEST SIGNIFICATIVI////////////////////////////////////////////////


//////////////////////////////////////////ALTRI TEST////////////////////////////////////////////////////////

    @Test
    @Order(1)
    void testArrangeStaffCreation() {
        LOGGER.info("Testing arrangeStaff() method for staff creation");

        try {
            // Crea staff
            String staffName = "PierGiorgio";
            Staff newStaff = app.getStaffManager().arrangeStaff(staffName);

            app.getStaffManager().setCurrentStaff(newStaff);

            // Verifica staff creato
            assertNotNull(newStaff, "Staff should not be null");
            assertEquals(staffName, newStaff.getName(), "Staff name should match");
            assertEquals(organizer, newStaff.getOrganizer(), "Staff organizer should be the current user");

            LOGGER.info("Staff created successfully: " + newStaff.toString());

        } catch (UseCaseLogicException e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }


    @Test
    @Order(3)
    void testDeleteStaff() {
        try {
            // Carica uno staff esistente
            Staff staff = Staff.loadByName("Armando");
            assertNotNull(staff, "Staff 'Armando' should be loaded");
            assertTrue(staff.isActivity(), "Staff 'Armando' should be active");

            app.getStaffManager().setCurrentStaff(staff);

            // Verifica che l'utente corrente sia organizzatore
            User currentUser = app.getUserManager().getCurrentUser();
            assertTrue(currentUser.isOrganizer(), "Current user should be an organizer");

            // Chiama il metodo di deleteStaff
            app.getStaffManager().deleteStaff();

            // Ricarica lo staff dopo la cancellazione, dovrebbe restituire null
            Staff deletedStaff = Staff.loadById(staff.getId());
            assertNull(deletedStaff, "Staff should be deleted from DB");

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception during testDeleteStaff: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    void testDivideRolesAssignment() {
        LOGGER.info("Testing divideRoles() method");

        try {
            // Precondizioni
            User cuocoUser = User.load("Luca"); // Un utente che NON è organizzatore
            assertNotNull(cuocoUser, "Cuoco user should be loaded");
            assertTrue(!cuocoUser.isOrganizer(), "Luca should not be an organizer initially");

            Role cuocoRole = Role.getPredefined("CUOCO");
            assertNotNull(cuocoRole, "Cuoco role should be loaded");

            // Assegna ruolo cuoco
            app.getStaffManager().divideRoles(cuocoUser, cuocoRole);

            // Verifica che il ruolo sia stato aggiunto
            assertTrue(cuocoUser.hasRole(cuocoRole), "Luca should now have the Cuoco role");

            LOGGER.info("Role 'Cuoco' successfully assigned to user: " + cuocoUser.getUserName());

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception during divideRoles(): " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testDeleteRolesRemoval() {
        LOGGER.info("Testing deleteRoles() method");

        try {
            // Carica un utente non organizzatore
            User cuocoUser = User.load("Luca");
            assertNotNull(cuocoUser, "Cuoco user should be loaded");

            Role cuocoRole = Role.getPredefined("CUOCO");
            assertNotNull(cuocoRole, "Cuoco role should be loaded");

            // Assicuriamoci che l'utente abbia il ruolo Cuoco
            if (!cuocoUser.hasRole(cuocoRole)) {
                app.getStaffManager().divideRoles(cuocoUser, cuocoRole);
            }
            assertTrue(cuocoUser.hasRole(cuocoRole), "Luca should have the Cuoco role before removal");

            // Rimuove il ruolo Cuoco da Luca
            app.getStaffManager().deleteRoles(cuocoUser, cuocoRole);

            // Verifica che il ruolo sia stato rimosso
            assertFalse(cuocoUser.hasRole(cuocoRole), "Luca should no longer have the Cuoco role");

            LOGGER.info("Role 'Cuoco' successfully removed from user: " + cuocoUser.getUserName());

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception during deleteRoles(): " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testMarkContact() {
        try {
            // Setup: creo un contact nuovo non ancora nella lista
            ContactList contactList = new ContactList();
            Contact newContact = new Contact("Marco Mastrangelo", "Via Firenze 10", "VRFNCL99X01H501Y", "3335557788", true);

            // Verifico che il contatto NON sia già presente nella lista
            assertFalse(newContact.isAdded(contactList), "Contact should not be in the list initially");

            // Eseguo il metodo da testare
            app.getStaffManager().markContact(newContact, contactList, new Date());

            // Ora il contatto deve essere presente nella lista in memoria
            assertTrue(newContact.isAdded(contactList), "Contact should be added to the list");

            // Verifico che il contatto sia stato inserito nel DB
            final boolean[] foundInDb = {false};
            String query = "SELECT * FROM ContactList WHERE fiscal_code = ?";
            PersistenceManager.executeQuery(query, rs -> {
                foundInDb[0] = true;
                assertEquals(newContact.getName(), rs.getString("name"));
                assertEquals(newContact.getAddress(), rs.getString("address"));
                assertEquals(newContact.getFiscalCode(), rs.getString("fiscal_code"));
                assertEquals(newContact.getPhoneNumber(), rs.getString("phone_number"));
                assertEquals(newContact.isInterested(), rs.getBoolean("interested"));
            }, newContact.getFiscalCode());

            assertTrue(foundInDb[0], "Contact should be found in DB after marking");

            // Provo a richiamare markContact con lo stesso contatto e mi aspetto eccezione
            UseCaseLogicException ex = assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().markContact(newContact, contactList, new Date());
            });
            assertEquals("Il contatto è già presente nella lista.", ex.getMessage());

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception in testMarkContact: " + e.getMessage());
        }
    }

    @Test
    @Order(7)
    void testModifyContact() {
        try {
            ContactList contactList = new ContactList();

            // Creo un contatto e lo aggiungo (simulando contatto già presente)
            Contact contact = Contact.loadById(1);
            assertNotNull(contact, "Contact with id=1 should be loaded");
            contactList.addContact(contact);

            // Verifico che il contatto sia nella lista
            assertTrue(contact.isAdded(contactList), "Contact should be in the list before modification");

            // Dati nuovi per la modifica
            String newName = "Pietro Marrone";
            String newAddress = "Via Napoli 12";
            String newFiscalCode = "NPLNPC80C12H501Y";
            String newPhoneNumber = "3336669999";
            boolean newInterested = true;

            // Chiamo il metodo da testare
            app.getStaffManager().modifyContact(contact, contactList, newName, newAddress, newFiscalCode, newPhoneNumber, newInterested);

            // Verifico che il contatto sia stato aggiornato in memoria
            assertEquals(newName, contact.getName());
            assertEquals(newAddress, contact.getAddress());
            assertEquals(newFiscalCode, contact.getFiscalCode());
            assertEquals(newPhoneNumber, contact.getPhoneNumber());
            assertEquals(newInterested, contact.isInterested());

            // Verifico aggiornamento su DB
            final boolean[] found = {false};
            String query = "SELECT * FROM ContactList WHERE id = ?";
            PersistenceManager.executeQuery(query, rs -> {
                found[0] = true;
                assertEquals(newName, rs.getString("name"));
                assertEquals(newAddress, rs.getString("address"));
                assertEquals(newFiscalCode, rs.getString("fiscal_code"));
                assertEquals(newPhoneNumber, rs.getString("phone_number"));
                assertEquals(newInterested, rs.getBoolean("interested"));
            }, contact.getId());

            assertTrue(found[0], "Modified contact should be found in DB");

            // Verifico che non sia possibile modificare un contatto non presente nella lista
            Contact newContact = new Contact("Giorgio Locatelli", "Via Bologna 9", "BLGLNG80D01H501X", "3337778888", false);
            UseCaseLogicException ex = assertThrows(UseCaseLogicException.class, () -> {
                app.getStaffManager().modifyContact(newContact, contactList, "name", "addr", "fc", "phone", true);
            });
            assertEquals("Il contatto non è presente nella lista.", ex.getMessage());

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception in testModifyContact: " + e.getMessage());
        }
    }

    @Test
    @Order(8)
    void testDeleteContact() {
        try {
            // Carica un contatto esistente
            Contact contact = Contact.loadById(2);
            assertNotNull(contact, "Contact should be loaded");

            // Carica la lista dei contatti
            ContactList list = new ContactList();
            // Assumiamo che la lista sia popolata con il contatto, altrimenti aggiungiamolo
            if (!contact.isAdded(list)) {
                list.addContact(contact);
            }

            // Verifica preliminare che il contatto sia nella lista
            assertTrue(contact.isAdded(list), "Contact should be in the list before deletion");

            // Elimina il contatto
            app.getStaffManager().deleteContact(contact, list);

            // Ora verifica che il contatto non sia più in lista
            assertFalse(contact.isAdded(list), "Contact should be removed from the list");

            // Prova a caricare il contatto dal DB, deve risultare null (cancellato)
            Contact deleted = Contact.loadById(contact.getId());
            assertNull(deleted, "Contact should be deleted from database");

        } catch (UseCaseLogicException | SQLException e) {
            fail("Exception in testDeleteContact: " + e.getMessage());
        }
    }

  @Test
    @Order(9)
    void testAddCollaboratorToStaff() {
        try {
            Staff staff = Staff.loadByName("PierGiorgio");
            assertNotNull(staff, "Staff 'PierGiorgio' should be loaded");

            app.getStaffManager().setCurrentStaff(staff);

            CollaboratorsList list = new CollaboratorsList(); // crea la lista in memoria
            final Collaborator[] collaborator = {null};
            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE name = ?",
                rs -> {
                    Collaborator col = new Collaborator(rs.getString("name"));
                    col.setPhoneNumber(rs.getString("phone_number"));
                    col.setAccepted(rs.getBoolean("accepted"));
                    col.setDisposable(rs.getBoolean("disposable"));
                    collaborator[0] = col;
                    list.addCollaborator(col); // lo aggiungiamo alla lista
                },
                "Andrea Verdi"
            );

            assertNotNull(collaborator[0], "Collaboratore non trovato nella CollaboratorsList");

            boolean alreadyPart = collaborator[0].isPartOf(staff, collaborator[0].getName());
            assertFalse(alreadyPart, "Collaboratore già parte dello staff");

            app.getStaffManager().addCollaboratorToStaff(collaborator[0], list);

            assertTrue(staff.getCollaboratorsList().getCollaborators().contains(collaborator[0]),
                "Collaboratore non presente nello staff");

            assertFalse(collaborator[0].isAccepted(), "Il campo 'accepted' dovrebbe essere false");

            final boolean[] foundInDb = {false};
            PersistenceManager.executeQuery(
                "SELECT * FROM Staff WHERE name = ? AND collaborator = 1",
                rs -> {
                    foundInDb[0] = true;
                    assertEquals(collaborator[0].getName(), rs.getString("name"));
                },
                collaborator[0].getName()
            );
            assertTrue(foundInDb[0], "Collaboratore non trovato nel DB come staff");

            final boolean[] stillInList = {false};
            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE name = ?",
                rs -> stillInList[0] = true,
                collaborator[0].getName()
            );
            assertFalse(stillInList[0], "Collaboratore ancora presente nella CollaboratorsList");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Exception in testAddCollaboratorToStaff: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    void testDeleteExistingCollaboratorFromStaff() {
        try {
            Staff staff = Staff.loadByName("PierGiorgio");
            assertNotNull(staff, "Lo staff 'PierGiorgio' non dovrebbe essere null.");

            app.getStaffManager().setCurrentStaff(staff);

            Collaborator collaborator = new Collaborator("Andrea Verdi");
            collaborator.setPhoneNumber("3331122333"); // Imposta dati aggiuntivi se necessario
            collaborator.setAccepted(true);
            collaborator.setDisposable(false);

            if (!collaborator.isPartOf(staff, collaborator.getName())) {
                CollaboratorsList list = new CollaboratorsList();
                list.addCollaborator(collaborator);
                app.getStaffManager().addCollaboratorToStaff(collaborator, list);
                collaborator.setAccepted(true);
                collaborator.setDisposable(false);
            }

            assertTrue(collaborator.isPartOf(staff, collaborator.getName()),
                    "Il collaboratore dovrebbe essere stato aggiunto allo staff.");

            app.getStaffManager().deleteCollaboratorFromStaff(collaborator);

            assertFalse(staff.getCollaboratorsList().getCollaborators().contains(collaborator),
            "Il collaboratore dovrebbe essere stato rimosso dallo staff.");

            final boolean[] stillExistsInStaff = {false};
            PersistenceManager.executeQuery(
                "SELECT * FROM Staff WHERE name = ? AND collaborator = 1",
                rs -> stillExistsInStaff[0] = true,
                collaborator.getName()
            );
            assertFalse(stillExistsInStaff[0], "Il collaboratore non dovrebbe più essere presente nella tabella Staff.");

            final boolean[] nowInCollaboratorsList = {false};
            final String[] phone = {null};
            final boolean[] accepted = {false};
            final boolean[] disposable = {true};

            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE name = ?",
                rs -> {
                    nowInCollaboratorsList[0] = true;
                    phone[0] = rs.getString("phone_number");
                    accepted[0] = rs.getBoolean("accepted");
                    disposable[0] = rs.getBoolean("disposable");
                },
                collaborator.getName()
            );

            assertTrue(nowInCollaboratorsList[0], "Il collaboratore dovrebbe essere presente nella CollaboratorsList.");
            assertEquals("3331122333", phone[0], "Il numero di telefono non corrisponde.");
            assertTrue(accepted[0], "Il campo 'accepted' dovrebbe essere true.");
            assertFalse(disposable[0], "Il campo 'disposable' dovrebbe essere false.");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Errore durante il test: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    void testModifyCollaboratorInList() {
        try {
            // Creazione lista collaboratori e inserimento collaboratore iniziale
            CollaboratorsList list = new CollaboratorsList();
            Collaborator original = new Collaborator("Mario Rossi", "1234567890", false, false);
            list.addCollaborator(original);

            // Inserimento nel DB e recupero ID
            PersistenceManager.executeUpdate(
                "INSERT INTO CollaboratorsList (name, phone_number, accepted, disposable) VALUES (?, ?, ?, ?)",
                original.getName(), original.getPhoneNumber(), original.isAccepted(), original.isDisposable()
            );

            // Recupero ID del collaboratore appena inserito
            PersistenceManager.executeQuery(
                "SELECT id FROM CollaboratorsList WHERE name = ?",
                rs -> original.setId(rs.getInt("id")),
                original.getName()
            );

            // Modifica dei dati
            String newName = "Mario Bianchi";
            String newPhone = "0987654321";
            Boolean newAccepted = true;
            Boolean newDisposable = true;

            app.getStaffManager().modifyCollaboratorsList(list, original, newName, newPhone, newAccepted, newDisposable);

            //Verifica in memoria
            assertEquals(newName, original.getName());
            assertEquals(newPhone, original.getPhoneNumber());
            assertTrue(original.isAccepted());
            assertTrue(original.isDisposable());

            //Verifica nel database
            final boolean[] found = {false};
            final String[] dbPhone = {null};
            final boolean[] dbAccepted = {false};
            final boolean[] dbDisposable = {false};

            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE id = ?",
                rs -> {
                    found[0] = true;
                    dbPhone[0] = rs.getString("phone_number");
                    dbAccepted[0] = rs.getBoolean("accepted");
                    dbDisposable[0] = rs.getBoolean("disposable");
                },
                original.getId()
            );

            assertTrue(found[0]);
            assertEquals(newPhone, dbPhone[0]);
            assertEquals(newAccepted, dbAccepted[0]);
            assertEquals(newDisposable, dbDisposable[0]);

        } catch (UseCaseLogicException e) {
            fail("Errore durante il test di modifica della CollaboratorsList: " + e.getMessage());
        }
    }

    @Test
    @Order(12)
    void testRemoveExistingCollaboratorFromList() {
        try {
            // Carica un collaboratore già presente nel database
            final Collaborator col = new Collaborator();
            final CollaboratorsList list = new CollaboratorsList();

            // Recupero del collaboratore dal DB
            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE name = ?",
                rs -> {
                    col.setId(rs.getInt("id"));
                    col.setName(rs.getString("name"));
                    col.setPhoneNumber(rs.getString("phone_number"));
                    col.setAccepted(rs.getBoolean("accepted"));
                    col.setDisposable(rs.getBoolean("disposable")); // viene comunque aggiornato sotto
                },
                "Giulia Neri"
            );

            // Verifica che l'ID sia stato effettivamente trovato
            assertTrue(col.getId() > 0, "Collaboratore 'Giulia Neri' non trovato nel database.");

            // Assicura che sia disposable
            col.setDisposable(true);

            // Aggiunta alla lista in memoria
            list.addCollaborator(col);
            assertTrue(list.isCollaboratorAdded(col), "Il collaboratore non è stato aggiunto alla lista in memoria.");

            // Esegui rimozione
            app.getStaffManager().removeCollaboratorFromList(list, col);

            // Verifica che non sia più nella lista in memoria
            assertFalse(list.getCollaborators().contains(col), "Il collaboratore è ancora presente nella lista in memoria.");

            // Verifica che non sia più nel database
            final boolean[] foundInDB = {false};
            PersistenceManager.executeQuery(
                "SELECT * FROM CollaboratorsList WHERE id = ?",
                rs -> foundInDB[0] = true,
                col.getId()
            );
            assertFalse(foundInDB[0], "Il collaboratore è ancora presente nel database.");

        } catch (UseCaseLogicException e) {
            fail("Errore durante il test di rimozione dalla CollaboratorsList: " + e.getMessage());
        }
    }

    @Test
    @Order(14)
    void testGrantHolidaysToStaff() {
        try {
            // Carica uno staff esistente e disponibile
            Staff staff = Staff.loadByName("PierGiorgio");
            assertNotNull(staff, "Staff 'PierGiorgio' should be loaded");
            assertTrue(staff.isAvailable(), "Staff should be available");

            app.getStaffManager().setCurrentStaff(staff);

            // Crea due date (1-10 luglio 2025)
            LocalDate startDate = LocalDate.of(2025, 7, 1);
            LocalDate endDate = LocalDate.of(2025, 7, 10);

            // Crea nuove ferie
            Holidays holidays = new Holidays(startDate, endDate);

            // Verifica che holidays NON sia ancora assegnata
            assertFalse(holidays.isAssigned(staff), "Holiday should not be assigned before grant");

            // Chiama il metodo da testare
            app.getStaffManager().grantHolidays(holidays);

            // Dopo la chiamata, holidays deve essere assegnata
            assertTrue(holidays.isAssigned(staff), "Holiday should now be assigned to staff");

            // Verifica che la ferie sia stata salvata nel DB
            final boolean[] holidaySaved = {false};
            String checkQuery = "SELECT * FROM Holidays WHERE staff_id = ? AND startDate = ? AND endDate = ?";
            PersistenceManager.executeQuery(checkQuery, rs -> {
                holidaySaved[0] = true;
            }, staff.getId(), holidays.getStartDate(), holidays.getEndDate());

            assertTrue(holidaySaved[0], "Holiday should be saved in the database");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Exception in testGrantHolidaysToStaff: " + e.getMessage());
        }
    }

    @Test
    @Order(15)
    void testModifyHolidaysForStaff() {
        try {
            // Carica uno staff esistente e disponibile
            Staff staff = Staff.loadByName("PierGiorgio");
            assertNotNull(staff, "Staff 'PierGiorgio' should be loaded");
            assertTrue(staff.isAvailable(), "Staff should be available");

            app.getStaffManager().setCurrentStaff(staff);

            // Assegna ferie iniziali (10-15 luglio 2025)
            LocalDate initialStart = LocalDate.of(2025, 7, 10);
            LocalDate initialEnd = LocalDate.of(2025, 7, 15);
            Holidays holidays = new Holidays(initialStart, initialEnd);

            // Associa le ferie allo staff
            app.getStaffManager().grantHolidays(holidays);
            assertTrue(holidays.isAssigned(staff), "Holiday should be assigned before modification");

            // Nuove date da assegnare (12-18 luglio 2025)
            LocalDate newStart = LocalDate.of(2025, 7, 12);
            LocalDate newEnd = LocalDate.of(2025, 7, 18);

            // Esegui la modifica
            app.getStaffManager().modifyHolidays(holidays, newStart, newEnd);

            // Verifica che le date siano state aggiornate
            assertEquals(newStart, holidays.getStartDate(), "Start date should be updated");
            assertEquals(newEnd, holidays.getEndDate(), "End date should be updated");

            // Verifica nel database che le nuove date siano presenti
            final boolean[] updatedInDb = {false};
            String query = "SELECT * FROM Holidays WHERE id = ? AND startDate = ? AND endDate = ?";
            PersistenceManager.executeQuery(query, rs -> {
                updatedInDb[0] = true;
            }, holidays.getId(), newStart, newEnd);

            assertTrue(updatedInDb[0], "Modified holiday should be updated in the database");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Exception in testModifyHolidaysForStaff: " + e.getMessage());
        }
    }

    @Test
    @Order(16)
    void testDeleteHolidaysForStaff() {
        try {
            // Carica uno staff esistente e disponibile
            Staff staff = Staff.loadByName("Piero");
            assertNotNull(staff, "Staff 'Piero' should be loaded");
            assertTrue(staff.isAvailable(), "Staff should be available");

            app.getStaffManager().setCurrentStaff(staff);

            // Crea e assegna ferie (20-25 luglio 2025)
            LocalDate startDate = LocalDate.of(2025, 7, 20);
            LocalDate endDate = LocalDate.of(2025, 7, 25);
            Holidays holidays = new Holidays(startDate, endDate);

            app.getStaffManager().grantHolidays(holidays);
            assertTrue(holidays.isAssigned(staff), "Holiday should be assigned before deletion");

            // Chiama il metodo per cancellare le ferie
            app.getStaffManager().deleteHolidays(holidays);

            // Dopo la cancellazione, le ferie NON devono più essere assegnate
            assertFalse(holidays.isAssigned(staff), "Holiday should no longer be assigned to staff");

            // Verifica che non ci siano più ferie nel database con quell'id
            final boolean[] existsInDb = {false};
            String checkQuery = "SELECT * FROM Holidays WHERE id = ?";
            PersistenceManager.executeQuery(checkQuery, rs -> {
                existsInDb[0] = true;
            }, holidays.getId());

            assertFalse(existsInDb[0], "Holiday should be deleted from the database");

        } catch (SQLException | UseCaseLogicException e) {
            fail("Exception in testDeleteHolidaysForStaff: " + e.getMessage());
        }
    }


    @Test
    @Order(17)
    void testAddNotesToEvent() {
        try {
            // Carica un evento esistente dal DB
            Event event = Event.loadByName("Matrimonio 2025");
            assertNotNull(event, "Matrimonio 2025' dovrebbe esistere");

            // Imposta l'evento come corrente
            app.getStaffManager().setCurrentEvent(event);

            // Crea una nuova nota da aggiungere
            String noteContent = "Controllare il numero di sedie disponibili";
            Notes note = new Notes(noteContent, event);

            // Aggiunge la nota
            app.getStaffManager().addNotesToCurrentEvent(note);

            // Verifica che la nota sia presente nel DB
            final boolean[] noteSaved = {false};
            String checkQuery = "SELECT * FROM Notes WHERE content = ? AND event_id = ?";
            PersistenceManager.executeQuery(checkQuery, rs -> {
                noteSaved[0] = true;
            }, noteContent, event.getId());

            assertTrue(noteSaved[0], "La nota dovrebbe essere stata salvata nel DB");

        } catch (UseCaseLogicException e) {
            fail("Exception in testAddNotesToCurrentEvent: " + e.getMessage());
        }
    }

    @Test
    @Order(18)
    void testDeleteNotesFromEvent() {
        try {
            // Carica un evento esistente
            Event event = Event.loadByName("Matrimonio 2025");
            assertNotNull(event, "Evento 'Matrimonio 2025' dovrebbe esistere");

            // Imposta l'evento corrente
            StaffManager manager = app.getStaffManager();
            manager.setCurrentEvent(event);

            // Carica una nota esistente legata all'evento
            final Notes[] noteToDelete = {null};
            String selectQuery = "SELECT * FROM Notes WHERE event_id = ? LIMIT 1";
            PersistenceManager.executeQuery(selectQuery, rs -> {
                Notes n = new Notes(rs.getString("content"), event);
                n.setId(rs.getInt("id"));
                noteToDelete[0] = n;
            }, event.getId());

            event.addNotes(noteToDelete[0]);

            assertNotNull(noteToDelete[0], "Dovrebbe esserci almeno una nota legata all'evento");

            // Assicurati che non sia occupata
            noteToDelete[0].setOccupied(false);

            // Elimina la nota
            manager.deleteNotesFromEvent(noteToDelete[0]);

            // Verifica che sia stata cancellata dal DB
            final boolean[] stillExists = {false};
            String checkQuery = "SELECT * FROM Notes WHERE id = ?";
            PersistenceManager.executeQuery(checkQuery, rs -> {
                stillExists[0] = true;
            }, noteToDelete[0].getId());

            assertFalse(stillExists[0], "La nota dovrebbe essere stata eliminata dal database");

        } catch (UseCaseLogicException e) {
            fail("Exception in testDeleteNotesFromEvent: " + e.getMessage());
        }
    }

    @Test
    @Order(19)
    void testModifyNotesOfEvent() {
        try {
            // Carica un evento esistente
            Event event = Event.loadByName("Gala Aziendale Annuale");
            assertNotNull(event, "Evento 'Gala Aziendale Annuale' dovrebbe esistere");

            // Imposta l'evento corrente
            StaffManager manager = app.getStaffManager();
            manager.setCurrentEvent(event);

            // Carica una nota esistente legata all'evento
            final Notes[] noteToModify = {null};
            String selectQuery = "SELECT * FROM Notes WHERE event_id = ? LIMIT 1";
            PersistenceManager.executeQuery(selectQuery, rs -> {
                Notes n = new Notes(rs.getString("content"), event);
                n.setId(rs.getInt("id"));
                n.setOccupied(rs.getBoolean("occupied"));
                noteToModify[0] = n;
            }, event.getId());

            assertNotNull(noteToModify[0], "Dovrebbe esserci almeno una nota legata all'evento");

            // Associa la nota all'evento (necessario se currentEvent.hasNotes() controlla la lista interna)
            event.addNotes(noteToModify[0]);

            // Verifica che la nota non sia occupata per poterla modificare
            noteToModify[0].setOccupied(false);

            // Cambia il contenuto della nota
            String newContent = noteToModify[0].getContent() + " - aggiornato";

            noteToModify[0].setContent(newContent);

            // Modifica la nota tramite il manager
            manager.modifyNotesOfEvent(noteToModify[0]);

            // Verifica che il contenuto sia stato aggiornato nel DB
            final String[] contentFromDb = {null};
            String checkQuery = "SELECT content FROM Notes WHERE id = ?";
            PersistenceManager.executeQuery(checkQuery, rs -> {
                contentFromDb[0] = rs.getString("content");
            }, noteToModify[0].getId());

            assertEquals(newContent, contentFromDb[0], "Il contenuto della nota dovrebbe essere aggiornato nel DB");

        } catch (UseCaseLogicException e) {
            fail("Exception in testModifyNotesOfEvent: " + e.getMessage());
        }
    }
//////////////////////////////////////////ALTRI TEST////////////////////////////////////////////////////////
}