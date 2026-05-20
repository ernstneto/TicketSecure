## 🏗️ Arquitetura do Sistema

O diagrama abaixo ilustra a comunicação entre os componentes da aplicação:

```mermaid
graph TD;
    Client([Cliente / Frontend]) -->|Requisições HTTP| API[API REST Spring Boot]
    
    subgraph Backend [Ecossistema Backend (Docker)]
        API -->|Leitura/Escrita| DB[(PostgreSQL)]
        API -->|Publica Mensagens| MQ[[RabbitMQ]]
        MQ -->|Consome Mensagens| Worker[Worker / Microsserviço]
    end
    
    style API fill:#6db33f,stroke:#333,stroke-width:2px,color:#fff
    style DB fill:#336791,stroke:#333,stroke-width:2px,color:#fff
    style MQ fill:#ff6600,stroke:#333,stroke-width:2px,color:#fff
