package sessionhub.core;

/**
 * Links a Participant to an ActivitySession.
 *
 * ID format : ENR-XXXX (e.g. ENR-0001)
 * IDs are sequential and never reused after cancellation.
 *
 * Lifecycle: ENROLLED → UPDATED | CANCELLED | COMPLETED
 */
public class Enrollment {

    private String                 enrollmentId;
    private final Participant      participant;
    private       ActivitySession  session;
    private       EnrollmentStatus status;

    public Enrollment(String enrollmentId, Participant participant, ActivitySession session) {
        this.enrollmentId = enrollmentId;
        this.participant  = participant;
        this.session      = session;
        this.status       = EnrollmentStatus.ENROLLED;
    }

    // ── State transitions ─────────────────────────────────────────────────────

    public void updateSession(String newEnrollmentId, ActivitySession newSession) {
        this.enrollmentId = newEnrollmentId;
        this.session = newSession;
        this.status  = EnrollmentStatus.UPDATED;
    }

    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
    }

    public void markCompleted() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    // ── Predicates ────────────────────────────────────────────────────────────

    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED || status == EnrollmentStatus.UPDATED;
    }

    public boolean isCancelled()  { return status == EnrollmentStatus.CANCELLED;  }
    public boolean isCompleted()  { return status == EnrollmentStatus.COMPLETED;  }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String           getId()          { return enrollmentId; }
    public Participant      getParticipant() { return participant;  }
    public ActivitySession  getSession()     { return session;      }
    public EnrollmentStatus getStatus()      { return status;       }

    @Override
    public String toString() {
        return String.format("Enrollment[%s] Participant: %s | Session: %s | Status: %s",
                enrollmentId, participant.getName(), session.getSessionId(), status);
    }
}
