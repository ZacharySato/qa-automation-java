package com.tcs.edu.service;

import com.tcs.edu.decorator.MessageDecorator;
import com.tcs.edu.decorator.TimestampMessageDecorator;
import com.tcs.edu.domain.Message;
import com.tcs.edu.repository.InMemoryMessageRepositoryImpl;
import com.tcs.edu.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tcs.edu.decorator.Severity.*;
import static com.tcs.edu.service.Doubling.DISTINCT;
import static com.tcs.edu.service.Doubling.DOUBLES;
import static com.tcs.edu.service.Order.DESC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderedDistinctedMessageServiceTests {
    private MessageService service;
    private Exception exception;


    @BeforeEach
    public void setUp(){
        MessageDecorator decorator = new TimestampMessageDecorator();
        MessageRepository repository = new InMemoryMessageRepositoryImpl();
        service = new OrderedDistinctedMessageService(decorator, repository);
    }

    @Test
    @DisplayName("Service throws custom exception on null instead of message")
    public void nullMessageTest() {
        exception = assertThrows(LogException.class, () -> service.process((Message) null));
        assertThat(exception.getCause().getMessage(), equalTo("Null passed instead of Message"));
    }

    @Test
    @DisplayName("Service throws custom exception on empty message body")
    public void emptyMessageBodyTest() {
        exception = assertThrows(LogException.class, () -> service.process(new Message(REGULAR, "")));
        assertThat(exception.getCause().getMessage(), equalTo("Empty Message passed"));
    }

    @Test
    @DisplayName("Service throws custom exception on null message severity")
    public void nullMessageSeverityTest() {
        exception = assertThrows(LogException.class, () -> service.process(new Message(null, "Sample")));
        assertThat(exception.getCause().getMessage(), equalTo("Current Message value contains null"));
    }

    @Test
    @DisplayName("Deduplicated processing")
    public void distinctProcessTest() throws LogException {
        Message doubling = new Message(MAJOR, "Первый");
        service.process(DISTINCT,
                doubling,
                new Message(REGULAR, "Второй"),
                new Message(MINOR, "Третий"),
                doubling,
                doubling
        );
        assertThat(service.findAll().size(), is(3));
    }

    @Test
    @DisplayName("Processing with doubles")
    public void doublesProcessTest() throws LogException {
        Message doubling = new Message(MAJOR, "Первый");
        service.process(DOUBLES,
                doubling,
                new Message(REGULAR, "Второй"),
                new Message(MINOR, "Третий"),
                doubling,
                doubling
        );
        assertThat(service.findAll().size(), is(5));
    }

    @Test
    @DisplayName("Processing in reverse order")
    public void descProcessTest() throws LogException {
        service.process(DESC,
                new Message(MAJOR, "Первый"),
                new Message(REGULAR, "Второй"),
                new Message(MINOR, "Третий")
        );
        Message last = service.findAll().stream().filter(message -> message.getSeverity() == MINOR).findFirst().get();
        assertThat(last.getBody(), allOf(startsWith("1"), endsWith("()")));

    }

    @Test
    @DisplayName("Processing in straight order")
    public void ascProcessTest() throws LogException {
        service.process(Order.ASC,
                new Message(MAJOR, "Первый"),
                new Message(REGULAR, "Второй"),
                new Message(MINOR, "Третий")
        );
        Message last = service.findAll().stream().filter(message -> message.getSeverity() == MAJOR).findFirst().get();
        assertThat(last.getBody(), allOf(startsWith("1"), endsWith("(!!!)")));

    }
}
