#!/bin/bash

# Nimbbl SDK Migration Script
# This script migrates the codebase to Bitbucket and sets up JitPack publishing

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

# Function to check if file exists
file_exists() {
    [[ -f "$1" ]]
}

# Function to check if directory exists
dir_exists() {
    [[ -d "$1" ]]
}

# Function to validate Bitbucket URL
validate_bitbucket_url() {
    local url=$1
    if [[ ! $url =~ ^https://bitbucket\.org/[^/]+/[^/]+\.git$ ]]; then
        print_error "Invalid Bitbucket URL format. Expected: https://bitbucket.org/workspace/repository.git"
        return 1
    fi
    return 0
}

# Function to clean repository
clean_repository() {
    print_status "Cleaning repository..."
    
    # Remove sensitive files
    rm -f .env 2>/dev/null || true
    rm -f local.properties 2>/dev/null || true
    rm -rf .idea/ 2>/dev/null || true
    rm -rf build/ 2>/dev/null || true
    rm -rf .gradle/ 2>/dev/null || true
    
    # Check for other sensitive files
    sensitive_files=$(find . -name "*.key" -o -name "*.pem" -o -name "*.p12" -o -name "*.jks" 2>/dev/null || true)
    if [[ -n "$sensitive_files" ]]; then
        print_warning "Found potentially sensitive files:"
        echo "$sensitive_files"
        print_warning "Please review and remove if necessary"
    fi
    
    print_success "Repository cleaned"
}

# Function to update .gitignore
update_gitignore() {
    print_status "Updating .gitignore..."
    
    # Check if .gitignore exists
    if ! file_exists ".gitignore"; then
        print_warning ".gitignore not found, creating new one"
        touch .gitignore
    fi
    
    # Add JitPack-specific exclusions
    cat >> .gitignore << 'EOF'

# JitPack
.jitpack/
jitpack.yml

# Build outputs
build/
.gradle/
*.apk
*.aab
*.aar

# IDE files
.idea/
*.iml
.vscode/

# Local configuration
local.properties
*.local

# Logs
*.log

# OS files
.DS_Store
Thumbs.db

# Temporary files
*.tmp
*.temp
EOF
    
    print_success ".gitignore updated"
}

# Function to verify repository structure
verify_repository_structure() {
    print_status "Verifying repository structure..."
    
    # Check for required files
    required_files=(
        "build.gradle.kts"
        "bitbucket-pipelines.yml"
        "bitbucket-ci-setup.sh"
        "README.md"
        ".jitpack.yml"
        "release.sh"
    )
    
    missing_files=()
    for file in "${required_files[@]}"; do
        if ! file_exists "$file"; then
            missing_files+=("$file")
        fi
    done
    
    if [[ ${#missing_files[@]} -gt 0 ]]; then
        print_error "Missing required files:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        return 1
    fi
    
    print_success "Repository structure verified"
}

# Function to setup Bitbucket remote
setup_bitbucket_remote() {
    local bitbucket_url=$1
    
    print_status "Setting up Bitbucket remote..."
    
    # Check if remote already exists
    if git remote get-url bitbucket >/dev/null 2>&1; then
        print_warning "Bitbucket remote already exists"
        git remote set-url bitbucket "$bitbucket_url"
    else
        git remote add bitbucket "$bitbucket_url"
    fi
    
    # Verify remote
    git remote -v
    
    print_success "Bitbucket remote configured"
}

# Function to push to Bitbucket
push_to_bitbucket() {
    print_status "Pushing code to Bitbucket..."
    
    # Check if we're on main branch
    current_branch=$(git branch --show-current)
    if [[ "$current_branch" != "main" && "$current_branch" != "master" ]]; then
        print_warning "Not on main/master branch. Current branch: $current_branch"
        read -p "Continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Aborted"
            exit 1
        fi
    fi
    
    # Add all files
    git add .
    
    # Check if there are changes to commit
    if [[ -n $(git status --porcelain) ]]; then
        git commit -m "Initial commit: Nimbbl SDK with CI/CD pipeline

- Added comprehensive CI/CD documentation
- Configured Bitbucket Pipelines
- Set up environment-specific configurations
- Added build scripts and utilities
- Configured JitPack publishing"
    else
        print_warning "No changes to commit"
    fi
    
    # Push to Bitbucket
    git push -u bitbucket main
    
    print_success "Code pushed to Bitbucket"
}

# Function to test CI/CD pipeline
test_cicd_pipeline() {
    print_status "Testing CI/CD pipeline..."
    
    # Make a test commit
    echo "# Test commit for CI/CD pipeline - $(date)" >> README.md
    git add README.md
    git commit -m "test: Trigger first CI/CD build"
    git push bitbucket main
    
    print_success "Test commit pushed"
    print_warning "Please check Bitbucket Pipelines for build status"
}

# Function to setup JitPack
setup_jitpack() {
    print_status "Setting up JitPack publishing..."
    
    # Check if .jitpack.yml exists
    if ! file_exists ".jitpack.yml"; then
        print_error ".jitpack.yml not found"
        return 1
    fi
    
    # Check if build.gradle.kts has publishing configuration
    if ! grep -q "publishing" build.gradle.kts; then
        print_warning "Publishing configuration not found in build.gradle.kts"
        print_warning "Please add JitPack publishing configuration"
    fi
    
    print_success "JitPack configuration verified"
    print_warning "Please manually connect repository to JitPack at: https://jitpack.io"
}

# Function to create first release
create_first_release() {
    local version=$1
    
    print_status "Creating first release version $version..."
    
    # Check if release script exists
    if ! file_exists "release.sh"; then
        print_error "release.sh not found"
        return 1
    fi
    
    # Make release script executable
    chmod +x release.sh
    
    # Create release
    ./release.sh "$version"
    
    print_success "First release created"
}

# Function to show next steps
show_next_steps() {
    local bitbucket_url=$1
    
    echo ""
    print_success "Migration completed successfully!"
    echo ""
    print_status "Next steps:"
    echo ""
    echo "1. Bitbucket Setup:"
    echo "   - Go to: $bitbucket_url"
    echo "   - Enable Pipelines in repository settings"
    echo "   - Add repository variables (EVENT_LOG_URL, DEFAULT_TENANT_ID, JITPACK_TOKEN)"
    echo "   - Configure branch permissions"
    echo ""
    echo "2. JitPack Setup:"
    echo "   - Go to: https://jitpack.io"
    echo "   - Sign in with your Bitbucket account"
    echo "   - Add your repository"
    echo "   - Configure build settings"
    echo ""
    echo "3. Test Integration:"
    echo "   - Monitor Bitbucket Pipelines"
    echo "   - Check JitPack build status"
    echo "   - Test published artifact"
    echo ""
    echo "4. Documentation:"
    echo "   - Review migration guides"
    echo "   - Update team documentation"
    echo "   - Share with stakeholders"
    echo ""
    print_status "For detailed instructions, see:"
    echo "  - BITBUCKET_MIGRATION_GUIDE.md"
    echo "  - JITPACK_PUBLISHING_GUIDE.md"
    echo "  - COMPREHENSIVE_CI_CD_DOCUMENTATION.md"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <bitbucket-url> [version] [options]"
    echo ""
    echo "Arguments:"
    echo "  bitbucket-url    Bitbucket repository URL (e.g., https://bitbucket.org/workspace/repo.git)"
    echo "  version          Initial release version (e.g., 3.0.6)"
    echo ""
    echo "Options:"
    echo "  --help           Show this help message"
    echo "  --dry-run        Show what would be done without executing"
    echo "  --skip-release   Skip creating first release"
    echo ""
    echo "Examples:"
    echo "  $0 https://bitbucket.org/myworkspace/nimbbl-sdk.git 3.0.6"
    echo "  $0 https://bitbucket.org/myworkspace/nimbbl-sdk.git --dry-run"
    echo ""
    echo "This script will:"
    echo "  1. Clean the repository"
    echo "  2. Update .gitignore"
    echo "  3. Verify repository structure"
    echo "  4. Setup Bitbucket remote"
    echo "  5. Push code to Bitbucket"
    echo "  6. Test CI/CD pipeline"
    echo "  7. Setup JitPack configuration"
    echo "  8. Create first release (if version provided)"
}

# Function to perform dry run
dry_run() {
    local bitbucket_url=$1
    local version=$2
    
    print_warning "DRY RUN - No changes will be made"
    echo ""
    print_status "Would perform the following actions:"
    echo "  1. Clean repository (remove sensitive files)"
    echo "  2. Update .gitignore with JitPack exclusions"
    echo "  3. Verify repository structure"
    echo "  4. Setup Bitbucket remote: $bitbucket_url"
    echo "  5. Push code to Bitbucket"
    echo "  6. Test CI/CD pipeline"
    echo "  7. Setup JitPack configuration"
    if [[ -n "$version" ]]; then
        echo "  8. Create first release: $version"
    fi
    echo ""
    print_status "To execute these actions, run: $0 $bitbucket_url $version"
}

# Main function
main() {
    local bitbucket_url=""
    local version=""
    local dry_run=false
    local skip_release=false
    
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
            --skip-release)
                skip_release=true
                shift
                ;;
            -*)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
            *)
                if [[ -z "$bitbucket_url" ]]; then
                    bitbucket_url=$1
                elif [[ -z "$version" ]]; then
                    version=$1
                else
                    print_error "Too many arguments"
                    show_usage
                    exit 1
                fi
                shift
                ;;
        esac
    done
    
    # Check if Bitbucket URL is provided
    if [[ -z "$bitbucket_url" ]]; then
        print_error "Bitbucket URL is required"
        show_usage
        exit 1
    fi
    
    # Validate Bitbucket URL
    if ! validate_bitbucket_url "$bitbucket_url"; then
        exit 1
    fi
    
    # Check if required commands exist
    if ! command_exists git; then
        print_error "Git is not installed"
        exit 1
    fi
    
    # Perform dry run if requested
    if [[ "$dry_run" == true ]]; then
        dry_run "$bitbucket_url" "$version"
        exit 0
    fi
    
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi
    
    # Execute migration steps
    clean_repository
    update_gitignore
    verify_repository_structure
    setup_bitbucket_remote "$bitbucket_url"
    push_to_bitbucket
    test_cicd_pipeline
    setup_jitpack
    
    # Create first release if version provided and not skipped
    if [[ -n "$version" && "$skip_release" == false ]]; then
        create_first_release "$version"
    fi
    
    # Show next steps
    show_next_steps "$bitbucket_url"
}

# Run main function with all arguments
main "$@" 