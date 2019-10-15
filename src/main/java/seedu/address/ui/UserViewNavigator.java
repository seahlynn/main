package seedu.address.ui;

import seedu.address.logic.Logic;
import seedu.address.ui.views.ProjectDashboardView;
import seedu.address.ui.views.TaskListPanel;

/**
 * Utility class for controlling navigation between user views.
 * Stores the different views here.
 *
 * Main access of the navigator is from {@code UserViewMain}.
 */
public class UserViewNavigator {


    private ProjectDashboardView projectDashboardView;
    private TaskListPanel taskListPanel;

    /** The main application layout controller. */
    private static UserViewController userViewController;

    /**
     * Stores the main controller for later use in navigation tasks.
     *
     * @param userViewController the dynamic application layout controller.
     */
    public static void setMainController(UserViewController userViewController) {
        UserViewNavigator.userViewController = userViewController;
    }


    public void loadDashboard(Logic logic) {
        projectDashboardView = new ProjectDashboardView(logic.getFilteredTaskListNotStarted(), logic.getFilteredTaskListDoing(),
                logic.getFilteredTaskListDone());
        userViewController.setUserView(projectDashboardView);
    }

    public void loadTaskListView(Logic logic) {
        taskListPanel = new TaskListPanel(logic.getFilteredTaskList());
        userViewController.setUserView(taskListPanel);
    }

}
