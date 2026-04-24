package sessionhub.bootstrap;

import sessionhub.core.ActivitySession;
import sessionhub.core.DayCategory;
import sessionhub.core.Enrollment;
import sessionhub.core.Participant;
import sessionhub.core.SessionWindow;
import sessionhub.engine.EnrollmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Loads startup data for the SessionHub application.
 */
public class DataLoader {

    private static final double PRICE_POWER_SCULPT = 15.00;
    private static final double PRICE_METABOLIC_BURN = 20.00;
    private static final double PRICE_DYNAMIC_STRETCH = 12.00;
    private static final double PRICE_FUNCTIONAL_FITNESS = 18.00;

    public List<Participant> loadParticipants() {
        return Arrays.asList(
                new Participant("P01", "Alice Carter"),
                new Participant("P02", "Brian Stone"),
                new Participant("P03", "Clara Hughes"),
                new Participant("P04", "David Wong"),
                new Participant("P05", "Ella Johnson"),
                new Participant("P06", "Franklin Moore"),
                new Participant("P07", "Grace Patel"),
                new Participant("P08", "Henry Smith"),
                new Participant("P09", "Isla Brown"),
                new Participant("P10", "Jack Wilson")
        );
    }

    public List<ActivitySession> loadSessions() {
        List<ActivitySession> sessions = new ArrayList<>();
        int counter = 1;

        for (int week = 1; week <= 8; week++) {
            sessions.add(new ActivitySession("S" + fmt(counter++), "Power Sculpt",
                    DayCategory.SATURDAY, SessionWindow.MORNING, week, PRICE_POWER_SCULPT));
            sessions.add(new ActivitySession("S" + fmt(counter++), "Metabolic Burn",
                    DayCategory.SATURDAY, SessionWindow.AFTERNOON, week, PRICE_METABOLIC_BURN));
            sessions.add(new ActivitySession("S" + fmt(counter++), "Functional Fitness",
                    DayCategory.SATURDAY, SessionWindow.EVENING, week, PRICE_FUNCTIONAL_FITNESS));
            sessions.add(new ActivitySession("S" + fmt(counter++), "Dynamic Stretch",
                    DayCategory.SUNDAY, SessionWindow.MORNING, week, PRICE_DYNAMIC_STRETCH));
            sessions.add(new ActivitySession("S" + fmt(counter++), "Power Sculpt",
                    DayCategory.SUNDAY, SessionWindow.AFTERNOON, week, PRICE_POWER_SCULPT));
            sessions.add(new ActivitySession("S" + fmt(counter++), "Metabolic Burn",
                    DayCategory.SUNDAY, SessionWindow.EVENING, week, PRICE_METABOLIC_BURN));
        }

        return sessions;
    }

    public void seedAttendance(EnrollmentManager manager,
                               List<ActivitySession> sessions,
                               List<Participant> participants) {

        attend(manager, findParticipant(participants, "P01"), findSession(sessions, "S001"), 5, "Fantastic energy from the instructor!");
        attend(manager, findParticipant(participants, "P02"), findSession(sessions, "S001"), 4, "Great strength workout, felt results.");
        attend(manager, findParticipant(participants, "P03"), findSession(sessions, "S001"), 5, "Best start to the weekend!");

        attend(manager, findParticipant(participants, "P04"), findSession(sessions, "S002"), 4, "High intensity and very engaging.");
        attend(manager, findParticipant(participants, "P05"), findSession(sessions, "S002"), 3, "Good session, a bit fast-paced for me.");

        attend(manager, findParticipant(participants, "P06"), findSession(sessions, "S004"), 5, "Perfect recovery session. Very calming.");
        attend(manager, findParticipant(participants, "P07"), findSession(sessions, "S004"), 4, "Well structured and relaxing.");

        attend(manager, findParticipant(participants, "P01"), findSession(sessions, "S009"), 4, "Full body workout with great variety.");
        attend(manager, findParticipant(participants, "P08"), findSession(sessions, "S009"), 5, "Outstanding class, pushing my limits.");

        attend(manager, findParticipant(participants, "P09"), findSession(sessions, "S012"), 3, "Tough but fair. Would attend again.");
        attend(manager, findParticipant(participants, "P10"), findSession(sessions, "S012"), 5, "Excellent class, loved the challenge.");

        attend(manager, findParticipant(participants, "P02"), findSession(sessions, "S013"), 5, "Even better second time around.");
        attend(manager, findParticipant(participants, "P04"), findSession(sessions, "S013"), 4, "Consistent quality. No complaints.");

        attend(manager, findParticipant(participants, "P05"), findSession(sessions, "S016"), 4, "Helped my flexibility noticeably.");
        attend(manager, findParticipant(participants, "P06"), findSession(sessions, "S016"), 3, "Good, but the music was too loud.");

        attend(manager, findParticipant(participants, "P07"), findSession(sessions, "S020"), 5, "Instructor was incredibly motivating.");
        attend(manager, findParticipant(participants, "P08"), findSession(sessions, "S020"), 4, "Great interval structure. Very effective.");

        attend(manager, findParticipant(participants, "P09"), findSession(sessions, "S023"), 5, "My favourite class so far.");
        attend(manager, findParticipant(participants, "P10"), findSession(sessions, "S023"), 4, "Good workout. Will book again.");

        attend(manager, findParticipant(participants, "P03"), findSession(sessions, "S025"), 5, "Surpassed my expectations again.");
        attend(manager, findParticipant(participants, "P01"), findSession(sessions, "S027"), 4, "Challenging and rewarding.");
        attend(manager, findParticipant(participants, "P02"), findSession(sessions, "S030"), 5, "High-energy and well-paced session.");

        silentBook(manager, findParticipant(participants, "P03"), findSession(sessions, "S031"));
        silentBook(manager, findParticipant(participants, "P04"), findSession(sessions, "S034"));
        silentBook(manager, findParticipant(participants, "P05"), findSession(sessions, "S037"));
        silentBook(manager, findParticipant(participants, "P06"), findSession(sessions, "S044"));
    }

    private String fmt(int n) {
        return String.format("%03d", n);
    }

    private Participant findParticipant(List<Participant> list, String id) {
        return list.stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow();
    }

    private ActivitySession findSession(List<ActivitySession> list, String id) {
        return list.stream().filter(s -> s.getId().equals(id)).findFirst().orElseThrow();
    }

    private void attend(EnrollmentManager manager, Participant participant, ActivitySession session,
                        int rating, String comment) {
        Enrollment enrollment = manager.createEnrollment(participant, session);
        if (enrollment != null) {
            manager.confirmAttendance(enrollment.getId(), rating, comment);
        }
    }

    private void silentBook(EnrollmentManager manager, Participant participant, ActivitySession session) {
        manager.createEnrollment(participant, session);
    }
}
