package sessionhub.core;

/**
 * Represents a registered member of Furzefield Leisure Centre.
 * Identity only — all booking state is held in Enrollment.
 */
public class Participant {

    private final String participantId;
    private final String name;

    public Participant(String participantId, String name) {
        this.participantId = participantId;
        this.name = name;
    }

    public String getId()   { return participantId; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "[" + participantId + "] " + name;
    }
}
