package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticketsystem.models.InboundMailMessage;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class InboundMailMessageParserTest {

	private final InboundMailMessageParser parser = new InboundMailMessageParser();

	@Test
	void parsePlainTextMessage() throws Exception {
		MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
		message.setSubject("VPN funktioniert nicht");
		message.setFrom(new InternetAddress("user@example.test", "Max User"));
		message.setText("Ich kann mich nicht verbinden.");

		InboundMailMessage parsed = parser.parse(message);

		assertThat(parsed.subject()).isEqualTo("VPN funktioniert nicht");
		assertThat(parsed.from()).contains("user@example.test");
		assertThat(parsed.body()).isEqualTo("Ich kann mich nicht verbinden.");
	}

	@Test
	void parseMultipartMessagePrefersPlainTextOverHtml() throws Exception {
		MimeBodyPart plain = new MimeBodyPart();
		plain.setText("Plain text body");
		MimeBodyPart html = new MimeBodyPart();
		html.setText("<p>HTML body</p>", "UTF-8", "html");
		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(html);
		multipart.addBodyPart(plain);

		MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
		message.setSubject("Multipart");
		message.setContent(multipart);

		assertThat(parser.parse(message).body()).isEqualTo("Plain text body");
	}

	@Test
	void parseMessageWithoutSubjectUsesFallback() throws Exception {
		MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
		message.setText("Body");

		assertThat(parser.parse(message).subject()).isEqualTo("E-Mail ohne Betreff");
		assertThat(parser.parse(message).from()).isEqualTo("unbekannt");
	}
}
