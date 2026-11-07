# Batch File Processor

**Batch File Processor** is a bulk file management and editing application designed to streamline the sorting, renaming, and modification of large groups of similar files. Whether you're organizing photo collections, managing video assets, handling indexed datasets, or processing modification files, this tool eliminates the tedium of repetitive file operations through an intuitive interface and powerful batch automation capabilities.

## How it works

The application opens to a simple Swing-based GUI. You browse to a folder, select files, and apply operations from the labeled control panels at the bottom of the window. The folder you last used is remembered between sessions and reloads automatically the next time you start the app.

Every operation that modifies files prompts you for confirmation first. After each action, a status bar at the bottom briefly displays the result (e.g. "Renamed 4 files.") before reverting to a live count of the current folder and selection.

There's no installer, no `.exe`, and no third-party dependencies. The application runs as a single Java source file from NetBeans, drawing only on the standard Java and Swing libraries.

## Key Features

* **Folder persistence** — the last opened folder reloads automatically on startup
* **Multiple selection modes** — Ctrl/Shift multi-select, Select All, or Select by Pattern (substring match)
* **Bulk Rename** — apply a base name plus a sequential suffix to selected files, with optional filtering
* **Mass Overwrite** — rename every file in the folder regardless of selection or filter
* **Sequence Changer** — apply a new sequence to selected files while preserving their original base names
* **Extension Rename** — change file extensions for selected files (e.g. `.png` → `.jpg`)
* **Move-to-Deletion** — sends selected files to a "Files for Deletion" subfolder for review before permanent removal
* **Confirmation prompts** — every destructive action requires explicit user confirmation before executing
* **Live status bar** — shows folder count, selection count, and operation results in real time
* **Tooltips** — hover any button for a one-line description of what it does

Ideal for photographers, videographers, data analysts, and anyone managing large file collections that require consistent, repeatable operations.

## Installation and Setup

This application runs through the NetBeans IDE rather than as a standalone executable.

### Prerequisites

* Java Development Kit (JDK) 17 or higher
* NetBeans IDE (recent version)

### Steps

1. Clone or download the repository:
   ```
   git clone https://github.com/josrush92/Batch-File-Processor.git
   ```
2. Open NetBeans IDE.
3. Open or import the project containing `BatchFileProcessor.java` (package `fnl8`).
4. In the NetBeans Projects panel, right-click `BatchFileProcessor.java` and select **Run File** (or press Shift+F6).
5. The application window opens with focus on the **Browse** button — pressing Enter immediately opens the folder chooser.

## Usage

The window has four sections below the file list, each grouped into a labeled panel.

### Folder

* **Browse** — opens a folder chooser; the selected folder's files populate the list
* **Refresh** — reloads the file list from disk (useful when files have changed externally)

### Selection

* **Select All** — highlights every file in the list
* **Select by Pattern** — highlights files whose names contain the substring typed in the **Pattern** field (case-sensitive)

### Rename and Renumber

* **Filter** field — optional; restricts the rename to selected files matching the filter substring
* **Base Name** field — the new name to apply (e.g. `holiday`)
* **Type** dropdown — *Numerical* (1, 2, 3 …) or *Alphabetical* (a, b, c …, z, 2a, 2b …, 2z, 3a …)
* **Rename** — applies the base name and sequence to the selected and filtered files
* **Mass Overwrite** — applies the base name and sequence to *every* file in the folder, ignoring selection and filter
* **Sequence Changer** — applies only the sequence to selected files, preserving each file's existing base name

### Rename File Extension

* **Change Extension** — rename extensions on selected files matching the **From** value to use the **To** value
* **From** field — original extension (e.g. `png`)
* **To** field — target extension (e.g. `jpg`)
* Note: extension matching is case-sensitive. The operation is a string-only rename — it does **not** perform actual image/video format conversion.

### Delete Files

* **Delete** — moves selected files into a `Files for Deletion` subfolder inside the current folder. Files are *not* actually deleted from disk; they're set aside for review and manual removal.

### Behavior Notes

* **Confirmation prompts**: every destructive action (Rename, Mass Overwrite, Sequence Changer, Change Extension, Delete) shows a confirmation dialog before executing.
* **Collision handling**: if a target name already exists, that file is skipped and reported in a summary dialog. The sequence counter advances regardless, so subsequent files claim the next available slot.
* **Status bar**: the bar at the bottom shows live folder and selection counts. After each operation it briefly displays a result message (e.g. "Renamed 4 files.") before reverting to the count.
* **Progress bar**: the thin bar at the very bottom indicates when an operation is running.

## Repository Structure

```
Batch-File-Processor/
├── BatchFileProcessor.java   Main source file (single-class implementation, package fnl8)
├── README.md                 This document
└── media/                    Folder for additional project files
```
