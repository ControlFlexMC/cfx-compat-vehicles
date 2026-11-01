#!/usr/bin/env bash
# Build the NeoForge (MC 1.21.1) artifact for cfx-compat-vehicles.
# Mirrors cfx-compat-create/tools/build-neoforge.sh, adjusted for this repository's
# single-project build (no :neoforge: subproject) and its own jar name.
#
# Usage:
#   ./tools/build-neoforge.sh
#   ./tools/build-neoforge.sh --release
#   ./tools/build-neoforge.sh --clean
#
# Environment:
#   MC_1_21_1_NEOFORGE_PATH  instance dir; unset -> skip instance deploy (WARNING)
#   RELEASE_DEPLOY_PATH   extra copy dest on --release; unset -> WARNING
#
# This branch's build.gradle has no release switch, so debug and release builds share
# one jar name; --release only turns on the release copy.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

RELEASE=false
CLEAN=false
EXTRA_ARGS=()

for arg in "$@"; do
    case "$arg" in
        --release) RELEASE=true ;;
        --clean)   CLEAN=true ;;
        *)         EXTRA_ARGS+=("$arg") ;;
    esac
done

mod_version=$(grep -E '^mod_version=' gradle.properties | cut -d= -f2)
mc_ver=$(grep -E '^minecraft_version=' gradle.properties | cut -d= -f2)
label="${mod_version}"

if $RELEASE; then build_type="Release"; else build_type="Debug"; fi
echo "==> ${build_type} NeoForge build: version ${label}"

if $CLEAN; then
    echo "==> Cleaning first..."
    ./gradlew clean
fi

gradle_cmd=(./gradlew build)
if [[ ${#EXTRA_ARGS[@]} -gt 0 ]]; then gradle_cmd+=("${EXTRA_ARGS[@]}"); fi
echo "==> Running: ${gradle_cmd[*]}"
"${gradle_cmd[@]}"

NEW_JAR="${PROJECT_DIR}/build/libs/cfx-compat-vehicles-${label}-mc${mc_ver}-neoforge.jar"
if [[ ! -f "${NEW_JAR}" ]]; then
    echo "JAR not found: ${NEW_JAR}"
    exit 1
fi

echo ""
echo "Build complete: $(basename "${NEW_JAR}")"

MC_INSTANCE="${MC_1_21_1_NEOFORGE_PATH:-}"
MODS_DIR="${MC_INSTANCE}/mods"

if [[ -z "${MC_INSTANCE}" ]]; then
    echo "MC_1_21_1_NEOFORGE_PATH is not set; skipping instance deploy"
    echo "   artifact: ${NEW_JAR}"
else
    LOGS_DIR="${MC_INSTANCE}/logs"
    if [[ -d "${LOGS_DIR}" ]]; then
        rm -rf "${LOGS_DIR:?}"/*
        echo "  cleared logs: ${LOGS_DIR}"
    fi

    CRASH_DIR="${MC_INSTANCE}/crash-reports"
    if [[ -d "${CRASH_DIR}" ]]; then
        rm -rf "${CRASH_DIR:?}"/*
        echo "  cleared crash-reports: ${CRASH_DIR}"
    fi

    if [[ -d "${MODS_DIR}" ]]; then
        rm -f "${MODS_DIR}"/cfx-compat-vehicles-*-mc*-neoforge.jar
        echo "  removed old cfx-compat-vehicles NeoForge jars"
    fi

    mkdir -p "${MODS_DIR}"
    cp "${NEW_JAR}" "${MODS_DIR}/"
    echo "  deployed: ${MODS_DIR}/$(basename "${NEW_JAR}")"
fi

if $RELEASE; then
    if [[ -n "${RELEASE_DEPLOY_PATH:-}" ]]; then
        mkdir -p "${RELEASE_DEPLOY_PATH}"
        cp "${NEW_JAR}" "${RELEASE_DEPLOY_PATH}/"
        echo "  release copy: ${RELEASE_DEPLOY_PATH}/$(basename "${NEW_JAR}")"
    else
        echo "RELEASE_DEPLOY_PATH is not set; skipping release copy"
    fi
fi
