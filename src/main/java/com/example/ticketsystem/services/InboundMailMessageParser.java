package com.example.ticketsystem.services;

import com.example.ticketsystem.models.InboundMailMessage;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class InboundMailMessageParser {

	public InboundMailMessage parse(Message message) throws MessagingException, IOException {
		return new InboundMailMessage(
				normalizeSubject(message.getSubject()),
				formatAddresses(message.getFrom()),
				normalizeBody(extractText(message))
		);
	}

	private String normalizeSubject(String subject) {
		if (subject == null || subject.isBlank()) {
			return "E-Mail ohne Betreff";
		}
		return subject.trim();
	}

	private String formatAddresses(Address[] addresses) {
		if (addresses == null || addresses.length == 0) {
			return "unbekannt";
		}

		StringBuilder builder = new StringBuilder();
		for (Address address : addresses) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(address.toString());
		}
		return builder.toString();
	}

	private String normalizeBody(String body) {
		if (body == null || body.isBlank()) {
			return "Kein Nachrichtentext vorhanden.";
		}
		return body.trim();
	}

	private String extractText(Part part) throws MessagingException, IOException {
		Object content = part.getContent();
		if (content instanceof Multipart multipart) {
			return extractTextFromMultipart(multipart);
		}

		if (part.isMimeType("text/plain")) {
			return content == null ? "" : content.toString();
		}

		if (part.isMimeType("text/html")) {
			return stripHtml(content == null ? "" : content.toString());
		}

		if (part.isMimeType("multipart/*")) {
			return content == null ? "" : content.toString();
		}

		return "";
	}

	private String extractTextFromMultipart(Multipart multipart) throws MessagingException, IOException {
		String htmlFallback = "";
		for (int index = 0; index < multipart.getCount(); index++) {
			BodyPart bodyPart = multipart.getBodyPart(index);
			String text = extractText(bodyPart);
			if (bodyPart.isMimeType("text/plain") && !looksLikeHtml(text) && !text.isBlank()) {
				return text;
			}
			if (htmlFallback.isBlank() && (bodyPart.isMimeType("text/html") || looksLikeHtml(text))) {
				htmlFallback = stripHtml(text);
			}
		}
		return htmlFallback;
	}

	private boolean looksLikeHtml(String text) {
		if (text == null) {
			return false;
		}
		return text.matches("(?is).*<\\s*[a-z][^>]*>.*");
	}

	private String stripHtml(String html) {
		return html.replaceAll("(?is)<br\\s*/?>", "\n")
				.replaceAll("(?is)</p>", "\n")
				.replaceAll("(?is)<[^>]+>", " ")
				.replaceAll("[ \\t\\x0B\\f\\r]+", " ")
				.trim();
	}
}
