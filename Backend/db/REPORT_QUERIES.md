# Report SQL insertion guide

The five SQL strings at the top of `src/main/java/dao/ReportDao.java` are deliberately empty. Fill those strings only. Existing application SQL and `schema.sql` are unchanged; the existing `Report` table already has every required column. No migration is needed.

Each `?` is a JDBC parameter, in the exact order below. Keep user input in parameters, never concatenate it into SQL. The Java binding and result-reading code is already implemented. Blank queries produce HTTP 503 and an unavailable message before opening a database connection. They never simulate successful submission or an empty queue.

All references below use the **printed slide numbers** in the repository's `Chapter 6-SQL.pdf` (these differ from PDF page numbers). Only Chapter 6 SQL constructs are needed: SELECT/FROM/WHERE, comparisons, AND/OR/NOT, AS, joins, NOT EXISTS, ORDER BY, INSERT with named columns, and UPDATE. Do not add LIMIT/OFFSET, outer joins, vendor-specific functions, stored procedures, triggers, or SQL transaction/locking statements. The queue cap uses JDBC `setMaxRows(100)`.

## R1 — `FIND_ITEM_SQL`

Purpose: retrieve one public item and its owner for reporting.

- Tables: `Item`, with a correlated subquery on `RedditItem`.
- Result columns: `item_id`, `user_id`, `title`.
- Conditions: match the item ID; exclude the removed status; exclude any related RedditItem whose approval status differs from the approved value. This matches the existing catalog's visibility rules. Returned items can still be reported.
- Parameters: **1** item ID (integer), **2** `REMOVED` (string), **3** `APPROVED` (string).
- Zero rows means the target does not exist or is not publicly visible.
- Slides: 10 (SELECT/FROM/WHERE), 37 (conditions), 69–70 (EXISTS and correlated NOT EXISTS).

## R2 — `FIND_PROFILE_SQL`

Purpose: retrieve the student profile being reported.

- Table: `User`.
- Result columns: `user_id`, `name`.
- Conditions: match the user ID and role.
- Parameters: **1** user ID (integer), **2** `STUDENT` (string).
- Zero rows means no reportable student profile exists.
- Slides: 10 (SELECT/FROM/WHERE), 37–38 (conditions).

## R3 — `CREATE_REPORT_SQL`

Purpose: insert one row into `Report` with a named column list.

- Columns, in parameter order: `reported_by_user_id`, `item_id`, `reported_user_id`, `reason`, `status`.
- Parameters: **1** logged-in reporter ID (integer), **2** item ID (integer) or SQL NULL for a profile report, **3** reported user ID (integer), **4** normalized reason (string), **5** `PENDING` (string).
- For an item report, the server loads parameter 3 from the item's owner. For a profile report it comes from the loaded profile. Neither the reporter ID nor reported user ID is trusted from a submitted form.
- Omit `report_id` and `report_date` so the existing schema supplies them.
- Exactly one affected row is required for success.
- Slides: 41 (NULL), 91–96 (INSERT, named attributes, and default values).

## R4 — `LIST_REPORTS_SQL`

Purpose: retrieve the administrator review queue, including both item and profile reports.

- Tables: `Report` and two aliases of `User`: one for the reporter and one for the reported user. Join on the two report user IDs. An ordinary join (or comma-separated FROM with equality conditions) is sufficient.
- Do **not** join `Item`: profile reports have a null item ID and would disappear from an inner join. The UI links to an item by its ID, so its title is unnecessary.
- Required result columns/aliases:

| Result name | Source |
| --- | --- |
| `report_id` | Report.report_id |
| `reporter_id` | Report.reported_by_user_id, renamed with AS |
| `reporter_name` | reporter User.name, renamed with AS |
| `item_id` | Report.item_id (nullable) |
| `reported_user_id` | Report.reported_user_id |
| `reported_user_name` | reported User.name, renamed with AS |
| `reason` | Report.reason |
| `status` | Report.status |
| `report_date` | Report.report_date |

- Filter condition: parameter 1 equals the literal `ALL` **or** the report status equals parameter 2. Parenthesize that OR condition when combining it with joins.
- Parameters: **1** filter (string), **2** the same filter (string). Allowed values are `ALL`, `PENDING`, `RESOLVED`, `DISMISSED`.
- Sort by `report_date` descending, then `report_id` descending. Java caps results at 100.
- Slides: 10 (SELECT), 32 (ORDER BY DESC example), 33 (AS), 37 (AND/OR), 47–52 (multiple relations, aliases, self joins), 88 (JOIN/ON).

## R5 — `UPDATE_STATUS_SQL`

Purpose: update one pending report after an administrator decision.

- Table: `Report`; assignment: set `status` to parameter 1.
- Conditions: match `report_id` with parameter 2 **and** current status with parameter 3. Both conditions are essential: a second administrator's stale form must not overwrite a completed decision.
- Parameters: **1** `RESOLVED` or `DISMISSED` (string), **2** report ID (integer), **3** `PENDING` (string).
- Exactly one affected row means success; zero means missing or already reviewed. Only the two named actions are accepted, behind administrator and CSRF checks.
- Slides: 37–38 (conditions), 104–106 (UPDATE).

## Routes and verification

- Item report: `/report/new?type=item&id=ITEM_ID`, linked on item posts.
- Profile report: `/report/new?type=profile&id=USER_ID`, linked on profiles.
- Administrator queue: `/admin/reports`, linked in the navigation only for `ADMINISTRATOR` accounts.
- Status changes: POST `/admin/reports/action`, using the queue's buttons.
- A report requires a nonblank reason of at most 5000 characters. Self-reporting is blocked. Report details are visible only to administrators.
- Resolve/dismiss records a review decision. Content deletion and account administration are separate features.

Build with `mvn -f Backend/pom.xml package`. Run the dependency-free service/placeholder checks after compiling tests:

```sh
mvn -f Backend/pom.xml test-compile
java -cp Backend/target/test-classes:Backend/target/classes service.ReportServiceTest
```

After filling the five blanks, deploy and verify with a student and administrator account: submit both target types; confirm null item ID for profile reports and the correct item owner for item reports; check pending/all/closed filters; resolve and dismiss; submit a stale second decision and confirm it does not overwrite the first. Verify guests are sent to login, students cannot open or POST to administrator routes, invalid CSRF tokens are rejected, and report text containing HTML is rendered as text. Database integration cannot be verified while SQL is intentionally blank.
