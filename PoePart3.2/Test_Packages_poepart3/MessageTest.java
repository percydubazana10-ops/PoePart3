package poepart3;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MessageTest {

    private final Message m = new Message();

    @Test
    public void messageLengthSuccess() {
        assertEquals("Message ready to send.",
                m.checkMessageLength("Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    public void messageLengthFailure() {
        String longMsg = "a".repeat(300);
        assertEquals("Message exceeds 250 characters by 50, please reduce the size.",
                m.checkMessageLength(longMsg));
    }

    @Test
    public void recipientSuccess() {
        assertEquals("Cell phone number successfully captured.",
                m.checkRecipientCell("+27718693002"));
    }

    @Test
    public void recipientFailure() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.",
                m.checkRecipientCell("08575975889"));
    }

    @Test
    public void messageHashTestData1() {
        assertEquals("23:0:HITONIGHT",
                m.createMessageHash("2345678901", 0, "Hi Mike, can you join us for dinner tonight?"));
    }

    @Test
    public void messageHashExample() {
        assertEquals("00:0:HITHANKS", m.createMessageHash("0012345678", 0, "Hi, thanks"));
    }

    @Test
    public void messageIdCreated() {
        String id = m.generateMessageID();
        assertEquals(10, id.length());
        assertTrue(m.checkMessageID(id));
    }

    @Test
    public void sentMessageOptions() {
        assertEquals("Message successfully sent.", m.sentMessage(1));
        assertEquals("Press 0 to delete the message.", m.sentMessage(2));
        assertEquals("Message successfully stored.", m.sentMessage(3));
    }

    @Test
    public void totalMessagesSent() {
        m.addSentMessage("1234567890", "12:0:HITONIGHT", "+27718693002", "Hi Mike, tonight");
        assertEquals(1, m.returnTotalMessages());
        // Message 2 was discarded, so the total stays at 1
        assertEquals(1, m.returnTotalMessages());
    }

    // =====================================================================
    // PART 3 - test data from the task (Test Data Messages 1 to 5)
    // =====================================================================

    private Message testData() {
        Message t = new Message();
        String m1 = "Did you get the cake?";
        String m2 = "Where are you? You are late! I have asked you to be on time.";
        String m3 = "Yohoooo, I am at your gate.";
        String m4 = "It is dinner time!";
        String m5 = "Ok, I am leaving without you.";
        // Message 1 - Sent
        t.addSentMessage("1000000001", t.createMessageHash("1000000001", 0, m1), "+27834557896", m1);
        // Message 2 - Stored
        t.addStoredMessage("1000000002", t.createMessageHash("1000000002", 1, m2), "+27838884567", m2);
        // Message 3 - Disregard
        t.addDisregardedMessage(m3);
        // Message 4 - Sent (the task identifies it by 0838884567)
        t.addSentMessage("0838884567", t.createMessageHash("0838884567", 2, m4), "0838884567", m4);
        // Message 5 - Stored
        t.addStoredMessage("1000000005", t.createMessageHash("1000000005", 3, m5), "+27838884567", m5);
        return t;
    }

    @Test
    public void sentMessagesArrayCorrectlyPopulated() {
        Message t = testData();
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("Did you get the cake?", "It is dinner time!"));
        assertEquals(expected, t.getSentMessages());
    }

    @Test
    public void disregardedAndStoredArraysPopulated() {
        Message t = testData();
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."), t.getDisregardedMessages());
        assertEquals(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), t.getStoredMessages());
    }

    @Test
    public void messageHashAndIdArraysPopulated() {
        Message t = testData();
        assertEquals(4, t.getMessageHash().size());
        assertEquals(4, t.getMessageID().size());
    }

    @Test
    public void displayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                testData().displayLongestMessage());
    }

    @Test
    public void searchForMessageID() {
        assertEquals("It is dinner time!", testData().searchMessageID("0838884567"));
    }

    @Test
    public void searchForMessageIDNotFound() {
        assertNull(testData().searchMessageID("9999999999"));
    }

    @Test
    public void searchAllMessagesForRecipient() {
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."));
        assertEquals(expected, testData().searchByRecipient("+27838884567"));
    }

    @Test
    public void deleteMessageUsingMessageHash() {
        Message t = testData();
        String hashOfMessage2 = t.getMessageHash().get(1);
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time\" successfully deleted.",
                t.deleteByHash(hashOfMessage2));
        assertEquals(1, t.getStoredMessages().size());
    }

    @Test
    public void displayReport() {
        Message t = testData();
        String report = t.displayReport();
        assertTrue(report.contains(t.getMessageHash().get(0)));
        assertTrue(report.contains("+27834557896"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains(t.getMessageHash().get(2)));
        assertTrue(report.contains("0838884567"));
        assertTrue(report.contains("It is dinner time!"));
        // stored messages are in the report too
        assertTrue(report.contains(t.getMessageHash().get(1)));
        assertTrue(report.contains("Ok, I am leaving without you."));
    }

    @Test
    public void storedMessagesSurviveJsonRoundTrip(@TempDir Path dir) {
        Message t = testData();
        String file = dir.resolve("messages.json").toString();
        assertTrue(t.saveStoredMessages(file));

        Message fresh = new Message();
        assertEquals(2, fresh.loadStoredMessages(file));
        assertEquals(t.getStoredMessages(), fresh.getStoredMessages());
        assertEquals("Where are you? You are late! I have asked you to be on time.", fresh.displayLongestMessage());
    }
}
