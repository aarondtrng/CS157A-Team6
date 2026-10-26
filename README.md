# SpartanFinds

A lost and found app for students at SJSU

The main app is in `Backend/`: Java servlets and JSP pages running on Tomcat, with a MySQL database. `Frontend/` holds a separate React + Vite app that runs on its own.

## Dependencies

- Java 17+
- [Maven](https://maven.apache.org/) 3.9+
- [MySQL](https://dev.mysql.com/downloads/) 8+
- [Apache Tomcat](https://tomcat.apache.org/) 10.1 or 11
- [Node.js](https://nodejs.org/) v20+ and npm v10+ (only for `Frontend/`)

On macOS with Homebrew:

```bash
brew install openjdk maven mysql tomcat node
```

On Windows, install each one from the links above, then set `JAVA_HOME` and `CATALINA_HOME` (the Tomcat folder) as environment variables.

## Running the backend

All commands run from the repo root unless noted.

### 1. Set up the database (once)

Start MySQL, then create the tables and load the starting categories:

```bash
brew services start mysql        # macOS; on Windows, start the MySQL service
mysql -u root -p < Backend/db/schema.sql
mysql -u root -p < Backend/db/seed.sql
```

This creates a database named `spartan_finds`. Run `seed.sql` only once: a second run fails because the category names already exist.

### 2. Give Tomcat the database login (once)

The app reads its database settings from environment variables set in Tomcat's `bin/setenv` script. Credentials are never stored in the repo.

macOS / Linux (Homebrew's Tomcat folder is `/opt/homebrew/opt/tomcat/libexec`):

```bash
export CATALINA_HOME=/opt/homebrew/opt/tomcat/libexec
cp Backend/tomcat/setenv.sh.example "$CATALINA_HOME/bin/setenv.sh"
chmod 600 "$CATALINA_HOME/bin/setenv.sh"
```

Windows:

```bat
copy Backend\tomcat\setenv.bat.example %CATALINA_HOME%\bin\setenv.bat
```

Open the copied file and replace the placeholder values:

```bash
export SPARTANFINDS_DB_URL="jdbc:mysql://localhost:3306/spartan_finds"
export SPARTANFINDS_DB_USER="root"
export SPARTANFINDS_DB_PASSWORD="your_mysql_password"
```

Uploaded item photos go to `~/spartanfinds-uploads` by default. Set `SPARTANFINDS_UPLOAD_DIR` in the same file to use a different folder.

Never commit a filled-in `setenv.sh` or `setenv.bat`. Both are in `.gitignore`.

### 3. Build and deploy

Run these again after every code change:

```bash
cd Backend
mvn clean package
cp target/spartanfinds.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" run
```

On Windows, use `copy target\spartanfinds.war %CATALINA_HOME%\webapps\` and `%CATALINA_HOME%\bin\catalina.bat run`.

Tomcat logs print in that terminal. Press Ctrl+C to stop it.

### 4. Try it out

Open these in a browser:

| Page | URL |
| --- | --- |
| Sign up | http://localhost:8080/spartanfinds/signup |
| Log in | http://localhost:8080/spartanfinds/login |
| Catalog | http://localhost:8080/spartanfinds/items |
| Post an item (requires login) | http://localhost:8080/spartanfinds/post/new |
| Map | http://localhost:8080/spartanfinds/map |

### Troubleshooting

- **`Database setting SPARTANFINDS_DB_URL is not set`**: Tomcat didn't find `bin/setenv.sh` (or `setenv.bat`). Check that the file is in `$CATALINA_HOME/bin` and restart Tomcat.
- **`Access denied for user`**: the user or password in `setenv` doesn't match MySQL.
- **`Unknown database 'spartan_finds'`**: run `Backend/db/schema.sql` first.
- **Post form shows no categories**: run `Backend/db/seed.sql`.
- **Port 8080 already in use**: another Tomcat is running. Stop it with `brew services stop tomcat` or close the other terminal.
- **Old code still showing**: delete `$CATALINA_HOME/webapps/spartanfinds/` and copy the WAR again.

## Running the frontend

```bash
cd Frontend
npm install
npm run dev
```

Then open http://localhost:5173.
