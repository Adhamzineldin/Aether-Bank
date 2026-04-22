package com.maayn.notificationservice.templates;

/**
 * Wraps an arbitrary plain-text notification body in the same branded card
 * layout used by transfer emails. Uses inline styles only — Gmail / Outlook /
 * Apple Mail strip {@code <style>} blocks. Intended for the generic
 * {@code NotificationDispatchService} path (loan / mortgage / certificate
 * approval, etc.) so every outbound email looks like the rest of the product.
 */
public final class GenericEmailTemplate {

    private GenericEmailTemplate() {}

    private static final String BRAND = "Aether Bank";
    private static final String BRAND_COLOR = "#6366F1";
    private static final String NAVY = "#0F172A";
    private static final String MUTED = "#64748B";
    private static final String TEXT = "#334155";
    private static final String CARD_BG = "#FFFFFF";
    private static final String PAGE_BG = "#F1F5F9";
    private static final String DIVIDER = "#E2E8F0";

    public static String wrap(String heading, String bodyText) {
        // Preserve newlines in plain-text bodies as <br>.
        String safeBody = escape(bodyText).replace("\n", "<br>");
        return "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escape(heading) + "</title></head>"
                + "<body style=\"margin:0;padding:0;background:" + PAGE_BG + ";"
                + "font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;color:" + TEXT + ";\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background:" + PAGE_BG + ";padding:32px 16px;\">"
                + "<tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"max-width:560px;background:" + CARD_BG + ";border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,0.08);\">"
                + "<tr><td style=\"background:" + NAVY + ";padding:20px 28px;\">"
                + "<div style=\"font-size:13px;letter-spacing:2px;color:" + BRAND_COLOR + ";font-weight:600;text-transform:uppercase;\">" + BRAND + "</div>"
                + "</td></tr>"
                + "<tr><td style=\"height:4px;background:" + BRAND_COLOR + ";\"></td></tr>"
                + "<tr><td style=\"padding:32px 28px;\">"
                + "<h1 style=\"margin:0 0 16px 0;font-size:22px;font-weight:700;color:" + NAVY + ";line-height:1.3;\">" + escape(heading) + "</h1>"
                + "<div style=\"font-size:15px;line-height:1.6;color:" + TEXT + ";\">" + safeBody + "</div>"
                + "</td></tr>"
                + "<tr><td style=\"padding:18px 28px;background:#F8FAFC;border-top:1px solid " + DIVIDER + ";\">"
                + "<div style=\"font-size:12px;color:" + MUTED + ";\">"
                + "Sent by <strong style=\"color:" + NAVY + ";\">" + BRAND + "</strong>. This is an automated message — please do not reply."
                + "</div>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
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

