import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *  Author @Ahmed Yasser
 * Simple Command-Line To-Do App
 * Commands:
 *   add "Buy milk" [--priority N] [--tags t1,t2]
 *   list
 *   done <index>
 *   delete <index>
 *   help
 *   exit
 *
 * Tasks are stored in tasks.txt (tab-delimited):
 * done\tcreatedISO\tpriority\ttags\tdescription\n
 *
 * Single-file implementation for easy compilation:
 * javac TaskManager.java
 * java TaskManager
 */
public class TaskManager {
    private static final Path STORAGE = Paths.get("tasks.txt");
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    static class Task {
        boolean done;
        String createdIso;
        int priority;
        List<String> tags;
        String description;

        Task(boolean done, String createdIso, int priority, List<String> tags, String description) {
            this.done = done;
            this.createdIso = createdIso;
            this.priority = priority;
            this.tags = tags;
            this.description = (description == null) ? "" : description;
        }

        static Task fromLine(String line) {
            // expected 5 fields separated by tab
            String[] parts = line.split("\t", -1); // keep empty fields
            if (parts.length < 5) {
                // fallback for older/malformed lines
                String desc = parts.length > 0 ? parts[0] : "";
                return new Task(false, Instant.now().toString(), 0, new ArrayList<>(), desc);
            }
            boolean done = parts[0].equals("1");
            String created = parts[1];
            int priority = 0;
            try { priority = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            List<String> tags = new ArrayList<>();
            if (!parts[3].isEmpty()) {
                for (String t : parts[3].split(",")) {
                    if (!t.trim().isEmpty()) tags.add(t.trim());
                }
            }
            String desc = parts[4].replace("\\n", "\n").replace("\\t", "\t");
            return new Task(done, created, priority, tags, desc);
        }

        String toLine() {
            String tagsField = String.join(",", tags);
            String descEscaped = description.replace("\t", "\\t").replace("\n", "\\n");
            return (done ? "1" : "0") + "\t" + createdIso + "\t" + priority + "\t" + tagsField + "\t" + descEscaped;
        }

        String createdDisplay() {
            try {
                Instant ins = Instant.parse(createdIso);
                return DISPLAY_FMT.format(ins);
            } catch (Exception e) {
                return createdIso;
            }
        }
    }

    private final List<Task> tasks = new ArrayList<>();

    public static void main(String[] args) {
        TaskManager app = new TaskManager();
        app.load();
        System.out.println("Simple To-Do App (type 'help' for commands)");
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) break;
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    app.save();
                    System.out.println("Goodbye!");
                    break;
                }
                app.handle(line);
            }
        }
    }

    private void handle(String input) {
        String[] parts = tokenize(input);
        if (parts.length == 0) return;
        String cmd = parts[0].toLowerCase();
        try {
            switch (cmd) {
                case "add":
                    handleAdd(parts, input);
                    break;
                case "list":
                    handleList();
                    break;
                case "done":
                    handleDone(parts);
                    break;
                case "delete":
                    handleDelete(parts);
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Basic tokenizer: splits by spaces but keeps quoted string as one token
    private String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"' ) {
                inQuotes = !inQuotes;
                continue;
            }
            if (Character.isWhitespace(c) && !inQuotes) {
                if (cur.length() > 0) {
                    tokens.add(cur.toString());
                    cur.setLength(0);
                }
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) tokens.add(cur.toString());
        return tokens.toArray(new String[0]);
    }

    private void handleAdd(String[] parts, String rawInput) {
        // Try to extract description from quotes
        String description = null;
        int firstQuote = rawInput.indexOf("\"");
        if (firstQuote >= 0) {
            int secondQuote = rawInput.indexOf("\"", firstQuote + 1);
            if (secondQuote > firstQuote) {
                description = rawInput.substring(firstQuote + 1, secondQuote);
            }
        }

        // Fallback: if no quotes found, take everything after 'add' excluding flags
        if (description == null) {
            List<String> descParts = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                if (parts[i].equalsIgnoreCase("--priority") && i + 1 < parts.length) {
                    i++; // skip priority value
                } else if (parts[i].equalsIgnoreCase("--tags") && i + 1 < parts.length) {
                    i++; // skip tags value
                } else {
                    descParts.add(parts[i]);
                }
            }
            description = String.join(" ", descParts);
        }

        // Ensure description is never null
        if (description == null) description = "";

        int priority = 0;
        List<String> tags = new ArrayList<>();
        // parse optional flags
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase("--priority") && i + 1 < parts.length) {
                try { priority = Integer.parseInt(parts[i + 1]); } catch (Exception ignored) {}
            } else if (parts[i].equalsIgnoreCase("--tags") && i + 1 < parts.length) {
                String[] t = parts[i + 1].split(",");
                for (String s : t) if (!s.trim().isEmpty()) tags.add(s.trim());
            }
        }

        Task t = new Task(false, Instant.now().toString(), priority, tags, description);
        tasks.add(t);
        save();
        System.out.println("Added: " + description);
    }


    private void handleList() {
        if (tasks.isEmpty()) {
            System.out.println("(no tasks)");
            return;
        }
        // Show with index starting at 1. Show priority, tags, timestamp
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            String status = t.done ? "[x]" : "[ ]";
            String pr = t.priority != 0 ? " (P:" + t.priority + ")" : "";
            String tg = t.tags.isEmpty() ? "" : " Tags:" + String.join(",", t.tags);
            System.out.printf("%d. %s %s%s - %s%s\n", i + 1, status, t.description, pr, t.createdDisplay(), tg);
        }
    }

    private void handleDone(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: done <index>");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            System.out.println("Invalid index.");
            return;
        }
        if (idx < 1 || idx > tasks.size()) {
            System.out.println("Index out of range.");
            return;
        }
        Task t = tasks.get(idx - 1);
        if (t.done) {
            System.out.println("Task is already marked as done: " + t.description);
            return;
        }
        t.done = true;
        save();
        System.out.println("Marked done: " + t.description);
    }

    private void handleDelete(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage: delete <index>");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            System.out.println("Invalid index.");
            return;
        }
        if (idx < 1 || idx > tasks.size()) {
            System.out.println("Index out of range.");
            return;
        }
        Task t = tasks.remove(idx - 1);
        save();
        System.out.println("Deleted: " + t.description);
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  add \"Buy milk\" [--priority N] [--tags tag1,tag2]  Add a task");
        System.out.println("  list                                               List tasks");
        System.out.println("  done <index>                                       Mark task #index as done");
        System.out.println("  delete <index>                                     Delete task #index");
        System.out.println("  help                                               Show this help");
        System.out.println("  exit                                               Save and exit");
    }

    private void load() {
        tasks.clear();
        if (!Files.exists(STORAGE)) return;
        try (BufferedReader br = Files.newBufferedReader(STORAGE)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task t = Task.fromLine(line);
                tasks.add(t);
            }
        } catch (IOException e) {
            System.out.println("Failed to load tasks: " + e.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(STORAGE)) {
            for (Task t : tasks) {
                bw.write(t.toLine());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Failed to save tasks: " + e.getMessage());
        }
    }
}
