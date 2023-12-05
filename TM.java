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

enum TShirtSize {
    S,
    M,
    L,
    XL
}

interface Command {
    void execute();
}


class StartTaskCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public StartTaskCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }

    @Override
    public void execute() {
        taskRepo.startTask(taskName);
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
    public void execute() {
        taskRepo.startTask(taskName);
    }
}

class DescribeTaskCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public DescribeTaskCommand(TaskRepository repo, String name, String description, String size) {
        this.taskRepo = repo;
        this.taskName = name;
    }

    @Override
    public void execute() {
        taskRepo.startTask(taskName);
    }
}

class SummaryCommand implements Command {
    private final TaskRepository taskRepo;
    private final String taskName;

    public SummaryCommand(TaskRepository repo, String name) {
        this.taskRepo = repo;
        this.taskName = name;
    }

    @Override
    public void execute() {
        taskRepo.startTask(taskName);
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
    public void execute() {
        taskRepo.startTask(taskName);
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
    public void execute() {
        taskRepo.startTask(taskName);
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
    public void execute() {
        taskRepo.startTask(newName);
    }
}

class CommandInvoker {
    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public void invoke() {
        if (command != null) {
            command.execute();
        }
    }
}

class TaskRepository {
    private final String logFilePath;

    public TaskRepository(String filePath) {
        this.logFilePath = filePath;
        // Initialize the log file if necessary
    }

    public void startTask(String taskName) {
        // Logic to start a task, write to log file
    }

    public void stopTask(String taskName) {
        // Logic to stop a task, write to log file
    }

    public void describeTask(String taskName, String description, String size) {
        // Logic to describe a task, write to log file
    }

    // Other methods for rename, delete, summary
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
