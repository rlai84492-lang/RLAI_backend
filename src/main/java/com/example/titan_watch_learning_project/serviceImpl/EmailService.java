package com.example.titan_watch_learning_project.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generic email-sending service — Excel ya kisi bhi attachment
 * ke saath email bhejne ke liye.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Excel attachment ke saath email bhejta hai.
     *
     * @param to          recipient email(s) — comma-separated string
     * @param subject     email subject
     * @param bodyText    email body (plain text/HTML)
     * @param attachment  Excel file ke bytes
     * @param fileName    attachment ka filename (jaise "leads_2026-06-20.xlsx")
     */
    public void sendEmailWithExcelAttachment(
            String to,
            String subject,
            String bodyText,
            byte[] attachment,
            String fileName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true = multipart (attachment ke liye)

            // ── Multiple recipients (comma-separated) handle karo ──
            String[] recipients = to.split(",");
            for (int i = 0; i < recipients.length; i++) {
                recipients[i] = recipients[i].trim();
            }
            helper.setTo(recipients);

            helper.setSubject(subject);
            helper.setText(bodyText, true); // true = HTML body

            helper.addAttachment(
                    fileName,
                    new org.springframework.core.io.ByteArrayResource(attachment)
            );

            mailSender.send(message);
            log.info("Email sent successfully to={} subject={}", to, subject);

        } catch (MessagingException e) {
            log.error("Failed to send email to={} subject={}", to, subject, e);
            // ✅ YE KARO — exception propagate karo
        } catch (Exception e) {
            log.error("Unexpected error while sending email to={}", to, e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * Daily report ke liye HTML email body banata hai.
     */
    public String buildDailyReportEmailBody(int totalLeads, int callbacks, int storeVisits) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color: #1A1713;">
                    <div style="background: #0A1123; padding: 20px; border-radius: 8px 8px 0 0;">
                        <h2 style="color: #D4AF37; margin: 0;">TITAN WORLD</h2>
                        <p style="color: #94A3B8; margin: 4px 0 0 0;">Daily Lead Report</p>
                    </div>
                    <div style="padding: 20px; border: 1px solid #EEEBE6; border-top: none;">
                        <p>Hi Team,</p>
                        <p>Yahan hai aaj (<b>%s</b>) ka lead summary:</p>
                        <table style="border-collapse: collapse; width: 100%%; margin: 16px 0;">
                            <tr style="background: #F5F3F0;">
                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Total Leads</b></td>
                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Callbacks</b></td>
                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
                            </tr>
                            <tr style="background: #F5F3F0;">
                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Store Visits</b></td>
                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
                            </tr>
                        </table>
                        <p>Poori details ke liye attached Excel file dekhein.</p>
                        <p style="color: #B0A9A1; font-size: 12px; margin-top: 24px;">
                            Ye automated email hai — Titan Watch Bot Dashboard se generate hui hai.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(today, totalLeads, callbacks, storeVisits);
    }
}