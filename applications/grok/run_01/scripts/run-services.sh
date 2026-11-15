#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

mvn -q -pl order-service spring-boot:run &
mvn -q -pl payment-service spring-boot:run &
mvn -q -pl inventory-service spring-boot:run &
mvn -q -pl notification-service spring-boot:run &
wait
