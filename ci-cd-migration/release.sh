#!/bin/bash

# Nimbbl SDK Release Script
# This script automates the release process for JitPack publishing

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to validate version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Invalid version format. Use semantic versioning (e.g., 3.0.6)"
        exit 1
    fi
}

# Function to check if tag exists
tag_exists() {
    local version=$1
    git tag -l "v$version" | grep -q "v$version"
}

# Function to check if working directory is clean
check_working_directory() {
    if [[ -n $(git status --porcelain) ]]; then
        print_error "Working directory is not clean. Please commit or stash changes."
        git status --porcelain
        exit 1
    fi
}

# Function to update version in build.gradle.kts
update_version() {
    local version=$1
    local build_file="build.gradle.kts"
    
    print_status "Updating version to $version in $build_file"
    
    # Update version in build.gradle.kts
    sed -i.bak "s/version = \"[0-9]\+\.[0-9]\+\.[0-9]\+\"/version = \"$version\"/" "$build_file"
    
    # Remove backup file
    rm -f "${build_file}.bak"
    
    print_success "Version updated to $version"
}

# Function to create release commit
create_release_commit() {
    local version=$1
    
    print_status "Creating release commit for version $version"
    
    git add .
    git commit -m "release: Bump version to $version"
    
    print_success "Release commit created"
}

# Function to create and push tag
create_tag() {
    local version=$1
    
    print_status "Creating tag v$version"
    
    git tag -a "v$version" -m "Release version $version"
    git push origin "v$version"
    
    print_success "Tag v$version created and pushed"
}

# Function to create release branch
create_release_branch() {
    local version=$1
    
    print_status "Creating release branch release/v$version"
    
    git checkout -b "release/v$version"
    git push origin "release/v$version"
    
    print_success "Release branch release/v$version created and pushed"
}

# Function to check JitPack build status
check_jitpack_status() {
    local version=$1
    
    print_status "Checking JitPack build status for version $version"
    print_warning "Please check manually at: https://jitpack.io"
    print_warning "Look for: com.github.nimbbl-tech:nimbbl-checkout-core-sdk:$version"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <version> [options]"
    echo ""
    echo "Arguments:"
    echo "  version    Semantic version (e.g., 3.0.6)"
    echo ""
    echo "Options:"
    echo "  --help     Show this help message"
    echo "  --dry-run  Show what would be done without executing"
    echo ""
    echo "Examples:"
    echo "  $0 3.0.6"
    echo "  $0 3.1.0 --dry-run"
    echo ""
    echo "This script will:"
    echo "  1. Validate the version format"
    echo "  2. Check if working directory is clean"
    echo "  3. Update version in build.gradle.kts"
    echo "  4. Create a release commit"
    echo "  5. Create and push a tag"
    echo "  6. Create a release branch"
    echo "  7. Provide JitPack monitoring instructions"
}

# Function to perform dry run
dry_run() {
    local version=$1
    
    print_warning "DRY RUN - No changes will be made"
    echo ""
    print_status "Would perform the following actions:"
    echo "  1. Validate version: $version"
    echo "  2. Check working directory cleanliness"
    echo "  3. Update version in build.gradle.kts to: $version"
    echo "  4. Create commit with message: 'release: Bump version to $version'"
    echo "  5. Create and push tag: v$version"
    echo "  6. Create and push branch: release/v$version"
    echo "  7. Provide JitPack monitoring instructions"
    echo ""
    print_status "To execute these actions, run: $0 $version"
}

# Main function
main() {
    local version=""
    local dry_run=false
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --help)
                show_usage
                exit 0
                ;;
            --dry-run)
                dry_run=true
                shift
                ;;
            -*)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
            *)
                if [[ -z "$version" ]]; then
                    version=$1
                else
                    print_error "Multiple versions specified"
                    exit 1
                fi
                shift
                ;;
        esac
    done
    
    # Check if version is provided
    if [[ -z "$version" ]]; then
        print_error "Version is required"
        show_usage
        exit 1
    fi
    
    # Validate version format
    validate_version "$version"
    
    # Check if tag already exists
    if tag_exists "$version"; then
        print_error "Tag v$version already exists"
        exit 1
    fi
    
    # Check if required commands exist
    if ! command_exists git; then
        print_error "Git is not installed"
        exit 1
    fi
    
    if ! command_exists sed; then
        print_error "Sed is not installed"
        exit 1
    fi
    
    # Perform dry run if requested
    if [[ "$dry_run" == true ]]; then
        dry_run "$version"
        exit 0
    fi
    
    # Check working directory
    check_working_directory
    
    # Update version
    update_version "$version"
    
    # Create release commit
    create_release_commit "$version"
    
    # Create and push tag
    create_tag "$version"
    
    # Create release branch
    create_release_branch "$version"
    
    # Check JitPack status
    check_jitpack_status "$version"
    
    echo ""
    print_success "Release process completed for version $version"
    echo ""
    print_status "Next steps:"
    echo "  1. Monitor JitPack build at: https://jitpack.io"
    echo "  2. Test the published artifact"
    echo "  3. Update documentation if needed"
    echo "  4. Create release notes"
    echo ""
    print_status "JitPack artifact will be available as:"
    echo "  com.github.nimbbl-tech:nimbbl-checkout-core-sdk:$version"
}

# Run main function with all arguments
main "$@" 