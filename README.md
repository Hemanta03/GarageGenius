# GarageGenius (Servlet/JSP + MySQL + Tomcat)

GarageGenius is a Java (Jakarta EE) MVC web app for garage management: customers/vehicles, appointment requests, job cards, inventory, billing/invoices, reporting, staff workflow, and a public contact form.

## Tech stack
- **Java**: 21+ (works with newer JDKs too)
- **Build**: Maven (`mvnw`)
- **Server**: Tomcat 11
- **DB**: MySQL
- **UI**: JSP + JSTL + pure CSS/JS

## Quick start (local)

### 1) Create DB + schema + sample data
Create a MySQL database named `garagegenius` and import:

- `garagegenius.sql` (full schema + sample data)
- `appointment_migration.sql` only if you already imported an older database and want to add the appointment feature without rebuilding the database

Example:

```bash
mysql -u root -p < garagegenius.sql
```

### 2) Configure DB credentials
Edit:

- `src/main/resources/db.properties`

and set:

- `db.url`
- `db.username`
- `db.password`

### 3) Build WAR

```bash
./mvnw clean package
```

Output:
- `target/GarageGenius.war`

### 4) Deploy to Tomcat 11
Copy WAR into Tomcat:

```bash
cp target/GarageGenius.war /path/to/tomcat/webapps/
```

Start Tomcat and open:
- `http://localhost:8080/GarageGenius/`

## Demo accounts (seeded)
These credentials match `garagegenius.sql`:

- **Admin**: `admin@garagegenius.com` / `Admin@123`
- **Staff**: `mike@garagegenius.com` / `Staff@123`
- **Staff**: `sarah@garagegenius.com` / `Staff@123`

## Role-based manual test checklist (recommended)

### Admin smoke flow
- Login as admin
- Customers: create a customer → confirm appears in list
- Vehicles: create a vehicle for that customer → confirm appears in list
- Job cards: create job card (select customer+vehicle+staff, select some services) → confirm appears in list
- Job cards: view job card → confirm services/parts sections render
- Inventory: add spare part → restock spare part
- Job cards: edit job card → add parts used
- Mark job card `completed`
- Invoices: generate invoice from completed job → view invoice → confirm real line items show (services + parts)
- Record payment: set status to `paid`
- Reports: open revenue report and inventory report
- Contacts: open contact inbox, mark a message read/replied

### Staff smoke flow
- Login as staff
- Dashboard shows only assigned job cards
- Open a job card → update status (e.g. `in_progress`, then `completed`)
- Open staff inventory view and verify list renders

### Customer smoke flow
- Login as customer
- Dashboard loads (vehicles + jobs lists)
- Request an appointment for a vehicle and service
- My Appointments page shows the request as `pending`
- My Invoices page loads
- View invoice (if exists) → print view renders line items

### Public smoke flow
- Submit contact form
- Verify message shows in Admin → Contact Inbox

## Security notes
- All POST forms include a CSRF token validated by `CsrfFilter`.
- Role-based access is enforced via filters.
- Passwords are stored using BCrypt hashes.
- Error pages are configured for invalid requests, unauthorized access, missing pages, and server errors.

## Database backup and recovery

Create a timestamped backup:

```bash
mysqldump -u root -p garagegenius > backup_garagegenius_$(date +%Y%m%d_%H%M%S).sql
```

Restore from a backup:

```bash
mysql -u root -p garagegenius < backup_garagegenius.sql
```

For coursework evidence, include screenshots of the exported SQL backup file and the restored database tables in MySQL.
