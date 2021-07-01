#  Desafio: Pix
## KeyManager-gRPC(gerenciador das chaves Pix)

KeyManager-gRPC: micro serviço responsável por fazer todo o gerenciamento das chaves Pix dos nossos clientes (usuários), além de ser o ponto central de comunicação da nossa arquitetura para busca de chaves

O **KeyManager-gRPC** sera exposto publicamente atraves do [KeyManager-REST](https://github.com/fmchagas/orange-talents-04-template-pix-keymanager-rest)

## Começando
Para executar o projeto, será necessário instalar os seguintes programas:

- [Java 11+](https://openjdk.java.net/projects/jdk/11/)
- Docker
- Gradle 7+ (vem integrado com IDE IntelliJ Community)
- Ferramenta [BloomRPC](https://github.com/uw-labs/bloomrpc/releases)
- IDE IntelliJ Community
- PostgreSQL

## Observação
* Framework Micronaut e suas dependência para data/JPA, gRPC, HealthCheck
* Dois serviços rodando em container Docker(emular ERP e Banco Central)