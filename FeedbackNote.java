package sessionhub.core;

/**
 * An immutable post-attendance feedback note submitted by a participant.
 *
 * Rating scale:
 *   1 = Very Dissatisfied
 *   2 = Dissatisfied
 *   3 = Neutral
 *   4 = Satisfied
 *   5 = Very Satisfied
 */
public class FeedbackNote {

    private final String feedbackId;
    private final Participant participant;
    private final ActivitySession session;
    private final int rating;
    private final String comment;

    public FeedbackNote(String feedbackId, Participant participant,
                        ActivitySession session, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5. Received: " + rating);
        }
        this.feedbackId  = feedbackId;
        this.participant = participant;
        this.session     = session;
        this.rating      = rating;
        this.comment     = comment;
    }

    public String       getFeedbackId()  { return feedbackId;  }
    public Participant  getParticipant() { return participant; }
    public ActivitySession getSession()  { return session;     }
    public int          getRating()      { return rating;      }
    public String       getComment()     { return comment;     }

    @Override
    public String toString() {
        return participant.getName() + " rated " + rating + "/5 — \"" + comment + "\"";
    }
}
