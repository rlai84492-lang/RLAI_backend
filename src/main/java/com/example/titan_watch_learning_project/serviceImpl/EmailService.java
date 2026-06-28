//package com.example.titan_watch_learning_project.serviceImpl;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
///**
// * Generic email service for sending emails with optional attachments.
// */
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    /**
//     * Sends an email with an Excel file attachment.
//     *
//     * @param to         recipient email address(es), comma-separated
//     * @param subject    email subject line
//     * @param bodyText   email body content (supports HTML)
//     * @param attachment Excel file bytes to attach
//     * @param fileName   attachment filename (e.g. "leads_2026-06-20.xlsx")
//    //     */
////    public void sendEmailWithExcelAttachment(
////            String to,
////            String subject,
////            String bodyText,
////            byte[] attachment,
////            String fileName
////    ) {
////        try {
////            MimeMessage message = mailSender.createMimeMessage();
////            MimeMessageHelper helper = new MimeMessageHelper(message, true);
////
////            helper.setFrom("bela@rightleft.ai"); // ✅ Sender fix
////
////            // Support multiple comma-separated recipients
////            String[] recipients = to.split(",");
////            for (int i = 0; i < recipients.length; i++) {
////                recipients[i] = recipients[i].trim();
////            }
////            helper.setTo(recipients);
////
////            helper.setSubject(subject);
////            helper.setText(bodyText, true); // true = HTML content
////            helper.addAttachment(fileName, new ByteArrayResource(attachment));
////
////            mailSender.send(message);
////            log.info("Email sent successfully. to={} subject={}", to, subject);
////
////        } catch (MessagingException e) {
////            log.error("Failed to send email. to={} subject={}", to, subject, e);
////            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
////        } catch (Exception e) {
////            log.error("Unexpected error while sending email. to={}", to, e);
////            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
////        }
////    }
//
//
//
//    public void sendEmailWithExcelAttachment(
//            String to,
//            String subject,
//            String bodyText,
//            byte[] attachment,
//            String fileName
//    ) {
//        String[] recipients = to.split(",");
//
//        for (String recipient : recipients) {
//            try {
//                MimeMessage message = mailSender.createMimeMessage();
//                MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//                helper.setFrom("bela@rightleft.ai");
//                helper.setTo(recipient.trim());
//                helper.setSubject(subject);
//                helper.setText(bodyText, true);
//                helper.addAttachment(fileName, new ByteArrayResource(attachment));
//
//                mailSender.send(message);
//                log.info("Email sent successfully to={}", recipient.trim());
//
//            } catch (MessagingException e) {
//                log.error("Failed to send email to={} — skipping to next recipient", recipient.trim(), e);
//                // Continue loop — next recipient pe jao
//            } catch (Exception e) {
//                log.error("Unexpected error for recipient={} — skipping to next", recipient.trim(), e);
//                // Continue loop — next recipient pe jao
//            }
//        }
//    }
//
//    /**
//     * Builds the HTML email body for the daily lead report.
//     *
//     * @param totalLeads  total number of leads for the day
//     * @param callbacks   number of callback requests
//     * @param storeVisits number of store visit requests
//     * @return formatted HTML string
//     */
//    public String buildDailyReportEmailBody(int totalLeads, int callbacks, int storeVisits) {
//        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
//
//        return """
//                <html>
//                <body style="font-family: Arial, sans-serif; color: #1A1713;">
//                    <div style="background: #0A1123; padding: 20px; border-radius: 8px 8px 0 0;">
//                        <h2 style="color: #D4AF37; margin: 0;">TITAN WORLD</h2>
//                        <p style="color: #94A3B8; margin: 4px 0 0 0;">Daily Lead Report</p>
//                    </div>
//                    <div style="padding: 20px; border: 1px solid #EEEBE6; border-top: none;">
//                        <p>Hi Team,</p>
//                        <p>Please find below the lead summary for <b>%s</b>:</p>
//                        <table style="border-collapse: collapse; width: 100%%; margin: 16px 0;">
//                            <tr style="background: #F5F3F0;">
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Total Leads</b></td>
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
//                            </tr>
//                            <tr>
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Callbacks</b></td>
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
//                            </tr>
//                            <tr style="background: #F5F3F0;">
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;"><b>Store Visits</b></td>
//                                <td style="padding: 10px; border: 1px solid #EEEBE6;">%d</td>
//                            </tr>
//                        </table>
//                        <p>Please refer to the attached Excel file for complete details.</p>
//                        <p style="color: #B0A9A1; font-size: 12px; margin-top: 24px;">
//                            This is an automated email generated by the Titan Watch Bot Dashboard.
//                        </p>
//                    </div>
//                </body>
//                </html>
//                """.formatted(today, totalLeads, callbacks, storeVisits);
//    }
//}



package com.example.titan_watch_learning_project.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmailWithExcelAttachment(
            String to,
            String subject,
            String bodyText,
            byte[] attachment,
            String fileName
    ) {
        String[] recipients = to.split(",");

        for (String recipient : recipients) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom("bela@rightleft.ai");
                helper.setTo(recipient.trim());
                helper.setSubject(subject);
                helper.setText(bodyText, true);

                // Attachment only if bytes exist
                if (attachment != null && attachment.length > 0) {
                    helper.addAttachment(fileName, new ByteArrayResource(attachment));
                }

                mailSender.send(message);
                log.info("Email sent successfully to={}", recipient.trim());

            } catch (MessagingException e) {
                log.error("Failed to send email to={} — skipping to next recipient", recipient.trim(), e);
            } catch (Exception e) {
                log.error("Unexpected error for recipient={} — skipping to next", recipient.trim(), e);
            }
        }
    }

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
                        <p>Please find below the lead summary for <b>%s</b>:</p>
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
                        <p>Please refer to the attached Excel file for complete details.</p>
                        <p style="color: #B0A9A1; font-size: 12px; margin-top: 24px;">
                            This is an automated email generated by the Titan Watch Bot Dashboard.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(today, totalLeads, callbacks, storeVisits);
    }

    public String buildFailureAlertBody(String errorMessage) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <div style="background: #8B0000; padding: 20px; border-radius: 8px 8px 0 0;">
                        <h2 style="color: #FFFFFF; margin: 0;">❌ BATCH FAILED — TITAN WORLD</h2>
                    </div>
                    <div style="padding: 20px; border: 1px solid #EEEBE6; border-top: none;">
                        <p><b>Date:</b> %s</p>
                        <p><b>Error:</b> %s</p>
                        <p style="color: red;">Please check the server logs immediately.</p>
                    </div>
                </body>
                </html>
                """.formatted(LocalDate.now(), errorMessage);
    }

    public String buildSuccessAlertBody(int totalLeads, String recipients) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <div style="background: #006400; padding: 20px; border-radius: 8px 8px 0 0;">
                        <h2 style="color: #FFFFFF; margin: 0;">✅ BATCH SUCCESS — TITAN WORLD</h2>
                    </div>
                    <div style="padding: 20px; border: 1px solid #EEEBE6; border-top: none;">
                        <p><b>Date:</b> %s</p>
                        <p><b>Total Leads Processed:</b> %d</p>
                        <p><b>Report Sent To:</b> %s</p>
                        <p>Daily lead report batch completed successfully.</p>
                    </div>
                </body>
                </html>
                """.formatted(LocalDate.now(), totalLeads, recipients);
    }
}