#!/bin/bash

# Nimbbl SDK Automated Release Script
# This script handles the complete release process following the dependency hierarchy:
# Core API SDK → WebView SDK → Sample App → Play Store

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

# Function to check JitPack build status
check_jitpack_build() {
    local repo=$1
    local version=$2
    local max_wait=1800  # 30 minutes
    
    print_status "Checking JitPack build status for $repo:$version"
    print_warning "Please check manually at: https://jitpack.io"
    print_warning "Look for: com.github.nimbbl-tech:$repo:$version"
    
    # Wait for build completion (simplified)
    print_status "Waiting for JitPack build completion..."
    sleep 300  # 5 minutes
}

# Function to update dependency version
update_dependency() {
    local file=$1
    local old_version=$2
    local new_version=$3
    
    print_status "Updating dependency in $file"
    print_status "Changing from $old_version to $new_version"
    
    # Update version in build.gradle.kts
    sed -i.bak "s/$old_version/$new_version/g" "$file"
    
    # Remove backup file
    rm -f "${file}.bak"
    
    print_success "Dependency updated"
}

# Function to release Core API SDK
release_core_api_sdk() {
    local version=$1
    
    print_status "=== Releasing Core API SDK v$version ==="
    
    # Navigate to Core API SDK
    cd nimbbl_mobile_kit_core_api_sdk
    
    # Check if release script exists
    if [[ ! -f "release.sh" ]]; then
        print_error "release.sh not found in Core API SDK"
        exit 1
    fi
    
    # Make release script executable
    chmod +x release.sh
    
    # Create release
    ./release.sh "$version"
    
    # Check JitPack build
    check_jitpack_build "nimbbl-checkout-core-sdk" "$version"
    
    print_success "Core API SDK v$version released"
}

# Function to release WebView SDK
release_webview_sdk() {
    local version=$1
    local core_sdk_version=$2
    
    print_status "=== Releasing WebView SDK v$version ==="
    
    # Navigate to WebView SDK
    cd ../nimbbl_mobile_kit_android_webview_sdk
    
    # Check if release script exists
    if [[ ! -f "release.sh" ]]; then
        print_error "release.sh not found in WebView SDK"
        exit 1
    fi
    
    # Update Core API SDK dependency
    update_dependency "build.gradle.kts" "nimbbl-checkout-core-sdk:3.0.6" "nimbbl-checkout-core-sdk:$core_sdk_version"
    
    # Make release script executable
    chmod +x release.sh
    
    # Create release
    ./release.sh "$version"
    
    # Check JitPack build
    check_jitpack_build "nimbbl-checkout-webview-sdk" "$version"
    
    print_success "WebView SDK v$version released"
}

# Function to release Sample App
release_sample_app() {
    local version=$1
    local webview_sdk_version=$2
    
    print_status "=== Releasing Sample App v$version ==="
    
    # Navigate to Sample App
    cd ../nimbbl_mobile_android_sample_app
    
    # Check if release script exists
    if [[ ! -f "release.sh" ]]; then
        print_error "release.sh not found in Sample App"
        exit 1
    fi
    
    # Update WebView SDK dependency
    update_dependency "app/build.gradle.kts" "nimbbl-checkout-webview-sdk:2.0.0" "nimbbl-checkout-webview-sdk:$webview_sdk_version"
    
    # Make release script executable
    chmod +x release.sh
    
    # Create release
    ./release.sh "$version"
    
    print_success "Sample App v$version released"
}

# Function to deploy to Play Store
deploy_to_play_store() {
    local track=$1
    local release_notes=$2
    
    print_status "=== Deploying to Play Store $track track ==="
    
    # Navigate to Sample App
    cd ../nimbbl_mobile_android_sample_app
    
    # Check if deployment script exists
    if [[ ! -f "deploy-to-play-store.sh" ]]; then
        print_error "deploy-to-play-store.sh not found in Sample App"
        exit 1
    fi
    
    # Make deployment script executable
    chmod +x deploy-to-play-store.sh
    
    # Deploy to Play Store
    ./deploy-to-play-store.sh "$track" "$release_notes"
    
    print_success "Deployed to Play Store $track track"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <core-sdk-version> <webview-sdk-version> <sample-app-version> [options]"
    echo ""
    echo "Arguments:"
    echo "  core-sdk-version    Core API SDK version (e.g., 3.0.7)"
    echo "  webview-sdk-version WebView SDK version (e.g., 2.0.1)"
    echo "  sample-app-version  Sample App version (e.g., 1.0.1)"
    echo ""
    echo "Options:"
    echo "  --help              Show this help message"
    echo "  --dry-run           Show what would be done without executing"
    echo "  --skip-play-store   Skip Play Store deployment"
    echo "  --track <track>     Play Store track (default: internal)"
    echo "  --release-notes <notes> Release notes for Play Store"
    echo ""
    echo "Examples:"
    echo "  $0 3.0.7 2.0.1 1.0.1"
    echo "  $0 3.0.7 2.0.1 1.0.1 --track alpha"
    echo "  $0 3.0.7 2.0.1 1.0.1 --skip-play-store"
    echo ""
    echo "This script will:"
    echo "  1. Release Core API SDK"
    echo "  2. Wait for JitPack build completion"
    echo "  3. Update WebView SDK dependency"
    echo "  4. Release WebView SDK"
    echo "  5. Wait for JitPack build completion"
    echo "  6. Update Sample App dependency"
    echo "  7. Release Sample App"
    echo "  8. Deploy to Play Store (if not skipped)"
}

# Function to perform dry run
dry_run() {
    local core_version=$1
    local webview_version=$2
    local sample_version=$3
    local track=$4
    local release_notes=$5
    
    print_warning "DRY RUN - No changes will be made"
    echo ""
    print_status "Would perform the following actions:"
    echo "  1. Release Core API SDK: v$core_version"
    echo "  2. Wait for JitPack build completion"
    echo "  3. Update WebView SDK dependency to Core API SDK v$core_version"
    echo "  4. Release WebView SDK: v$webview_version"
    echo "  5. Wait for JitPack build completion"
    echo "  6. Update Sample App dependency to WebView SDK v$webview_version"
    echo "  7. Release Sample App: v$sample_version"
    if [[ "$skip_play_store" == false ]]; then
        echo "  8. Deploy to Play Store $track track"
        echo "     Release notes: $release_notes"
    fi
    echo ""
    print_status "To execute these actions, run:"
    echo "  $0 $core_version $webview_version $sample_version"
}

# Main function
main() {
    local core_version=""
    local webview_version=""
    local sample_version=""
    local dry_run=false
    local skip_play_store=false
    local track="internal"
    local release_notes="Automated release with Nimbbl SDK integration"
    
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
            --skip-play-store)
                skip_play_store=true
                shift
                ;;
            --track)
                track="$2"
                shift 2
                ;;
            --release-notes)
                release_notes="$2"
                shift 2
                ;;
            -*)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
            *)
                if [[ -z "$core_version" ]]; then
                    core_version=$1
                elif [[ -z "$webview_version" ]]; then
                    webview_version=$1
                elif [[ -z "$sample_version" ]]; then
                    sample_version=$1
                else
                    print_error "Too many arguments"
                    show_usage
                    exit 1
                fi
                shift
                ;;
        esac
    done
    
    # Check if all versions are provided
    if [[ -z "$core_version" || -z "$webview_version" || -z "$sample_version" ]]; then
        print_error "All three versions are required"
        show_usage
        exit 1
    fi
    
    # Validate versions
    validate_version "$core_version"
    validate_version "$webview_version"
    validate_version "$sample_version"
    
    # Check if required commands exist
    if ! command_exists git; then
        print_error "Git is not installed"
        exit 1
    fi
    
    # Perform dry run if requested
    if [[ "$dry_run" == true ]]; then
        dry_run "$core_version" "$webview_version" "$sample_version" "$track" "$release_notes"
        exit 0
    fi
    
    # Check if we're in the correct directory
    if [[ ! -d "nimbbl_mobile_kit_core_api_sdk" ]]; then
        print_error "Please run this script from the root directory containing all SDK projects"
        exit 1
    fi
    
    print_status "Starting automated release process..."
    echo ""
    print_status "Release Configuration:"
    echo "  Core API SDK: v$core_version"
    echo "  WebView SDK: v$webview_version"
    echo "  Sample App: v$sample_version"
    if [[ "$skip_play_store" == false ]]; then
        echo "  Play Store Track: $track"
        echo "  Release Notes: $release_notes"
    fi
    echo ""
    
    # Execute release process
    release_core_api_sdk "$core_version"
    echo ""
    
    release_webview_sdk "$webview_version" "$core_version"
    echo ""
    
    release_sample_app "$sample_version" "$webview_version"
    echo ""
    
    # Deploy to Play Store if not skipped
    if [[ "$skip_play_store" == false ]]; then
        deploy_to_play_store "$track" "$release_notes"
        echo ""
    fi
    
    print_success "Automated release process completed successfully!"
    echo ""
    print_status "Summary:"
    echo "  ✅ Core API SDK v$core_version released"
    echo "  ✅ WebView SDK v$webview_version released"
    echo "  ✅ Sample App v$sample_version released"
    if [[ "$skip_play_store" == false ]]; then
        echo "  ✅ Deployed to Play Store $track track"
    fi
    echo ""
    print_status "Next steps:"
    echo "  1. Monitor JitPack builds at: https://jitpack.io"
    echo "  2. Test published artifacts"
    echo "  3. Monitor Play Store deployment"
    echo "  4. Update documentation if needed"
}

# Run main function with all arguments
main "$@" 