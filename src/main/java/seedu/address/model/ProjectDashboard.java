package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import java.time.Duration;
import java.time.LocalDateTime;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import seedu.address.commons.util.DateTimeUtil;
import seedu.address.model.calendar.CalendarWrapper;
import seedu.address.model.calendar.Meeting;
import seedu.address.model.calendar.UniqueCalendarList;
import seedu.address.model.inventory.Inventory;
import seedu.address.model.inventory.UniqueInventoryList;
import seedu.address.model.member.Member;
import seedu.address.model.member.MemberId;
import seedu.address.model.mapping.InvMemMapping;
import seedu.address.model.mapping.InvTasMapping;
import seedu.address.model.mapping.TasMemMapping;
import seedu.address.model.mapping.Mapping;
import seedu.address.model.mapping.UniqueMappingManager;
import seedu.address.model.member.UniqueMemberList;

import seedu.address.model.task.Task;
import seedu.address.model.task.TaskStatus;
import seedu.address.model.task.UniqueTaskList;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .isSameTask comparison)
 */
public class ProjectDashboard implements ReadOnlyProjectDashboard {

    private final UniqueTaskList tasks;
    private final UniqueTaskList tasksNotStarted;
    private final UniqueTaskList tasksDoing;
    private final UniqueTaskList tasksDone;
    private final UniqueTaskList tasksByDeadline;
    private final UniqueMemberList members;
    private final UniqueInventoryList inventories;
    private final UniqueMappingManager mappings;
    private final UniqueCalendarList calendars;

    /*
     * The 'unusual' code block below is a non-static initialization block, sometimes used to avoid duplication
     * between constructors. See https://docs.oracle.com/javase/tutorial/java/javaOO/initial.html
     *
     * Note that non-static init blocks are not recommended to use. There are other ways to avoid duplication
     *   among constructors.
     */
    {
        tasks = new UniqueTaskList();
        tasksNotStarted = new UniqueTaskList();
        tasksDoing = new UniqueTaskList();
        tasksDone = new UniqueTaskList();
        tasksByDeadline = new UniqueTaskList();
        members = new UniqueMemberList();
        inventories = new UniqueInventoryList();
        mappings = new UniqueMappingManager();
        calendars = new UniqueCalendarList();
    }

    public ProjectDashboard() {}

    /**
     * Creates an ProjectDashboard using the Persons in the {@code toBeCopied}
     */
    public ProjectDashboard(ReadOnlyProjectDashboard toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the task list with {@code tasks}.
     * {@code tasks} must not contain duplicate tasks.
     */
    public void setTasks(List<Task> tasks) {
        this.tasks.setTasks(tasks);
        splitTasksBasedOnStatus(); // initial loading
        splitTasksByDeadline();
    }

    public void setMembers(List<Member> members) {
        this.members.setMembers(members);
    }

    public void setMappings(List<InvMemMapping> invMemMappings,
                            List<InvTasMapping> invTasMappings,
                            List<TasMemMapping> tasMemMappings) {
        this.mappings.setInvMemMappings(invMemMappings);
        this.mappings.setInvTasMappings(invTasMappings);
        this.mappings.setTasMemMappings(tasMemMappings);
    }

    public void setCalendars(List<CalendarWrapper> calendars) {
        this.calendars.setCalendars(calendars);
    }

    /**
     * Resets the existing data of this {@code ProjectDashboard} with {@code newData}.
     * Replaces the contents of the inventory list with {@code inventories}.
     */
    public void setInventories(List<Inventory>inventories) {
        this.inventories.setInventories(inventories);
    }

    /**
     * Resets the existing data of this {@code ProjectDashboard} with {@code newData}.
     */
    public void resetData(ReadOnlyProjectDashboard newData) {
        requireNonNull(newData);

        setTasks(newData.getTaskList());
        setInventories(newData.getInventoryList());
        setMembers(newData.getMemberList());
        setMappings(newData.getInvMemMappingList(), newData.getInvTasMappingList(), newData.getTasMemMappingList());
        setCalendars(newData.getCalendarList());
    }

    //// task-level operations

    /**
     * Returns true if a task with the same identity as {@code task} exists in the project dashboard.
     */
    public boolean hasTask(Task task) {
        requireNonNull(task);
        return tasks.contains(task);
    }

    /**
     * Adds a task to the address book.
     * The task must not already exist in the address book.
     */
    public void addTask(Task p) {
        tasks.add(p);
    }

    /**
     * Replaces the given task {@code target} in the list with {@code editedTask}.
     * {@code target} must exist in the project dashboard.
     * The task identity of {@code editedTask} must not be the same as another existing task in the project dashboard.
     */
    public void setTask(Task target, Task editedTask) {
        requireNonNull(editedTask);

        tasks.setTask(target, editedTask);
    }

    /**
     * Removes {@code key} from this {@code ProjectDashboard}.
     * {@code key} must exist in the address book.
     */
    public void removeTask(Task key) {
        int index = tasks.getIndexOf(key);
        tasks.remove(key);
        mappings.updateTaskRemoved(index);
    }

    //// inventory-level operations
    /**
     * Returns true if a inventory with the same identity as {@code inventory} exists in the dashboard.
     */
    public boolean hasInventory(Inventory inventory) {
        requireNonNull(inventory);
        return inventories.contains(inventory);
    }

    /**
     * Adds a inventory to the dashboard.
     * The inventory must not already exist in the dashboard.
     */
    public void addInventory(Inventory inventory) {
        inventories.add(inventory);
    }

    /**
     * Removes {@code key} from this {@code ProjectDashboard}.
     * {@code key} must exist in the dashboard.
     */
    public void removeInventory(Inventory target) {
        int index = inventories.getIndexOf(target);
        inventories.remove(target);
        mappings.updateInventoryRemoved(index);
    }

    //// util methods TODO add them to the another util class, this breaks SRP
    /**
     * Replaces the given inventory {@code target} in the list with {@code editedInventory}.
     * {@code target} must exist in the project dashboard.
     * The inventory identity of {@code editedInventory} must not be the same as another existing inventory in the project dashboard.
     */
    public void setInventory(Inventory target, Inventory editedInventory) {
        requireNonNull(editedInventory);

        inventories.setInventory(target, editedInventory);
    }

    //// Task util

    /**
     * Utility method to split the main task list into three separate lists based on progress status.
     * Called by the getter methods in {@code ModelManager}
     */
    public void splitTasksBasedOnStatus() {
        tasksNotStarted.setTasks(tasks.toStream()
                .filter(task -> task.getTaskStatus().equals(TaskStatus.UNBEGUN)).collect(Collectors.toList()));

        tasksDoing.setTasks(tasks.toStream()
                .filter(task -> task.getTaskStatus().equals(TaskStatus.DOING)).collect(Collectors.toList()));

        tasksDone.setTasks(tasks.toStream()
                .filter(task -> task.getTaskStatus().equals(TaskStatus.DONE)).collect(Collectors.toList()));
    }

    /**
     * Utility method to split tasks by their deadline, for Ui purposes.
     * Called by getter methods in {@code ModelManager}
     */
    public void splitTasksByDeadline() {
        tasksByDeadline.setTasks(tasks.toStream()
                .filter(Task::hasDeadline)
                .filter(task -> !(task.getTaskStatus().equals(TaskStatus.UNBEGUN)))
                .filter(task -> DateTimeUtil.checkIfDueSoon(2, task.getDeadline()))
                .collect(Collectors.toList()));
    }

    /// Mapping util

    public void addMapping(Mapping mapping) {
        mappings.add(mapping);
    }

    public void removeMapping(Mapping mapping) {
        mappings.remove(mapping);
    }

    /**
     * returns whether the mapping list contains targetMapping
     */
    public boolean hasMapping(Mapping mapping) {
        requireNonNull(mapping);
        return mappings.contains(mapping);
    }

    @Override
    public ObservableList<Inventory> getInventoryList() {
        return inventories.asUnmodifiableList();
    }

    @Override
    public ObservableList<InvMemMapping> getInvMemMappingList() {
        return mappings.getUnmodifiableObservableInvMemList();
    }

    @Override
    public ObservableList<InvTasMapping> getInvTasMappingList() {
        return mappings.getUnmodifiableObservableInvTasList();
    }

    @Override
    public ObservableList<TasMemMapping> getTasMemMappingList() {
        return mappings.getUnmodifiableObservableTasMemList();
    }

    @Override
    public ObservableList<Mapping> getMappingList() {
        return mappings.getUnmodifiableObserableList();
    }

    @Override
    public ObservableList<Task> getTaskList() {
        return tasks.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Task> getTasksNotStarted() {
        return tasksNotStarted.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Task> getTasksDoing() {
        return tasksDoing.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Task> getTasksDone() {
        return tasksDone.asUnmodifiableObservableList();
    }

    public ObservableList<Task> getTasksByDeadline() {
        return tasksByDeadline.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ProjectDashboard // instanceof handles nulls
                && tasks.equals(((ProjectDashboard) other).tasks));
    }

    //// list overwrite operations

    //// member-level operations

    /**
     * Returns true if a task with the same identity as {@code task} exists in the address book.
     */
    public boolean hasMember(Member member) {
        requireNonNull(member);
        return members.contains(member);
    }

    /**
     * Returns true if a member with the same identity as {@code memberId} exists in the dashboard.
     */
    public boolean hasMemId(MemberId memId) {
        requireNonNull(memId);
        return members.containsId(memId);
    }

    /**
     * Adds a task to the address book.
     * The task must not already exist in the address book.
     */
    public void addMember(Member member) {
        members.add(member);
    }

    /**
     * Replaces the given task {@code target} in the list with {@code editedTask}.
     * {@code target} must exist in the address book.
     * The task identity of {@code editedTask} must not be the same as another existing task in the address book.
     */
    public void setMember(Member target, Member editedMember) {
        requireNonNull(editedMember);

        members.setMember(target, editedMember);
    }

    /**
     * Removes {@code key} from this {@code ProjectDashboard}.
     * {@code key} must exist in the address book.
     */
    public void removeMember(Member key) {
        int index = members.getIndexOf(key);
        members.remove(key);
        mappings.updateMemberRemoved(index);
    }

    //// util methods

    @Override
    public ObservableList<Member> getMemberList() {
        return members.asUnmodifiableObservableList();
    }

    @Override
    public HashMap<Task, ObservableList<Member>> listMemberByTask() {
        HashMap<Integer, ObservableList<Integer>> indexedListMemberByTask = mappings.listMemberByTask();
        HashMap<Task, ObservableList<Member>> result = new HashMap<>();
        ObservableList<Task> tasksObservableList = tasks.asUnmodifiableObservableList();
        ObservableList<Member> membersObservableList = members.asUnmodifiableObservableList();
        for (int taskIndex : indexedListMemberByTask.keySet()) {
            Task currentTask = tasksObservableList.get(taskIndex);
            ObservableList<Member> mappedMembers = FXCollections.observableArrayList();
            result.put(currentTask, mappedMembers);
            for (int memberIndex : indexedListMemberByTask.get(taskIndex)) {
                mappedMembers.add(membersObservableList.get(memberIndex));
            }
        }
        return result;
    }

    public void addCalendar(CalendarWrapper calendar) {
        calendars.add(calendar);
    }

    public boolean hasCalendar(CalendarWrapper calendar) {
        return calendars.contains(calendar);
    }

    @Override
    public ObservableList<CalendarWrapper> getCalendarList() {
        return calendars.asUnmodifiableObservableList();
    }

    public ObservableList<Meeting> findMeetingTime(LocalDateTime startDate, LocalDateTime endDate, Duration meetingDuration) {
        return calendars.findMeetingTime(startDate, endDate, meetingDuration);
    }
}
