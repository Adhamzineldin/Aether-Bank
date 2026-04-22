package com.maayn.notificationservice.templates;

import com.maayn.notificationservice.dto.TransferFailedPayload;
import com.maayn.notificationservice.dto.TransferSuccessPayload;

/**
 * Branded HTML email templates. All emails render as a single-column,
 * inline-styled card to maximise compatibility across mail clients
 * (Gmail / Outlook / Apple Mail strip {@code <style>} blocks aggressively,
 * so styles must live on each element).
 *
 * <p>Color palette mirrors the Aether Bank web app:
 * brand purple {@code #6366F1}, deep navy {@code #0F172A},
 * slate text {@code #334155}, success {@code #10B981},
 * danger {@code #EF4444}.
 */
public final class EmailTemplate {

    private EmailTemplate() {}

    private static final String BRAND = "Aether Bank";
    private static final String BRAND_COLOR = "#6366F1";
    private static final String NAVY = "#0F172A";
    private static final String MUTED = "#64748B";
    private static final String TEXT = "#334155";
    private static final String SUCCESS = "#10B981";
    private static final String DANGER = "#EF4444";
    private static final String CARD_BG = "#FFFFFF";
    private static final String PAGE_BG = "#F1F5F9";
    private static final String DIVIDER = "#E2E8F0";

    public static String transferSuccessSubject(String referenceNumber) {
        return "✅ Transfer completed — " + referenceNumber;
    }

    public static String transferSuccessBody(TransferSuccessPayload e) {
        String rows = row("Reference", e.referenceNumber())
                + row("Amount", formatAmount(e.amount(), e.currency()))
                + row("From account", e.sourceAccountId().toString())
                + row("To account", e.destinationAccountId().toString());
        String badge = badge("COMPLETED", SUCCESS);
        return wrap(
                "Transfer completed",
                badge,
                "Your transfer has been processed successfully and the funds have been moved.",
                rows,
                "If you didn't authorise this transfer, contact support immediately.",
                SUCCESS
        );
    }

    public static String transferFailedSubject(String referenceNumber) {
        return "⚠️ Transfer could not be completed — " + referenceNumber;
    }

    public static String transferFailedBody(TransferFailedPayload e) {
        String reason = e.failureReason() != null ? e.failureReason() : "Unknown reason";
        String rows = row("Reference", e.referenceNumber())
                + row("Amount", formatAmount(e.amount(), e.currency()))
                + row("From account", e.sourceAccountId().toString())
                + row("Reason", reason);
        String badge = badge("FAILED", DANGER);
        return wrap(
                "Transfer failed",
                badge,
                "We were unable to complete your transfer. No funds have been moved.",
                rows,
                "If you need help understanding why, please contact support and quote the reference above.",
                DANGER
        );
    }

    // ----- building blocks -----

    private static String wrap(String heading, String badge, String intro, String detailRows,
                               String footerNote, String accentColor) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escape(heading) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:" + PAGE_BG + ";"
                + "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;"
                + "color:" + TEXT + ";\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background:" + PAGE_BG + ";padding:32px 16px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"max-width:560px;background:" + CARD_BG + ";border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,0.08);\">"
                // header strip
                + "<tr><td style=\"background:" + NAVY + ";padding:20px 28px;\">"
                + "<div style=\"font-size:13px;letter-spacing:2px;color:" + BRAND_COLOR + ";font-weight:600;text-transform:uppercase;\">" + BRAND + "</div>"
                + "</td></tr>"
                // accent bar
                + "<tr><td style=\"height:4px;background:" + accentColor + ";\"></td></tr>"
                // body
                + "<tr><td style=\"padding:32px 28px 8px 28px;\">"
                + "<h1 style=\"margin:0 0 12px 0;font-size:22px;font-weight:700;color:" + NAVY + ";line-height:1.3;\">" + escape(heading) + "</h1>"
                + badge
                + "<p style=\"margin:16px 0 0 0;font-size:15px;line-height:1.6;color:" + TEXT + ";\">" + escape(intro) + "</p>"
                + "</td></tr>"
                // detail rows
                + "<tr><td style=\"padding:24px 28px;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"border:1px solid " + DIVIDER + ";border-radius:12px;overflow:hidden;\">"
                + detailRows
                + "</table>"
                + "</td></tr>"
                // footer note
                + "<tr><td style=\"padding:0 28px 28px 28px;\">"
                + "<p style=\"margin:0;font-size:13px;line-height:1.6;color:" + MUTED + ";\">" + escape(footerNote) + "</p>"
                + "</td></tr>"
                // brand footer
                + "<tr><td style=\"padding:18px 28px;background:#F8FAFC;border-top:1px solid " + DIVIDER + ";\">"
                + "<div style=\"font-size:12px;color:" + MUTED + ";\">"
                + "Sent by <strong style=\"color:" + NAVY + ";\">" + BRAND + "</strong>. This is an automated message — please do not reply."
                + "</div>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private static String row(String label, String value) {
        return "<tr>"
                + "<td style=\"padding:12px 16px;font-size:13px;color:" + MUTED + ";background:#F8FAFC;border-bottom:1px solid " + DIVIDER + ";width:40%;text-transform:uppercase;letter-spacing:0.5px;\">" + escape(label) + "</td>"
                + "<td style=\"padding:12px 16px;font-size:14px;color:" + NAVY + ";font-weight:600;border-bottom:1px solid " + DIVIDER + ";font-family:'SF Mono','Menlo','Monaco',Consolas,monospace;word-break:break-all;\">" + escape(value) + "</td>"
                + "</tr>";
    }

    private static String badge(String label, String color) {
        return "<span style=\"display:inline-block;padding:4px 10px;font-size:11px;font-weight:700;letter-spacing:1px;"
                + "color:" + color + ";background:" + color + "1A;border-radius:999px;text-transform:uppercase;\">"
                + escape(label) + "</span>";
    }

    private static String formatAmount(Object amount, String currency) {
        return amount + " " + (currency != null ? currency : "");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
