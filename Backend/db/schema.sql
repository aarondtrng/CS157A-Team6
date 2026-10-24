-- =====================================================================================
-- SpartanFinds schema -- SKELETON. Contains no SQL yet: every section is a plain-English TODO.
-- The team writes the statements under each heading. Sections are in dependency order, so a table
-- appears after every table it references. Source of truth for names: "Schema reference" in CLAUDE.md.
-- Run (once SQL is written):  mysql -u root -p < Backend/db/schema.sql
-- =====================================================================================

-- -------------------------------------------------------------------------------------
-- 0. Database and application user
-- -------------------------------------------------------------------------------------
-- TODO: create the database (suggested name: spartanfinds) with the utf8mb4 character set, then select it.
-- TODO: create an application user limited to connecting from localhost, with its own password (never
--       commit the real password; it belongs in Tomcat's bin/setenv.sh). Grant it only the privileges it
--       needs on this one database. These values must match SPARTANFINDS_DB_URL / _USER / _PASSWORD.

-- -------------------------------------------------------------------------------------
-- 1. User
-- -------------------------------------------------------------------------------------
-- Columns: user_id, name, email, phone_number, password_hash, profile_image, role, created_at.
-- TODO: user_id is the primary key and generated automatically. Email must be unique and required;
--       password_hash is required (hash only, never plaintext). Role values: guest is not stored; decide
--       the stored values (student, administrator). phone_number and profile_image are optional.
-- TODO: "User" is a reserved-ish word in MySQL: quote it everywhere or name the table differently
--       (then update the DAOs to match).
-- OPEN (CLAUDE.md gap): no profile description column yet -- decide whether to add one.

-- -------------------------------------------------------------------------------------
-- 2. Category
-- -------------------------------------------------------------------------------------
-- Columns: category_id, category_name.
-- TODO: category_id primary key, generated automatically. category_name required and unique.
-- TODO: decide whether to load starting categories (that is seed data, written by the team).

-- -------------------------------------------------------------------------------------
-- 3. Item   (references User, Category)
-- -------------------------------------------------------------------------------------
-- Columns: item_id, user_id, category_id, title, description, type, status, created_at.
-- TODO: item_id primary key, generated automatically. user_id (creator) and category_id are required
--       references. type holds LOST or FOUND only. title and type required.
-- OPEN (CLAUDE.md gap): allowed status values for an item are undefined (e.g. active, returned, removed,
--       pending approval for Reddit imports). Item status is separate from request status.
-- TODO: created_at defaults to the current time. Consider an index on category_id, type, status, and
--       created_at for catalog filtering and sorting. Decide delete behavior for items that have images,
--       requests, reports, or a location (block, cascade, or soft delete).

-- -------------------------------------------------------------------------------------
-- 4. Location   (references Item)
-- -------------------------------------------------------------------------------------
-- Columns: location_id, item_id, longitude, latitude, location_name.
-- TODO: location_id primary key. At most one location per item: item_id is unique. Latitude and longitude
--       need a numeric type with enough precision for map coordinates; location_name is a short label.
-- OPEN (CLAUDE.md gap): exact coordinates conflict with "avoid precise locations" -- coarsen at entry or
--       at display. Deleting an item should remove its location.

-- -------------------------------------------------------------------------------------
-- 5. ItemImage   (references Item)
-- -------------------------------------------------------------------------------------
-- Columns: image_id, item_id, image_url, display_order.
-- TODO: image_id primary key. item_id required. display_order is a whole number; the pair (item_id,
--       display_order) should be unique so images have a stable order. image_url is a path or URL.
-- OPEN (CLAUDE.md gap): where image files live (disk vs external) is undecided. Deleting an item should
--       remove its image rows (and the files).

-- -------------------------------------------------------------------------------------
-- 6. RedditItem   (subtype of Item)
-- -------------------------------------------------------------------------------------
-- Columns: item_id, post_url.
-- TODO: item_id is both the primary key and a reference to Item. post_url required and UNIQUE to prevent
--       duplicate imports.
-- OPEN (CLAUDE.md gap): no approval status and no clear creator/ownership model for imported posts. Also
--       store attribution (original author name, original URL). Decide whether approval lives here or in
--       Item.status.

-- -------------------------------------------------------------------------------------
-- 7. ItemRequest   (references User, Item)
-- -------------------------------------------------------------------------------------
-- Columns: request_id, requester_id, item_id, request_description, status, created_at.
-- TODO: request_id primary key. requester_id and item_id required references. request_description holds
--       private identifying evidence (long text, required). status starts as pending.
-- OPEN (CLAUDE.md gaps): allowed status values undefined; no field for the poster's "more info" question or
--       the requester's reply; no updated/resolved timestamp.
-- OPEN: "one active request per student per item" cannot be a plain unique pair on requester + item,
--       because a new request is allowed after an earlier one becomes inactive. Enforce in the service
--       inside a transaction, or choose a schema design that enforces it.

-- -------------------------------------------------------------------------------------
-- 8. Conversation
-- -------------------------------------------------------------------------------------
-- Columns: conversation_id, created_at.
-- TODO: conversation_id primary key, generated automatically; created_at defaults to the current time.

-- -------------------------------------------------------------------------------------
-- 9. ConversationParticipant   (references User, Conversation)
-- -------------------------------------------------------------------------------------
-- Columns: user_id, conversation_id.
-- TODO: composite primary key (user_id, conversation_id); both are required references.
-- OPEN (CLAUDE.md gap): nothing prevents two separate one-to-one conversations between the same pair of
--       users -- enforce in the service or the schema.

-- -------------------------------------------------------------------------------------
-- 10. Message   (references User, Conversation)
-- -------------------------------------------------------------------------------------
-- Columns: message_id, sender_id, conversation_id, text, sent_at, read_status.
-- TODO: message_id primary key. sender_id and conversation_id required references (the sender must be a
--       participant of the conversation -- enforced in the service). sent_at defaults to the current time.
--       read_status is a flag, default unread.
-- OPEN (CLAUDE.md gaps): text must be allowed to be empty for attachment-only messages; read_status needs
--       rethinking if group conversations are ever added.

-- -------------------------------------------------------------------------------------
-- 11. MessageAttachment   (references Message)
-- -------------------------------------------------------------------------------------
-- Columns: attachment_id, message_id, image_url.
-- TODO: attachment_id primary key. message_id required reference; image_url required.

-- -------------------------------------------------------------------------------------
-- 12. Notification   (references User, optionally ItemRequest)
-- -------------------------------------------------------------------------------------
-- Columns: notification_id, user_id, request_id, message, notification_type, is_read, created_at.
-- TODO: notification_id primary key. user_id (recipient) required reference. request_id must be OPTIONAL
--       (null) for notifications not about a request. is_read defaults to false; created_at defaults to now.
-- OPEN (CLAUDE.md gap): no way to point to a message, conversation, or item; allowed notification_type
--       values undefined.

-- -------------------------------------------------------------------------------------
-- 13. Report   (references User twice, optionally Item)
-- -------------------------------------------------------------------------------------
-- Columns: report_id, reported_by_user_id, item_id, reported_user_id, reason, report_date, status.
-- TODO: report_id primary key. reported_by_user_id required. Exactly ONE of item_id / reported_user_id must
--       be set, never both and never neither -- enforce in the schema (a check) or the service.
--       reason required (non-empty). report_date defaults to now. status starts as open/pending.
-- OPEN (CLAUDE.md gaps): allowed status values undefined; no resolving admin or resolution note.
