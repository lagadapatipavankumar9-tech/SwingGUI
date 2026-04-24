package sessionhub;

import org.junit.jupiter.api.Test;
import sessionhub.bootstrap.DataLoader;
import sessionhub.core.ActivitySession;
import sessionhub.core.Enrollment;
import sessionhub.core.EnrollmentStatus;
import sessionhub.core.Participant;
import sessionhub.engine.EnrollmentManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SessionHubTest {

    @Test
    void updateEnrollmentAssignsNewEnrollmentId() {
        DataLoader loader = new DataLoader();
        List<Participant> participants = loader.loadParticipants();
        List<ActivitySession> sessions = loader.loadSessions();
        EnrollmentManager manager = new EnrollmentManager();

        Participant participant = participants.get(0);
        ActivitySession originalSession = sessions.get(0);
        ActivitySession replacementSession = sessions.get(1);

        Enrollment enrollment = manager.createEnrollment(participant, originalSession);
        assertNotNull(enrollment);

        String originalId = enrollment.getId();
        manager.updateEnrollment(originalId, replacementSession);

        assertTrue(manager.findById(originalId).isEmpty());

        Enrollment updated = manager.allEnrollments().stream()
                .filter(item -> item.getParticipant().getId().equals(participant.getId()))
                .findFirst()
                .orElseThrow();

        assertNotEquals(originalId, updated.getId());
        assertEquals(replacementSession.getSessionId(), updated.getSession().getSessionId());
        assertEquals(EnrollmentStatus.UPDATED, updated.getStatus());
    }
}
