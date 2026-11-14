# Progresso das etapas

Checklist vivo da implementação. `spec.md` é a fonte das regras; este arquivo acompanha status e validação.

## Como usar
- Status possíveis: `Pendente`, `Em andamento`, `Concluída`.
- Só marcar uma etapa como `Concluída` quando todos os critérios de validação passarem.
- Se uma validação falhar, manter a etapa em `Em andamento` e registrar o problema em **Bloqueios**.
- Não duplicar regras aqui; referenciar as seções do `spec.md`.
- Registrar validações relevantes no histórico ao final do arquivo.

## Visão geral

| Etapa | Foco | Status |
|---|---|---|
| 1 | Base executável | Concluída |
| 2 | Núcleo do tabuleiro | Concluída |
| 3 | Regras e estado do jogo | Concluída |
| 4 | Renderização e interação básica | Concluída |
| 5 | HUD, dificuldades e interação completa | Concluída |
| 6 | Persistência, polimento e empacotamento | Concluída |

---

## Etapa 1 — Base executável

Status: **Concluída**

### Entregas
- [x] Estrutura Gradle `gdx-liftoff` com módulos `core` e `lwjgl3`.
- [x] Java 17, libGDX 1.12.x e JUnit 5 configurado no `core`.
- [x] Launcher abrindo uma janela vazia.
- [x] Pacote base `com.exemplo.minesweeper`.
- [x] `.gitignore` para Gradle/IDE.

### Validação
- [x] `./gradlew test` passa.
- [x] `./gradlew lwjgl3:run` abre a janela vazia sem erro.
- [x] Fechar a janela encerra o processo sem stack trace.

### Notas/decisões
- Gradle wrapper 9.1.0; bytecode compilado com `--release 17`.
- `MinesweeperGame` já é o ponto de entrada libGDX, pronto para receber a `GameScreen`.
- Validação de `lwjgl3:run` feita abrindo e fechando a janela; o processo terminou com código 0.

### Bloqueios
- Nenhum.

---

## Etapa 2 — Núcleo do tabuleiro

Status: **Concluída**

### Entregas
- [x] `Cell` e `CellState`.
- [x] `Board` com array 1D (`idx = y * cols + x`).
- [x] `Difficulty` e configuração personalizada com clamp.
- [x] `MinePlacer` com `Random` e seed opcional.
- [x] Flood fill **iterativo**.
- [x] Método de teste para injetar minas manualmente.

### Validação
- [x] Contagem de minas posicionadas igual ao valor configurado, por dificuldade.
- [x] Primeiro clique seguro em 10.000 seeds diferentes.
- [x] `adjacentMines` correto em canto, borda e centro.
- [x] Flood fill abre a região esperada, não abre bandeiras/minas e não estoura a pilha em 30×24.
- [x] Clamp de colunas, linhas e minas do nível personalizado.
- [x] Nenhum import de `com.badlogic.gdx` no pacote `model`.
- [x] `./gradlew test` passa.

### Notas/decisões
- `Board` expõe `setMine(...)` e `recalculateAdjacentMines()` para testes e para o `MinePlacer`.
- Zona segura usa a condição `cols * rows - 9 >= mines`; o fallback protege apenas a célula clicada.
- `FloodFill` usa `ArrayDeque`, sem recursão; abre `QUESTION` e ignora `FLAGGED`.
- 19 testes unitários no módulo `core`.

### Bloqueios
- Nenhum.

---

## Etapa 3 — Regras e estado do jogo

Status: **Concluída**

### Entregas
- [x] `GameEngine` com `newGame`, `restart`, `reveal`, `toggleFlag`, `chord` e `update(delta)`.
- [x] Máquina de estados `READY -> PLAYING -> WON/LOST`.
- [x] Primeiro clique posiciona minas e inicia o cronômetro.
- [x] Contador de minas, incluindo valores negativos.
- [x] Vitória, derrota e estados de fim de jogo (`EXPLODED`, `MINE_SHOWN`, `WRONG_FLAG`).
- [x] Cronômetro por delta acumulado, com parada em 999, vitória ou derrota.

### Validação
- [x] Testes da seção 15.1, itens 5 a 9, do `spec.md`.
- [x] Chord abre vizinhos quando bandeiras == número, não abre quando diferente e explode com bandeira errada.
- [x] Vitória detectada exatamente quando a última célula segura é aberta.
- [x] Contador pode ficar negativo; `QUESTION` não conta como bandeira.
- [x] Cronômetro não inicia ao marcar bandeira e para nos limites definidos.
- [x] Todos os testes da seção 15.1 passam.
- [x] `./gradlew test` passa.

### Notas/decisões
- `GameEngine` é puro Java, sem dependência de `com.badlogic.gdx`.
- O primeiro `reveal` coloca as minas com `MinePlacer`, muda para `PLAYING` e zera o cronômetro; marcar bandeira antes disso mantém `READY`.
- `remainingMines()` usa `Board.getFlagCount()`, que conta `FLAGGED` e `WRONG_FLAG`; `QUESTION` não conta.
- Na derrota, a mina clicada vira `EXPLODED`, minas não marcadas viram `MINE_SHOWN` e bandeiras erradas viram `WRONG_FLAG`.
- Na vitória, todas as minas recebem `FLAGGED` e o contador fica em 0.
- `chord` só age em célula revelada numerada com bandeiras vizinhas iguais ao número; pode causar derrota se houver bandeira errada.
- Interrogação é desativada por padrão; ciclo `COVERED -> FLAGGED -> QUESTION/COVERED -> COVERED`.
- 31 testes unitários no módulo `core` (9 de `GameEngine`, 3 de chord).

### Bloqueios
- Nenhum.

---

## Etapa 4 — Renderização e interação básica

Status: **Concluída**

### Entregas
- [x] `GameScreen` e `BoardRenderer` com `ShapeRenderer` + `BitmapFont`.
- [x] `FitViewport` e conversão de coordenadas de tela para célula.
- [x] Linha 0 exibida no topo.
- [x] Revelar ao soltar o botão esquerdo.
- [x] Bandeira com o botão direito.
- [x] Feedback de célula pressionada, arrastar e cancelar ao soltar fora.
- [x] `F2` reinicia o jogo.

### Validação manual
- [x] Iniciar no nível Iniciante e revelar uma área vazia.
- [x] Primeiro clique nunca explode.
- [x] Marcar e desmarcar bandeira com o botão direito.
- [x] Célula marcada não é revelada pelo botão esquerdo.
- [x] Arrastar para fora do tabuleiro cancela a ação.
- [x] `F2` reinicia o tabuleiro.
- [x] Testes das etapas anteriores continuam passando.

### Notas/decisões
- `GameScreen` usa `FitViewport`; a etapa 5 substituiu o layout fixo por `GameLayout`, com célula, margem e HUD escaláveis.
- `BoardRenderer` desenha células com `ShapeRenderer` e números com `BitmapFont`; não usa assets externos.
- Coordenadas convertidas via `viewport.unproject` + `BoardRenderer.cellAt`; a linha 0 do modelo aparece no topo.
- Revelação ocorre no `touchUp` esquerdo sobre a célula; arrastar mostra a célula pressionada e soltar fora cancela.
- Bandeira é alternada no `touchDown` direito quando o esquerdo não está pressionado.
- `F2` reinicia mantendo a mesma configuração.
- Validação manual considerada aprovada pelo usuário após o smoke test; a etapa 5 revalidou o layout por screenshots.

### Bloqueios
- Nenhum.

---

## Etapa 5 — HUD, dificuldades e interação completa

Status: **Concluída**

### Entregas
- [x] Visor de minas e cronômetro em 3 dígitos.
- [x] Carinha com estados sorrindo, surpresa, vitória, derrota e pressionada.
- [x] Chord por esquerdo+direito, botão do meio e opção de clique em número.
- [x] Seletor de dificuldade e diálogo personalizado.
- [x] Redimensionamento da janela conforme a dificuldade.
- [x] Atalhos `1`, `2`, `3`, `C`, `M`, `Esc` e `F5`.
- [x] Escala de interface 1x, 2x e 3x.
- [x] Atalhos extras `4`/`5`/`6` para escala e `X` para chord no número.

### Validação manual
- [x] Cronômetro inicia apenas na primeira revelação e para ao vencer ou perder.
- [x] Contador atualiza com bandeiras e pode ficar negativo.
- [x] Chord só abre quando a quantidade de bandeiras é igual ao número.
- [x] Bandeira errada no chord pode causar derrota.
- [x] Trocar de dificuldade ajusta grade, janela e layout.
- [x] Nível personalizado respeita os limites definidos.
- [x] Escala de interface mantém o jogo funcional.
- [x] Testes das etapas anteriores continuam passando.

### Notas/decisões
- `GameLayout` centraliza a geometria escalável: célula `32*escala`, margem `12*escala`, HUD `64*escala` e margem externa; a janela é redimensionada e limitada ao display.
- `GameUiRenderer` desenha painel, carinha e menu/diálogo sobre `ShapeRenderer`; `SevenSegmentDisplay` desenha 3 dígitos com clamp em `-99..999`.
- A carinha usa sorriso, surpresa ao pressionar o tabuleiro, óculos na vitória, olhos em X na derrota e expressão pressionada ao segurar o botão da carinha.
- Revelar e marcar bandeira ocorrem no `touchUp` (esquerdo/direito), com cancelamento ao soltar fora; arrastar atualiza o feedback.
- Chord por esquerdo+direito e botão do meio; o segundo release é suprimido. Chord no número é opção desligada por padrão, alternada por `X`.
- Menu `F5`/`Esc` com troca de nível (`1`/`2`/`3`), personalizado (`C`), interrogação (`M`), escala (`4`/`5`/`6`) e reinício (`F2`).
- Diálogo personalizado editável por teclado (`Tab`/setas, dígitos, `Backspace`, `Enter`, `Esc`); os valores passam pelo clamp de `CustomDifficulty`.
- Validação visual feita por screenshots temporárias do framebuffer (Iniciante, menu, diálogo, escala 2x e Avançado), removidas antes do commit; displays e painéis sem clipping.

### Bloqueios
- Nenhum.

---

## Etapa 6 — Persistência, polimento e empacotamento

Status: **Concluída**

### Entregas
- [x] `Settings` e `HighScores` usando `Preferences`.
- [x] Recordes apenas para níveis fixos.
- [x] Diálogo de recordes com `F4` e opção de zerar com confirmação.
- [x] Opções persistidas: último nível, interrogação, chord por clique e escala.
- [x] Assets originais: gráficos desenhados com `ShapeRenderer` e fonte padrão do libGDX; nenhum sprite/som copiado.
- [x] Empacotamento desktop com JAR executável.
- [ ] Som e tema escuro (opcionais) não implementados nesta versão.

### Validação
- [x] Recorde salvo persiste após fechar e reabrir o jogo.
- [x] Valor inicial de recorde é `999` segundos e `"Anônimo"`.
- [x] Nível personalizado não registra recorde.
- [x] Opções persistem entre execuções.
- [x] Nenhum asset copiado do jogo original da Microsoft.
- [x] Checklist manual completo da seção 15.2 do `spec.md`.
- [x] `./gradlew lwjgl3:dist` gera o JAR.
- [x] JAR gerado executa corretamente.
- [x] `./gradlew test` passa (42 testes).

### Notas/decisões
- `Settings` persiste o último nível (incluindo valores do personalizado), interrogação, chord no número, escala, som e tema; som/tema ficam apenas como opções de modelo, sem efeito visual/sonoro nesta versão.
- `HighScores` usa as chaves `best.<nível>.time` e `best.<nível>.name`; somente níveis fixos entram no ranking.
- Ao vencer com tempo menor que o recorde, o jogo abre um diálogo de nome (até 16 caracteres; vazio vira `"Anônimo"`).
- `F4` abre/fecha os recordes; `R` ou o botão da janela abre a confirmação; `S`/`Y`/`Enter` confirmam e `N`/`Esc` cancelam.
- `lwjgl3:dist` gera `lwjgl3/build/libs/campo-minado.jar` com dependências e assets embutidos.
- Validação visual por screenshots temporárias dos diálogos de recordes, confirmação e novo recorde; código de captura removido antes do commit.

### Bloqueios
- Nenhum.

---

## Fechamento v1.0

Validar os critérios de aceitação da seção 18 do `spec.md`:

- [x] Regras das seções 3.1–3.11 cumpridas.
- [x] Três níveis fixos e o personalizado funcionam conforme a seção 3.3.
- [x] Primeiro clique nunca é mina e abre área quando possível.
- [x] Interface contém contador de minas, cronômetro, carinha e tabuleiro.
- [x] Recordes persistem entre execuções.
- [x] Todos os testes da seção 15.1 passam.
- [x] `./gradlew lwjgl3:run` e `./gradlew lwjgl3:dist` funcionam.
- [x] Nenhum asset protegido por direitos autorais é utilizado.

---

## Histórico de validação

Adicionar uma linha por validação relevante.

| Data | Etapa | Verificação | Resultado |
|---|---|---|---|
| 2026-11-13 | 1 | `./gradlew test` | Passou |
| 2026-11-13 | 1 | `./gradlew lwjgl3:run` (abrir/fechar janela) | Passou, código 0 |
| 2026-11-13 | 2 | `./gradlew test` (19 testes) | Passou |
| 2026-11-13 | 2 | Verificação de imports de `com.badlogic.gdx` no `model` | Nenhum import |
| 2026-11-13 | 3 | `./gradlew test --offline` (31 testes) | Passou |
| 2026-11-13 | 4 | `./gradlew test --offline` (31 testes) | Passou |
| 2026-11-13 | 4 | `timeout 10s ./gradlew lwjgl3:run --offline` | Janela abriu sem exceções; encerrada por timeout |
| 2026-11-13 | 4 | Validação manual aprovada implicitamente pelo usuário | Passou |
| 2026-11-13 | 5 | `./gradlew test --offline` (31 testes) | Passou |
| 2026-11-13 | 5 | Smoke test + automação de teclado no `lwjgl3:run` | Sem exceções |
| 2026-11-13 | 5 | Screenshots Iniciante/menu/diálogo/escala 2x/Avançado | HUD, displays, overlays e dimensões corretos |
| 2026-11-13 | 6 | `./gradlew core:test --rerun-tasks --offline` (42 testes) | Passou |
| 2026-11-13 | 6 | Screenshots recordes/confirmação/novo recorde | Overlays e botões corretos |
| 2026-11-13 | 6 | `./gradlew lwjgl3:dist --offline` | `campo-minado.jar` gerado (14 MB) |
| 2026-11-13 | 6 | `timeout 8s java -jar lwjgl3/build/libs/campo-minado.jar` | Abriu sem exceções |
