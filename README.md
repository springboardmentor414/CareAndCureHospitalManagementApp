# 🏥 Care and Cure Hospital App

The **Care and Cure Hospital App** is a **Java Spring Boot-based full-stack hospital management system** built during **Infosys Springboard Internship 5.0**.  
It automates key hospital operations like **doctor management, patient profiles, appointments, billing, payments, and reporting**, providing a scalable and secure solution for healthcare workflows.  

---

## ⚙️ System Architecture

The application follows a **3-Layered Architecture (MVC pattern)**:
- **Controller Layer (Spring MVC)** → Handles HTTP requests, routing, and Thymeleaf views.  
- **Service Layer** → Contains business logic, validations, and workflows.  
- **DAO/Repository Layer (Spring Data JPA)** → Manages persistence and communication with the MySQL database.  

### 📐 Tech Stack
- **Backend**: Java 17, Spring Boot 3.x, Spring Data JPA, Hibernate ORM  
- **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript, Bootstrap  
- **Database**: MySQL 8 (Relational database with entity relationships)  
- **Build Tool**: Maven  
- **Version Control**: Git/GitHub  
- **Deployment**: Local Tomcat (Spring Boot embedded)  
- **Email Notifications**: Spring Mail (SMTP)  
- **Security (Optional Module)**: Spring Security for authentication/authorization  

---

## 📂 Project Structure

```

care-and-cure-hospital-app/
│── src/main/java/com/careandcure/
│   ├── controller/       # Handles endpoints and UI routing
│   ├── service/          # Business logic
│   ├── repository/       # DAO layer (Spring Data JPA)
│   ├── model/entity/     # JPA entities mapped to DB tables
│   └── CareAndCureApp.java  # Main Spring Boot Application
│
│── src/main/resources/
│   ├── application.properties  # DB + Server configuration
│   ├── templates/              # Thymeleaf HTML templates
│   └── static/                 # CSS, JS, Images
│
└── pom.xml                     # Maven dependencies

````

---

## ✨ Core Features / Modules

### 👨‍⚕️ Doctor Directory
- Add, update, deactivate doctor profiles.  
- Track specialization, availability, qualifications, consultation fees.  
- Email notifications on profile changes.  

### 🧑‍🦽 Patient Profiles
- Patient registration with personal + medical details.  
- Maintain history, insurance info, active/inactive status.  
- Automatic alerts on profile updates.  

### 📅 Appointment Calendar
- Real-time booking based on doctor availability.  
- Prevent overbooking (slot management).  
- Patients/Doctors notified via email/SMS reminders.  

### 💳 Billing & Payments
- Auto-generated bills (consultations, diagnostics, pharmacy).  
- Discounts for bulk purchases.  
- Multi-mode payments (cards, net banking, wallets).  
- Receipts auto-emailed to patients.  

### 📊 Reporting Module
- Doctor Appointment Report  
- Health Issue Report  
- Revenue Report  
- Low Consultation Doctor Report  
- Daily Appointments Report  
- Patient No-Show Report  
- Payment/Insurance Report  

---

## 🛠️ Installation & Setup

### 1️⃣ Clone the Repository
```bash
git clone <repo-url>
cd care-and-cure-hospital-app
````

### 2️⃣ Configure Database

* Create a MySQL DB (e.g., `careandcuredb`)
* Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/careandcuredb
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### 3️⃣ Build & Run the App

```bash
mvn clean install
mvn spring-boot:run
```

### 4️⃣ Access the Application

* App URL: `http://localhost:9093`
* Default Modules: `/patientRegistration`, `/doctorHomePage`, `/adminDashboard`

---

## 🔐 Security Considerations

* Spring Security (Role-based access for Admin, Doctor, Patient).
* Form validation at both client & server side.
* Passwords stored in encrypted format.

---

## 📈 Future Enhancements

* REST APIs for mobile app integration.
* Role-based dashboards (Admin/Doctor/Patient).
* Integration with external **insurance claim APIs**.
* Cloud deployment (AWS/Azure).

---

## 👩‍💻 Team & Acknowledgment

Developed under **Infosys Springboard Internship 5.0**
Project: **Care and Cure – Hospital Management Application**

Mentors provided guidance on **Spring Boot, MySQL, MVC, and real-world healthcare workflows**.
