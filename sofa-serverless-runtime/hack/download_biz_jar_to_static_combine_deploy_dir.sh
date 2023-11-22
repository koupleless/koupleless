#!/bin/bash

# Check if the correct number of arguments are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <config_file> <target_directory>"
    exit 1
fi

# Reading arguments
CONFIG_FILE=$1
TARGET_DIR=$2

# Check if the configuration file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Configuration file not found: $CONFIG_FILE"
    exit 1
fi

# Check if the target directory exists, if not create it
if [ ! -d "$TARGET_DIR" ]; then
    echo "Target directory not found, creating: $TARGET_DIR"
    mkdir -p "$TARGET_DIR"
fi

# Function to download and move jar
download_and_move() {
    GROUP_ID=$(echo $1 | cut -d ':' -f 1)
    ARTIFACT_ID=$(echo $1 | cut -d ':' -f 2)
    VERSION=$(echo $1 | cut -d ':' -f 3)
    mvn dependency:get -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION -Dtransitive=false
    mvn dependency:copy -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION -DoutputDirectory="$TARGET_DIR"
}

# Reading each line in the configuration file
while IFS= read -r line
do
    download_and_move "$line"
done < "$CONFIG_FILE"
