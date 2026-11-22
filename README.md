# M-Kopo SACCO Management System
Console-based Java application for managing SACCO members, savings/loan accounts, and transaction persistence.

## Architecture
- **Auditable** (Interface): Transaction logging contract.
- **Account** (Abstract Class): Base banking logic extended by `SavingsAccount` (5% interest) and `LoanAccount` (12% interest).
- **Member**: Stores member metadata and maps accounts by type.
- **SaccoManager**: Console menu, collections (`HashMap`, `HashSet`), and CSV/TXT persistence.