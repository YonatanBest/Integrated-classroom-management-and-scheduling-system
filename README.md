# Integrated Classroom Management and Scheduling System

A comprehensive Java-based system for managing classroom schedules, resources, and student assignments in educational institutions.

## Features

- **Schedule Management**
  - Create and manage course schedules
  - Handle regular and evening program schedules
  - Prevent time slot conflicts
  - Manage schedule statuses (draft/published/revision requested)

- **Resource Management**
  - Track classroom resources (projectors, connectors, etc.)
  - Manage resource availability
  - Prevent resource booking conflicts

- **User Management**
  - Multiple user roles (Coordinator, Instructor, Student)
  - Room representative assignment
  - Student room assignments

- **Calendar View**
  - Visual weekly schedule display
  - Filter by program type and status
  - Color-coded schedule status

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6 or higher
- SQLite 3

## Installation

1. Clone the repository:
```bash
git clone https://github.com/YonatanBest/Integrated-classroom-management-and-scheduling-system.git
cd Integrated-classroom-management-and-scheduling-system
```

2. Build the project using Maven:
```bash
mvn clean install package
```

3. Run the application:
```bash
java -jar target/classroom-management-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Initial Setup

The system will automatically:
- Create the SQLite database (classroom.db)
- Initialize required tables
- Create a default admin account

Default admin credentials:
- Username: admin
- Password: admin123

## Usage

### Coordinator Features

1. **Schedule Management**
   - Create new schedules
   - Edit existing schedules
   - Manage schedule statuses
   - Handle revision requests

2. **Course Management**
   - Add/edit courses
   - Assign instructors
   - Set credit hours

3. **Room Management**
   - Assign students to rooms
   - Manage room representatives
   - Monitor room resources

### Instructor Features

1. **Schedule Viewing**
   - View assigned schedules
   - Filter by program type
   - See schedule status

2. **Schedule Revision**
   - Request schedule revisions
   - Track revision status
   - View revision history

### Student Features

1. **Schedule Access**
   - View room schedules
   - See enrolled courses
   - Check resource availability

## Database Schema

The system uses SQLite with the following main tables:
- Users
- Courses
- Schedule
- Resources
- ScheduleResources
- ScheduleRevisionRequests
- Enrollments

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support, please open an issue in the GitHub repository or contact the development team.

