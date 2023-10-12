#!/bin/bash

# Dynamically determine path to gradle.properties
PROPERTIES_PATH="$(dirname "$0")/../gradle.properties"

# Check if at least a tag name is provided
if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <tag_name> [-f]"
    exit 1
fi

TAG_NAME=$1
FORCE=""

# Check if second arg is -f to force push
if [ "$#" -eq 2 ] && [ "$2" == "-f" ]; then
    FORCE="--force"
fi

# Extract app.version from gradle.properties
APP_VERSION=$(awk -F'=' '/^app.version/ {print $2}' $PROPERTIES_PATH)

# Check if TAG_NAME matches APP_VERSION
if [ "$TAG_NAME" != "$APP_VERSION" ]; then
    echo "ERROR: The provided TAG_NAME ($TAG_NAME) does not match app.version ($APP_VERSION) in $PROPERTIES_PATH."
    echo "Please update the gradle.properties file or provide the correct TAG_NAME."
    exit 1
fi

# Set the tag to the current HEAD (forcefully if -f is provided)
git tag $FORCE "$TAG_NAME"
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to set git tag $TAG_NAME."
    exit 1
fi

# Push the tag to origin (force push if -f is provided)
git push origin "$TAG_NAME" $FORCE
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to push git tag $TAG_NAME to origin."
    exit 1
fi

echo "Tag $TAG_NAME set to current HEAD and pushed to origin."
