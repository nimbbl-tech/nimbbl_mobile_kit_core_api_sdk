#!/bin/bash

# Bitbucket CI/CD Setup Script for Nimbbl SDK
# This script helps configure and run builds for different environments

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
DEFAULT_EVENT_LOG_URL="https://eventlogpipepp.nimbbl.tech/v1/log"
DEFAULT_TENANT_ID="5fd6a596-a39f-4cb2-9a4b-ed72713e537e"

# Environment configurations
ENVIRONMENTS=(
    "development:assembleDebug:https://eventlogdev.nimbbl.tech/v1/log:dev-tenant-id"
    "pre-production:assembleRelease:https://eventlogpp.nimbbl.tech/v1/log:5fd6a596-a39f-4cb2-9a4b-ed72713e537e"
    "production:assembleRelease:https://eventlogpipepp.nimbbl.tech/v1/log:5fd6a596-a39f-4cb2-9a4b-ed72713e537e"
)

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

# Function to show available environments
show_environments() {
    echo "Available environments:"
    echo "======================"
    for env_config in "${ENVIRONMENTS[@]}"; do
        IFS=':' read -r env_name build_type event_url tenant_id <<< "$env_config"
        echo "  • $env_name"
        echo "    - Build Type: $build_type"
        echo "    - Event Log URL: $event_url"
        echo "    - Tenant ID: $tenant_id"
        echo ""
    done
}

# Function to configure environment
configure_environment() {
    local env_name="$1"
    
    # Find environment configuration
    for env_config in "${ENVIRONMENTS[@]}"; do
        IFS=':' read -r name build_type event_url tenant_id <<< "$env_config"
        if [[ "$name" == "$env_name" ]]; then
            export GRADLE_BUILD_TYPE="$build_type"
            export EVENT_LOG_URL="$event_url"
            export DEFAULT_TENANT_ID="$tenant_id"
            
            print_success "Configured for $env_name environment"
            echo "  Build Type: $build_type"
            echo "  Event Log URL: $event_url"
            echo "  Tenant ID: $tenant_id"
            return 0
        fi
    done
    
    print_error "Unknown environment: $env_name"
    return 1
}

# Function to build SDK
build_sdk() {
    local build_type="${GRADLE_BUILD_TYPE:-assembleRelease}"
    
    print_status "Building SDK with $build_type..."
    
    # Build with environment variables
    ./gradlew "$build_type" \
        -PEVENT_LOG_URL="$EVENT_LOG_URL" \
        -PDEFAULT_TENANT_ID="$DEFAULT_TENANT_ID" \
        --no-daemon \
        --stacktrace
    
    print_success "Build completed successfully!"
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    ./gradlew test --no-daemon
    print_success "Tests completed!"
}

# Function to show Bitbucket Pipelines usage
show_bitbucket_usage() {
    echo ""
    echo "Bitbucket Pipelines Usage:"
    echo "=========================="
    echo ""
    echo "1. Automatic Triggers:"
    echo "   • Default branch (main/master): Production build"
    echo "   • Develop branch: Development + Pre-Production builds"
    echo "   • Pull Requests: Development build"
    echo ""
    echo "2. Manual Triggers (Custom Pipelines):"
    echo "   • build-development: Development environment"
    echo "   • build-pre-production: Pre-Production environment"
    echo "   • build-production: Production environment"
    echo "   • build-all-environments: All environments"
    echo ""
    echo "3. Environment Variables in Bitbucket:"
    echo "   Go to Repository Settings > Pipelines > Repository variables"
    echo "   Add these variables if you want to override defaults:"
    echo "   • EVENT_LOG_URL"
    echo "   • DEFAULT_TENANT_ID"
    echo "   • GRADLE_BUILD_TYPE"
    echo ""
}

# Function to validate setup
validate_setup() {
    print_status "Validating setup..."
    
    # Check if gradlew exists
    if [[ ! -f "gradlew" ]]; then
        print_error "gradlew not found. Make sure you're in the correct directory."
        return 1
    fi
    
    # Check if bitbucket-pipelines.yml exists
    if [[ ! -f "bitbucket-pipelines.yml" ]]; then
        print_error "bitbucket-pipelines.yml not found."
        return 1
    fi
    
    # Make gradlew executable
    chmod +x gradlew
    
    print_success "Setup validation passed!"
}

# Main function
main() {
    echo "🚀 Nimbbl SDK Bitbucket CI/CD Setup"
    echo "===================================="
    echo ""
    
    # Parse command line arguments
    case "${1:-help}" in
        "dev"|"development")
            configure_environment "development"
            build_sdk
            run_tests
            ;;
        "pp"|"pre-production")
            configure_environment "pre-production"
            build_sdk
            run_tests
            ;;
        "prod"|"production")
            configure_environment "production"
            build_sdk
            run_tests
            ;;
        "all")
            for env_config in "${ENVIRONMENTS[@]}"; do
                IFS=':' read -r env_name build_type event_url tenant_id <<< "$env_config"
                echo ""
                echo "Building for $env_name..."
                configure_environment "$env_name"
                build_sdk
            done
            run_tests
            ;;
        "validate")
            validate_setup
            ;;
        "environments"|"env")
            show_environments
            ;;
        "bitbucket"|"pipeline")
            show_bitbucket_usage
            ;;
        "help"|*)
            echo "Usage: $0 [command]"
            echo ""
            echo "Commands:"
            echo "  dev, development     Build for development environment"
            echo "  pp, pre-production  Build for pre-production environment"
            echo "  prod, production    Build for production environment"
            echo "  all                 Build for all environments"
            echo "  validate            Validate the setup"
            echo "  environments, env   Show available environments"
            echo "  bitbucket, pipeline Show Bitbucket Pipelines usage"
            echo "  help                Show this help message"
            echo ""
            show_environments
            ;;
    esac
}

# Run main function
main "$@" 