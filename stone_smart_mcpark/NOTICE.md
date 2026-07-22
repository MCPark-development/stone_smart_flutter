# NOTICE

`stone_smart_mcpark` é uma **obra derivada**.

## Origem

Deriva de dois pacotes de **Jhonathan C. Queiroz**, publicados sob
**BSD 3-Clause License** (ver [`LICENSE`](LICENSE)):

| Pacote original | Papel |
|---|---|
| `stone_smart_flutter` | plugin Flutter (ponte MethodChannel para a SDK Android da Stone) |
| `interface_stone_smart_flutter` | modelos, handlers, tipos e extensões |

Snapshot de origem: commit `aa50f968bb5525f60d6a15206c3d3e80cc24121e` da branch
`feat/update-to-new-flutter` de `github.com/jhonathanqz/stone_smart_flutter`
(11/07/2025 — último commit do upstream em 30/09/2025).

O aviso de copyright e o disclaimer originais são preservados em `LICENSE`,
conforme exigem as cláusulas 1 e 2 da BSD 3-Clause. Em respeito à cláusula 3, o
nome do autor original **não** é usado para endossar ou promover este pacote nem
os produtos da MCPark.

## Por que existe

1. **Continuidade.** O upstream está sem commits desde 30/09/2025 e a versão
   pública foi descontinuada pelo autor.
2. **Supply-chain.** O plugin era consumido por uma *branch* (ponteiro móvel) do
   repositório pessoal de um terceiro, e a interface vinha do pub.dev. Como
   ambos eram `direct main`, qualquer indisponibilidade quebrava
   `flutter pub get` em todo checkout limpo — inclusive o do Windows em
   produção.
3. **Correções de raiz** que exigem controle do código (abaixo).

## Divergências em relação ao original

### Unificação dos pacotes
`interface_stone_smart_flutter` foi absorvido em `stone_smart_mcpark`. Os
modelos eram usados até na camada de domínio do app; manter metade da
integração num pacote de terceiro no pub.dev era risco sem contrapartida.

### O MethodChannel nunca era respondido
No original, `core/StoneSmart.java` tratava `ACTIVE_PINPAD`,
`ACTIVE_PINPAD_CREDENTIALS`, `PAYMENT_ABORT`, `PAYMENT_ABORT_PIX`,
`PAYMENT_CANCEL_TRANSACTION` e **todos** os pagamentos com um `break;` que nunca
chamava `result.success()`. Apenas `getSerialNumber` e `getManufacture`
respondiam.

Consequência: o `await channel.invokeMethod(...)` do lado Dart **jamais
completava**. Aguardar a ativação travava o aplicativo indefinidamente, sem
timeout e sem saída; o cancelamento prendia a interface em "Cancelando...".

Correção: as consultas síncronas retornam cedo com o próprio valor; todo o
restante confirma o **despacho** do comando com `success(true)` num ponto único.
O desfecho real continua chegando pelos callbacks do handler — a arquitetura
orientada a evento não mudou.

### Versão da SDK nativa da Stone
Era fixa em `4.10.2` (12/09/2024). Passou a ser configurável por
`STONE_SDK_VERSION` (variável de ambiente) ou `stoneSdkVersion`
(`local.properties`), com padrão **4.15.0**. A SDK expõe o erro
`SDK_VERSION_IS_OUTDATED`, ou seja, versões antigas podem ser recusadas.

### Toolchain
* Java 11 → **17** — exigência da SDK da Stone a partir da v4.11.1.
* `lintOptions` → `lint` — `lintOptions` foi removido no AGP 8, e o aplicativo
  consumidor usa AGP 8.11.

### Token do PackageCloud
`PACKAGECLOUD_READ_TOKEN_INTERNAL` não tinha valor padrão: ausente, a URL do
Maven virava literalmente `.../priv/null/...`. Agora cai em vazio, como o token
principal, e a falha fica compreensível.

## Namespace

O pacote Android passou de `com.qztech.stone_smart_flutter` para
`br.com.mcpark.stone_smart`.

O identificador do MethodChannel (`"stone_smart_flutter"`) foi **mantido de
propósito**: é protocolo de fio interno, e renomeá-lo introduziria risco de
falha silenciosa — divergência entre os dois lados não gera erro de compilação,
apenas um canal que nunca responde.
