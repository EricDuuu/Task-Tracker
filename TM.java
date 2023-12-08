import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.time.Instant;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TM {
    public static void main(String[] args){
        final String logFilePath = "task-manager.log";
        TaskLogger logger = new Logger(logFilePath);
        TaskLogParser logParser = new LogParser(logFilePath);
        TaskExecutor taskExecutor = new TaskExecutor(logger, logParser);

        CommandParser commandParser = new CommandParser(taskExecutor);
        try {
            commandParser.parseThenExecute(args);
        }   catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}

interface TaskLogger {
    void logAction(String taskName, String command, String description, String size);
    void renameTasks(String oldName, String newName);
    void deleteTasks(String taskName);
}

interface TaskLogParser {
    Map<String, Task> parseLogFile();
}

class SummaryInfo {
    private final Map<String, Task> taskMap;
    public SummaryInfo(Map<String, Task> taskMap) {
        this.taskMap = taskMap;
    }

    public List<Duration> getTaskDurations() {
        return taskMap.values().stream()
                .map(Task::getTotalDuration)
                .collect(Collectors.toList());
    }

    public Duration minTimeSpent() {
        return getTaskDurations().stream()
                .min(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    public Duration maxTimeSpent() {
        return getTaskDurations().stream()
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    public Duration avgTimeSpent() {
        List<Duration> durations = getTaskDurations();
        return durations.stream()
                .reduce(Duration::plus)
                .map(total -> total.dividedBy(durations.size()))
                .orElse(Duration.ZERO);
    }
}

interface SummaryStrategy {
    void generateSummary();
}

class SummaryByName implements SummaryStrategy {
    private final SummaryInfo summary;

    public SummaryByName(SummaryInfo summary, String arg) {
        this.summary = summary;
    }

    @Override
    public void generateSummary() {

    }
}

class SummaryBySize implements SummaryStrategy {
    private final SummaryInfo summary;

    public SummaryBySize(SummaryInfo summary, String arg) {
        this.summary = summary;
    }

    @Override
    public void generateSummary() {

    }
}

class SummaryForAll implements SummaryStrategy {
    private final SummaryInfo summary;

    public SummaryForAll(SummaryInfo summary) {
        this.summary = summary;
    }

    @Override
    public void generateSummary() {
        System.out.println("hi");
    }
}

class CommandParser {
    TaskExecutor taskExecutor;
    public CommandParser(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void parseThenExecute(String[] args)
            throws IllegalCommandException, MissingArgumentException {
        if (args.length == 0) {
            System.err.println("No command provided");
            return;
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "start":
                if (args.length < 2) {
                    throw new MissingArgumentException("start <task name>");
                }
                taskExecutor.startTask(args[1]);
                break;
            case "stop":
                if (args.length < 2) {
                    throw new MissingArgumentException("stop <task name>");
                }
                taskExecutor.stopTask(args[1]);
                break;
            case "describe":
                if (args.length < 3) {
                    throw new MissingArgumentException("describe " +
                            "<task name> <description> [{S|M|L|XL}]");
                }
                taskExecutor.describe(args[1], args[2], args.length > 3 ?
                        args[3] : null);
                break;

            case "size":
                if (args.length < 3) {
                    throw new MissingArgumentException("size <task name> " +
                            "{S|M|L|XL}");
                }
                taskExecutor.size(args[1], args[2]);
                break;

            case "rename":
                if (args.length < 3) {
                    throw new MissingArgumentException("rename " +
                            "<old task name> <new task name>");
                }
                taskExecutor.rename(args[1], args[2]);
                break;

            case "delete":
                if (args.length < 2) {
                    throw new MissingArgumentException("delete <task name>");
                }
                taskExecutor.delete(args[1]);
                break;

            case "summary":
                taskExecutor.summary(args.length > 1 ? args[1] : null);
                break;

            default:
                throw new IllegalArgumentException("Unknown command: "
                        + command);
        }
    }
}

class TaskExecutor {
    private final Map<String, Task> taskMap;
    private final TaskLogger logger;
    public TaskExecutor(TaskLogger logger, TaskLogParser logParser) {
        this.logger = logger;
        this.taskMap = logParser.parseLogFile();
    }

    private boolean isValidSize(String size) {
        return size.toUpperCase().matches("S|M|L|XL");
    }

    public void startTask(String taskName) throws IllegalCommandException {
        Task task = taskMap.get(taskName);
        if(task != null && !task.lastEntryStopped()){
            throw new IllegalCommandException(taskName,
                    "has not been stopped");
        }
        logger.logAction(taskName,"start",null, null);
    }

    public void stopTask(String taskName) throws IllegalCommandException {
        Task task = taskMap.get(taskName);
        if(task == null || task.lastEntryStopped()){
            throw new IllegalCommandException(taskName,
                    "has not been started");
        }
        logger.logAction(taskName, "stop", null, null);
    }

    public void describe(String taskName, String description, String size)
            throws IllegalCommandException {
        Task task = taskMap.get(taskName);
        if(task == null){
            throw new IllegalCommandException(taskName,
                    "does not exist");
        } else if (size != null && !isValidSize(size)){
            throw new IllegalCommandException(taskName,
                    "invalid size");
        }
        logger.logAction(taskName,"describe", description, size);
    }

    public void size(String taskName, String size)
            throws IllegalCommandException {
        Task task = taskMap.get(taskName);
        if(task == null){
            throw new IllegalCommandException(taskName,
                    "does not exist");
        } else if (!isValidSize(size)){
            throw new IllegalCommandException(taskName,
                    "invalid size");
        }
        logger.logAction(taskName,"size",null, size);
    }

    public void summary(String arg)
            throws IllegalCommandException {
        SummaryStrategy strategy;
        SummaryInfo summary = new SummaryInfo(taskMap);
        if (arg == null) {
            strategy = new SummaryForAll(summary);
        } else if (isValidSize(arg)) {
            strategy = new SummaryBySize(summary, arg);
        } else {
            Task task = taskMap.get(arg);
            if(task == null){
                throw new IllegalCommandException(arg,
                        "does not exist");
            }
            strategy = new SummaryByName(summary, arg);
        }

        strategy.generateSummary();
    }

    public void delete(String taskName)
            throws IllegalCommandException {
        Task task = taskMap.get(taskName);
        if(task == null){
            throw new IllegalCommandException(taskName,
                    "does not exist");
        }
        logger.deleteTasks(taskName);
    }

    public void rename(String oldName, String newName)
            throws IllegalCommandException {
        Task newTask = taskMap.get(newName);
        Task oldTask = taskMap.get(oldName);
        if(oldTask == null){
            throw new IllegalCommandException(oldName,
                    "does not exist");
        } else if (newTask != null){
            throw new IllegalCommandException(newName,
                    "already exists");
        }
        logger.renameTasks(oldName, newName);
    }
}

class Logger implements TaskLogger {
    private final String logFilePath;
    public Logger(String logFilePath) {
        this.logFilePath = logFilePath;
        createLogIfNotExist();
    }

    private void createLogIfNotExist() {
        File file = new File(this.logFilePath);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    throw new IOException("File already exists.");
                }
            } catch (IOException e) {
                System.err.println("Error creating log file: "
                        + e.getMessage());
            }
        }
    }

    public void renameTasks(String oldName, String newName) {
        updateLogFile(line -> renameTaskInLine(line, oldName, newName));
    }

    public void deleteTasks(String taskName) {
        updateLogFile(line -> containsTask(line, taskName) ? null : line);
    }

    private void updateLogFile(Function<String, String> operationFunction) {
        try {
            Path path = Paths.get(logFilePath);
            List<String> logLines = Files.readAllLines(path);
            List<String> modifiedLines = logLines.stream()
                    .map(operationFunction)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Files.write(path, modifiedLines);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private String renameTaskInLine(String line, String oldName, String newName) {
        String[] parts = line.split(",", 3);

        if (parts.length == 3) {
            String timestamp = parts[0];
            String taskName = parts[1];
            String restOfLine = parts[2];

            if (taskName.equals(oldName)) {
                taskName = newName;
            }
            return timestamp + "," + taskName + "," + restOfLine;
        }
        return line;
    }

    private boolean containsTask(String line, String taskName) {
        String[] parts = line.split(",", 3);
        return parts.length > 1 && parts[1].trim().equalsIgnoreCase(taskName);
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

class LogParser implements TaskLogParser {
    private final Map<String, Task> taskMap;
    private final String logFilePath;
    public LogParser(String logFilePath) {
        this.taskMap = new HashMap<>();
        this.logFilePath = logFilePath;
    }
    
    public Map<String, Task> parseLogFile() {
        try {
            Path path = Paths.get(logFilePath);
            List<String> logLines = Files.readAllLines(path);
            Iterator<String> iterator = logLines.iterator();
            int lineNumber = 1;

            while (iterator.hasNext()) {
                String line = iterator.next();
                if (!parseLogLine(line, lineNumber)) {
                    iterator.remove();
                }
                lineNumber++;
            }
            Files.write(path, logLines);
        } catch (IOException e) {
            System.err.println("Error reading the log file: "
                    + e.getMessage());
        }
        return taskMap;
    }

    private boolean parseLogLine(String line, int lineNum) {
        if (line.trim().isEmpty()) { return false; }
        String[] parts = line.split(",");

        String time = parts[0];
        String name = parts[1].toLowerCase();
        String command = parts[2].toLowerCase();
        String desc = parts[3];
        String size = parts[4].toUpperCase();

        if(!validArgs(time, name, desc, command, size)){
            System.err.println("Malformed at line " +
                    lineNum + ", removing line <" + line + ">");
            return false;
        }

        Instant parsedTime = Instant.parse(time);

        // Case: Start a new task
        Task task = taskMap.get(name);
        if(command.equals("start") && task == null){
            taskMap.put(name, new Task(parsedTime));
            return true;
        }

        // Case: commands but never started
        if(command.matches("stop|describe|size")
                && task == null){
            printError(lineNum, name, " never started");
        }

        // Case valid commands
        return handleCommands(name, lineNum, command, parsedTime, desc, size);
    }

    private boolean handleCommands(String name, int lineNum,
                                   String command, Instant parsedTime,
                                   String desc, String size){
        switch (command){
            case "start":
                if(validStart(name)){
                    printError(lineNum, name, " never stopped");
                    return false;
                }
                taskMap.get(name).upsertTimeEntry(parsedTime, command);
                break;
            case "stop":
                if(validStop(name)){
                    printError(lineNum, name, " no matching start");
                    return false;
                } else if(taskMap.get(name).isNegativeDuration(parsedTime)){
                    printError(lineNum, name, " negative duration");
                    return false;
                }
                taskMap.get(name).upsertTimeEntry(parsedTime, command);
                break;
            case "describe":
                taskMap.get(name).setDescription(desc);
                if(!size.equals("null")){
                    taskMap.get(name).setSize(size);
                }
                break;
            case "size":
                taskMap.get(name).setSize(size);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean validStart(String name){
        return !taskMap.get(name).lastEntryStopped();
    }

    private boolean validStop(String name){
        return taskMap.get(name).lastEntryStopped();
    }

    private boolean validArgs(String time, String name, String desc,
                              String command, String size){
        // Valid Time
        try {
            Instant.parse(time);
        } catch (Exception e) {
            return false;
        }
        // Name and Command required for all commands
        if (name.equals("null")
                || !command.matches("start|stop|describe|size")){
            return false;
        }

        // TShirtSize is required
        if(command.equals("size") && size.equals("NULL")
                && !size.matches("S|M|L|XL")){
            return false;
        }

        // TShirtSize is optional
        if (command.equals("describe") && (size.equals(
                "NULL")
                || size.matches("S|M|L|XL"))){
            return false;
        }

        if (command.equals("describe") && desc.equals("null")){
            return false;
        }

        return true;
    }

    private void printError(int lineNum, String name, String condition){
        System.err.println("Log Parsing Error: line " +
                lineNum + " " + name + " " + condition + ", removing " +
                "line");
    }
}

class Task {
    private String description;
    private String size;
    private final Stack<TimeEntry> timeEntries;

    public Task(Instant startTime){
        this.timeEntries = new Stack<>();
        this.timeEntries.push(new TimeEntry(startTime));
    }

    public boolean lastEntryStopped(){
        return timeEntries.peek().hasStop();
    }

    public boolean isNegativeDuration(Instant stop){
        return timeEntries.peek().isNegativeDuration(stop);
    }

    public void upsertTimeEntry(Instant time, String command) {
        if (command.equals("start")){
            timeEntries.push(new TimeEntry(time));
        } else if (command.equals("stop")) {
            timeEntries.peek().setStop(time);
        }
    }

    public String getSize() {
        return size;
    }

    public String getDescription() {
        return description;
    }

    public Duration getTotalDuration(){
        return timeEntries.stream()
                .filter(TimeEntry::hasStop)
                .map(TimeEntry::getDuration)
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

class TimeEntry{
    private final Instant start;
    private Instant stop;
    public TimeEntry(Instant start){
        this.start = start;
        this.stop = null;
    }

    public void setStop(Instant end) {
        stop = end;
    }

    public boolean isNegativeDuration(Instant stop){
        return Duration.between(start, stop).compareTo(Duration.ZERO) < 0;
    }

    public boolean hasStop(){
        return stop != null;
    }

    public Duration getDuration(){
        return Duration.between(start, stop);
    }
}

class IllegalCommandException extends Exception {
    public IllegalCommandException(String taskName, String reason){
        super("Error Executing Command: " + taskName + " " + reason);
    }
}

class MissingArgumentException extends Exception {
    public MissingArgumentException(String e){
        super("Missing Argument(s), Usage: " + e);
    }
}