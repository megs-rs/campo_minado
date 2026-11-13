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
| 1 | Base executável | Pendente |
| 2 | Núcleo do tabuleiro | Pendente |
| 3 | Regras e estado do jogo | Pendente |
| 4 | Renderização e interação básica | Pendente |
| 5 | HUD, dificuldades e interação completa | Pendente |
| 6 | Persistência, polimento e empacotamento | Pendente |

---

## Etapa 1 — Base executável

Status: **Pendente**

### Entregas
- [ ] Estrutura Gradle `gdx-liftoff` com módulos `core` e `lwjgl3`.
- [ ] Java 17, libGDX 1.12.x e JUnit 5 configurado no `core`.
- [ ] Launcher abrindo uma janela vazia.
- [ ] Pacote base `com.exemplo.minesweeper`.
- [ ] `.gitignore` para Gradle/IDE.

### Validação
- [ ] `./gradlew test` passa.
- [ ] `./gradlew lwjgl3:run` abre a janela vazia sem erro.
- [ ] Fechar a janela encerra o processo sem stack trace.

### Notas/decisões
-

### Bloqueios
-

---

## Etapa 2 — Núcleo do tabuleiro

Status: **Pendente**

### Entregas
- [ ] `Cell` e `CellState`.
- [ ] `Board` com array 1D (`idx = y * cols + x`).
- [ ] `Difficulty` e configuração personalizada com clamp.
- [ ] `MinePlacer` com `Random` e seed opcional.
- [ ] Flood fill **iterativo**.
- [ ] Método de teste para injetar minas manualmente.

### Validação
- [ ] Contagem de minas posicionadas igual ao valor configurado, por dificuldade.
- [ ] Primeiro clique seguro em 10.000 seeds diferentes.
- [ ] `adjacentMines` correto em canto, borda e centro.
- [ ] Flood fill abre a região esperada, não abre bandeiras/minas e não estoura a pilha em 30×24.
- [ ] Clamp de colunas, linhas e minas do nível personalizado.
- [ ] Nenhum import de `com.badlogic.gdx` no pacote `model`.
- [ ] `./gradlew test` passa.

### Notas/decisões
-

### Bloqueios
-

---

## Etapa 3 — Regras e estado do jogo

Status: **Pendente**

### Entregas
- [ ] `GameEngine` com `newGame`, `restart`, `reveal`, `toggleFlag`, `chord` e `update(delta)`.
- [ ] Máquina de estados `READY -> PLAYING -> WON/LOST`.
- [ ] Primeiro clique posiciona minas e inicia o cronômetro.
- [ ] Contador de minas, incluindo valores negativos.
- [ ] Vitória, derrota e estados de fim de jogo (`EXPLODED`, `MINE_SHOWN`, `WRONG_FLAG`).
- [ ] Cronômetro por delta acumulado, com parada em 999, vitória ou derrota.

### Validação
- [ ] Testes da seção 15.1, itens 5 a 9, do `spec.md`.
- [ ] Chord abre vizinhos quando bandeiras == número, não abre quando diferente e explode com bandeira errada.
- [ ] Vitória detectada exatamente quando a última célula segura é aberta.
- [ ] Contador pode ficar negativo; `QUESTION` não conta como bandeira.
- [ ] Cronômetro não inicia ao marcar bandeira e para nos limites definidos.
- [ ] Todos os testes da seção 15.1 passam.
- [ ] `./gradlew test` passa.

### Notas/decisões
-

### Bloqueios
-

---

## Etapa 4 — Renderização e interação básica

Status: **Pendente**

### Entregas
- [ ] `GameScreen` e `BoardRenderer` com `ShapeRenderer` + `BitmapFont`.
- [ ] `FitViewport` e conversão de coordenadas de tela para célula.
- [ ] Linha 0 exibida no topo.
- [ ] Revelar ao soltar o botão esquerdo.
- [ ] Bandeira com o botão direito.
- [ ] Feedback de célula pressionada, arrastar e cancelar ao soltar fora.
- [ ] `F2` reinicia o jogo.

### Validação manual
- [ ] Iniciar no nível Iniciante e revelar uma área vazia.
- [ ] Primeiro clique nunca explode.
- [ ] Marcar e desmarcar bandeira com o botão direito.
- [ ] Célula marcada não é revelada pelo botão esquerdo.
- [ ] Arrastar para fora do tabuleiro cancela a ação.
- [ ] `F2` reinicia o tabuleiro.
- [ ] Testes das etapas anteriores continuam passando.

### Notas/decisões
-

### Bloqueios
-

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
|  |  |  |  |
