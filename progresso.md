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
| 4 | Renderização e interação básica | Em andamento |
| 5 | HUD, dificuldades e interação completa | Pendente |
| 6 | Persistência, polimento e empacotamento | Pendente |

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

Status: **Em andamento**

### Entregas
- [x] `GameScreen` e `BoardRenderer` com `ShapeRenderer` + `BitmapFont`.
- [x] `FitViewport` e conversão de coordenadas de tela para célula.
- [x] Linha 0 exibida no topo.
- [x] Revelar ao soltar o botão esquerdo.
- [x] Bandeira com o botão direito.
- [x] Feedback de célula pressionada, arrastar e cancelar ao soltar fora.
- [x] `F2` reinicia o jogo.

### Validação manual
- [ ] Iniciar no nível Iniciante e revelar uma área vazia.
- [ ] Primeiro clique nunca explode.
- [ ] Marcar e desmarcar bandeira com o botão direito.
- [ ] Célula marcada não é revelada pelo botão esquerdo.
- [ ] Arrastar para fora do tabuleiro cancela a ação.
- [ ] `F2` reinicia o tabuleiro.
- [x] Testes das etapas anteriores continuam passando.

### Notas/decisões
- `GameScreen` usa `FitViewport` com mundo de `colunas*32 + 24` por `linhas*32 + 24`; margem de 12 px e célula de 32 px.
- `BoardRenderer` desenha células com `ShapeRenderer` e números com `BitmapFont`; não usa assets externos.
- Coordenadas convertidas via `viewport.unproject` + `BoardRenderer.cellAt`; a linha 0 do modelo aparece no topo.
- Revelação ocorre no `touchUp` esquerdo sobre a célula; arrastar mostra a célula pressionada e soltar fora cancela.
- Bandeira é alternada no `touchDown` direito quando o esquerdo não está pressionado.
- `F2` reinicia mantendo a mesma configuração.
- Smoke test: janela abriu e permaneceu rodando sem exceções até o encerramento por timeout; interação visual ainda precisa de validação manual.

### Bloqueios
- Validação manual de clique, arraste, bandeira e visual pendente.

---

## Etapa 5 — HUD, dificuldades e interação completa

Status: **Pendente**

### Entregas
- [ ] Visor de minas e cronômetro em 3 dígitos.
- [ ] Carinha com estados sorrindo, surpresa, vitória, derrota e pressionada.
- [ ] Chord por esquerdo+direito, botão do meio e opção de clique em número.
- [ ] Seletor de dificuldade e diálogo personalizado.
- [ ] Redimensionamento da janela conforme a dificuldade.
- [ ] Atalhos `1`, `2`, `3`, `C`, `M`, `Esc` e `F5`.
- [ ] Escala de interface 1x, 2x e 3x.

### Validação manual
- [ ] Cronômetro inicia apenas na primeira revelação e para ao vencer ou perder.
- [ ] Contador atualiza com bandeiras e pode ficar negativo.
- [ ] Chord só abre quando a quantidade de bandeiras é igual ao número.
- [ ] Bandeira errada no chord pode causar derrota.
- [ ] Trocar de dificuldade ajusta grade, janela e layout.
- [ ] Nível personalizado respeita os limites definidos.
- [ ] Escala de interface mantém o jogo funcional.
- [ ] Testes das etapas anteriores continuam passando.

### Notas/decisões
-

### Bloqueios
-

---

## Etapa 6 — Persistência, polimento e empacotamento

Status: **Pendente**

### Entregas
- [ ] `Settings` e `HighScores` usando `Preferences`.
- [ ] Recordes apenas para níveis fixos.
- [ ] Diálogo de recordes com `F4` e opção de zerar com confirmação.
- [ ] Opções persistidas: último nível, interrogação, chord por clique, escala, som e tema opcional.
- [ ] Assets originais ou livres: atlas, fontes e sons.
- [ ] Empacotamento desktop.

### Validação
- [ ] Recorde salvo persiste após fechar e reabrir o jogo.
- [ ] Valor inicial de recorde é `999` segundos e `"Anônimo"`.
- [ ] Nível personalizado não registra recorde.
- [ ] Opções persistem entre execuções.
- [ ] Nenhum asset copiado do jogo original da Microsoft.
- [ ] Checklist manual completo da seção 15.2 do `spec.md`.
- [ ] `./gradlew lwjgl3:dist` gera o JAR.
- [ ] JAR gerado executa corretamente.
- [ ] `./gradlew test` passa.

### Notas/decisões
-

### Bloqueios
-

---

## Fechamento v1.0

Validar os critérios de aceitação da seção 18 do `spec.md`:

- [ ] Regras das seções 3.1–3.11 cumpridas.
- [ ] Três níveis fixos e o personalizado funcionam conforme a seção 3.3.
- [ ] Primeiro clique nunca é mina e abre área quando possível.
- [ ] Interface contém contador de minas, cronômetro, carinha e tabuleiro.
- [ ] Recordes persistem entre execuções.
- [ ] Todos os testes da seção 15.1 passam.
- [ ] `./gradlew lwjgl3:run` e `./gradlew lwjgl3:dist` funcionam.
- [ ] Nenhum asset protegido por direitos autorais é utilizado.

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
