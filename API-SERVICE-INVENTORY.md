# WebDisplay service and API inventory

This inventory is generated from the source snapshot. Swagger/OpenAPI remains the runtime source of truth for request/response schemas and paths.

## Service-layer components

| Service | Public methods detected in source |
|---|---|
| `ThemeService` | `getTheme`, `saveTheme` |
| `AccountServiceImpl` | `createAccount`, `getAccountById`, `deposit`, `withdraw`, `getAllAccounts`, `getAccountsByCustomerId`, `getAccountByAccountNumber`, `deactivateAccount`, `activateAccount`, `getTransactionHistory`, `getCustomerTransactionHistory`, `updateAccount`, `getCustomerAccountStatement`, `getHelpdeskCustomerAccounts`, `getHelpdeskCustomerTransactions`, `transfer`, `verifyRecipientAccount` |
| `BeneficiaryServiceImpl` | `getMyBeneficiaries`, `addBeneficiary`, `deleteBeneficiary` |
| `ConsoleOtpDeliveryService` | `deliverOtp` |
| `CustomerIdDocumentService` | `getAllApplicationsForEmployee`, `createAccountWithDocument`, `reuploadRejectedDocument`, `getPendingDocuments`, `getDocumentFile`, `approveDocument`, `rejectDocument`, `getRejectedDocumentsForEmployee`, `searchEmployeeApplications` |
| `CustomerServiceImpl` | `createCustomer`, `getCustomerByUserId`, `searchCustomers`, `getHelpdeskCustomerById` |
| `EmployeeServiceImpl` | `createEmployeeProfile`, `getAllEmployees`, `updateEmployee` |
| `HelpdeskTicketConversationService` | `getConversation` |
| `OtpService` | `sendOtp`, `verifyOtp` |
| `ProfileOtpService` | `generateOtp`, `verifyOtp` |
| `SupportTicketServiceImpl` | `getCustomerConversation`, `addCustomerReply`, `createTicket`, `getCustomerTickets`, `getAllTickets`, `updateTicket`, `sendCustomerUpdate`, `assignTicket`, `submitForReview`, `reviewTicket`, `getMyAssignedTickets`, `addRemark`, `getTicketRemarks`, `getTicketStatistics`, `filterTickets`, `getHelpdeskEmployees`, `endConversation` |
| `SupportTicketSlaScheduler` | `escalateOverdueTickets` |
| `UserServiceImpl` | `findByUsername`, `authenticateUser`, `createCustomerLogin`, `createStaffLogin`, `updateCustomerEmail` |
| `DocumentSecurityService` | `validateAndScan`, `ValidatedDocument` |

**Service components detected:** 14

## REST controllers

| Controller | Mapping annotations detected |
|---|---:|
| `AccountController` | 13 |
| `AccountPage` | 2 |
| `AdminController` | 3 |
| `AuditLogController` | 1 |
| `BeneficiaryController` | 3 |
| `CustomerAddressChangeController` | 7 |
| `CustomerAiAssistantController` | 1 |
| `CustomerController` | 2 |
| `CustomerIdDocumentController` | 9 |
| `CustomerProfileController` | 4 |
| `CustomerProfileVerificationController` | 4 |
| `HelpdeskController` | 4 |
| `HelpdeskTicketConversationController` | 1 |
| `SessionController` | 1 |
| `StudentController` | 1 |
| `SupportTicketController` | 18 |
| `ThemeController` | 2 |

**REST controllers detected:** 17
**Mapping annotations detected:** 76

Counts are static source counts. Overloaded mappings, inherited interface methods, and runtime conditional registrations can affect the actual exposed endpoint list; open Swagger UI after startup for the definitive API list.
