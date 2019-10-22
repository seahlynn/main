package seedu.address.ui.views;

import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.task.Task;
import seedu.address.ui.UiPart;

/**
 * Panel containing the dashboard, with tasks separated by status.
 * Is the default view called by {@code UserViewUpdate}.
 */
public class ProjectDashboardView extends UiPart<Region> {
    private static final String FXML = "ProjectDashboard.fxml";
    private final Logger logger = LogsCenter.getLogger(ProjectDashboardView.class);

    @FXML
    private ListView<Task> taskListViewNotStarted;
    @FXML
    private ListView<Task> taskListViewDoing;
    @FXML
    private ListView<Task> taskListViewDone;
    @FXML
    private ListView<Task> taskListViewUpcomingDeadlines;

    public ProjectDashboardView(ObservableList<Task> taskListNotStarted, ObservableList<Task> taskListDoing,
                                ObservableList<Task> taskListDone, ObservableList<Task> taskListDeadline) {
        super(FXML);

        taskListViewNotStarted.setItems(taskListNotStarted);
        taskListViewDoing.setItems(taskListDoing);
        taskListViewDone.setItems(taskListDone);
        taskListViewUpcomingDeadlines.setItems(taskListDeadline);

        taskListViewNotStarted.setCellFactory(listView -> new TaskListViewCell());
        taskListViewDoing.setCellFactory(listView -> new TaskListViewCell());
        taskListViewDone.setCellFactory(listView -> new TaskListViewCell());
        taskListViewUpcomingDeadlines.setCellFactory(listView -> new TaskListViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Task} using a {@code TaskCard}.
     */
    class TaskListViewCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task task, boolean empty) {
            super.updateItem(task, empty);

            if (empty || task == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new TaskCard(task, getIndex() + 1).getRoot());
            }
        }
    }

}
