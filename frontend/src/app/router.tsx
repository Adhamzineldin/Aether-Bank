import { lazy } from 'react';
import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppShell } from '@components/layout/AppShell';
import { AuthLayout } from '@components/layout/AuthLayout';
import { AuthGuard } from '@app/providers/AuthGuard';
import { GuardedOutlet } from '@app/providers/GuardedOutlet';
import { ROUTES } from './routes';

// Lazy-loaded pages
const LandingPage = lazy(() => import('@features/landing/pages/LandingPage'));
const LoginPage = lazy(() => import('@features/auth/pages/LoginPage'));
const RegisterPage = lazy(() => import('@features/auth/pages/RegisterPage'));
const ForgotPasswordPage = lazy(() => import('@features/auth/pages/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('@features/auth/pages/ResetPasswordPage'));
const MfaPage = lazy(() => import('@features/auth/pages/MfaPage'));
const VerifyEmailPage = lazy(() => import('@features/auth/pages/VerifyEmailPage'));

const DashboardPage = lazy(() => import('@features/dashboard/pages/DashboardPage'));
const AccountsListPage = lazy(() => import('@features/accounts/pages/AccountsListPage'));
const OpenAccountPage = lazy(() => import('@features/accounts/pages/OpenAccountPage'));
const AccountDetailPage = lazy(() => import('@features/accounts/pages/AccountDetailPage'));
const AccountStatementPage = lazy(() => import('@features/accounts/pages/AccountStatementPage'));

const TransactionsListPage = lazy(() => import('@features/transactions/pages/TransactionsListPage'));
const TransferPage = lazy(() => import('@features/transactions/pages/TransferPage'));
const TransactionDetailPage = lazy(() => import('@features/transactions/pages/TransactionDetailPage'));
const ScheduledTransfersPage = lazy(() => import('@features/transactions/pages/ScheduledTransfersPage'));

const CardsListPage = lazy(() => import('@features/cards/pages/CardsListPage'));
const IssueCardPage = lazy(() => import('@features/cards/pages/IssueCardPage'));
const CardDetailPage = lazy(() => import('@features/cards/pages/CardDetailPage'));
const CardTransactionsPage = lazy(() => import('@features/cards/pages/CardTransactionsPage'));

const PaymentsHomePage = lazy(() => import('@features/payments/pages/PaymentsHomePage'));
const CheckoutPage = lazy(() => import('@features/payments/pages/CheckoutPage'));
const PayBillsPage = lazy(() => import('@features/payments/pages/PayBillsPage'));
const MerchantsPage = lazy(() => import('@features/payments/pages/MerchantsPage'));

const LoansListPage = lazy(() => import('@features/loans/pages/LoansListPage'));
const ApplyLoanPage = lazy(() => import('@features/loans/pages/ApplyLoanPage'));
const LoanDetailPage = lazy(() => import('@features/loans/pages/LoanDetailPage'));

const MortgagesListPage = lazy(() => import('@features/mortgages/pages/MortgagesListPage'));
const ApplyMortgagePage = lazy(() => import('@features/mortgages/pages/ApplyMortgagePage'));
const MortgageDetailPage = lazy(() => import('@features/mortgages/pages/MortgageDetailPage'));

const SavingsListPage = lazy(() => import('@features/savings/pages/SavingsListPage'));
const ApplySavingsPage = lazy(() => import('@features/savings/pages/ApplySavingsPage'));

const InvestmentsHomePage = lazy(() => import('@features/investments/pages/InvestmentsHomePage'));
const AssetDetailPage = lazy(() => import('@features/investments/pages/AssetDetailPage'));
const PortfolioPage = lazy(() => import('@features/investments/pages/PortfolioPage'));

const FxPage = lazy(() => import('@features/fx/pages/FxPage'));
const BeneficiariesPage = lazy(() => import('@features/beneficiaries/pages/BeneficiariesPage'));

const NotificationsListPage = lazy(() => import('@features/notifications/pages/NotificationsListPage'));
const NotificationDetailPage = lazy(() => import('@features/notifications/pages/NotificationDetailPage'));
const TemplatesPage = lazy(() => import('@features/notifications/pages/TemplatesPage'));

const ProfilePage = lazy(() => import('@features/profile/pages/ProfilePage'));
const SecurityPage = lazy(() => import('@features/profile/pages/SecurityPage'));
const PreferencesPage = lazy(() => import('@features/profile/pages/PreferencesPage'));

const AdminHomePage = lazy(() => import('@features/admin/pages/AdminHomePage'));
const UsersListPage = lazy(() => import('@features/admin/pages/UsersListPage'));
const UserDetailPage = lazy(() => import('@features/admin/pages/UserDetailPage'));
const RolesPage = lazy(() => import('@features/admin/pages/RolesPage'));
const AuditLogsPage = lazy(() => import('@features/admin/pages/AuditLogsPage'));

const WorkflowInboxPage = lazy(() => import('@features/workflow/pages/WorkflowInboxPage'));
const WorkflowDetailPage = lazy(() => import('@features/workflow/pages/WorkflowDetailPage'));

const NotFoundPage = lazy(() => import('@features/misc/pages/NotFoundPage'));
const ForbiddenPage = lazy(() => import('@features/misc/pages/ForbiddenPage'));

export const router = createBrowserRouter([
  { path: ROUTES.landing, element: <LandingPage /> },
  {
    path: '/auth',
    element: <AuthLayout />,
    children: [
      { index: true, element: <Navigate to={ROUTES.login} replace /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'register', element: <RegisterPage /> },
      { path: 'forgot-password', element: <ForgotPasswordPage /> },
      { path: 'reset-password', element: <ResetPasswordPage /> },
      { path: 'mfa', element: <MfaPage /> },
      { path: 'verify-email', element: <VerifyEmailPage /> },
    ],
  },
  {
    element: <AuthGuard><AppShell /></AuthGuard>,
    children: [
      { path: ROUTES.dashboard, element: <DashboardPage /> },

      { path: ROUTES.accounts, element: <AccountsListPage /> },
      { path: ROUTES.newAccount, element: <OpenAccountPage /> },
      { path: ROUTES.account(), element: <AccountDetailPage /> },
      { path: ROUTES.accountStatement(), element: <AccountStatementPage /> },

      { path: ROUTES.transactions, element: <TransactionsListPage /> },
      { path: ROUTES.transfer, element: <TransferPage /> },
      { path: ROUTES.scheduledTransfers, element: <ScheduledTransfersPage /> },
      { path: ROUTES.transaction(), element: <TransactionDetailPage /> },

      { path: ROUTES.cards, element: <CardsListPage /> },
      { path: ROUTES.issueCard, element: <IssueCardPage /> },
      { path: ROUTES.card(), element: <CardDetailPage /> },
      { path: ROUTES.cardTransactions(), element: <CardTransactionsPage /> },

      { path: ROUTES.payments, element: <PaymentsHomePage /> },
      { path: ROUTES.checkout, element: <CheckoutPage /> },
      { path: ROUTES.payBills, element: <PayBillsPage /> },
      { path: ROUTES.merchants, element: <MerchantsPage /> },

      { path: ROUTES.loans, element: <LoansListPage /> },
      { path: ROUTES.applyLoan, element: <ApplyLoanPage /> },
      { path: ROUTES.loan(), element: <LoanDetailPage /> },

      { path: ROUTES.mortgages, element: <MortgagesListPage /> },
      { path: ROUTES.applyMortgage, element: <ApplyMortgagePage /> },
      { path: ROUTES.mortgage(), element: <MortgageDetailPage /> },

      { path: ROUTES.savings, element: <SavingsListPage /> },
      { path: ROUTES.applySavings, element: <ApplySavingsPage /> },

      { path: ROUTES.investments, element: <InvestmentsHomePage /> },
      { path: ROUTES.investmentAssets, element: <InvestmentsHomePage /> },
      { path: ROUTES.investmentAsset(), element: <AssetDetailPage /> },
      { path: ROUTES.portfolio(), element: <PortfolioPage /> },

      { path: ROUTES.fx, element: <FxPage /> },
      { path: ROUTES.beneficiaries, element: <BeneficiariesPage /> },

      { path: ROUTES.notifications, element: <NotificationsListPage /> },
      { path: ROUTES.notification(), element: <NotificationDetailPage /> },

      { path: ROUTES.profile, element: <ProfilePage /> },
      { path: ROUTES.security, element: <SecurityPage /> },
      { path: ROUTES.preferences, element: <PreferencesPage /> },

      // Employee + admin
      {
        element: <GuardedOutlet roles={['EMPLOYEE', 'ADMIN']} />,
        children: [
          { path: ROUTES.workflow, element: <WorkflowInboxPage /> },
          { path: ROUTES.workflowDetail(), element: <WorkflowDetailPage /> },
        ],
      },
      {
        element: <GuardedOutlet roles={['ADMIN']} />,
        children: [
          { path: ROUTES.admin, element: <AdminHomePage /> },
          { path: ROUTES.adminUsers, element: <UsersListPage /> },
          { path: ROUTES.adminUser(), element: <UserDetailPage /> },
          { path: ROUTES.adminRoles, element: <RolesPage /> },
          { path: ROUTES.audit, element: <AuditLogsPage /> },
          { path: ROUTES.templates, element: <TemplatesPage /> },
        ],
      },
    ],
  },
  { path: ROUTES.forbidden, element: <ForbiddenPage /> },
  { path: '*', element: <NotFoundPage /> },
]);



