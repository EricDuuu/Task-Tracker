import java.util.List;

public class TM {
    public static void main(String[] args){
        TaskRepository taskRepo = new TaskRepository("taskdata.log");
        CommandInvoker invoker = new CommandInvoker();
        Command command = null;

        if (args.length > 0) {
            String commandType = args[0].toLowerCase();
            switch (commandType) {
                case "start":
                    command = new StartTaskCommand(taskRepo,
                            argExists(1, args));
                    break;
                case "stop":
                    command = new StopTaskCommand(taskRepo,
                            argExists(1, args));
                    break;
                case "describe":
                    command = new DescribeTaskCommand(taskRepo,
                            argExists(1, args),
                            argExists(2, args),
                            argExists(3, args));
                    break;
                case "summary":
                    command = new SummaryCommand(taskRepo,
                            argExists(1, args));
                    break;
                case "size":
                    command = new SizeCommand(taskRepo,
                            argExists(1, args));
                    break;
                case "delete":
                    command = new DeleteCommand(taskRepo,
                            argExists(1, args));
                    break;
                case "rename":
                    command = new RenameCommand(taskRepo,
                            argExists(1, args),
                            argExists(2, args));
                    break;
                default:
                    System.out.println("Unknown command");
                    return;
            }

            // Set and invoke the command
            invoker.setCommand(command);
            invoker.invoke();
        }
    }
    private static String argExists(int argNum, String[] args){
        return args.length > argNum ? args[argNum] : null;
    }
}

class InvalidCommandException extends Exception {
    public InvalidCommandException(String usage){
        super("Missing Arguments, Usage: " + usage);
    }
}

interface Command {
    void execute() throws InvalidCommandException;
}


class StartTaskCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public StartTaskCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }
    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null) {
            taskRepo.startTask(taskName);
        } else {
            throw new InvalidCommandException("TM.java start <task name>");
        }
    }
}

class StopTaskCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;
    public StopTaskCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }
    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null) {
            taskRepo.stopTask(taskName);
        } else {
            throw new InvalidCommandException("TM.java stop <task name>");
        }
    }
}

class DescribeTaskCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;
    private final String description;
    private final String size;

    public DescribeTaskCommand(TaskRepository repo, String name,
                               String description, String size) {
        this.taskRepo = repo;
        this.taskName = name;
        this.description = description;
        this.size = size;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null && description != null) {
            taskRepo.describe(this.taskName, this.description, this.size);
        } else {
            throw new InvalidCommandException("TM.java describe <task name> " +
                    "<description> [{S|M|L|XL}]");
        }
    }
}

class SummaryCommand implements Command {
    private final TaskRepository taskRepo;
    private final String arg;

    public SummaryCommand(TaskRepository repo, String arg) {
        this.taskRepo = repo;
        this.arg = arg;
    }

    @Override
    public void execute() {
        taskRepo.summary(arg);
    }
}

class SizeCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public SizeCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null) {
            taskRepo.size(taskName);
        } else {
            throw new InvalidCommandException("TM.java size <task  name> " +
                    "{S|M|L|XL}");
        }
    }
}

class DeleteCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public DeleteCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null) {
            taskRepo.delete(taskName);
        } else {
            throw new InvalidCommandException("TM.java delete <task name>");
        }
    }
}

class RenameCommand implements Command {
    private final TaskRepository taskRepo;
    private final String oldName;
    private final String newName;

    public RenameCommand(TaskRepository repo, String oldName, String newName) {
        this.taskRepo = repo;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.oldName != null && this.newName != null) {
            taskRepo.rename(oldName, newName);
        } else {
            throw new InvalidCommandException("TM.java rename <old task " +
                    "name> <new task name>");
        }
    }
}

class CommandInvoker {
    private Command command;
    public void setCommand(Command command) {
        this.command = command;
    }
    public void invoke() {
        if (command != null) {
            try {
                command.execute();
            } catch (InvalidCommandException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

class TaskRepository {
    private final String logFilePath;
    private List<Task> tasks;

    public TaskRepository(String filePath) {
        this.logFilePath = filePath;
    }

    public void startTask(String taskName) {
    }

    public void stopTask(String taskName) {
    }

    public void describe(String taskName, String description, String size) {
    }

    public void size(String taskName) {
    }

    public void summary(String arg) {
    }

    public void delete(String taskName) {
    }

    public void rename(String oldName, String newName) {
    }
}

enum TShirtSize {
    S,
    M,
    L,
    XL
}

class Task {
    private String name;
    private String description;
    private TShirtSize size;
    private List<TimeEntry> timeEntries;
}

class TimeEntry {
    private String startTime;
    private String stopTime;
    public TimeEntry(String startTime, String stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }
    public int getDuration() {
        return 0;
    }
}
