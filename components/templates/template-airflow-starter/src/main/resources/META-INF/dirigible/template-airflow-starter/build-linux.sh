#!/bin/sh
# fail the whole script if any command below fails
set -e

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') --- $1"
}
log "Executing Linux build script..."

script_path=$(realpath "$0")
log "Script path: $script_path"

# Set source and target directories
SOURCE_DIR="$(pwd)"
TARGET_DIR="/opt/airflow"

# Check if the target directory exists, if not, create it
if [ ! -d "$TARGET_DIR" ]; then
  log "Target directory $TARGET_DIR does not exist. Creating it..."
  mkdir -p "$TARGET_DIR"
fi

# List of folders to copy (space-separated)
FOLDERS="config dags plugins"

# Copy specified folders from the source to the target, merging and replacing as needed
for FOLDER in $FOLDERS; do
  if [ -d "$SOURCE_DIR/$FOLDER" ]; then
    log "Copying $SOURCE_DIR/$FOLDER to $TARGET_DIR"
    # Ensure the subdirectory exists in the target before copying
    mkdir -p "$TARGET_DIR/$FOLDER"
    # Check if the source folder is empty before copying
    if [ "$(ls -A "$SOURCE_DIR/$FOLDER")" ]; then
      # Copy all files, including hidden ones (.*)
      cp -r "$SOURCE_DIR/$FOLDER/." "$TARGET_DIR/$FOLDER"
      log "$SOURCE_DIR/$FOLDER copied to $TARGET_DIR/$FOLDER."
    else
      log "$SOURCE_DIR/$FOLDER is empty. No files to copy."
    fi
  else
    log "Folder $SOURCE_DIR/$FOLDER does not exist in the source directory."
  fi
done

log "Copy operation completed."

log "Linux build script $script_path completed!"
