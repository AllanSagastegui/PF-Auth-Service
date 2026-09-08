#!/usr/bin/env bash
# ==============================================================================
# Pipeline Script: Build Auth Service Docker image & run Docker Compose
# Usage: ./devops/pipeline.sh [up|down|build|test|logs]
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.yml"
ENV_FILE="${SCRIPT_DIR}/.env"

# Colors for terminal output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

ensure_env() {
    if [ ! -f "${ENV_FILE}" ]; then
        log_warn ".env file not found in devops/. Creating from devops/.env.example..."
        cp "${SCRIPT_DIR}/.env.example" "${ENV_FILE}"
        log_success ".env created."
    fi
}

build_image() {
    log_info "Building Docker image using multi-stage build (devops/Dockerfile)..."
    docker build \
        -f "${SCRIPT_DIR}/Dockerfile" \
        -t auth-service:latest \
        "${ROOT_DIR}"
    log_success "Docker image auth-service:latest built successfully!"
}

start_compose() {
    ensure_env
    log_info "Starting services (PostgreSQL, Kafka, Auth Service) with Docker Compose..."
    docker compose -f "${COMPOSE_FILE}" up -d
    log_info "Waiting for services to become healthy..."

    local timeout=120
    local elapsed=0
    until docker compose -f "${COMPOSE_FILE}" ps | grep -q "(healthy)" || [ $elapsed -ge $timeout ]; do
        sleep 5
        elapsed=$((elapsed + 5))
        echo -n "."
    done
    echo ""

    docker compose -f "${COMPOSE_FILE}" ps
    log_success "Services started!"
}

test_endpoints() {
    log_info "Smoke-testing Auth Service OpenAPI & JWKS endpoints..."
    local port=9090
    if [ -f "${ENV_FILE}" ]; then
        port=$(grep '^PORT=' "${ENV_FILE}" | cut -d '=' -f2 || echo 9090)
    fi

    log_info "Pinging http://localhost:${port}/v3/api-docs..."
    if curl --fail --silent --retry 10 --retry-delay 3 "http://localhost:${port}/v3/api-docs" | grep -q "Ask Auth Service API"; then
        log_success "OpenAPI documentation endpoint is HEALTHY!"
    else
        log_error "Failed to reach OpenAPI endpoint. Showing container logs:"
        docker compose -f "${COMPOSE_FILE}" logs --tail=100
        exit 1
    fi
}

stop_compose() {
    log_info "Stopping and removing Docker Compose containers and networks..."
    docker compose -f "${COMPOSE_FILE}" down
    log_success "Containers stopped."
}

clean_all() {
    log_info "Tearing down containers, volumes and data..."
    docker compose -f "${COMPOSE_FILE}" down -v
    log_success "Environment cleaned."
}

show_logs() {
    docker compose -f "${COMPOSE_FILE}" logs -f
}

# Main Command Dispatcher
COMMAND="${1:-pipeline}"

case "${COMMAND}" in
    build)
        build_image
        ;;
    up)
        start_compose
        ;;
    down)
        stop_compose
        ;;
    clean)
        clean_all
        ;;
    test)
        test_endpoints
        ;;
    logs)
        show_logs
        ;;
    pipeline)
        ensure_env
        build_image
        start_compose
        test_endpoints
        log_success "All pipeline stages completed successfully!"
        ;;
    *)
        echo "Usage: $0 {pipeline|build|up|down|clean|test|logs}"
        exit 1
        ;;
esac
