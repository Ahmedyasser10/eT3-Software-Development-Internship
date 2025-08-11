# ET3 Software Development Internship – Technical Assignment

**Applicant:** Ahmed Yasser  
**Option Chosen:** Option 2 – Command-Line To-Do App  
**Language & Tools:**  
- Java 17  
- IntelliJ IDEA (for development)  
- Standard Java I/O and utilities  

---

## 📌 Overview
This is a **simple command-line To-Do list application** that supports:

- Adding tasks (with optional priority & tags)
- Listing tasks
- Marking tasks as done (checks if already done)
- Deleting tasks
- Persistent storage in `tasks.txt`

**Extra features implemented:**
- **Priority** (`--priority N`)
- **Tags** (`--tags tag1,tag2`)
- **Creation timestamp** for each task
- Defensive handling for malformed task file lines

---
## 📂 Project Structure
eT3-Software-Development-Internship/
├── src/
│ └── TaskManager.java
├── tasks.txt # Storage file (auto-created if missing)
├── README.md
└── .gitignore
---

## 🚀 How to Run

1. **Compile**  
   Navigate to the `src` folder and run:
   javac src/TaskManager.java

