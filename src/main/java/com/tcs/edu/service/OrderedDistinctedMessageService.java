package com.tcs.edu.service;

import com.tcs.edu.decorator.MessageDecorator;
import com.tcs.edu.decorator.Severity;
import com.tcs.edu.decorator.SeverityMessageDecorator;
import com.tcs.edu.decorator.TypographicMessageDecorator;
import com.tcs.edu.domain.Message;
import com.tcs.edu.repository.MessageRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code OrderedDistinctedMessageService} processes decorated messages with typography and severity labels to storage.
 * Allows to specify the order and/or filtering for repetitions.
 *
 * @author Zakhar Starokozhev
 */
public final class OrderedDistinctedMessageService extends ValidatedMessageService implements MessageService {
    private final MessageDecorator decorator;
    private final MessageRepository repository;
    /**
     * <code>messageCount</code> stores the proceeded line number
     */
    private final AtomicInteger messageCount = new AtomicInteger(1);

    /**
     * @param decorator  {@link MessageDecorator} specific addition to decorate process
     * @param repository {@link MessageRepository} storage for processed messages
     */
    public OrderedDistinctedMessageService(MessageDecorator decorator, MessageRepository repository) {
        this.decorator = Objects.requireNonNull(decorator, "Service decorator must be not NULL");
        this.repository = Objects.requireNonNull(repository, "Service repository must be not NULL");
    }

    public void process(Message... messages) throws LogException {
        try {
            isArgsValid(messages);
        } catch (IllegalArgumentException e) {
            throw new LogException("Message processing error", e);
        }
        proceedToRepository(messages);
    }

    public void process(Order order, Message... messages) throws LogException {
        process(processReverse(order, messages));
    }

    public void process(Doubling doubling, Message... messages) throws LogException {
        process(processUnique(doubling, messages));
    }

    public void process(Order order, Doubling doubling, Message... messages) throws LogException {
        messages = processReverse(order, messages);
        process(processUnique(doubling, messages));
    }

    public Collection<Message> findAll() {
        return repository.findAll();
    }

    public Collection<Message> findAllBySeverity(Severity by) {
        return repository.findAllBySeverity(by);
    }

    public Message findById(UUID id) {
        return repository.findByPrimaryKey(id);
    }

    /**
     * Outputs an array of <code>Message</>s in the passed order.
     *
     * @param messages array of <code>Message</>s to filter
     * @param order    the order of messages in the transmitted array
     * @return filtered array of <code>Message</>s
     */
    private Message[] processReverse(Order order, Message[] messages) {
        if (order == Order.DESC) {
            Message[] reversedList = new Message[messages.length];
            int reversedIndex = 0;
            for (int i = messages.length; i > 0; i--) {
                reversedList[reversedIndex++] = messages[i - 1];
            }
            return reversedList;
        } else {
            return messages;
        }
    }

    /**
     * Returns array of <code>Message</>s with or without duplicated messages depending on {@link Doubling} passed.
     *
     * @param messages array of <code>Message</>s to filter
     * @param doubles  {@link Doubling} filter type
     * @return filtered array of <code>Message</>s
     */
    private Message[] processUnique(Doubling doubles, Message[] messages) {
        if (doubles == Doubling.DISTINCT) {
            Set<Message> set = new LinkedHashSet<>(Arrays.asList(messages));
            Message[] uniqueList = new Message[set.size()];
            int i = 0;
            for (Message message : set) {
                uniqueList[i++] = message;
            }
            return uniqueList;
        } else {
            return messages;
        }
    }

    /**
     * Put decorated messages to storage (i.e. repository)
     * Side effect on global {@link #messageCount} - increment for each message passed in.
     *
     * @param messages - message to be stored in repository
     */
    private void proceedToRepository(Message... messages) {
        for (Message message : messages) {
            message = new SeverityMessageDecorator().decorate(message);
            message = decorator.decorate(message);
            message = new TypographicMessageDecorator(messageCount.getAndIncrement()).decorate(message);
            repository.create(message);
        }
    }
}

