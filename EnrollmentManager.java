package sessionhub.engine;

import sessionhub.core.ActivitySession;
import sessionhub.core.DayCategory;
import sessionhub.core.Enrollment;
import sessionhub.core.FeedbackNote;
import sessionhub.core.Participant;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central booking service for SessionHub.
 */
public class EnrollmentManager {

    private final Map<String, Enrollment> enrollments = new LinkedHashMap<>();
    private int idCounter = 1;

    public Enrollment createEnrollment(Participant participant, ActivitySession session) {
        requireCapacity(session);
        requireNoDuplicate(participant, session);
        requireNoTimeConflict(participant, session, null);

        String id = generateId();
        Enrollment enrollment = new Enrollment(id, participant, session);
        enrollments.put(id, enrollment);
        session.addEnrollment(enrollment);
        return enrollment;
    }

    public Enrollment updateEnrollment(String enrollmentId, ActivitySession newSession) {
        Enrollment existing = requireActiveEnrollment(enrollmentId);

        if (existing.getSession().getSessionId().equalsIgnoreCase(newSession.getSessionId())) {
            throw new IllegalArgumentException("Choose a different session for the reschedule request.");
        }

        requireCapacity(newSession);
        requireNoTimeConflict(existing.getParticipant(), newSession, enrollmentId);

        String previousId = existing.getId();
        String replacementId = generateId();

        existing.getSession().removeEnrollment(previousId);
        newSession.addEnrollment(existing);
        enrollments.remove(previousId);
        existing.updateSession(replacementId, newSession);
        enrollments.put(replacementId, existing);

        return existing;
    }

    public Enrollment cancelEnrollment(String enrollmentId) {
        Enrollment existing = requireActiveEnrollment(enrollmentId);
        existing.getSession().removeEnrollment(enrollmentId);
        existing.cancel();
        return existing;
    }

    public Enrollment confirmAttendance(String enrollmentId, int rating, String comment) {
        Enrollment existing = enrollments.get(enrollmentId);
        if (existing == null) {
            throw new IllegalArgumentException("Reservation code not found.");
        }
        if (existing.isCancelled()) {
            throw new IllegalStateException("Cancelled reservations cannot be checked in.");
        }
        if (existing.isCompleted()) {
            throw new IllegalStateException("Attendance has already been recorded for this reservation.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must stay between 1 and 5.");
        }

        existing.markCompleted();
        FeedbackNote note = new FeedbackNote(
                "FB-" + enrollmentId,
                existing.getParticipant(),
                existing.getSession(),
                rating,
                comment == null || comment.isBlank() ? "No additional comment." : comment.trim());
        existing.getSession().addFeedback(note);
        return existing;
    }

    public List<ActivitySession> viewTimetableByDay(List<ActivitySession> sessions, DayCategory day) {
        return sessions.stream()
                .filter(session -> session.getDay() == day)
                .sorted(Comparator.comparingInt(ActivitySession::getWeekNumber)
                        .thenComparing(ActivitySession::getWindow))
                .collect(Collectors.toList());
    }

    public List<ActivitySession> viewTimetableByActivity(List<ActivitySession> sessions, String activityName) {
        return sessions.stream()
                .filter(session -> session.getActivityName().equalsIgnoreCase(activityName))
                .sorted(Comparator.comparingInt(ActivitySession::getWeekNumber)
                        .thenComparing(ActivitySession::getDay)
                        .thenComparing(ActivitySession::getWindow))
                .collect(Collectors.toList());
    }

    public Optional<Enrollment> findById(String enrollmentId) {
        return Optional.ofNullable(enrollments.get(enrollmentId));
    }

    public List<Enrollment> activeEnrollmentsFor(String participantId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.getParticipant().getId().equalsIgnoreCase(participantId))
                .filter(Enrollment::isActive)
                .sorted(Comparator.comparing((Enrollment e) -> e.getSession().getWeekNumber())
                        .thenComparing(e -> e.getSession().getDay())
                        .thenComparing(e -> e.getSession().getWindow()))
                .collect(Collectors.toList());
    }

    public List<Enrollment> enrollmentsFor(String participantId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.getParticipant().getId().equalsIgnoreCase(participantId))
                .sorted(Comparator.comparing((Enrollment e) -> e.getSession().getWeekNumber())
                        .thenComparing(e -> e.getSession().getDay())
                        .thenComparing(e -> e.getSession().getWindow()))
                .collect(Collectors.toList());
    }

    public Collection<Enrollment> allEnrollments() {
        return Collections.unmodifiableCollection(enrollments.values());
    }

    private void requireCapacity(ActivitySession session) {
        if (!session.hasCapacity()) {
            throw new IllegalStateException("This session is already full.");
        }
    }

    private void requireNoDuplicate(Participant participant, ActivitySession session) {
        boolean duplicate = enrollments.values().stream()
                .anyMatch(enrollment -> enrollment.getParticipant().getId().equalsIgnoreCase(participant.getId())
                        && enrollment.getSession().getSessionId().equalsIgnoreCase(session.getSessionId())
                        && enrollment.isActive());
        if (duplicate) {
            throw new IllegalStateException("The participant already has this session reserved.");
        }
    }

    private void requireNoTimeConflict(Participant participant, ActivitySession session, String ignoredEnrollmentId) {
        Enrollment conflict = enrollments.values().stream()
                .filter(Enrollment::isActive)
                .filter(enrollment -> enrollment.getParticipant().getId().equalsIgnoreCase(participant.getId()))
                .filter(enrollment -> ignoredEnrollmentId == null || !enrollment.getId().equalsIgnoreCase(ignoredEnrollmentId))
                .filter(enrollment -> enrollment.getSession().getDay() == session.getDay())
                .filter(enrollment -> enrollment.getSession().getWindow() == session.getWindow())
                .filter(enrollment -> enrollment.getSession().getWeekNumber() == session.getWeekNumber())
                .findFirst()
                .orElse(null);

        if (conflict != null) {
            ActivitySession existing = conflict.getSession();
            throw new IllegalStateException(participant.getName()
                    + " already has " + existing.getActivityName()
                    + " booked in Week " + existing.getWeekNumber()
                    + " on " + existing.getDay()
                    + " during the " + existing.getWindow()
                    + " slot. Two different exercises cannot be booked in the same time slot.");
        }
    }

    private Enrollment requireActiveEnrollment(String enrollmentId) {
        Enrollment existing = enrollments.get(enrollmentId);
        if (existing == null) {
            throw new IllegalArgumentException("Reservation code not found.");
        }
        if (!existing.isActive()) {
            throw new IllegalStateException("Only active reservations can be updated.");
        }
        return existing;
    }

    private String generateId() {
        return String.format("ENR-%04d", idCounter++);
    }
}
