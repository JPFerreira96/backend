# Makefile para User Card CRUD Application
# Uso: make <comando>

.PHONY: help clean build test run-auth run-user run-card run-gateway run-all stop reset-db

help: ## Mostra esta ajuda
	@echo "Comandos disponíveis:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

clean: ## Limpa arquivos compilados
	@echo "🧹 Limpando arquivos compilados..."
	@mvn clean -q

build: ## Compila todos os serviços
	@echo "🔨 Compilando todos os serviços..."
	@mvn compile -q

test: ## Executa todos os testes
	@echo "🧪 Executando testes..."
	@mvn test

run-auth: ## Inicia o auth-service (porta 8081)
	@echo "🚀 Iniciando auth-service..."
	@cd auth-service && mvn spring-boot:run

run-user: ## Inicia o user-service (porta 8082)
	@echo "🚀 Iniciando user-service..."
	@cd user-service && mvn spring-boot:run

run-card: ## Inicia o card-service (porta 8083)
	@echo "🚀 Iniciando card-service..."
	@cd card-service && mvn spring-boot:run

run-gateway: ## Inicia o gateway (porta 8080)
	@echo "🚀 Iniciando gateway..."
	@cd gateway && mvn spring-boot:run

reset-db: ## Limpa migrações Flyway (resolve problemas de migração)
	@echo "🗄️ Limpando migrações Flyway..."
	@echo "Limpando card-service..."
	@cd card-service && mvn flyway:clean -q
	@echo "Limpando user-service..."
	@cd user-service && mvn flyway:clean -q
	@echo "✅ Migrações limpas! Agora você pode executar os serviços."

install: ## Instala dependências de todos os serviços
	@echo "📦 Instalando dependências..."
	@mvn install -DskipTests -q