# ET3 Software Development Internship – Technical Assignment

**Applicant:** Ahmed Yasser  
**Option Chosen:** Option 2 – Command-Line To-Do App  
**Language & Tools:**  
- Java 17  
- IntelliJ IDEA (development)  
- Standard Java I/O and utilities  

---

## 📌 Overview
A **simple command-line To-Do list application** that supports:

- Adding tasks (with optional **priority** & **tags**)
- Listing tasks with status, priority, tags, and timestamp
- Marking tasks as done (with duplicate-check prevention)
- Deleting tasks
- Persistent storage in `tasks.txt`

### **Bonus Features Implemented**
- `--priority N` → Assign task priority
- `--tags tag1,tag2` → Assign tags/categories
- Automatic creation timestamp for each task
- Defensive handling for malformed lines in the storage file

---

## 📂 Project Structure
eT3-Software-Development-Internship/
├── src/
│ └── TaskManager.java
├── tasks.txt # Storage file (auto-created if missing)
├── README.md
└── .gitignore

1. **Compile**
   ```bash
   javac src/TaskManager.java

