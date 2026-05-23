# 🎫 TicketSecure - Sistema Desacoplado de Venda de Ingressos e Antifraude

O **TicketSecure** é uma plataforma distribuída, assíncrona e de alta performance para venda de ingressos e gestão de eventos. O sistema foi desenhado seguindo os conceitos de microsserviços e arquitetura orientada a eventos (EDA), garantindo consistência de dados, prevenção contra overbooking e análise inteligente de risco em tempo real.

---

## 🏗️ Arquitetura do Sistema

O projeto é dividido em três módulos independentes que se comunicam de forma assíncrona através de mensageria e APIs REST:

1. **Backend Core (Java & Spring Boot 4):** Gerencia as regras de negócio, persistência de dados, controle de lotes, emissão de ingressos com hashes criptográficos e orquestração de pedidos.
2. **Cérebro Antifraude (Python):** Microsserviço assíncrono que consome dados da fila de transações, simulando uma rede neural para análise de risco em tempo L7 (IP, User-Agent).
3. **Frontend (React.js & Tailwind CSS v4):** Interface do usuário reativa e otimizada via Vite, contendo uma vitrine de eventos e um Chatbot inteligente integrado para busca conversacional.

---

## 🛠️ Tecnologias e Ferramentas Utilizadas

### Backend Core
* **Java 24** & **Spring Boot 4.0.x** (Spring Framework 7)
* **Spring Data JPA** & **Hibernate 7.2.x**
* **Spring Security** (Configuração customizada de CORS e liberação de rotas)
* **PostgreSQL** (Banco de dados principal)
* **RabbitMQ** (Broker de mensageria assíncrona)
* **Jackson 3 (JsonMapper)** (Tratamento e serialização ultra-rápida de JSON)
* **Groq API & OpenRouter** (Integração com LLMs - Llama 3.1)

### Cérebro AI (Microsserviço)
* **Python 3.x**
* **Pika** (Cliente oficial do RabbitMQ para Python)

### Frontend Moderno
* **React.js** (Variant JavaScript puro focado em performance de estado)
* **Vite** (Build tool de última geração)
* **Tailwind CSS v4** (Novo motor de estilos baseado em CSS-first nativo no Vite)
* **Axios** & **Lucide React** (Comunicação HTTP e iconografia minimalista)

---

## 🚀 Funcionalidades Principais Implementadas

* **Garantia Antioverbooking:** Controle estrito de concorrência de estoque através de transações isoladas (`@Transactional`) na reserva de lotes.
* **Rotina Automática de Expiração:** Scheduler integrado (`@Scheduled` + `@EnableScheduling`) que varre o banco a cada 1 minuto, liberando ingressos travados em reservas não pagas após 15 minutos.
* **Processamento Assíncrono de Pagamentos:** Desacoplamento total via RabbitMQ. O cliente recebe resposta imediata (HTTP 200) enquanto a análise ocorre em segundo plano.
* **Barreira de Resiliência de IA (Cascata de Modelos):** Mecanismo de fallback inteligente no backend que consome o modelo estável `llama-3.1-8b-instant` via Groq API.
* **Chatbot com Rolagem Inteligente:** Caixa de conversa que limita o crescimento vertical e força o scroll automático para o final à medida que novas interações acontecem.

---

## 🔧 Como Executar o Projeto

### Pré-requisitos
* Docker e Docker Compose instalados
* JDK 24
* Node.js v22+ e Python 3

### 1. Subindo a Infraestrutura (Banco e Mensageria)
Na raiz do projeto, execute o Docker para subir o PostgreSQL e o RabbitMQ:
```bash
docker compose up -d
