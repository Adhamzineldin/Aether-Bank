package com.maayn.notificationservice.templates;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EmailTemplateEngine {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public String accountCreatedEmail(String accountNumber, String accountType, String currency) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
                        <h1>Welcome to Aether Bank!</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Valued Customer,</p>
                        <p>Congratulations! Your new %s account has been successfully opened.</p>
                        <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Account Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Account Type:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Currency:</strong> %s</p>
                        </div>
                        <p>You can now start using your account for deposits, withdrawals, and transfers.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(accountType, accountNumber, accountType, currency);
    }

    public String transferSuccessEmail(String referenceNumber, BigDecimal amount, String currency, 
                                       String fromAccount, String toAccount, LocalDateTime timestamp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #2196F3; color: white; padding: 20px; text-align: center;">
                        <h1>Transfer Successful</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Customer,</p>
                        <p>Your transfer has been completed successfully.</p>
                        <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Reference Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Amount:</strong> %s %s</p>
                            <p style="margin: 5px 0;"><strong>From Account:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>To Account:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Date & Time:</strong> %s</p>
                        </div>
                        <p>If you did not authorize this transaction, please contact us immediately.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(referenceNumber, amount, currency, fromAccount, toAccount, 
                             timestamp.format(FORMATTER));
    }

    public String loanApplicationSubmittedEmail(String loanNumber, BigDecimal amount, String loanType) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #FF9800; color: white; padding: 20px; text-align: center;">
                        <h1>Loan Application Received</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Customer,</p>
                        <p>Thank you for applying for a loan with Aether Bank!</p>
                        <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #FF9800; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Application Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Loan Type:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Amount Requested:</strong> $%s</p>
                            <p style="margin: 5px 0;"><strong>Status:</strong> Under Review</p>
                        </div>
                        <p>Your application is now being reviewed by our team. This typically takes 3-5 business days.</p>
                        <p>We will notify you once a decision has been made.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(loanNumber, loanType, amount);
    }

    public String loanApprovedEmail(String loanNumber, BigDecimal approvedAmount, BigDecimal interestRate, 
                                    int tenureMonths) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
                        <h1>🎉 Loan Approved!</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Customer,</p>
                        <p><strong>Congratulations!</strong> Your loan application has been approved.</p>
                        <div style="background-color: #e8f5e9; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Loan Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Approved Amount:</strong> $%s</p>
                            <p style="margin: 5px 0;"><strong>Interest Rate:</strong> %s%%</p>
                            <p style="margin: 5px 0;"><strong>Tenure:</strong> %d months</p>
                        </div>
                        <p>The funds will be disbursed to your account within 1-2 business days.</p>
                        <p>You will receive a repayment schedule shortly.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(loanNumber, approvedAmount, interestRate, tenureMonths);
    }

    public String loanRejectedEmail(String loanNumber, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #f44336; color: white; padding: 20px; text-align: center;">
                        <h1>Loan Application Update</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Customer,</p>
                        <p>We regret to inform you that your loan application has not been approved at this time.</p>
                        <div style="background-color: #ffebee; padding: 15px; border-left: 4px solid #f44336; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Application Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Reason:</strong> %s</p>
                        </div>
                        <p>You may reapply after 30 days or contact us to discuss alternative options.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(loanNumber, reason);
    }

    public String loanDisbursedEmail(String loanNumber, BigDecimal amount, String accountNumber) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
                        <h1>Loan Disbursed</h1>
                    </div>
                    <div style="padding: 20px;">
                        <p>Dear Customer,</p>
                        <p>Great news! Your loan has been disbursed successfully.</p>
                        <div style="background-color: #e8f5e9; padding: 15px; border-left: 4px solid #4CAF50; margin: 20px 0;">
                            <p style="margin: 5px 0;"><strong>Loan Number:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Amount Disbursed:</strong> $%s</p>
                            <p style="margin: 5px 0;"><strong>Credited to Account:</strong> %s</p>
                        </div>
                        <p>The funds are now available in your account.</p>
                        <p>Your first EMI will be due next month. You will receive a payment reminder.</p>
                        <p>Best regards,<br/>The Aether Bank Team</p>
                    </div>
                    <div style="background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 12px; color: #666;">
                        <p>© 2026 Aether Bank. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(loanNumber, amount, accountNumber);
    }
}

