CREATE DATABASE IF NOT EXISTS spartan_finds
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE spartan_finds;

CREATE TABLE User (
    user_id INT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(25),
    password_hash VARCHAR(255) NOT NULL,
    profile_description VARCHAR(1000),
    profile_image VARCHAR(2048),
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id)
);


CREATE TABLE Category (
    category_id INT AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,

    PRIMARY KEY (category_id)
);


CREATE TABLE Item (
    item_id INT AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (item_id),

    FOREIGN KEY (user_id)
        REFERENCES User(user_id),

    FOREIGN KEY (category_id)
        REFERENCES Category(category_id)
);


CREATE TABLE Location (
    location_id INT AUTO_INCREMENT,
    item_id INT NOT NULL UNIQUE,
    longitude DECIMAL(10, 7) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    location_name VARCHAR(255),

    PRIMARY KEY (location_id),

    FOREIGN KEY (item_id)
        REFERENCES Item(item_id)
        ON DELETE CASCADE
);


CREATE TABLE Conversation (
    conversation_id INT AUTO_INCREMENT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (conversation_id)
);


CREATE TABLE ConversationParticipant (
    user_id INT NOT NULL,
    conversation_id INT NOT NULL,

    PRIMARY KEY (user_id, conversation_id),

    FOREIGN KEY (user_id)
        REFERENCES User(user_id),

    FOREIGN KEY (conversation_id)
        REFERENCES Conversation(conversation_id)
        ON DELETE CASCADE
);


CREATE TABLE Message (
    message_id INT AUTO_INCREMENT,
    sender_id INT NOT NULL,
    conversation_id INT NOT NULL,
    text VARCHAR(5000),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status TINYINT NOT NULL DEFAULT 0,

    PRIMARY KEY (message_id),

    FOREIGN KEY (sender_id)
        REFERENCES User(user_id),

    FOREIGN KEY (conversation_id)
        REFERENCES Conversation(conversation_id)
        ON DELETE CASCADE
);


CREATE TABLE MessageAttachment (
    attachment_id INT AUTO_INCREMENT,
    message_id INT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,

    PRIMARY KEY (attachment_id),

    FOREIGN KEY (message_id)
        REFERENCES Message(message_id)
        ON DELETE CASCADE
);


CREATE TABLE ItemImage (
    image_id INT AUTO_INCREMENT,
    item_id INT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    display_order INT NOT NULL DEFAULT 1,

    PRIMARY KEY (image_id),

    FOREIGN KEY (item_id)
        REFERENCES Item(item_id)
        ON DELETE CASCADE,

    UNIQUE (item_id, display_order)
);


CREATE TABLE ItemRequest (
    request_id INT AUTO_INCREMENT,
    requester_id INT NOT NULL,
    item_id INT NOT NULL,
    request_description VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (request_id),

    FOREIGN KEY (requester_id)
        REFERENCES User(user_id),

    FOREIGN KEY (item_id)
        REFERENCES Item(item_id)
        ON DELETE CASCADE
);


CREATE TABLE Notification (
    notification_id INT AUTO_INCREMENT,
    user_id INT NOT NULL,
    request_id INT,
    message VARCHAR(1000) NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    is_read TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (notification_id),

    FOREIGN KEY (user_id)
        REFERENCES User(user_id)
        ON DELETE CASCADE,

    FOREIGN KEY (request_id)
        REFERENCES ItemRequest(request_id)
        ON DELETE SET NULL
);


CREATE TABLE Report (
    report_id INT AUTO_INCREMENT,
    reported_by_user_id INT NOT NULL,
    item_id INT,
    reported_user_id INT,
    reason VARCHAR(5000) NOT NULL,
    report_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    PRIMARY KEY (report_id),

    FOREIGN KEY (reported_by_user_id)
        REFERENCES User(user_id),

    FOREIGN KEY (item_id)
        REFERENCES Item(item_id),

    FOREIGN KEY (reported_user_id)
        REFERENCES User(user_id)
);


CREATE TABLE RedditItem (
    item_id INT NOT NULL,
    post_url VARCHAR(2048) NOT NULL,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    PRIMARY KEY (item_id),

    FOREIGN KEY (item_id)
        REFERENCES Item(item_id)
        ON DELETE CASCADE
);