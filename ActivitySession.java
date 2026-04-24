package sessionhub.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single scheduled group exercise session at Furzefield Leisure Centre.
 *
 * Business rules enforced here:
 *   - Maximum 4 participants per session (CAPACITY_LIMIT)
 *   - Price is fixed per activity type regardless of time slot
 *   - Feedback notes are only appended after attendance is confirmed
 */
public class ActivitySession {

    public static final int CAPACITY_LIMIT = 4;

    private final String        sessionId;
    private final String        activityName;
    private final DayCategory   day;
    private final SessionWindow window;
    private final int           weekNumber;
    private final double        price;

    private final List<Enrollment>    enrollments   = new ArrayList<>();
    private final List<FeedbackNote>  feedbackNotes = new ArrayList<>();

    public ActivitySession(String sessionId, String activityName,
                           DayCategory day, SessionWindow window,
                           int weekNumber, double price) {
        this.sessionId    = sessionId;
        this.activityName = activityName;
        this.day          = day;
        this.window       = window;
        this.weekNumber   = weekNumber;
        this.price        = price;
    }

    // ── Capacity ──────────────────────────────────────────────────────────────

    public boolean hasCapacity() {
        return enrollments.size() < CAPACITY_LIMIT;
    }

    public int availableSpots() {
        return CAPACITY_LIMIT - enrollments.size();
    }

    // ── Enrollment management ─────────────────────────────────────────────────

    public void addEnrollment(Enrollment e) {
        if (!hasCapacity()) {
            throw new IllegalStateException("Error: Session capacity exceeded.");
        }
        enrollments.add(e);
    }

    public void removeEnrollment(String enrollmentId) {
        enrollments.removeIf(e -> e.getId().equals(enrollmentId));
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    public void addFeedback(FeedbackNote note) {
        feedbackNotes.add(note);
    }

    // ── Calculations ──────────────────────────────────────────────────────────

    /**
     * Mean satisfaction rating from all submitted feedback notes.
     * Returns 0.0 if no feedback has been submitted.
     */
    public double calculateAverageRating() {
        if (feedbackNotes.isEmpty()) return 0.0;
        return feedbackNotes.stream()
                .mapToInt(FeedbackNote::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Total income from COMPLETED enrollments only.
     */
    public double calculateIncome() {
        return enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count() * price;
    }

    public int completedAttendeeCount() {
        return (int) enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String        getSessionId()    { return sessionId;    }
    public String        getId()           { return sessionId;    }
    public String        getActivityName() { return activityName; }
    public DayCategory   getDay()          { return day;          }
    public SessionWindow getWindow()       { return window;       }
    public int           getWeekNumber()   { return weekNumber;   }
    public double        getPrice()        { return price;        }

    public List<Enrollment>   getEnrollments()   { return Collections.unmodifiableList(enrollments);   }
    public List<FeedbackNote> getFeedbackNotes()  { return Collections.unmodifiableList(feedbackNotes); }

    @Override
    public String toString() {
        return String.format("%-6s | Wk%-2d | %-9s | %-10s | %-20s | £%-5.2f | Spots: %d/%d",
                sessionId, weekNumber, day, window, activityName,
                price, availableSpots(), CAPACITY_LIMIT);
    }
}
