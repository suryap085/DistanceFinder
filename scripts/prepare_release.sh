#!/bin/bash

# Script to prepare the app for Play Store release
# This script performs all necessary checks and builds the release bundle

set -e  # Exit on any error

echo "🚀 Location Finder - Play Store Release Preparation"
echo "=================================================="

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

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

print_status "Starting release preparation checks..."

# 1. Check for required files
print_status "Checking required files..."

REQUIRED_FILES=(
    "local.properties"
    "app/build.gradle.kts"
    "gradle.properties"
    "settings.gradle.kts"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        print_success "✓ $file exists"
    else
        print_error "✗ $file is missing"
        exit 1
    fi
done

# 2. Check for API keys
print_status "Checking API keys..."

if grep -q "MAPS_API_KEY=" local.properties && ! grep -q "YOUR_MAPS_API_KEY_HERE" local.properties; then
    print_success "✓ Maps API key is configured"
else
    print_error "✗ Maps API key is not properly configured in local.properties"
    echo "Please add: MAPS_API_KEY=your_actual_api_key"
    exit 1
fi

# 3. Check for release signing configuration
print_status "Checking release signing configuration..."

SIGNING_PROPS=(
    "RELEASE_STORE_FILE"
    "RELEASE_STORE_PASSWORD"
    "RELEASE_KEY_ALIAS"
    "RELEASE_KEY_PASSWORD"
)

for prop in "${SIGNING_PROPS[@]}"; do
    if grep -q "$prop=" local.properties; then
        print_success "✓ $prop is configured"
    else
        print_warning "⚠ $prop is not configured in local.properties"
        echo "Run ./scripts/generate_keystore.sh to generate a release keystore"
    fi
done

# 4. Clean the project
print_status "Cleaning project..."
./gradlew clean
print_success "✓ Project cleaned"

# 5. Run lint checks
print_status "Running lint checks..."
./gradlew lintRelease
if [ $? -eq 0 ]; then
    print_success "✓ Lint checks passed"
else
    print_warning "⚠ Lint checks have warnings (check app/build/reports/lint-results-release.html)"
fi

# 6. Run unit tests
print_status "Running unit tests..."
./gradlew testReleaseUnitTest
if [ $? -eq 0 ]; then
    print_success "✓ Unit tests passed"
else
    print_error "✗ Unit tests failed"
    exit 1
fi

# 7. Check version code and name
print_status "Checking version information..."
VERSION_CODE=$(grep "versionCode" app/build.gradle.kts | head -1 | sed 's/.*= //' | sed 's/[^0-9]*//g')
VERSION_NAME=$(grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*= "//' | sed 's/".*//')

print_success "✓ Version Code: $VERSION_CODE"
print_success "✓ Version Name: $VERSION_NAME"

# 8. Check target SDK version
TARGET_SDK=$(grep "targetSdk" app/build.gradle.kts | head -1 | sed 's/.*= //' | sed 's/[^0-9]*//g')
if [ "$TARGET_SDK" -ge 35 ]; then
    print_success "✓ Target SDK: $TARGET_SDK (Play Store compliant)"
else
    print_error "✗ Target SDK: $TARGET_SDK (Must be 35+ for Play Store)"
    exit 1
fi

# 9. Build release bundle
print_status "Building release App Bundle (AAB)..."
./gradlew bundleRelease

if [ $? -eq 0 ]; then
    print_success "✓ Release bundle built successfully"
    
    # Find the generated AAB file
    AAB_FILE=$(find app/build/outputs/bundle/release -name "*.aab" | head -1)
    if [ -n "$AAB_FILE" ]; then
        AAB_SIZE=$(du -h "$AAB_FILE" | cut -f1)
        print_success "✓ AAB file: $AAB_FILE ($AAB_SIZE)"
        
        # Create release directory if it doesn't exist
        mkdir -p release
        
        # Copy AAB to release directory with version info
        RELEASE_AAB="release/location-finder-v${VERSION_NAME}-${VERSION_CODE}.aab"
        cp "$AAB_FILE" "$RELEASE_AAB"
        print_success "✓ Release AAB copied to: $RELEASE_AAB"
    fi
else
    print_error "✗ Failed to build release bundle"
    exit 1
fi

# 10. Generate release notes template
print_status "Generating release notes template..."
cat > release/release-notes-v${VERSION_NAME}.txt << EOF
# Release Notes - Location Finder v${VERSION_NAME}

## What's New
- Modernized app with Material Design 3
- Updated to target Android 15 (API 35)
- Improved performance and security
- Enhanced user interface and experience

## Bug Fixes
- Fixed compatibility issues with modern Android versions
- Resolved location permission handling for Android 12+
- Improved app stability and performance

## Technical Updates
- Updated to latest Android development standards
- Migrated to Jetpack Compose for modern UI
- Implemented edge-to-edge design
- Enhanced security with network security configuration

## Requirements
- Android 7.0 (API 24) or higher
- Location permissions for core functionality
- Internet connection for maps and place search

---
Generated on: $(date)
Version Code: ${VERSION_CODE}
Target SDK: ${TARGET_SDK}
EOF

print_success "✓ Release notes template created: release/release-notes-v${VERSION_NAME}.txt"

# 11. Create upload checklist
print_status "Creating Play Store upload checklist..."
cat > release/play-store-checklist.md << EOF
# Play Store Upload Checklist - Location Finder v${VERSION_NAME}

## Pre-Upload Verification
- [ ] AAB file is signed with release keystore
- [ ] Version code (${VERSION_CODE}) is higher than current Play Store version
- [ ] Target SDK is 35 (Android 15) ✓
- [ ] App uses Android App Bundle (AAB) format ✓
- [ ] No debug code or logging in release build ✓

## App Store Listing Requirements
- [ ] App title and description are up to date
- [ ] Screenshots showcase new Material Design 3 UI
- [ ] Feature graphic highlights modern design
- [ ] App icon uses adaptive icon format ✓
- [ ] Privacy Policy is accessible and current

## Technical Requirements
- [ ] 64-bit support enabled ✓
- [ ] ProGuard/R8 optimization enabled ✓
- [ ] Network security configuration implemented ✓
- [ ] Sensitive permissions properly justified
- [ ] App startup time optimized
- [ ] Memory usage optimized

## Testing Verification
- [ ] App tested on Android 7.0 (minSdk 24)
- [ ] App tested on Android 15 (targetSdk 35)
- [ ] Location permissions work on Android 12+
- [ ] Edge-to-edge display works properly
- [ ] Dark theme functions correctly
- [ ] App shortcuts work as expected

## Content Rating & Compliance
- [ ] Content rating questionnaire completed
- [ ] Data safety section filled out
- [ ] Target audience selected appropriately
- [ ] Advertising ID declaration (if applicable)

## Release Management
- [ ] Release notes written and reviewed
- [ ] Staged rollout percentage decided (recommended: 20%)
- [ ] Release timeline communicated to team
- [ ] Rollback plan prepared

## Files Ready for Upload
- [ ] App Bundle: ${RELEASE_AAB}
- [ ] Release notes: release/release-notes-v${VERSION_NAME}.txt
- [ ] Updated screenshots (if UI changed)
- [ ] Updated store listing (if needed)

---
Generated: $(date)
Bundle Location: ${RELEASE_AAB}
EOF

print_success "✓ Upload checklist created: release/play-store-checklist.md"

# 12. Final summary
echo ""
echo "🎉 Release preparation completed successfully!"
echo "============================================"
echo ""
echo "📦 Release Bundle: $RELEASE_AAB"
echo "📝 Release Notes: release/release-notes-v${VERSION_NAME}.txt"
echo "✅ Upload Checklist: release/play-store-checklist.md"
echo ""
echo "🚀 Next Steps:"
echo "1. Review the upload checklist"
echo "2. Test the release bundle on physical devices"
echo "3. Update Play Store listing if needed"
echo "4. Upload to Play Console"
echo "5. Configure staged rollout (recommended: 20%)"
echo ""
echo "⚠️  Remember to:"
echo "• Keep your keystore and passwords secure"
echo "• Back up your release keystore"
echo "• Test thoroughly before releasing"
echo "• Monitor crash reports and reviews after release"
echo ""
print_success "Ready for Play Store upload! 🚀"