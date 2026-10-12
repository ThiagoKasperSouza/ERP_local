# ERP local

## Setup do backend:
export GOOGLE_CLIENT_ID=<o mesmo do .env> \
  GOOGLE_CLIENT_SECRET=<segredo da tela do cliente OAuth> \
  JWT_SECRET=<qualquer string longa aleatória>
./gradlew bootRun

## Setup do frontend:
cd frontend/erplocal
configurar .env conforme exemplo
npm i
npm run tauri dev