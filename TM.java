import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


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
                case "size": // modified this case under the assumption that specified size is mandatory
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
    private String size; //modified here as well to be able to pass in size type

    public SizeCommand(TaskRepository repo, String name, String size) {
        this.taskRepo = repo;
        this.taskName = name;
        this.size = size;
    }

    @Override
    public void execute() throws InvalidCommandException {
        if (this.taskName != null && taskRepo.doesSizeExist(size)) { // if invalid size, prompt default exception
            taskRepo.size(taskName, size);
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
    private Logger logger;

    public TaskRepository(String filePath) {
        this.logFilePath = filePath;

         // Initialize logger and set up FileHandler
        try {

            logger = Logger.getLogger(TaskRepository.class.getName());
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Task findTaskInList (String taskName) {
        for (Task task : tasks) {
            if (task.getName().equals(taskName)) {
                return task;
            }
        }
        return null;
    }

    public boolean doesSizeExist (String size) {
        List<String> stringList = Arrays.asList("S", "M", "L", "XL");

        if (stringList.contains(size)) {
            return true;
        }

        return false;
    }

    public void startTask(String taskName) throws InvalidCommandException {
        //assume startime correlates to whatever the current time is at runtime
        // Log the start time of the task
        Task currentTask = findTaskInList(taskName);

        if (currentTask != null && !currentTask.isTaskStopped()) {
        throw new InvalidCommandException("Invalid Command: " + taskName + " has not yet been stopped.");
        }

        //Log time into logger
        String taskStartTime = Long.toString(System.currentTimeMillis());
        logger.info(taskName + " started at: " + taskStartTime);

        if(currentTask != null){
            currentTask.addTimeEntry(new TimeEntry(taskStartTime, "N/A"));
            return;
        }
        
        tasks.add(new Task(taskName, new TimeEntry(taskStartTime, "N/A")));

    }

    public void stopTask(String taskName) throws InvalidCommandException {
        Task currentTask = findTaskInList(taskName);

        if (currentTask == null || currentTask.isTaskStopped()) {
        throw new InvalidCommandException("Invalid Command: " + taskName + " is already stopped or does not exist.");
        }
        //Log time into logger
        String taskStopTime = Long.toString(System.currentTimeMillis());
        logger.info(taskName + " stopped at: " + taskStopTime);

        currentTask.setStopTime(taskStopTime);

    }

    public void describe(String taskName, String description, String size) throws InvalidCommandException {
         Task targetTask = findTaskInList(taskName);

        if(targetTask == null){
            throw new InvalidCommandException("Invalid Command: " + taskName + " does not exist.");
        }

        targetTask.setDescription(description);

        if(doesSizeExist(size)){
            size(taskName, size);
        }


    }

    public void size(String taskName, String size) throws InvalidCommandException {
        Task targetTask = findTaskInList(taskName);

        if(targetTask == null){
            throw new InvalidCommandException("Invalid Command: " + taskName + " does not exist.");
        }

        targetTask.setSize(TShirtSize.valueOf(size));
    }

    public void summary(String arg) {
    }

    public void delete(String taskName) throws InvalidCommandException {
        Task targetTask = findTaskInList(taskName);

        if(targetTask == null){
            throw new InvalidCommandException("Invalid Command: " + taskName + " does not exist or is already deleted.");
        }

        tasks.remove(targetTask);

    }

    public void rename(String oldName, String newName) throws InvalidCommandException {
         Task targetTask = findTaskInList(oldName);

        if(targetTask == null){
            throw new InvalidCommandException("Invalid Command: " + oldName + " does not exist.");
        }

        targetTask.setName(newName);
        
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

    public boolean isTaskStopped(){
        TimeEntry currentTimeEntry = timeEntries.get(timeEntries.size() - 1);
        if(currentTimeEntry.getStopTime() == "N/A"){
            return false;
        }
        return true;
    }

    public Task(String name, TimeEntry timestamp) {
        this.name = name;
        timeEntries.add(timestamp);
    }

    public String getName(){
        return new String(name);
    }

    public void setName(String name){
        this.name = name;
    }
    
    public void setDescription(String description) {
         if (this.description == null) {
            this.description = description;
        } else {
            this.description += " " + description;
        }
    }

    public void setSize(TShirtSize size) {
        this.size = size;
    }

    public void setStopTime(String stopTime){
        TimeEntry currentTimeEntry = timeEntries.get(timeEntries.size() - 1);
        currentTimeEntry.setStopTime(stopTime);
    }

    public void addTimeEntry(TimeEntry timeEntry) {
        timeEntries.add(timeEntry);
    }

}

class TimeEntry {
    private String startTime;
    private String stopTime;
    public TimeEntry(String startTime, String stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

     public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getStopTime() {
        return new String(stopTime);
    }

    public int getDuration() {
        return 0;
    }
}
