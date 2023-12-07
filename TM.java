import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.Instant;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.stream.Collectors;

public class TM {
    public static void main(String[] args){
        TaskRepository taskRepo = new TaskRepository("taskdata.log");
        CommandInvoker invoker = new CommandInvoker();
        Command command;

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
                            argExists(1, args),
                            argExists(2, args));
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
                    System.err.println("Unknown command");
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
    private final String size;

    public SizeCommand(TaskRepository repo, String name, String size) {
        this.taskRepo = repo;
        this.taskName = name;
        this.size = size;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null) {
            taskRepo.size(taskName,size);
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
    private final List<Task> tasks;
    private final Logger logger;

    public TaskRepository(String filePath) {
        this.logger = new Logger(filePath);
        LogParser logParser = new LogParser();
        this.tasks = logParser.parseLogFile(filePath);
    }

    private Task getLatestTask(String taskName) {
        return tasks.stream()
                .filter(task -> task.getName().equals(taskName))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    // these shouldn't modify task at all
    public void startTask(String taskName) {
        logger.logAction(taskName, "start", null, null);
    }

    public void stopTask(String taskName) {
            logger.logAction(taskName, "stop", null, null);
    }

    public void describe(String taskName, String description, String size) {
        Task task = getLatestTask(taskName);
        if (task != null) {
            logger.logAction(taskName, "describe",
                    description, size);
        }
    }

    public void size(String taskName, String size) {
        Task task = getLatestTask(taskName);
        if (task != null) {
            logger.logAction(taskName, "size", null, size);
        }
    }

    // iterate through task
    public void summary(String arg) {

    }

    // Deletes in logger
    public void delete(String taskName) {

    }

    // rename in logger
    public void rename(String oldName, String newName) {

    }
}

class Logger {
    private final String logFilePath;
    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
        createLogIfNotExist();
    }

    private void createLogIfNotExist() {
        File file = new File(this.logFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating log file: "
                        + e.getMessage());
            }
        }
    }

    public void logAction(String taskName, String command,
                          String description, String size) {
        String logEntry = String.format("%s,%s,%s,%s,%s",
                Instant.now().toString(), taskName, command, description, size);

        try (FileWriter fw = new FileWriter(logFilePath, true);
             BufferedWriter writer = new BufferedWriter(fw)) {
            writer.append(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}

class LogParser {
    private List<Task> tasks;

    public LogParser() {
        this.tasks = new ArrayList<>();
    }
    public List<Task> parseLogFile(String logFilePath) {
        try {
            List<String> logLines = Files.readAllLines(Paths.get(logFilePath));
            for (String line : logLines) {
                Task task = parseLogLine(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the log file: " + e.getMessage());
        }
        return tasks;
    }

    private Task getLatestTask(String taskName) {
        return tasks.stream()
                .filter(task -> task.getName().equals(taskName))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public Task parseLogLine(String line) {
        String[] parts = line.split(",");

        String timestamp = parts[0];
        String taskName = parts[1];
        String command = parts[2];
        String description = (parts.length > 3) ? parts[3] : null;
        String size = (parts.length > 4) ? parts[4] : null;

        Task task = getLatestTask(taskName);
        // Initialize new task if task is null command is start
        if (task == null && command.equals("start")) {
            return new Task(taskName, timestamp);
        }

        if (task!= null) {
            switch (command) {
                case "start", "stop" -> {
                    task.upsertTimeEntry(timestamp, command);
                }
                case "describe" -> {
                    task.setDescription(description);
                }
                case "size" -> {
                    task.setTShirtSize(size);
                }
            }
        }

        return null;
    }
}

class Task {
    private final String name;
    private String description;
    private String size;

    public Stack<TimeEntry> timeEntries;

    public Task(String name, String timestamp) {
        this.name = name;
        this.timeEntries = new Stack<>();
        this.timeEntries.push(new TimeEntry(timestamp));
    }

    public String getName() {
        return name;
    }

    public String getDescription(){ return description; }

    public String getTShirtSize(){ return size; }

    public void setDescription(String description){ this.description =
            description;  }

    public void setTShirtSize(String size){ this.size = size; }

    public void upsertTimeEntry(String time, String command){
        if(command.equals("start")){
            timeEntries.push(new TimeEntry(time));
        } else if(command.equals("stop")){
            timeEntries.push(new TimeEntry(time));
        }
    }

}

class TimeEntry {
    private final String startTime;
    private String stopTime;
    public TimeEntry(String startTime) {
        this.startTime = startTime;
        this.stopTime = null;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getStopTime() {
        return this.stopTime;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public int getDuration() {
        return 0;
    }
}

class InvalidCommandException extends Exception {
    public InvalidCommandException(String usage){
        super("Missing Arguments, Usage: " + usage);
    }
}

enum TShirtSize {
    S,
    M,
    L,
    XL
}
