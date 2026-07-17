# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Added the `AccountStatus` enum and an injectable `AccountStatusPolicy` with a balance-based implementation.
- Added Standard, Premium, and Corporate customer types through an abstract `Customer` hierarchy and switch-based factory.
- Added customer and user-role enums, constructor validation, defensive password storage, and constant-time password matching.

### Changed

- Updated `Account` to permit inheritance and to validate account numbers, owners, and finite nonnegative balances.
- Updated account status reporting to use enum values instead of hard-coded strings.
- Excluded customer passwords from public APIs and string representations.
