# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Added the `AccountStatus` enum and an injectable `AccountStatusPolicy` with a balance-based implementation.
- Added Standard, Premium, and Corporate customer types through an abstract `Customer` hierarchy and switch-based factory.
- Added customer and user-role enums, constructor validation, defensive password storage, and constant-time password matching.
- Added a fixed-capacity array-backed customer repository with unique customer ID, username, and account-number enforcement, defensive reads, and compacting deletes.
- Added stateless administrator and customer credential verification with immutable user sessions and indistinguishable credential failures.
- Added an externally composed active-session registry shared by authentication and authorization services.
- Added role-based customer services for administrator management operations and customer self-service account access.
- Added flexible administrator customer search and Lambda/Stream audit filtering.
- Added recoverable English guest, administrator, and customer console menus with a testable I/O boundary and masked-password system-terminal adapter.
- Added a production composition root that seeds five customers in a capacity-100 repository and wires shared-session authentication, customer services, and console control.
- Added an executable JAR manifest and end-user build, run, credential, terminal-masking, feature, and compliance documentation.

### Changed

- Flattened the account and customer classroom model into the root package with simple rules and constructors.
- Updated `Account` to permit inheritance and to validate account numbers, owners, and finite nonnegative balances.
- Updated account status reporting to use enum values instead of hard-coded strings.
- Excluded customer passwords from public APIs and string representations.
- Hardened role authorization with active-session provenance, logout invalidation, reserved administrator usernames, and temporary credential-buffer wiping.
- Recover invalidated console sessions by returning safely to the guest menu without retry loops.
- Removed internal planning documents from the deliverable.
- Migrated the legacy five-account fixture to five valid Standard, Premium, and Corporate customers while retaining compatible account creation and the original status sequence.
- Delegated the legacy `AccountDemo` entry point to the production application and replaced its invalid negative opening balance with zero.
- Removed `final` from `AccountDemo` and from the `Account` ID, owner, balance, and status-policy fields as required by the classroom model.
