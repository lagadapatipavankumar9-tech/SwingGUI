package sessionhub.ui;

import sessionhub.core.ActivitySession;
import sessionhub.core.DayCategory;
import sessionhub.core.Enrollment;
import sessionhub.core.Participant;
import sessionhub.engine.EnrollmentManager;
import sessionhub.engine.ReportGenerator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Swing-based SessionHub desktop application.
 */
public class SessionHubFrame extends JFrame {

    private static final Color PAGE_BACKGROUND = new Color(245, 241, 233);
    private static final Color PANEL_BACKGROUND = new Color(255, 251, 245);
    private static final Color HEADER_BACKGROUND = new Color(34, 78, 90);
    private static final Color HEADER_TEXT = new Color(244, 238, 223);
    private static final Color ACCENT = new Color(210, 122, 74);
    private static final Color ACCENT_DARK = new Color(154, 82, 42);
    private static final Color SOFT_BORDER = new Color(219, 205, 188);
    private static final Color LIST_BACKGROUND = new Color(252, 248, 242);
    private static final Color STATUS_BACKGROUND = new Color(242, 235, 222);
    private static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 28);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 13);

    private final EnrollmentManager manager;
    private final ReportGenerator reports;
    private final List<ActivitySession> sessions;
    private final List<Participant> participants;

    private final DefaultListModel<ActivitySession> scheduleModel = new DefaultListModel<>();
    private final DefaultListModel<ActivitySession> sessionCatalogModel = new DefaultListModel<>();
    private final DefaultListModel<Enrollment> reservationModel = new DefaultListModel<>();
    private final DefaultListModel<Enrollment> feedbackModel = new DefaultListModel<>();

    private final JComboBox<String> dayFilter = new JComboBox<>(new String[]{"All days", "Saturday", "Sunday"});
    private final JComboBox<String> activityFilter = new JComboBox<>();
    private final JComboBox<Participant> reservationParticipantBox = new JComboBox<>();
    private final JComboBox<Participant> feedbackParticipantBox = new JComboBox<>();

    private final JList<ActivitySession> scheduleList = new JList<>(scheduleModel);
    private final JList<ActivitySession> sessionCatalogList = new JList<>(sessionCatalogModel);
    private final JList<Enrollment> reservationList = new JList<>(reservationModel);
    private final JList<Enrollment> feedbackList = new JList<>(feedbackModel);

    private final JTextArea scheduleDetails = createTextArea();
    private final JTextArea attendanceArea = createTextArea();
    private final JTextArea incomeArea = createTextArea();
    private final JTextArea commentArea = createTextArea();
    private final JSpinner ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
    private final JTextArea statusArea = createTextArea();

    private final JLabel participantsMetric = createMetricValue();
    private final JLabel sessionsMetric = createMetricValue();
    private final JLabel activeMetric = createMetricValue();
    private final JLabel completedMetric = createMetricValue();

    public SessionHubFrame(EnrollmentManager manager,
                           ReportGenerator reports,
                           List<ActivitySession> sessions,
                           List<Participant> participants) {
        this.manager = manager;
        this.reports = reports;
        this.sessions = sessions;
        this.participants = participants;
        this.commentArea.setEditable(true);

        configureFrame();
        setContentPane(buildRoot());
        refreshEverything();
    }

    private void configureFrame() {
        setTitle("SessionHub Desktop Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1260, 820));
        setLocationRelativeTo(null);
    }

    private JPanel buildRoot() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(PAGE_BACKGROUND);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(HEADER_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(28, 63, 73), 1),
                new EmptyBorder(18, 22, 18, 22)));

        JPanel textPanel = new JPanel(new BorderLayout(0, 8));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("SessionHub Control Deck");
        title.setFont(TITLE_FONT);
        title.setForeground(HEADER_TEXT);

        JLabel subtitle = new JLabel("Desktop workspace for bookings, attendance logging, and management reporting.");
        subtitle.setFont(SUBTITLE_FONT);
        subtitle.setForeground(new Color(220, 228, 224));

        textPanel.add(title, BorderLayout.NORTH);
        textPanel.add(subtitle, BorderLayout.CENTER);

        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0));
        metrics.setOpaque(false);
        metrics.add(createMetricCard("Participants", participantsMetric));
        metrics.add(createMetricCard("Timetable Sessions", sessionsMetric));
        metrics.add(createMetricCard("Active Reservations", activeMetric));
        metrics.add(createMetricCard("Completed Check-Ins", completedMetric));

        panel.add(textPanel, BorderLayout.NORTH);
        panel.add(metrics, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(PANEL_BACKGROUND);
        tabs.addTab("Schedule Studio", buildScheduleTab());
        tabs.addTab("Reservations Desk", buildReservationTab());
        tabs.addTab("Feedback Lounge", buildFeedbackTab());
        tabs.addTab("Reports Board", buildReportsTab());
        return tabs;
    }

    private JPanel buildScheduleTab() {
        JPanel panel = createTabPanel();

        JPanel filters = createToolbarPanel();
        filters.add(toolbarLabel("Day"));
        filters.add(styleCombo(dayFilter));
        filters.add(toolbarLabel("Activity"));
        filters.add(styleCombo(activityFilter));

        JButton apply = createPrimaryButton("Refresh Schedule");
        apply.addActionListener(event -> refreshSchedule());
        filters.add(apply);

        scheduleList.setCellRenderer(scheduleRenderer());
        scheduleList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                renderScheduleDetails(scheduleList.getSelectedValue());
            }
        });

        JSplitPane split = createSplitPane(
                borderedPane("Available Programme", createScrollPane(scheduleList)),
                borderedPane("Session Detail Card", createScrollPane(scheduleDetails)),
                0.55);

        panel.add(filters, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReservationTab() {
        JPanel panel = createTabPanel();

        reservationParticipantBox.setModel(new DefaultComboBoxModel<>(participants.toArray(new Participant[0])));
        reservationParticipantBox.setRenderer(participantRenderer());
        reservationParticipantBox.addActionListener(event -> refreshReservationData());

        JPanel top = createToolbarPanel();
        top.add(toolbarLabel("Participant"));
        top.add(styleCombo(reservationParticipantBox));

        sessionCatalogList.setCellRenderer(sessionRenderer());
        reservationList.setCellRenderer(reservationRenderer());

        JSplitPane split = createSplitPane(
                borderedPane("Open Sessions", createScrollPane(sessionCatalogList)),
                borderedPane("Participant Reservation History", createScrollPane(reservationList)),
                0.56);

        JPanel actions = createToolbarPanel();
        JButton book = createPrimaryButton("Reserve Session");
        book.addActionListener(event -> handleCreateReservation());
        JButton reschedule = createSecondaryButton("Reschedule Selection");
        reschedule.addActionListener(event -> handleReschedule());
        JButton cancel = createSecondaryButton("Cancel Reservation");
        cancel.addActionListener(event -> handleCancelReservation());
        actions.add(book);
        actions.add(reschedule);
        actions.add(cancel);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFeedbackTab() {
        JPanel panel = createTabPanel();

        feedbackParticipantBox.setModel(new DefaultComboBoxModel<>(participants.toArray(new Participant[0])));
        feedbackParticipantBox.setRenderer(participantRenderer());
        feedbackParticipantBox.addActionListener(event -> refreshFeedbackData());

        JPanel top = createToolbarPanel();
        top.add(toolbarLabel("Participant"));
        top.add(styleCombo(feedbackParticipantBox));

        feedbackList.setCellRenderer(reservationRenderer());

        JPanel entry = new JPanel(new BorderLayout(10, 10));
        entry.setOpaque(false);

        JPanel ratingPanel = createToolbarPanel();
        ratingPanel.add(toolbarLabel("Rating"));
        ratingSpinner.setFont(BODY_FONT);
        ratingPanel.add(ratingSpinner);

        commentArea.setRows(7);
        commentArea.setText("Write the participant review here...");
        commentArea.setBackground(LIST_BACKGROUND);
        commentArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        commentArea.setFont(BODY_FONT);

        JButton submit = createPrimaryButton("Record Attendance");
        submit.addActionListener(event -> handleAttendance());

        entry.add(ratingPanel, BorderLayout.NORTH);
        entry.add(createScrollPane(commentArea), BorderLayout.CENTER);
        entry.add(submit, BorderLayout.SOUTH);

        JSplitPane split = createSplitPane(
                borderedPane("Check-In Queue", createScrollPane(feedbackList)),
                borderedPane("Feedback Composer", entry),
                0.52);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildReportsTab() {
        JPanel panel = createTabPanel();

        JPanel top = createToolbarPanel();
        JButton refresh = createPrimaryButton("Refresh Reports");
        refresh.addActionListener(event -> refreshReports());
        top.add(refresh);

        attendanceArea.setBackground(LIST_BACKGROUND);
        incomeArea.setBackground(LIST_BACKGROUND);

        JSplitPane split = createSplitPane(
                borderedPane("Attendance And Rating View", createScrollPane(attendanceArea)),
                borderedPane("Income Leaderboard", createScrollPane(incomeArea)),
                0.58);

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        panel.setBackground(STATUS_BACKGROUND);

        JLabel heading = new JLabel("Workspace Status");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(new Color(82, 64, 47));

        statusArea.setRows(2);
        statusArea.setText("Ready.");
        statusArea.setBackground(STATUS_BACKGROUND);
        statusArea.setBorder(null);
        statusArea.setFont(BODY_FONT);
        statusArea.setForeground(new Color(92, 74, 58));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(statusArea, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBorder(new EmptyBorder(14, 10, 10, 10));
        panel.setBackground(PAGE_BACKGROUND);
        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setOpaque(false);
        return panel;
    }

    private JSplitPane createSplitPane(Component left, Component right, double resizeWeight) {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(resizeWeight);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(SOFT_BORDER));
        scrollPane.getViewport().setBackground(LIST_BACKGROUND);
        return scrollPane;
    }

    private JPanel borderedPane(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_BORDER),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(80, 63, 48));
        label.setBorder(new EmptyBorder(0, 2, 8, 2));

        panel.add(label, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(true);
        panel.setBackground(new Color(244, 238, 227));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 192, 171)),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(101, 88, 70));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createMetricValue() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Georgia", Font.BOLD, 24));
        label.setForeground(ACCENT_DARK);
        return label;
    }

    private JLabel toolbarLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(89, 71, 54));
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_DARK),
                new EmptyBorder(8, 14, 8, 14)));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(new Color(93, 67, 45));
        button.setBackground(new Color(243, 232, 216));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 175, 149)),
                new EmptyBorder(8, 14, 8, 14)));
        return button;
    }

    private <T> JComboBox<T> styleCombo(JComboBox<T> combo) {
        combo.setFont(BODY_FONT);
        combo.setBackground(new Color(255, 252, 247));
        return combo;
    }

    private DefaultListCellRenderer participantRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Participant participant = (Participant) value;
                String text = participant == null ? "" : participant.getName() + " (" + participant.getId() + ")";
                JLabel label = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                styleListLabel(label, isSelected);
                return label;
            }
        };
    }

    private DefaultListCellRenderer scheduleRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                ActivitySession session = (ActivitySession) value;
                String text = "<html><b>" + session.getActivityName() + "</b><br/>"
                        + "Week " + session.getWeekNumber()
                        + " | " + session.getDay() + " | " + session.getWindow()
                        + " | " + session.getSessionId() + "</html>";
                JLabel label = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                styleListLabel(label, isSelected);
                return label;
            }
        };
    }

    private DefaultListCellRenderer sessionRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                ActivitySession session = (ActivitySession) value;
                String text = "<html><b>" + session.getSessionId() + "</b>  "
                        + session.getActivityName()
                        + "<br/>Week " + session.getWeekNumber()
                        + " | " + session.getDay() + " " + session.getWindow()
                        + " | Open spots: " + session.availableSpots() + "</html>";
                JLabel label = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                styleListLabel(label, isSelected);
                return label;
            }
        };
    }

    private DefaultListCellRenderer reservationRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Enrollment enrollment = (Enrollment) value;
                String text = "<html><b>" + enrollment.getId() + "</b> | "
                        + enrollment.getSession().getActivityName()
                        + "<br/>" + enrollment.getSession().getSessionId()
                        + " | Week " + enrollment.getSession().getWeekNumber()
                        + " | " + enrollment.getSession().getDay() + " " + enrollment.getSession().getWindow()
                        + " | " + enrollment.getStatus() + "</html>";
                JLabel label = (JLabel) super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
                styleListLabel(label, isSelected);
                return label;
            }
        };
    }

    private void styleListLabel(JLabel label, boolean isSelected) {
        label.setBorder(new EmptyBorder(10, 12, 10, 12));
        label.setFont(BODY_FONT);
        label.setOpaque(true);
        if (isSelected) {
            label.setBackground(new Color(222, 164, 132));
            label.setForeground(new Color(46, 32, 24));
        } else {
            label.setBackground(LIST_BACKGROUND);
            label.setForeground(new Color(72, 58, 45));
        }
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(MONO_FONT);
        area.setBorder(new EmptyBorder(12, 12, 12, 12));
        area.setBackground(LIST_BACKGROUND);
        area.setForeground(new Color(70, 58, 46));
        return area;
    }

    private void refreshEverything() {
        refreshMetrics();
        refreshActivityOptions();
        refreshSchedule();
        refreshReservationData();
        refreshFeedbackData();
        refreshReports();
    }

    private void refreshMetrics() {
        participantsMetric.setText(String.valueOf(participants.size()));
        sessionsMetric.setText(String.valueOf(sessions.size()));
        activeMetric.setText(String.valueOf(manager.allEnrollments().stream().filter(Enrollment::isActive).count()));
        completedMetric.setText(String.valueOf(manager.allEnrollments().stream().filter(Enrollment::isCompleted).count()));
    }

    private void refreshActivityOptions() {
        String previous = Objects.toString(activityFilter.getSelectedItem(), "All activities");
        activityFilter.removeAllItems();
        activityFilter.addItem("All activities");
        sessions.stream()
                .map(ActivitySession::getActivityName)
                .distinct()
                .sorted()
                .forEach(activityFilter::addItem);
        activityFilter.setSelectedItem(previous);
    }

    private void refreshSchedule() {
        scheduleModel.clear();

        String daySelection = Objects.toString(dayFilter.getSelectedItem(), "All days");
        String activitySelection = Objects.toString(activityFilter.getSelectedItem(), "All activities");

        sessions.stream()
                .filter(session -> matchesDay(daySelection, session))
                .filter(session -> "All activities".equals(activitySelection)
                        || session.getActivityName().equalsIgnoreCase(activitySelection))
                .sorted(Comparator.comparingInt(ActivitySession::getWeekNumber)
                        .thenComparing(ActivitySession::getDay)
                        .thenComparing(ActivitySession::getWindow))
                .forEach(scheduleModel::addElement);

        if (!scheduleModel.isEmpty()) {
            scheduleList.setSelectedIndex(0);
        } else {
            scheduleDetails.setText("No sessions match the selected filters.");
        }
    }

    private void renderScheduleDetails(ActivitySession session) {
        if (session == null) {
            scheduleDetails.setText("Select a session to view details.");
            return;
        }

        scheduleDetails.setText("""
                Session overview
                ----------------
                Session code: %s
                Activity: %s
                Week: %d
                Day: %s
                Window: %s
                Price: %.2f
                Open spots: %d
                Completed attendees: %d
                Average rating: %.2f
                Current enrollments: %d
                """.formatted(
                session.getSessionId(),
                session.getActivityName(),
                session.getWeekNumber(),
                session.getDay(),
                session.getWindow(),
                session.getPrice(),
                session.availableSpots(),
                session.completedAttendeeCount(),
                session.calculateAverageRating(),
                session.getEnrollments().size()));
    }

    private void refreshReservationData() {
        sessionCatalogModel.clear();
        reservationModel.clear();

        sessions.stream()
                .filter(ActivitySession::hasCapacity)
                .sorted(Comparator.comparingInt(ActivitySession::getWeekNumber)
                        .thenComparing(ActivitySession::getDay)
                        .thenComparing(ActivitySession::getWindow))
                .forEach(sessionCatalogModel::addElement);

        Participant participant = (Participant) reservationParticipantBox.getSelectedItem();
        if (participant != null) {
            manager.enrollmentsFor(participant.getId()).forEach(reservationModel::addElement);
        }

        if (!sessionCatalogModel.isEmpty()) {
            sessionCatalogList.setSelectedIndex(0);
        }
        if (!reservationModel.isEmpty()) {
            reservationList.setSelectedIndex(0);
        }
    }

    private void refreshFeedbackData() {
        feedbackModel.clear();
        Participant participant = (Participant) feedbackParticipantBox.getSelectedItem();
        if (participant != null) {
            manager.activeEnrollmentsFor(participant.getId()).forEach(feedbackModel::addElement);
        }
        if (!feedbackModel.isEmpty()) {
            feedbackList.setSelectedIndex(0);
        }
    }

    private void refreshReports() {
        attendanceArea.setText(reports.buildAttendanceReport(sessions));
        incomeArea.setText(reports.buildIncomeReport(sessions));
    }

    private void handleCreateReservation() {
        Participant participant = (Participant) reservationParticipantBox.getSelectedItem();
        ActivitySession session = sessionCatalogList.getSelectedValue();
        if (participant == null || session == null) {
            showWarning("Select both a participant and an open session.");
            return;
        }

        try {
            Enrollment enrollment = manager.createEnrollment(participant, session);
            setStatus("Reservation created: " + enrollment.getId() + " for " + participant.getName());
            refreshEverything();
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleReschedule() {
        Enrollment existing = reservationList.getSelectedValue();
        ActivitySession newSession = sessionCatalogList.getSelectedValue();
        if (existing == null || newSession == null) {
            showWarning("Choose an existing reservation and a replacement session.");
            return;
        }

        try {
            Enrollment updated = manager.updateEnrollment(existing.getId(), newSession);
            setStatus("Reservation rescheduled. New code: " + updated.getId());
            refreshEverything();
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleCancelReservation() {
        Enrollment existing = reservationList.getSelectedValue();
        if (existing == null) {
            showWarning("Select a reservation to cancel.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Cancel reservation " + existing.getId() + "?",
                "Confirm cancellation",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manager.cancelEnrollment(existing.getId());
            setStatus("Reservation cancelled: " + existing.getId());
            refreshEverything();
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void handleAttendance() {
        Enrollment existing = feedbackList.getSelectedValue();
        if (existing == null) {
            showWarning("Select an active reservation to record attendance.");
            return;
        }

        try {
            manager.confirmAttendance(existing.getId(),
                    (Integer) ratingSpinner.getValue(),
                    commentArea.getText());
            commentArea.setText("");
            setStatus("Attendance recorded for " + existing.getParticipant().getName());
            refreshEverything();
        } catch (RuntimeException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "SessionHub", JOptionPane.WARNING_MESSAGE);
        setStatus("Action blocked: " + message);
    }

    private void setStatus(String message) {
        statusArea.setText(message);
    }

    private boolean matchesDay(String selection, ActivitySession session) {
        if ("All days".equals(selection)) {
            return true;
        }
        return ("Saturday".equals(selection) && session.getDay() == DayCategory.SATURDAY)
                || ("Sunday".equals(selection) && session.getDay() == DayCategory.SUNDAY);
    }
}
