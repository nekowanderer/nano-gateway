#!/bin/bash

set -e

# === Path Setup ===
KEYCLOAK_DIR="$(dirname "$(dirname "$0")")"
KEYSTORE_FILE="${KEYCLOAK_DIR}/keystore/nano_keycloak_key.keystore"
CERT_FILE="${KEYCLOAK_DIR}/keystore/keycloak-cert.pem"
TRUSTSTORE_FILE="${KEYCLOAK_DIR}/keystore/keycloak-truststore.p12"

# Path to Quarkus project
QUARKUS_DIR="${KEYCLOAK_DIR}/../../gateway-api"
QUARKUS_TRUSTSTORE_DIR="${QUARKUS_DIR}/src/main/resources/keystore"
QUARKUS_CERT_FILE="${QUARKUS_TRUSTSTORE_DIR}/keycloak-cert.pem"
QUARKUS_TRUSTSTORE_FILE="${QUARKUS_TRUSTSTORE_DIR}/keycloak-truststore.p12"

# === Config Parameters ===
ALIAS="nano_keycloak_key"
STORE_PASS="admin123"
TRUSTSTORE_PASS="changeit"

# === Cleanup old files if they exist ===
[ -f "${CERT_FILE}" ] && echo "🧹 Removing existing certificate: ${CERT_FILE}" && rm -f "${CERT_FILE}"
[ -f "${TRUSTSTORE_FILE}" ] && echo "🧹 Removing existing truststore: ${TRUSTSTORE_FILE}" && rm -f "${TRUSTSTORE_FILE}"

echo "🔑 Exporting public certificate from keystore as PEM..."
keytool -exportcert \
  -alias "${ALIAS}" \
  -keystore "${KEYSTORE_FILE}" \
  -storetype PKCS12 \
  -storepass "${STORE_PASS}" \
  -rfc \
  -file "${CERT_FILE}"

echo "📦 Creating truststore..."
keytool -importcert \
  -alias keycloak_cert \
  -file "${CERT_FILE}" \
  -keystore "${TRUSTSTORE_FILE}" \
  -storetype PKCS12 \
  -storepass "${TRUSTSTORE_PASS}" \
  -noprompt

# === Cleanup and copy to Quarkus project ===
echo "📁 Preparing Quarkus keystore directory..."

if [ -d "${QUARKUS_TRUSTSTORE_DIR}" ]; then
  echo "🧹 Removing existing Quarkus keystore directory: ${QUARKUS_TRUSTSTORE_DIR}"
  rm -rf "${QUARKUS_TRUSTSTORE_DIR}"
fi

mkdir -p "${QUARKUS_TRUSTSTORE_DIR}"

echo "📦 Copying truststore and cert to Quarkus project..."
cp "${CERT_FILE}" "${QUARKUS_CERT_FILE}"
cp "${TRUSTSTORE_FILE}" "${QUARKUS_TRUSTSTORE_FILE}"

echo "✅ Truststore created and copied to: ${QUARKUS_TRUSTSTORE_FILE}"
