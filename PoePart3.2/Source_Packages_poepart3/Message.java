/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poepart3;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * QuickChat messages.
 * Part 2: create, check, send and store messages.
 * Part 3: arrays, stored messages menu (JSON), search, delete and report.
 */
public class Message {

    // ---------------- Part 2 constants (unchanged) ----------------
    public static final String SENT = "Message successfully sent.";
    public static final String DELETE_PROMPT = "Press 0 to delete the message.";
    public static final String STORED = "Message successfully stored.";
    private static final String JSON_FILE = "messages.json";

    // ---------------- Part 3 flags ----------------
    private static final String FLAG_SENT = "Sent";
    private static final String FLAG_STORED = "Stored";

    // ---------------- Part 3 required arrays ----------------
    private final ArrayList<String> sentMessages = new ArrayList<>();
    private final ArrayList<String> disregardedMessages = new ArrayList<>();
    private final ArrayList<String> storedMessages = new ArrayList<>();
    private final ArrayList<String> messageHash = new ArrayList<>();
    private final ArrayList<String> messageID = new ArrayList<>();

    // Parallel to messageID / messageHash: one index = one sent or stored message
    private final ArrayList<String> recipients = new ArrayList<>();
    private final ArrayList<String> texts = new ArrayList<>();
    private final ArrayList<String> flags = new ArrayList<>();

    private final HashSet<String> usedIds = new HashSet<>();
    private final Random random = new Random();
    private final Scanner input;

    private int totalSent = 0;
    private int messageLimit = 0;
    private int entered = 0;
    private String sender = "Developer";

    public Message() {
        this(new Scanner(System.in));
    }

    public Message(Scanner input) {
        this.input = input;
    }

    public void setMessageLimit(int limit) {
        this.messageLimit = limit;
    }

    /** The logged-in user (shown as the sender of stored messages). */
    public void setSender(String sender) {
        this.sender = sender;
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = input.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    // =====================================================================
    // PART 2 (unchanged behaviour)
    // =====================================================================

    public String generateMessageID() {
        String id;
        do {
            long number = 1000000000L + random.nextLong(9000000000L);
            id = String.valueOf(number);
        } while (usedIds.contains(id));
        usedIds.add(id);
        return id;
    }

    public boolean checkMessageID(String messageID) {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    public String checkRecipientCell(String cell) {
        if (cell != null && cell.matches("^\\+[0-9]{10,13}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.";
    }

    public String checkMessageLength(String text) {
        if (text.length() > 250) {
            return "Message exceeds 250 characters by " + (text.length() - 250)
                    + ", please reduce the size.";
        }
        return "Message ready to send.";
    }

    public String createMessageHash(String messageID, int messageNumber, String messageText) {
        String prefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^A-Za-z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        return (prefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    public String sentMessage(int choice) {
        switch (choice) {
            case 1:
                return SENT;
            case 2:
                return DELETE_PROMPT;
            case 3:
                return STORED;
            default:
                return "Invalid option.";
        }
    }

    public String sentMessage() {
        while (true) {
            System.out.println("\nChoose what you want to do with this message:");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int choice = readInt("Enter your choice: ");

            if (choice < 1 || choice > 3) {
                System.out.println("Invalid option, please try again.");
                continue;
            }
            String result = sentMessage(choice);
            System.out.println(result);

            if (choice == 2) {
                int del;
                do {
                    del = readInt("");
                } while (del != 0);
            }
            return result;
        }
    }

    public void addSentMessage(String id, String hash, String recipient, String text) {
        addRecord(id, hash, recipient, text, FLAG_SENT);
        totalSent++;
    }

    /** Prints only the sent messages (Part 2). */
    public String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texts.size(); i++) {
            if (FLAG_SENT.equals(flags.get(i))) {
                sb.append("\n----------------------------------------");
                sb.append("\nMessage ID:   ").append(messageID.get(i));
                sb.append("\nMessage Hash: ").append(messageHash.get(i));
                sb.append("\nRecipient:    ").append(recipients.get(i));
                sb.append("\nMessage:      ").append(texts.get(i));
            }
        }
        return sb.toString();
    }

    public int returnTotalMessages() {
        return totalSent;
    }

    /** Stores a message: adds it to the Stored array and saves all stored messages to the JSON file. */
    public boolean storeMessage(String id, String hash, String recipient, String text) {
        addStoredMessage(id, hash, recipient, text);
        return saveStoredMessages(JSON_FILE);
    }

    private String readRecipient() {
        while (true) {
            System.out.print("Enter recipient cell number (with international code, e.g. +27718693002): ");
            String cell = input.nextLine().trim();
            String result = checkRecipientCell(cell);
            System.out.println(result);
            if (result.equals("Cell phone number successfully captured.")) {
                return cell;
            }
        }
    }

    private String readMessageText() {
        while (true) {
            System.out.print("Enter your message: ");
            String text = input.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
                continue;
            }
            if (text.length() > 250) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(checkMessageLength(text));
                continue;
            }
            System.out.println(checkMessageLength(text));
            return text;
        }
    }

    public void sendMessages() {
        if (entered >= messageLimit) {
            System.out.println("You have already entered all " + messageLimit + " messages.");
            return;
        }

        while (entered < messageLimit) {
            System.out.println("\nMESSAGE " + (entered + 1) + " of " + messageLimit);

            String recipient = readRecipient();
            String text = readMessageText();
            String id = generateMessageID();
            String hash = createMessageHash(id, entered, text);

            String result = sentMessage();
            entered++;

            if (result.equals(SENT)) {
                addSentMessage(id, hash, recipient, text);
                System.out.println("\nMessage ID:   " + id);
                System.out.println("Message Hash: " + hash);
                System.out.println("Recipient:    " + recipient);
                System.out.println("Message:      " + text);
            } else if (result.equals(STORED)) {
                storeMessage(id, hash, recipient, text);
            } else {
                addDisregardedMessage(text);
                System.out.println("Message deleted.");
            }
        }

        System.out.println("\n===== Messages sent =====" + printMessages());
        System.out.println("\nTotal messages sent: " + returnTotalMessages());
    }

    // =====================================================================
    // PART 3 - populating the arrays
    // =====================================================================

    public void addStoredMessage(String id, String hash, String recipient, String text) {
        addRecord(id, hash, recipient, text, FLAG_STORED);
    }

    public void addDisregardedMessage(String text) {
        disregardedMessages.add(text);
    }

    private void addRecord(String id, String hash, String recipient, String text, String flag) {
        messageID.add(id);
        messageHash.add(hash);
        recipients.add(recipient);
        texts.add(text);
        flags.add(flag);
        usedIds.add(id);
        if (FLAG_SENT.equals(flag)) {
            sentMessages.add(text);
        } else {
            storedMessages.add(text);
        }
    }

    // =====================================================================
    // PART 3 - menu option 4 "Stored Messages"
    // =====================================================================

    /** a. Sender and recipient of all stored messages. */
    public ArrayList<String> displaySenderAndRecipient() {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < flags.size(); i++) {
            if (FLAG_STORED.equals(flags.get(i))) {
                result.add("Sender: " + sender + " | Recipient: " + recipients.get(i));
            }
        }
        return result;
    }

    /** b. The longest stored message. */
    public String displayLongestMessage() {
        String longest = null;
        for (String m : storedMessages) {
            if (longest == null || m.length() > longest.length()) {
                longest = m;
            }
        }
        return longest == null ? "There are no stored messages." : longest;
    }

    /** c. Search for a message ID; returns the message text (null if not found). */
    public String searchMessageID(String id) {
        int index = messageID.indexOf(id);
        return index < 0 ? null : texts.get(index);
    }

    /** c. Same search, formatted with the recipient and the message for display. */
    public String searchMessageIDDetails(String id) {
        int index = messageID.indexOf(id);
        if (index < 0) {
            return "Message ID not found.";
        }
        return "Recipient: " + recipients.get(index) + " | Message: " + texts.get(index);
    }

    /** d. All messages (sent or stored) for a particular recipient. */
    public ArrayList<String> searchByRecipient(String recipient) {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            if (recipients.get(i).equals(recipient)) {
                result.add(texts.get(i));
            }
        }
        return result;
    }

    /** e. Delete a message using its message hash. */
    public String deleteByHash(String hash) {
        int index = messageHash.indexOf(hash);
        if (index < 0) {
            return "Message hash not found.";
        }
        String text = texts.get(index);
        if (FLAG_SENT.equals(flags.get(index))) {
            sentMessages.remove(text);
            totalSent--;
        } else {
            storedMessages.remove(text);
        }
        messageHash.remove(index);
        messageID.remove(index);
        recipients.remove(index);
        texts.remove(index);
        flags.remove(index);
        String shown = text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
        return "Message: \"" + shown + "\" successfully deleted.";
    }

    /**
     * f. Report with the full details of all stored messages (the task) and
     * of all sent messages (the unit test): ID, hash, sender, recipient, message.
     */
    public String displayReport() {
        StringBuilder sb = new StringBuilder("===== MESSAGES REPORT =====\n\n");
        appendSection(sb, "SENT MESSAGES", FLAG_SENT);
        appendSection(sb, "STORED MESSAGES", FLAG_STORED);
        return sb.toString();
    }

    private void appendSection(StringBuilder sb, String title, String flag) {
        sb.append("--- ").append(title).append(" ---\n");
        int count = 0;
        for (int i = 0; i < flags.size(); i++) {
            if (flag.equals(flags.get(i))) {
                count++;
                sb.append("Message ").append(count).append('\n')
                  .append("Message ID:   ").append(messageID.get(i)).append('\n')
                  .append("Message Hash: ").append(messageHash.get(i)).append('\n')
                  .append("Sender:       ").append(sender).append('\n')
                  .append("Recipient:    ").append(recipients.get(i)).append('\n')
                  .append("Message:      ").append(texts.get(i)).append("\n\n");
            }
        }
        if (count == 0) {
            sb.append("None.\n\n");
        }
    }

    /** The Stored Messages menu (main menu option 4). */
    public void storedMessagesMenu() {
        int loaded = loadStoredMessages();
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) read from " + JSON_FILE + ".");
        }
        int choice = 0;
        while (choice != 7) {
            System.out.println("\n===== STORED MESSAGES =====");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages for a recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a report of all messages");
            System.out.println("7) Back to main menu");
            choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> {
                    ArrayList<String> list = displaySenderAndRecipient();
                    if (list.isEmpty()) {
                        System.out.println("There are no stored messages.");
                    }
                    for (String line : list) {
                        System.out.println(line);
                    }
                }
                case 2 -> System.out.println("Longest stored message: " + displayLongestMessage());
                case 3 -> {
                    System.out.print("Enter the message ID: ");
                    System.out.println(searchMessageIDDetails(input.nextLine().trim()));
                }
                case 4 -> {
                    System.out.print("Enter the recipient cell number: ");
                    ArrayList<String> found = searchByRecipient(input.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }
                    for (String m : found) {
                        System.out.println("- " + m);
                    }
                }
                case 5 -> {
                    System.out.print("Enter the message hash: ");
                    System.out.println(deleteByHash(input.nextLine().trim()));
                    saveStoredMessages(JSON_FILE);
                }
                case 6 -> System.out.println(displayReport());
                case 7 -> System.out.println("Returning to main menu.");
                default -> System.out.println("Invalid option, please try again.");
            }
        }
    }

    // =====================================================================
    // PART 3 - JSON file (no external library needed)
    // =====================================================================

    /** Writes every stored message to the JSON file. */
    public boolean saveStoredMessages(String fileName) {
        ArrayList<String> objects = new ArrayList<>();
        for (int i = 0; i < flags.size(); i++) {
            if (FLAG_STORED.equals(flags.get(i))) {
                objects.add("  {\n"
                        + "    \"messageID\": \"" + esc(messageID.get(i)) + "\",\n"
                        + "    \"messageHash\": \"" + esc(messageHash.get(i)) + "\",\n"
                        + "    \"recipient\": \"" + esc(recipients.get(i)) + "\",\n"
                        + "    \"message\": \"" + esc(texts.get(i)) + "\"\n"
                        + "  }");
            }
        }
        try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
            writer.write("[\n" + String.join(",\n", objects) + "\n]\n");
            return true;
        } catch (IOException e) {
            System.out.println("Could not write " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public int loadStoredMessages() {
        return loadStoredMessages(JSON_FILE);
    }

    /**
     * Reads the JSON file into the Stored Messages array.
     * Stored messages already in memory are replaced, so nothing is duplicated.
     */
    public int loadStoredMessages(String fileName) {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            removeAllStored();
            int loaded = 0;
            for (Map<String, String> m : parseJsonArray(json)) {
                String text = m.getOrDefault("message", "");
                String id = m.getOrDefault("messageID", generateMessageID());
                String hash = m.getOrDefault("messageHash",
                        createMessageHash(id, loaded, text.isEmpty() ? "x" : text));
                addStoredMessage(id, hash, m.getOrDefault("recipient", ""), text);
                loaded++;
            }
            return loaded;
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
            return 0;
        }
    }

    private void removeAllStored() {
        for (int i = flags.size() - 1; i >= 0; i--) {
            if (FLAG_STORED.equals(flags.get(i))) {
                messageHash.remove(i);
                messageID.remove(i);
                recipients.remove(i);
                texts.remove(i);
                flags.remove(i);
            }
        }
        storedMessages.clear();
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    /** Minimal parser for a JSON array of flat objects such as [{"a":"b"}, ...]. */
    static ArrayList<Map<String, String>> parseJsonArray(String json) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        Map<String, String> current = null;
        int i = 0;
        int n = json.length();
        while (i < n) {
            char c = json.charAt(i);
            if (c == '{') {
                current = new LinkedHashMap<>();
                i++;
            } else if (c == '}') {
                if (current != null) {
                    list.add(current);
                }
                current = null;
                i++;
            } else if (c == '"' && current != null) {
                int[] pos = {i};
                String key = readString(json, pos);
                i = pos[0];
                while (i < n && json.charAt(i) != ':') {
                    i++;
                }
                i++;
                while (i < n && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                if (i < n && json.charAt(i) == '"') {
                    pos[0] = i;
                    current.put(key, readString(json, pos));
                    i = pos[0];
                } else {
                    int start = i;
                    while (i < n && json.charAt(i) != ',' && json.charAt(i) != '}') {
                        i++;
                    }
                    current.put(key, json.substring(start, i).trim());
                }
            } else {
                i++;
            }
        }
        return list;
    }

    private static String readString(String s, int[] pos) {
        StringBuilder sb = new StringBuilder();
        int i = pos[0] + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char e = s.charAt(i + 1);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 5 < s.length()) {
                            sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                            i += 4;
                        }
                        break;
                    default: sb.append(e);
                }
                i += 2;
            } else if (c == '"') {
                pos[0] = i + 1;
                return sb.toString();
            } else {
                sb.append(c);
                i++;
            }
        }
        pos[0] = i;
        return sb.toString();
    }

    // ---------------- getters (used by the unit tests) ----------------
    public ArrayList<String> getSentMessages() { return sentMessages; }
    public ArrayList<String> getDisregardedMessages() { return disregardedMessages; }
    public ArrayList<String> getStoredMessages() { return storedMessages; }
    public ArrayList<String> getMessageHash() { return messageHash; }
    public ArrayList<String> getMessageID() { return messageID; }
}
