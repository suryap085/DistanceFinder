#!/bin/bash

# Script to generate release keystore for Android app signing
# Run this script to create a new keystore for releasing your app

echo "🔐 Android Release Keystore Generator"
echo "======================================"

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "❌ Error: keytool not found. Please ensure Java is installed and in your PATH."
    exit 1
fi

# Set default values
DEFAULT_ALIAS="location-finder-key"
DEFAULT_VALIDITY="10000"  # ~27 years
DEFAULT_KEYSIZE="2048"
DEFAULT_KEYSTORE_NAME="release-keystore.jks"

echo ""
echo "📝 Please provide the following information:"
echo ""

# Prompt for keystore details
read -p "Keystore filename [$DEFAULT_KEYSTORE_NAME]: " KEYSTORE_NAME
KEYSTORE_NAME=${KEYSTORE_NAME:-$DEFAULT_KEYSTORE_NAME}

read -p "Key alias [$DEFAULT_ALIAS]: " KEY_ALIAS
KEY_ALIAS=${KEY_ALIAS:-$DEFAULT_ALIAS}

read -s -p "Keystore password: " KEYSTORE_PASSWORD
echo ""

read -s -p "Key password (press Enter to use same as keystore): " KEY_PASSWORD
echo ""
if [ -z "$KEY_PASSWORD" ]; then
    KEY_PASSWORD=$KEYSTORE_PASSWORD
fi

read -p "Validity in days [$DEFAULT_VALIDITY]: " VALIDITY
VALIDITY=${VALIDITY:-$DEFAULT_VALIDITY}

echo ""
echo "🏢 Certificate Information:"
read -p "First and Last Name (CN): " CN
read -p "Organizational Unit (OU): " OU
read -p "Organization (O): " O
read -p "City or Locality (L): " L
read -p "State or Province (ST): " ST
read -p "Country Code (C) [US]: " C
C=${C:-US}

# Build the distinguished name
DN="CN=$CN, OU=$OU, O=$O, L=$L, ST=$ST, C=$C"

echo ""
echo "🔧 Generating keystore..."
echo "Keystore: $KEYSTORE_NAME"
echo "Alias: $KEY_ALIAS"
echo "Validity: $VALIDITY days"
echo "DN: $DN"
echo ""

# Generate the keystore
keytool -genkeypair \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize $DEFAULT_KEYSIZE \
    -validity $VALIDITY \
    -keystore "$KEYSTORE_NAME" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "$DN"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore generated successfully!"
    echo ""
    echo "📋 Add these properties to your local.properties file:"
    echo "=================================================="
    echo "RELEASE_STORE_FILE=$(pwd)/$KEYSTORE_NAME"
    echo "RELEASE_STORE_PASSWORD=$KEYSTORE_PASSWORD"
    echo "RELEASE_KEY_ALIAS=$KEY_ALIAS"
    echo "RELEASE_KEY_PASSWORD=$KEY_PASSWORD"
    echo ""
    echo "⚠️  IMPORTANT SECURITY NOTES:"
    echo "• Keep your keystore file and passwords secure"
    echo "• Back up your keystore in a safe location"
    echo "• Never commit keystore or passwords to version control"
    echo "• If you lose this keystore, you cannot update your app on Play Store"
    echo ""
    echo "🚀 You can now build a release APK/AAB with:"
    echo "   ./gradlew bundleRelease"
    echo ""
else
    echo "❌ Error: Failed to generate keystore"
    exit 1
fi