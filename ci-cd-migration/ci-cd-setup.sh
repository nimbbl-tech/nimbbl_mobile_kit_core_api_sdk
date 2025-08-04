#!/bin/bash

# CI/CD Environment Setup Script for Nimbbl SDK
# This script automatically configures the build environment based on CI/CD variables

set -e  # Exit on any error

# Default values
DEFAULT_EVENT_LOG_URL="https://eventlogpipepp.nimbbl.tech/v1/log"
DEFAULT_TENANT_ID="5fd6a596-a39f-4cb2-9a4b-ed72713e537e"

# Environment detection and configuration
configure_environment() {
    local env="${BUILD_ENV:-production}"
    
    case "$env" in
        "dev"|"development")
            echo "🔧 Configuring for DEVELOPMENT environment"
            export EVENT_LOG_URL="${EVENT_LOG_URL:-https://eventlogdev.nimbbl.tech/v1/log}"
            export DEFAULT_TENANT_ID="${DEFAULT_TENANT_ID:-dev-tenant-id}"
            export GRADLE_BUILD_TYPE="assembleDebug"
            ;;
        "pp"|"preprod"|"pre-production")
            echo "🔧 Configuring for PRE-PRODUCTION environment"
            export EVENT_LOG_URL="${EVENT_LOG_URL:-https://eventlogpp.nimbbl.tech/v1/log}"
            export DEFAULT_TENANT_ID="${DEFAULT_TENANT_ID:-5fd6a596-a39f-4cb2-9a4b-ed72713e537e}"
            export GRADLE_BUILD_TYPE="assembleRelease"
            ;;
        "prod"|"production")
            echo "🔧 Configuring for PRODUCTION environment"
            export EVENT_LOG_URL="${EVENT_LOG_URL:-https://eventlogpipepp.nimbbl.tech/v1/log}"
            export DEFAULT_TENANT_ID="${DEFAULT_TENANT_ID:-5fd6a596-a39f-4cb2-9a4b-ed72713e537e}"
            export GRADLE_BUILD_TYPE="assembleRelease"
            ;;
        *)
            echo "⚠️  Unknown environment: $env, using PRODUCTION defaults"
            export EVENT_LOG_URL="${EVENT_LOG_URL:-$DEFAULT_EVENT_LOG_URL}"
            export DEFAULT_TENANT_ID="${DEFAULT_TENANT_ID:-$DEFAULT_TENANT_ID}"
            export GRADLE_BUILD_TYPE="assembleRelease"
            ;;
    esac
    
    echo "📋 Configuration:"
    echo "   Environment: $env"
    echo "   Event Log URL: $EVENT_LOG_URL"
    echo "   Tenant ID: $DEFAULT_TENANT_ID"
    echo "   Build Type: $GRADLE_BUILD_TYPE"
    echo ""
}

# Build function
build_sdk() {
    local build_type="${GRADLE_BUILD_TYPE:-assembleRelease}"
    
    echo "🏗️  Building SDK with $build_type..."
    
    # Build with environment variables
    ./gradlew "$build_type" \
        -PEVENT_LOG_URL="$EVENT_LOG_URL" \
        -PDEFAULT_TENANT_ID="$DEFAULT_TENANT_ID" \
        --no-daemon \
        --stacktrace
    
    echo "✅ Build completed successfully!"
}

# Main execution
main() {
    echo "🚀 Nimbbl SDK CI/CD Build Script"
    echo "=================================="
    echo ""
    
    # Configure environment
    configure_environment
    
    # Build SDK
    build_sdk
    
    echo ""
    echo "🎉 Build process completed!"
}

# Run main function
main "$@" 