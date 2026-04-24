package sessionhub.core;

/**
 * Lifecycle states for an Enrollment.
 *
 * ENROLLED  - initial state on creation
 * UPDATED   - session has been transferred
 * CANCELLED - booking withdrawn; ID retired
 * COMPLETED - session attended; feedback submitted
 *
 * Only COMPLETED enrollments count in reports.
 */
public enum EnrollmentStatus {
    ENROLLED,
    UPDATED,
    CANCELLED,
    COMPLETED
}
