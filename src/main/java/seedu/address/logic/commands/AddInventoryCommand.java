package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INVENTORY_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_INVENTORY_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBER_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TASK_INDEX;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.inventory.InvName;
import seedu.address.model.inventory.Inventory;
import seedu.address.model.inventory.Price;
import seedu.address.model.mapping.InvMemMapping;
import seedu.address.model.mapping.InvTasMapping;
import seedu.address.model.member.Member;
import seedu.address.model.member.MemberId;
import seedu.address.model.task.Task;

/**
 * Adds a task to the address book.
 */

public class AddInventoryCommand extends Command {
    public static final String COMMAND_WORD = "add-inv";
    public static final String PREFIX_USAGE = PREFIX_INVENTORY_NAME +
                                                " " + PREFIX_INVENTORY_PRICE +
                                                " " + PREFIX_TASK_INDEX +
                                                " " + PREFIX_MEMBER_ID;

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a inventory to the project Dashboard. "
            + "Parameters: "
            + PREFIX_INVENTORY_NAME + "NAME "
            + PREFIX_INVENTORY_PRICE + "PRICE "
            + PREFIX_TASK_INDEX + "TASKID "
            + PREFIX_MEMBER_ID + "MEMBERID"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_INVENTORY_NAME + "BALLS "
            + PREFIX_INVENTORY_PRICE + "8.50"
            + PREFIX_TASK_INDEX + "2"
            + PREFIX_MEMBER_ID + "AR";

    public static final String MESSAGE_SUCCESS = "New inventory added: %1$s";
    public static final String MESSAGE_DUPLICATE_INVENTORY = "This inventory already exists in the address book";


    private final Index taskId;
    private final InvName name;
    private final Price price;
    private final MemberId memId;

    /**
     * Creates an AddInventoryCommand to add the specified {@code Inventory}
     */
    public AddInventoryCommand(Index taskId, InvName name, Price price, MemberId memId) {
        requireAllNonNull(taskId, name, price);
        this.taskId = taskId;
        this.name = name;
        this.price = price;
        this.memId = memId;
    }

    public AddInventoryCommand(Index taskId, InvName name, MemberId memId) {
        requireAllNonNull(taskId, name);
        this.taskId = taskId;
        this.name = name;
        this.price = new Price(0);
        this.memId = memId;
    }

    public InvName getName() {
        return name;
    }

    public Index getTaskId() {
        return taskId;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Task> lastShownTaskList = new ArrayList<>(model.getFilteredTasksList());
        List<Member> lastShownMemberList = new ArrayList<>(model.getFilteredMembersList());
        List<Inventory> lastShownInvList = new ArrayList<>(model.getFilteredInventoriesList());
        int tasksLength = model.getTasksLength();
        int membersLength = model.getMembersLength();

        if (taskId.getOneBased() > tasksLength) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        boolean contains = false;
        Member memberToAdd = null;
        Integer memberIndex = null;
        for (int i = 0; i < lastShownMemberList.size(); i++) {
            if (lastShownMemberList.get(i).getId().equals(memId)) {
                contains = true;
                memberToAdd = lastShownMemberList.get(i);
                memberIndex = i;
                break;
            }
        }

        if (!contains) {
            throw new CommandException(Messages.MESSAGE_INVALID_MEMBER_ID);
        }

        Inventory toAdd = new Inventory(name, price);
        int invSize = lastShownInvList.size();
        InvMemMapping memMapToAdd = new InvMemMapping(memberIndex, invSize);
        InvTasMapping tasMapToAdd = new InvTasMapping(taskId.getZeroBased(), invSize);

        if (model.hasInventory(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_INVENTORY);
        }

        model.addInventory(toAdd);
        model.addMapping(memMapToAdd);
        model.addMapping(tasMapToAdd);

        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddInventoryCommand // instanceof handles nulls
                && taskId.equals(((AddInventoryCommand) other).taskId))
                && name.equals(((AddInventoryCommand) other).name)
                && price.equals(((AddInventoryCommand) other).price);
    }
}
