agent: ask


Task:
Goal: Implement and harden the login(String username, char[] password) method so it correctly authenticates the administrator and customers.
Actions:
Validate inputs (non-null, non-empty username; non-null password char[]).
If username equals ADMIN_USERNAME and Arrays.equals(ADMIN_PASSWORD, password) then set adminLoggedIn = true, currentCustomer = null, and return.
Otherwise find customer by username using existing findByUsername.
Verify customer password via customer.matchesPassword(password).
On success set adminLoggedIn = false and currentCustomer = customer.
On failure throw AccountException("Invalid username or password").
Do not convert passwords to String; avoid logging or returning password material.

Constraints:
Language/Platform: Java (compatible with repository, e.g., Java 11+).
API/Signature: Keep signature public void login(String username, char[] password).
Security: Never convert char[] to String; use Arrays.equals for admin check; avoid timing leaks as practicable; do not print or log passwords; do not retain extra references to password arrays.
Performance: O(n) customer lookup acceptable (n ≤ CAPACITY). Do not introduce heavy allocations.
Style: Follow existing project conventions (throw AccountException for errors, use existing helper methods like findByUsername()).
Side-effects: Only mutate adminLoggedIn and currentCustomer as specified; do not change other state.

Expected Output:
Method: public void login(String username, char[] password)
Behavior:
Successful admin login: isAdminLoggedIn() returns true, getCurrentCustomer() returns null.
Successful customer login: isAdminLoggedIn() returns false, getCurrentCustomer() returns that Customer object.
Failure: throws AccountException with message "Invalid username or password".
Return Type: void
Errors: Throw AccountException for invalid credentials and for invalid inputs (if you choose to validate null/blank).
Implementation Notes: Use Arrays.equals(ADMIN_PASSWORD, password) for admin, call customer.matchesPassword(password) for customer; set fields exactly as existing code pattern.

Verification:
Unit tests: Add/modify tests in AccountSystemTest.java:1
Assert admin success:
After login("admin", "Admin123".toCharArray()) → isAdminLoggedIn() == true and getCurrentCustomer() == null.
Assert customer success:
For existing user (e.g., username "alice") → isAdminLoggedIn() == false and getCurrentCustomer().getUsername().equals("alice").
Assert failure:
Invalid username or wrong password throws AccountException with message containing "Invalid username or password".