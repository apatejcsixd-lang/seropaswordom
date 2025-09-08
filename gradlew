#!/usr/bin/env sh
set -e

DIST_VER=8.10.2
DIST_NAME="gradle-${DIST_VER}-bin.zip"
DIST_URL="https://services.gradle.org/distributions/${DIST_NAME}"
CACHE_DIR="${HOME}/.gradle/wrapper/dists/gradle-${DIST_VER}/bin"
INSTALL_DIR=".gradle-local/gradle-${DIST_VER}"

# Download + extract Gradle if not present
if [ ! -x "${INSTALL_DIR}/bin/gradle" ]; then
  echo "-> Bootstrapping Gradle ${DIST_VER}..."
  mkdir -p "${INSTALL_DIR}"
  TMP_ZIP="${INSTALL_DIR}/${DIST_NAME}"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "${DIST_URL}" -o "${TMP_ZIP}"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "${DIST_URL}" -O "${TMP_ZIP}"
  else
    echo "ERROR: curl or wget is required." >&2
    exit 1
  fi
  unzip -q "${TMP_ZIP}" -d ".gradle-local"
  rm -f "${TMP_ZIP}"
fi

GRADLE_BIN="${INSTALL_DIR}/bin/gradle"
exec "${GRADLE_BIN}" "$@"
