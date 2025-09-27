#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OPS_DIR="$ROOT_DIR/drone-sim-environment-next/ops"

echo "==> Configurando ambiente de desenvolvimento do Drone Simulator"

if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

if [ ! -f "$OPS_DIR/dev.env" ]; then
    echo "==> Criando arquivo dev.env a partir do exemplo"
    cp "$OPS_DIR/dev.env.example" "$OPS_DIR/dev.env"
else
    echo "✅ Arquivo dev.env já existe"
fi

echo "==> Subindo PostgreSQL..."
cd "$OPS_DIR"
docker-compose --env-file dev.env up -d postgres

echo "==> Aguardando PostgreSQL ficar pronto..."
sleep 5

if docker-compose --env-file dev.env ps postgres | grep -q "healthy"; then
    echo "✅ PostgreSQL está rodando e saudável"
else
    echo "⏳ PostgreSQL ainda está inicializando..."
fi

echo "==> Ambiente configurado!"
echo ""
echo "📋 Próximos passos:"
echo "1. Execute 'mvn spring-boot:run' na raiz do projeto para rodar o backend"
echo "2. Acesse http://localhost:8080 para testar o backend"
echo "3. Para parar o ambiente: docker-compose --env-file dev.env down"