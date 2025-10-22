#!/bin/bash

# Nimbbl Core API SDK - Version Manager
# This script manages semantic versioning for the Core API SDK

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

VERSION_FILE="version.properties"

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to get current version
get_current_version() {
    if [ -f "$VERSION_FILE" ]; then
        grep "^SDK_VERSION=" "$VERSION_FILE" | cut -d'=' -f2
    else
        echo "1.0.0"
    fi
}

# Function to get version components
get_version_components() {
    local version=$1
    echo "$version" | tr '.' ' '
}

# Function to update version in properties file
update_version() {
    local new_version=$1
    local major minor patch
    read -r major minor patch <<< "$(get_version_components "$new_version")"
    
    # Calculate version code
    local version_code=$((major * 10000 + minor * 100 + patch))
    
    # Update version.properties
    sed -i.bak "s/^SDK_VERSION=.*/SDK_VERSION=$new_version/" "$VERSION_FILE"
    sed -i.bak "s/^VERSION_NAME=.*/VERSION_NAME=$new_version/" "$VERSION_FILE"
    sed -i.bak "s/^VERSION_CODE=.*/VERSION_CODE=$version_code/" "$VERSION_FILE"
    sed -i.bak "s/^MAJOR_VERSION=.*/MAJOR_VERSION=$major/" "$VERSION_FILE"
    sed -i.bak "s/^MINOR_VERSION=.*/MINOR_VERSION=$minor/" "$VERSION_FILE"
    sed -i.bak "s/^PATCH_VERSION=.*/PATCH_VERSION=$patch/" "$VERSION_FILE"
    sed -i.bak "s/^BUILD_DATE=.*/BUILD_DATE=$(date +%Y-%m-%d)/" "$VERSION_FILE"
    
    # Remove backup file
    rm -f "${VERSION_FILE}.bak"
    
    print_success "Version updated to $new_version"
}

# Function to bump version
bump_version() {
    local bump_type=$1
    local current_version=$(get_current_version)
    local version_components=($(get_version_components "$current_version"))
    local major=${version_components[0]}
    local minor=${version_components[1]}
    local patch=${version_components[2]}

    case $bump_type in
        "major")
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        "minor")
            minor=$((minor + 1))
            patch=0
            ;;
        "patch")
            patch=$((patch + 1))
            ;;
        *)
            print_error "Invalid bump type: $bump_type. Use major, minor, or patch."
            exit 1
            ;;
    esac
    
    local new_version="$major.$minor.$patch"
    update_version "$new_version"
}

# Function to set specific version
set_version() {
    local new_version=$1
    
    # Validate version format
    if [[ ! $new_version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Invalid version format: $new_version. Use MAJOR.MINOR.PATCH (e.g., 4.1.0)"
        exit 1
    fi
    
    update_version "$new_version"
}

# Function to generate changelog entry
generate_changelog_entry() {
    local version=$1
    local date=$(date +%Y-%m-%d)
    
    echo "# v$version - $date"
    echo "- Enhanced error handling with comprehensive validation"
    echo "- Improved code quality and reduced warnings"
    echo "- Added semantic versioning support"
    echo "- Fixed parameter order consistency in ValidationUtils"
    echo "- Removed redundant variable initializers"
    echo "- Optimized null safety handling"
    echo "- Implemented centralized version management system"
    echo ""
}

# Function to build project
build_project() {
    print_info "Building project..."
    if ./gradlew build; then
        print_success "Build completed successfully"
    else
        print_error "Build failed"
        exit 1
    fi
}

# Function to show version details
show_version_details() {
    local current_version=$(get_current_version)
    local version_components=($(get_version_components "$current_version"))
    local major=${version_components[0]}
    local minor=${version_components[1]}
    local patch=${version_components[2]}
    local version_code=$((major * 10000 + minor * 100 + patch))
    
    echo "Current Version Information:"
    echo "=========================="
    echo "SDK Version: $current_version"
    echo "Version Code: $version_code"
    echo "Major: $major"
    echo "Minor: $minor"
    echo "Patch: $patch"
    echo "Build Date: $(grep "^BUILD_DATE=" "$VERSION_FILE" | cut -d'=' -f2)"
    echo "Build Type: $(grep "^BUILD_TYPE=" "$VERSION_FILE" | cut -d'=' -f2)"
    echo ""
}

# Function to show help
show_help() {
    echo "Nimbbl Core API SDK - Version Manager"
    echo
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  show                    Show current version information"
    echo "  bump [TYPE]             Bump version (major, minor, patch)"
    echo "  set [VERSION]           Set specific version (e.g., 4.1.0)"
    echo "  build                   Build the project"
    echo "  changelog [VERSION]     Generate changelog entry for version"
    echo "  help                    Show this help message"
    echo
    echo "Examples:"
    echo "  $0 show                 # Show current version"
    echo "  $0 bump patch           # Bump patch version (1.0.0 -> 1.0.1)"
    echo "  $0 bump minor           # Bump minor version (1.0.0 -> 1.1.0)"
    echo "  $0 bump major           # Bump major version (1.0.0 -> 2.0.0)"
    echo "  $0 set 4.1.0            # Set version to 4.1.0"
    echo "  $0 build                # Build the project"
    echo "  $0 changelog 4.1.0      # Generate changelog entry"
    echo
    echo "Current version: $(get_current_version)"
}

# Main execution
case "${1:-}" in
    "show")
        show_version_details
        ;;
    "bump")
        if [ -z "${2:-}" ]; then
            print_error "Bump type required (major, minor, patch)"
            exit 1
        fi
        bump_version "$2"
        ;;
    "set")
        if [ -z "${2:-}" ]; then
            print_error "Version required (e.g., 4.1.0)"
            exit 1
        fi
        set_version "$2"
        ;;
    "build")
        build_project
        ;;
    "changelog")
        if [ -z "${2:-}" ]; then
            print_error "Version required (e.g., 4.1.0)"
            exit 1
        fi
        generate_changelog_entry "$2"
        ;;
    "help"|"--help"|"-h"|"")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
