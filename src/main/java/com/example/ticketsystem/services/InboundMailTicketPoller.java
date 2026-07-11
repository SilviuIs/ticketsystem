package com.example.ticketsystem.services;

import com.example.ticketsystem.models.InboundMailMessage;
import com.example.ticketsystem.models.Ticket;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "ticket-system.inbound-mail", name = "enabled", havingValue = "true")
public class InboundMailTicketPoller {

	private static final Logger LOGGER = LoggerFactory.getLogger(InboundMailTicketPoller.class);

	private final InboundMailProperties properties;
	private final InboundMailMessageParser parser;
	private final InboundMailTicketImportService importService;

	public InboundMailTicketPoller(
			InboundMailProperties properties,
			InboundMailMessageParser parser,
			InboundMailTicketImportService importService
	) {
		this.properties = properties;
		this.parser = parser;
		this.importService = importService;
	}

	@Scheduled(fixedDelayString = "${ticket-system.inbound-mail.poll-delay-ms:60000}")
	public void pollMailbox() {
		try {
			pollUnreadMessages();
		}
		catch (MessagingException | IOException | RuntimeException exception) {
			LOGGER.warn("inbound_mail_poll_failed message={}", exception.getMessage());
		}
	}

	private void pollUnreadMessages() throws MessagingException, IOException {
		Store store = null;
		Folder folder = null;
		try {
			store = createSession().getStore(properties.getProtocol());
			store.connect(
					properties.getHost(),
					properties.getPort(),
					properties.getUsername(),
					properties.getPassword()
			);
			folder = store.getFolder(properties.getFolder());
			folder.open(Folder.READ_WRITE);

			Message[] unreadMessages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			for (Message message : unreadMessages) {
				importUnreadMessage(message);
			}
		}
		finally {
			closeFolder(folder);
			closeStore(store);
		}
	}

	private Session createSession() {
		Properties sessionProperties = new Properties();
		sessionProperties.put("mail.store.protocol", properties.getProtocol());
		sessionProperties.put("mail." + properties.getProtocol() + ".host", properties.getHost());
		sessionProperties.put("mail." + properties.getProtocol() + ".port", Integer.toString(properties.getPort()));
		return Session.getInstance(sessionProperties);
	}

	private void importUnreadMessage(Message message) throws MessagingException, IOException {
		InboundMailMessage inboundMessage = parser.parse(message);
		Ticket ticket = importService.createTicketFromMail(inboundMessage);
		message.setFlag(Flags.Flag.SEEN, true);
		LOGGER.info("inbound_mail_ticket_created ticketId={} from={}", ticket.getId(), inboundMessage.from());
	}

	private void closeFolder(Folder folder) {
		if (folder == null || !folder.isOpen()) {
			return;
		}
		try {
			folder.close(false);
		}
		catch (MessagingException exception) {
			LOGGER.warn("inbound_mail_folder_close_failed message={}", exception.getMessage());
		}
	}

	private void closeStore(Store store) {
		if (store == null || !store.isConnected()) {
			return;
		}
		try {
			store.close();
		}
		catch (MessagingException exception) {
			LOGGER.warn("inbound_mail_store_close_failed message={}", exception.getMessage());
		}
	}
}
