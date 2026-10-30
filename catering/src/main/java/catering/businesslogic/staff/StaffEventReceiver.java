package catering.businesslogic.staff;

import catering.businesslogic.event.Notes;
import catering.businesslogic.holidays.Holidays;
import catering.businesslogic.user.User;

/*
 * Interfaccia per i metodi di aggiornamento
 */
public interface StaffEventReceiver {

    void updateStaffCreated(Staff st);

    void updateStaffChanged(Staff st);
    
    void updateStaffDeleted(Staff st);

    void updateContactModify(Contact contact);

    void updateContactMarked(Contact contact);

    void updateContactAdded(Contact contact);

    void updateContactDeleted(Contact contact);

    void updateRoleAdded(User user, Role rol);

    void updateRoleDeleted(User user, Role rol);

    void updateCollaboratorsListModified(CollaboratorsList list, Collaborator col);

    void updateCollaboratorDeletedFromList(CollaboratorsList list, Collaborator col);

    void updateCollaboratorAdded(Collaborator col);

    void updateCollaboratorDeleted(Collaborator col);

    void updateHolidaysAdded(Holidays holidays);

    void updateHolidaysModified(Holidays holidays);

    void updateHolidaysDeleted(Holidays holidays);

    void updateNotesAdded(Notes notes);

    void updateNotesDeleted(Notes notes);

    void updateNotesModified(Notes notes);
}
