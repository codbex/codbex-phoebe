#!/bin/sh
# fail the whole script if any command below fails
set -e

log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') --- $1"
}
log "Executing Linux build script..."

script_path=$(realpath "$0")
script_dir=$(dirname "$script_path")
project_name=$(basename "$script_dir")
log "Script path: $script_path"

# Set source and target directories
SOURCE_DIR="$(pwd)"
TARGET_DIR="/opt/airflow"
DAGS_TARGET_DIR="$TARGET_DIR/dags/$project_name"

# Check if the target directory exists, if not, create it
if [ ! -d "$TARGET_DIR" ]; then
  log "Target directory $TARGET_DIR does not exist. Creating it..."
  mkdir -p "$TARGET_DIR"
fi

# List of folders to copy as they are
FOLDERS="config plugins"

# Copy specified folders from the source to the target, merging and replacing as needed
for FOLDER in $FOLDERS; do
  if [ -d "$SOURCE_DIR/$FOLDER" ]; then
    log "Copying $SOURCE_DIR/$FOLDER to $TARGET_DIR"
    mkdir -p "$TARGET_DIR/$FOLDER"
    if [ "$(ls -A "$SOURCE_DIR/$FOLDER")" ]; then
      cp -r "$SOURCE_DIR/$FOLDER/." "$TARGET_DIR/$FOLDER"
      log "$SOURCE_DIR/$FOLDER copied to $TARGET_DIR/$FOLDER."
    else
      log "$SOURCE_DIR/$FOLDER is empty. No files to copy."
    fi
  else
    log "Folder $SOURCE_DIR/$FOLDER does not exist in the source directory."
  fi
done

# Handle dags folder separately
if [ -d "$SOURCE_DIR/dags" ]; then
  log "Copying $SOURCE_DIR/dags to $DAGS_TARGET_DIR"
  mkdir -p "$DAGS_TARGET_DIR"
  if [ "$(ls -A "$SOURCE_DIR/dags")" ]; then
    cp -r "$SOURCE_DIR/dags/." "$DAGS_TARGET_DIR"
    log "$SOURCE_DIR/dags copied to $DAGS_TARGET_DIR."
  else
    log "$SOURCE_DIR/dags is empty. No files to copy."
  fi
else
  log "Folder $SOURCE_DIR/dags does not exist in the source directory."
fi

log "Copy operation completed."
log "Linux build script $script_path completed!"
