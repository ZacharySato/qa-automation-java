package com.tcs.edu.repository;

import com.tcs.edu.decorator.Severity;
import com.tcs.edu.domain.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.tcs.edu.decorator.Severity.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public class InMemoryMessageRepositoryTests {
    private final MessageRepository storage = new InMemoryMessageRepositoryImpl();
    private final String testMessageContent = "Test Message";


    @Test
    @DisplayName("Save message")
    public void createdMessageExistsInStorage() {
        storage.create(new Message(testMessageContent));
        Optional<Message> message = storage.findAll().stream().filter(
                e -> e.getBody().equals(testMessageContent)
        ).findFirst();

        assertAll(
                () -> assertThat("Message found in storage.", message.isPresent()),
                () -> assertThat(storage.findAll().size(), is(1))
        );

    }

    @Test
    @DisplayName("Find message by key")
    public void findMessageByIdInStorage() {
        storage.create(new Message(MINOR, "Sample Message"));
        storage.create(new Message(REGULAR, "Example Message"));
        storage.create(new Message(MAJOR, testMessageContent));
        UUID keyToFind = storage.findAll().stream().
                filter(message -> message.getSeverity() == MAJOR).findFirst().get().getId();

        assertThat(storage.findByPrimaryKey(keyToFind).getBody(), is(testMessageContent));
    }

    @Test
    @DisplayName("Find elements by severity")
    public void findMessagesBySeverityInStorage() {
        Severity[] severities = new Severity[]{MINOR, REGULAR, MAJOR, MINOR, MINOR, MAJOR};
        for (Severity severity : severities) {
            storage.create(new Message(severity));
        }
        assertThat(storage.findAllBySeverity(MAJOR).size(), is(2));
    }
}
