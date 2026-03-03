# 🚀 RevWorkforce - Enterprise Workforce management System

RevWorkforce is a comprehensive **Monolithic Web Application** built with **Spring Boot 3** and **Oracle DB**. It is designed to streamline organizational processes, from employee management and leave tracking to performance reviews and internal communications.

---

## 🌟 Core Features

### 🛠️ Administrator Module
- **Organization Control**: Manage departments (Business Units) with unique validation.
- **Employee Lifecycle**: Register new employees, assign managers, and manage status (Active/Terminated).
- **Communication Center**: Broadcast announcements and manage company-wide events.
- **Leave Oversight**: System-wide approval or rejection of employee leave applications.
- **Security**: Manage authentication and role-based access control (RBAC).

### 👥 Manager Module
- **Team Leadership**: View team members and manage their growth.
- **Approval Workflow**: Review, approve, or reject leave requests with comments.
- **Goal Management**: Assign specific goals to team members and track their progress.
- **Performance Evaluation**: Conduct and document professional performance reviews.
- **Self-Service**: Apply for personal leaves and track individual balances.

### 👤 Employee Module
- **Personal Dashboard**: View upcoming company events and unread notifications.
- **Leave Management**: Apply for various leave types (Casual, Sick, etc.) and view history.
- **Goal Tracking**: Mark assigned goals as completed.
- **Performance Access**: View annual or quarterly performance reviews from managers.
- **Profile Customization**: Update contact details and address securely.

---

## 💻 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Spring Boot 3.5.11, Java 17+, Spring Data JPA, Spring Security |
| **Database** | Oracle 21c (ojdbc11) |
| **Frontend** | Thymeleaf, Bootstrap 5.3.2, Font Awesome 6.4.2 |
| **Design** | Inter (Google Fonts), CSS3, Glassmorphism elements |
| **Tooling** | Maven (mvnw), Lombok, Jakarta Validation |

---

## 🏗️ System Architecture

The project follows a clean **Three-Layer Architecture** to ensure separation of concerns:
- **`Controller/`**: Handles HTTP requests and interacts with Thymeleaf templates.
- **`Service/`**: Contains core business logic, transactional boundaries, and workflow rules.
- **`Repository/`**: Manages data access via Spring Data JPA interfaces.
- **`DTO/`**: Data Transfer Objects for secure and validated input handling.
- **`Security/`**: Custom user details and security configuration for RBAC.

### 🗺️ Data Model (Entities)
1.  **User**: Authentication credentials (Email/Password) and Role.
2.  **Employee**: Profile details, salary, manager-subordinate hierarchy.
3.  **Department**: Organizational units.
4.  **LeaveApplication**: Leave requests tracking status and duration.
5.  **LeaveBalance**: Remaining days per leave type for each employee.
6.  **Goal**: Assigned tasks or targets for employees.
7.  **PerformanceReview**: Evaluation metrics and feedback from managers.
8.  **Notification**: Real-time alerts for leave status, goal assignments, etc.
9.  **Announcement & Event**: Internal communication records.

---

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher.
- **Maven** (included via `mvnw`).
- **Oracle Database** instance running locally or on a server.

### 1. Database Configuration
Update `src/main/resources/application.properties` with your Oracle DB credentials:
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

### 2. Build & Run
Run the following commands in the root directory:
```bash
# Build the project
./mvnw clean compile

# Start the application
./mvnw spring-boot:run
```
Access the application at: `http://localhost:8080`

---

## 🔐 Default Credentials (Initial Setup)

The system is automatically initialized with a root administrator account:
- **Email**: `admin@rev.com`
- **Password**: `admin123`
- **Role**: `ADMIN`

*Note: For new employees created via the Admin Dashboard, the default password is `welcome123`.*

---

## 📍 Future Roadmap
- [ ] **Search/Filter**: Advanced employee search functionality.
- [ ] **Reporting**: Exportable PDF/Excel reports for payroll and leaves.
- [ ] **Attachment Support**: Upload medical certificates for Sick Leave.
- [ ] **API Access**: Complete REST API documentation for mobile integration.

---

### 📄 License
Internal Organizational Tool - All Rights Reserved.
