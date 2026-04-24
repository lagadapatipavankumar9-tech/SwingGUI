package sessionhub.engine;

import sessionhub.core.ActivitySession;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds report text for the desktop UI.
 */
public class ReportGenerator {

    public String buildAttendanceReport(List<ActivitySession> sessions) {
        List<ActivitySession> attended = sessions.stream()
                .filter(session -> session.completedAttendeeCount() > 0)
                .sorted(Comparator.comparingInt(ActivitySession::getWeekNumber)
                        .thenComparing(ActivitySession::getDay)
                        .thenComparing(ActivitySession::getWindow))
                .toList();

        StringBuilder builder = new StringBuilder();
        builder.append("ATTENDANCE DELIVERY SNAPSHOT\n");
        builder.append("================================================================================\n");
        builder.append(String.format("%-6s %-6s %-10s %-12s %-22s %-10s %-12s%n",
                "Code", "Week", "Day", "Window", "Activity", "Attended", "Avg Rating"));
        builder.append("--------------------------------------------------------------------------------\n");

        if (attended.isEmpty()) {
            builder.append("No completed attendance data is currently available.\n");
            return builder.toString();
        }

        for (ActivitySession session : attended) {
            builder.append(String.format("%-6s %-6d %-10s %-12s %-22s %-10d %-12.2f%n",
                    session.getSessionId(),
                    session.getWeekNumber(),
                    session.getDay(),
                    session.getWindow(),
                    session.getActivityName(),
                    session.completedAttendeeCount(),
                    session.calculateAverageRating()));
        }

        return builder.toString();
    }

    public String buildIncomeReport(List<ActivitySession> sessions) {
        Map<String, Double> incomeByActivity = sessions.stream()
                .collect(Collectors.groupingBy(
                        ActivitySession::getActivityName,
                        Collectors.summingDouble(ActivitySession::calculateIncome)));

        StringBuilder builder = new StringBuilder();
        builder.append("REVENUE LEADERBOARD\n");
        builder.append("============================================================\n");

        String topActivity = incomeByActivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (topActivity == null || incomeByActivity.get(topActivity) == 0.0) {
            builder.append("No revenue has been generated yet because no sessions are completed.\n");
            return builder.toString();
        }

        incomeByActivity.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> builder.append(String.format("%-24s %10.2f%s%n",
                        entry.getKey(),
                        entry.getValue(),
                        entry.getKey().equals(topActivity) ? "  <- highest income" : "")));

        builder.append("------------------------------------------------------------\n");
        builder.append("Top earning activity: ").append(topActivity).append('\n');
        return builder.toString();
    }
}
