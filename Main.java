package sessionhub.app;

import sessionhub.bootstrap.DataLoader;
import sessionhub.core.ActivitySession;
import sessionhub.core.Participant;
import sessionhub.engine.EnrollmentManager;
import sessionhub.engine.ReportGenerator;
import sessionhub.ui.SessionHubFrame;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.List;

/**
 * Desktop entry point for SessionHub.
 */
public class Main {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("SessionHub Desktop needs a graphical environment. Open it from IntelliJ or a desktop terminal.");
            return;
        }

        DataLoader loader = new DataLoader();
        List<Participant> participants = loader.loadParticipants();
        List<ActivitySession> sessions = loader.loadSessions();

        EnrollmentManager manager = new EnrollmentManager();
        ReportGenerator reports = new ReportGenerator();
        loader.seedAttendance(manager, sessions, participants);

        SwingUtilities.invokeLater(() -> {
            SessionHubFrame frame = new SessionHubFrame(manager, reports, sessions, participants);
            frame.setVisible(true);
        });
    }
}
