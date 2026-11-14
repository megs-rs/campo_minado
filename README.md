# Campo Minado (Java + libGDX)

Reimplementação do Campo Minado clássico em Java 17+ com libGDX e backend LWJGL3.

A especificação funcional completa está em [`spec.md`](spec.md). O andamento da implementação está em [`progresso.md`](progresso.md).

## Status

- Etapas 1–6 concluídas: regras, renderização, HUD, carinha, chord, dificuldades, nível personalizado, escala, recordes persistidos, configurações e empacotamento desktop.
- Versão 1.0 pronta para execução e distribuição como JAR.
- Som e tema escuro são opcionais e não estão implementados nesta versão; as flags já são persistidas em `Settings` para trabalho futuro.

## Requisitos

- JDK 17 ou superior.
- Ambiente gráfico com suporte a OpenGL para executar o jogo.
- Não é necessário instalar Gradle; use o wrapper `./gradlew`.

A primeira execução baixa o Gradle e as dependências do Maven Central.

## Como executar

```bash
./gradlew lwjgl3:run
```

O comando abre o tabuleiro no último nível jogado (Iniciante na primeira execução). Use o botão esquerdo para revelar, o direito para marcar e esquerdo+direito (ou o botão do meio) para o chord.

Atalhos: `F2` reinicia, `F4` abre os recordes, `F5`/`Esc` abre o menu, `1`/`2`/`3` trocam a dificuldade, `C` abre o nível personalizado, `M` alterna interrogações, `X` alterna chord no número e `4`/`5`/`6` mudam a escala da interface.

Ao vencer um nível fixo com tempo menor que o recorde, o jogo pede um nome (até 16 caracteres; vazio usa `Anônimo`). O menu de recordes (`F4`) permite zerar os recordes com confirmação; use `R` ou o botão na tela para abrir a confirmação.

Nos diálogos, `Enter` confirma e `Esc` cancela. No diálogo de nome, digite letras, números, espaço, `-` ou `.` e use `Backspace` para apagar.

## Testes

```bash
./gradlew test
```

Para executar uma classe de teste específica do módulo `core`:

```bash
./gradlew core:test --tests "com.exemplo.minesweeper.model.<TestClass>"
```

Os 42 testes automatizados ficam no módulo `core`, incluindo regras, motor e persistência. O pacote `model` não deve depender de `com.badlogic.gdx`.

Se a execução falhar por causa de uma atualização do JDK em andamento, reinicie o daemon do Gradle com `./gradlew --stop` e rode os testes novamente.

## Estrutura do projeto

```text
core/       lógica, persistência e renderização (com.exemplo.minesweeper)
lwjgl3/     launcher desktop e task de distribuição
assets/     reservado para assets originais ou com licença livre (vazio na v1.0)
spec.md     especificação funcional
progresso.md checklist e histórico de validação das etapas
```

## Etapas

| Etapa | Foco | Status |
|---|---|---|
| 1 | Base executável | Concluída |
| 2 | Núcleo do tabuleiro | Concluída |
| 3 | Regras e estado do jogo | Concluída |
| 4 | Renderização e interação básica | Concluída |
| 5 | HUD, dificuldades e interação completa | Concluída |
| 6 | Persistência, polimento e empacotamento | Concluída |

## Distribuição

O JAR desktop, com as dependências embutidas, é gerado com:

```bash
./gradlew lwjgl3:dist
java -jar lwjgl3/build/libs/campo-minado.jar
```

## Assets

Todos os gráficos, fontes e sons devem ser originais ou ter licença livre. Não copie sprites, ícones ou sons do jogo original da Microsoft. A v1.0 não usa assets externos: o tabuleiro, o HUD e os displays são desenhados com `ShapeRenderer`/`BitmapFont`, e não há sons.

## Licença

A definir antes de uma distribuição pública.
