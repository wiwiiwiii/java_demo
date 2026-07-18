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

### Changed

- Updated `Account` to permit inheritance and to validate account numbers, owners, and finite nonnegative balances.
- Updated account status reporting to use enum values instead of hard-coded strings.
- Excluded customer passwords from public APIs and string representations.
- Hardened role authorization with active-session provenance, logout invalidation, reserved administrator usernames, and temporary credential-buffer wiping.
- Recover invalidated console sessions by returning safely to the guest menu without retry loops.
- Removed internal planning documents from the deliverable.
