# Especificação: Campo Minado (estilo Windows clássico) em Java + libGDX

Versão: 1.0
Idioma do documento: Português (BR)
Alvo: desktop (Windows / Linux / macOS) via LWJGL3. Android/HTML são opcionais (ver seção 17).

---

## 1. Visão geral

Reimplementação do jogo **Campo Minado** (Minesweeper) clássico do Windows, usando **Java 17+** e **libGDX**. O objetivo é reproduzir fielmente as **regras, a jogabilidade e a estrutura de interface** do jogo original: grade de células, contador de minas, cronômetro, carinha de status, três níveis de dificuldade fixos e um nível personalizado, e tabela de recordes.

> **Nota sobre assets:** o jogo original é propriedade da Microsoft. Esta especificação descreve apenas **regras e comportamento** (que não são protegidos). Todos os gráficos, fontes e sons devem ser **criados originalmente** ou ter licença livre. Não copiar sprites, ícones ou sons do jogo original.

### 1.1 Objetivo do jogador
Revelar todas as células que **não** contêm mina, sem revelar nenhuma mina. O jogador usa os números das células reveladas (quantidade de minas vizinhas) para deduzir onde estão as minas e marcá-las com bandeiras.

### 1.2 Fora de escopo (v1.0)
- Multiplayer/online.
- Modos alternativos (hexagonal, 3D, etc.).
- Resolução automática (solver/dicas), exceto como ferramenta de teste (seção 15).
- Efeitos visuais complexos ou 3D.

---

## 2. Requisitos técnicos

| Item | Especificação |
|---|---|
| Linguagem | Java 17 ou superior |
| Framework | libGDX 1.12.x (ou mais recente estável) |
| Backend desktop | LWJGL3 |
| Build | Gradle (estrutura gerada pelo `gdx-liftoff`) |
| Módulos | `core` (lógica + renderização), `lwjgl3` (launcher desktop) |
| Persistência | `Preferences` do libGDX (recordes e configurações) |
| Testes | JUnit 5 (somente no módulo `core`, lógica sem dependência gráfica) |
| FPS alvo | 60 (renderização contínua desnecessária; pode-se usar `Gdx.graphics.setContinuousRendering(false)` + `requestRendering()`, mas o cronômetro exige atualização a cada segundo) |

---

## 3. Regras do jogo

### 3.1 Tabuleiro
- Grade retangular de `colunas × linhas` células.
- Cada célula possui: **estado de visibilidade**, **flag de mina** e **contagem de minas vizinhas**.
- Vizinhança: **8 direções** (horizontal, vertical e diagonais). Células nas bordas têm menos vizinhos.

### 3.2 Estados de uma célula (visíveis ao jogador)
| Estado | Descrição |
|---|---|
| `COVERED` | Célula fechada, sem marca |
| `FLAGGED` | Célula fechada com bandeira (jogador acredita ter mina) |
| `QUESTION` | Célula fechada com interrogação (opcional, configurável) |
| `REVEALED` | Célula aberta, exibe número 0–8 (0 = vazia) |
| `EXPLODED` | Mina que o jogador revelou e causou a derrota (destacada em vermelho) |
| `WRONG_FLAG` | Fim de jogo: bandeira colocada onde **não** havia mina (exibida com mina riscada) |
| `MINE_SHOWN` | Fim de jogo (derrota): mina não marcada, revelada |

### 3.3 Níveis de dificuldade

| Nível | Colunas | Linhas | Minas |
|---|---|---|---|
| Iniciante | 9 | 9 | 10 |
| Intermediário | 16 | 16 | 40 |
| Avançado (Especialista) | 30 | 16 | 99 |
| Personalizado | 9–30 | 9–24 | 10 até `(colunas-1) × (linhas-1)` |

Validação do nível personalizado: valores fora das faixas são ajustados (clamp) automaticamente para o limite mais próximo.

### 3.4 Primeiro clique seguro
- As minas **só são posicionadas após o primeiro clique de revelação**.
- A célula clicada **e suas 8 vizinhas** não podem conter mina (garante abertura de área no primeiro clique).
- Se o número de minas for tão alto que essa zona segura seja inviável (`colunas*linhas - 9 < minas`), reduzir a zona segura apenas para a célula clicada.
- Uma bandeira colocada **antes** do primeiro clique é permitida e não inicia o jogo nem o cronômetro.

### 3.5 Ações do jogador

| Ação | Entrada padrão | Efeito |
|---|---|---|
| **Revelar** | Botão esquerdo | Abre a célula (ver 3.6). Ignorado se `FLAGGED`. |
| **Marcar** | Botão direito | Alterna ciclo: `COVERED → FLAGGED → (QUESTION) → COVERED`. A interrogação só entra no ciclo se habilitada nas opções. |
| **Chord (acorde)** | Botão esquerdo + direito simultâneos, **ou** clique do meio, **ou** clique esquerdo numa célula já revelada com número (configurável) | Ver 3.7. |
| **Reiniciar** | Clique na carinha / tecla `F2` | Novo jogo com a mesma configuração. |

### 3.6 Revelar célula
1. Se `FLAGGED` ou `REVEALED`: não faz nada.
2. Se for o primeiro clique: posicionar minas (3.4), iniciar cronômetro.
3. Se a célula tem mina: **derrota** (3.9).
4. Caso contrário, revelar a célula:
   - Se `contagem > 0`: apenas revela.
   - Se `contagem == 0`: executa **flood fill** (abertura em cascata): revela recursivamente/iterativamente todos os vizinhos não marcados com bandeira e não minados; continua a expandir apenas a partir de células com contagem 0.
   - Células com `FLAGGED` **não** são abertas pelo flood fill. Células `QUESTION` **são** abertas.
5. Verificar vitória (3.8).

> **Implementação obrigatória:** o flood fill deve ser **iterativo** (fila/pilha explícita) para evitar `StackOverflowError` em tabuleiros grandes.

### 3.7 Chord (revelar vizinhos)
Aplicável a uma célula `REVEALED` com número `n > 0`:
- Contar bandeiras (`FLAGGED`) nos 8 vizinhos.
- Se `bandeiras == n`: revelar todos os vizinhos que **não** estejam `FLAGGED` (aplicando a regra 3.6 a cada um, incluindo possibilidade de explosão se alguma bandeira estiver errada).
- Se `bandeiras != n`: nenhuma abertura. Visualmente, enquanto o botão estiver pressionado, os vizinhos `COVERED` são exibidos em estado "pressionado" (feedback); ao soltar, voltam ao normal.

### 3.8 Vitória
- Condição: **todas as células sem mina estão `REVEALED`**.
- Efeito: parar cronômetro; marcar automaticamente com bandeira todas as minas restantes; contador de minas vai a 0; carinha de óculos escuros; verificar recorde (seção 9).

### 3.9 Derrota
- Condição: revelar uma célula com mina.
- Efeito: parar cronômetro; a mina clicada fica `EXPLODED` (fundo vermelho); revelar todas as demais minas não marcadas (`MINE_SHOWN`); bandeiras erradas viram `WRONG_FLAG`; carinha triste; entradas de tabuleiro bloqueadas até reiniciar.

### 3.10 Contador de minas
- Valor exibido = `totalMinas - totalBandeiras`.
- Pode ficar **negativo** (ex.: `-3`) se o jogador colocar mais bandeiras que minas.
- Exibição em 3 dígitos; faixa mínima `-99`, máxima `999`.
- Interrogações **não** contam como bandeiras.

### 3.11 Cronômetro
- Inicia no **primeiro clique de revelação** (não ao marcar bandeira).
- Incrementa de 1 em 1 segundo (inteiro). Começa em `0` e, ao iniciar, passa a contar de `1` após o primeiro segundo completo.
- Limite: `999`. Ao atingir, para de incrementar (o jogo continua).
- Para ao vencer ou perder. Zera ao reiniciar.
- Deve usar `delta` acumulado (`Gdx.graphics.getDeltaTime()`), não `System.currentTimeMillis()` solto, para consistência com pausa.

---

## 4. Interface e layout

### 4.1 Estrutura da janela

```
┌──────────────────────────────────────────┐
│ [Jogo] [Opções] [Ajuda]          (menu)  │
├──────────────────────────────────────────┤
│ ┌──────────────────────────────────────┐ │
│ │ [ 010 ]        (:-) rosto     [ 000 ]│ │  ← painel superior
│ └──────────────────────────────────────┘ │
│ ┌──────────────────────────────────────┐ │
│ │                                      │ │
│ │          GRADE DE CÉLULAS            │ │  ← tabuleiro
│ │                                      │ │
│ └──────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

- **Painel superior:** contador de minas (esquerda), carinha (centro), cronômetro (direita).
- **Tabuleiro:** grade de células quadradas com bordas em relevo (estilo 3D: luz no topo/esquerda, sombra embaixo/direita) nas células fechadas; células abertas com borda plana fina.
- **Menu:** como libGDX não tem menu nativo, implementar a barra superior com **Scene2D** (`Table` + `TextButton`/`SelectBox`/popup) ou como painel lateral/overlay acionado por teclas.

### 4.2 Dimensões (tamanho lógico)
- Célula: **32×32 px** (padrão; escalável por opção 1x/2x/3x).
- Margens do tabuleiro: 12 px.
- Painel superior: altura 64 px.
- Janela redimensiona automaticamente conforme a dificuldade (`Gdx.graphics.setWindowedMode(w, h)`).
- Usar `Viewport` do tipo `FitViewport` ou `ScreenViewport` para suportar redimensionamento e DPI alto.

### 4.3 Carinha (botão de status)
| Estado do jogo | Carinha |
|---|---|
| Pronto / jogando | Sorrindo |
| Botão do mouse pressionado sobre o tabuleiro | Surpresa ("oh") |
| Vitória | Óculos escuros |
| Derrota | Triste / olhos em X |
| Botão da carinha pressionado | Versão "pressionada" (sorrindo) |

### 4.4 Visor numérico (7 segmentos)
- 3 dígitos, estilo display de 7 segmentos, vermelho sobre fundo preto.
- Implementar via **spritesheet** de dígitos (`0–9` e `-`) ou desenho com `ShapeRenderer`.
- Valores acima de 999 são limitados; abaixo de -99 são limitados.

### 4.5 Cores dos números (convenção do clássico)
| Número | Cor |
|---|---|
| 1 | Azul |
| 2 | Verde |
| 3 | Vermelho |
| 4 | Azul escuro |
| 5 | Marrom/vinho |
| 6 | Ciano/turquesa |
| 7 | Preto |
| 8 | Cinza |

Fundo do tabuleiro: cinza claro (`#C0C0C0` aproximado). Mina explodida: fundo vermelho.

### 4.6 Feedback visual de interação
- Célula `COVERED` sob o cursor com botão esquerdo pressionado: exibir em estado "afundado" (aberta, sem número).
- Ao soltar fora do tabuleiro ou arrastar para fora: cancelar a ação.
- Ao arrastar com o botão esquerdo pressionado, a célula "afundada" acompanha o cursor; a ação só é executada ao **soltar** o botão (padrão do clássico).

---

## 5. Controles

### 5.1 Mouse
| Entrada | Ação |
|---|---|
| Botão esquerdo (soltar sobre célula) | Revelar |
| Botão direito | Marcar/desmarcar |
| Esquerdo + direito juntos | Chord |
| Botão do meio | Chord |
| Clique esquerdo em célula revelada numerada | Chord (opção, padrão: **desligado**) |
| Clique na carinha | Reiniciar |

### 5.2 Teclado
| Tecla | Ação |
|---|---|
| `F2` | Novo jogo |
| `F4` | Abrir recordes |
| `F5` / `Esc` | Abrir/fechar menu de opções |
| `1` / `2` / `3` | Iniciante / Intermediário / Avançado |
| `C` | Abrir dialog de nível personalizado |
| `M` | Alternar marcas de interrogação |
| `Setas` + `Espaço`/`Enter` | (Opcional) navegar o cursor e revelar |
| `F` (com cursor de teclado) | (Opcional) marcar bandeira |

### 5.3 Toque (opcional para Android)
- Toque curto: revelar. Toque longo (≥ 400 ms): bandeira. Toque em número: chord.

### 5.4 Implementação de entrada
- Usar `InputAdapter` (ou `InputMultiplexer` combinando `Stage` do Scene2D e o input do tabuleiro).
- Rastrear estado de botões (`leftDown`, `rightDown`, `middleDown`) para detectar combinação esquerdo+direito.
- Converter coordenadas de tela para mundo via `viewport.unproject(...)` e depois para índice de célula.

---

## 6. Arquitetura

### 6.1 Princípios
1. **Lógica separada da apresentação:** o pacote `model` não pode importar nada de `com.badlogic.gdx` (exceto utilitários puros, se necessário). Isso permite testes unitários simples.
2. **Máquina de estados** explícita para o jogo.
3. **Renderização desacoplada:** a view lê o estado do modelo e desenha; não altera regras.

### 6.2 Estrutura de pacotes

```
core/src/main/java/com/exemplo/minesweeper/
├── MinesweeperGame.java          // extends Game; ponto de entrada libGDX
├── model/
│   ├── Cell.java
│   ├── Board.java
│   ├── Difficulty.java           // enum + config personalizada
│   ├── GameState.java            // enum: READY, PLAYING, WON, LOST
│   ├── GameEngine.java           // regras: revelar, marcar, chord, vitória/derrota
│   ├── MinePlacer.java           // posicionamento aleatório com zona segura
│   └── GameListener.java         // callbacks (opcional)
├── screen/
│   ├── GameScreen.java           // tela principal
│   └── HighScoresScreen.java     // ou Dialog
├── view/
│   ├── BoardRenderer.java        // desenha grade e células
│   ├── SevenSegmentDisplay.java  // desenha contadores
│   ├── FaceButton.java
│   └── Assets.java               // AssetManager, atlas, fontes
├── input/
│   └── BoardInputHandler.java    // traduz mouse/teclado em ações do engine
├── ui/
│   ├── MenuBar.java              // Scene2D
│   ├── CustomGameDialog.java
│   └── HighScoresDialog.java
└── persistence/
    ├── HighScores.java
    └── Settings.java             // usa com.badlogic.gdx.Preferences
```

### 6.3 Máquina de estados do jogo

```
READY ──(1º clique revelar)──► PLAYING ──(todas seguras abertas)──► WON
  ▲                              │
  │                              └──(mina revelada)──► LOST
  └────────────(reiniciar, F2 ou clique na carinha)────────────────┘
```

- `READY`: tabuleiro vazio (minas ainda não posicionadas), cronômetro parado em 0.
- `PLAYING`: minas posicionadas, cronômetro correndo.
- `WON` / `LOST`: entradas de tabuleiro ignoradas (apenas reiniciar/menu).

---

## 7. Modelo de dados

### 7.1 `Cell`

```java
public final class Cell {
    public boolean mine;
    public int adjacentMines;      // 0..8, calculado após posicionar minas
    public CellState state;        // COVERED, FLAGGED, QUESTION, REVEALED
    // Estados de fim de jogo podem ser derivados (exploded/wrongFlag) ou guardados:
    public boolean exploded;       // true apenas para a mina que causou a derrota
}
```

### 7.2 `Board`
- Armazenamento em **array 1D** `Cell[] cells` com índice `idx = y * cols + x` (melhor cache e simplicidade).
- Métodos principais:
  - `Cell get(int x, int y)`
  - `boolean inBounds(int x, int y)`
  - `void forEachNeighbor(int x, int y, IntBiConsumer action)`
  - `int countFlagsAround(int x, int y)`
  - `void reset(int cols, int rows, int mines)`
- Campos: `cols`, `rows`, `mines`, `revealedCount`, `flagCount`.

### 7.3 `GameEngine` (API pública)

```java
public final class GameEngine {
    GameState state();
    int remainingMines();          // mines - flags (pode ser negativo)
    int elapsedSeconds();

    void newGame(Difficulty d);
    void restart();                // mesma dificuldade
    void reveal(int x, int y);
    void toggleFlag(int x, int y);
    void chord(int x, int y);
    void update(float delta);      // avança o cronômetro quando PLAYING
}
```

---

## 8. Algoritmos

### 8.1 Posicionamento de minas (após o 1º clique)

```
posicionarMinas(board, cliqueX, cliqueY):
    candidatos = todas as células, exceto (cliqueX, cliqueY) e seus vizinhos
    (se candidatos.size < minas → excluir só a célula clicada)
    embaralhar candidatos (Fisher-Yates)  // usar java.util.Random com seed opcional
    marcar as primeiras N como mina
    calcular adjacentMines de todas as células
```

- Aceitar **seed opcional** para reprodutibilidade em testes.
- Complexidade O(L·C).

### 8.2 Flood fill iterativo

```
revelarCascata(x, y):
    fila = [(x, y)]
    enquanto fila não vazia:
        (cx, cy) = fila.pop()
        c = board.get(cx, cy)
        se c.state == REVEALED ou c.state == FLAGGED: continuar
        c.state = REVEALED; revealedCount++
        se c.adjacentMines == 0:
            para cada vizinho v (dentro dos limites):
                se v.state ∈ {COVERED, QUESTION} e !v.mine: fila.push(v)
```

### 8.3 Verificação de vitória
`revealedCount == cols * rows - mines` → `WON`.

### 8.4 Chord
Ver seção 3.7. Reaproveitar `reveal()` para cada vizinho elegível; se qualquer um explodir, o estado passa a `LOST` e o processamento pode terminar.

---

## 9. Recordes (High Scores)

- Armazenar **o melhor tempo (segundos) e o nome do jogador** para cada um dos três níveis fixos. O nível personalizado **não** registra recorde.
- Persistência: `Gdx.app.getPreferences("minesweeper")`, chaves como `best.beginner.time`, `best.beginner.name` etc.
- Quando o jogador vence com tempo menor que o recorde atual (ou se não houver recorde): exibir dialog pedindo o nome (máx. 16 caracteres; padrão "Anônimo") e salvar.
- Tela/dialog de recordes (`F4`): lista dos três níveis com tempo e nome; botão **Zerar recordes** (com confirmação).
- Valor inicial de cada recorde: `999` segundos / `"Anônimo"`.

---

## 10. Opções e configurações

| Opção | Padrão | Persistida |
|---|---|---|
| Último nível jogado (incl. valores do personalizado) | Iniciante | Sim |
| Marcas de interrogação (`?`) | Desligado | Sim |
| Chord com clique esquerdo em número | Desligado | Sim |
| Escala da interface (1x / 2x / 3x) | 1x (ou 2x em HiDPI) | Sim |
| Som ligado | Ligado | Sim |
| Tema (claro/escuro) | Claro (opcional) | Sim |

---

## 11. Assets

### 11.1 Gráficos (criar originais)
Organizar em um **TextureAtlas** (`ui.atlas`) gerado com o `gdx-tools` TexturePacker:

| Grupo | Itens |
|---|---|
| Células | `covered`, `pressed`, `revealed`, `flag`, `question`, `mine`, `mine_exploded`, `mine_wrong`, números `1`–`8` (ou desenhados com fonte) |
| Carinha | `face_smile`, `face_pressed`, `face_oh`, `face_win`, `face_lose` |
| Display | `digit_0`–`digit_9`, `digit_minus`, `digit_off` |
| Moldura | Bordas em relevo (pode ser desenhada por código com `ShapeRenderer`) |

- Alternativa sem sprites: desenhar tudo com `ShapeRenderer` + `BitmapFont`, útil para o protótipo inicial.
- Usar **NinePatch** para bordas redimensionáveis.
- Filtros de textura: `Nearest` para estética pixelada.

### 11.2 Fontes
- Fonte bitmap (`.fnt`) gerada com Hiero/FreeType para os números 1–8 e textos de UI.
- Usar fonte com licença livre (ex.: OFL).

### 11.3 Sons (opcionais)
- `click`, `flag`, `explosion`, `win`, `tick` (tique do cronômetro, desligado por padrão).
- Formatos `.ogg` ou `.wav`, carregados com `AssetManager`.

---

## 12. Renderização

- **`SpriteBatch`** para células e displays (uma única chamada `begin/end` por frame).
- **`OrthographicCamera` + `Viewport`** com origem no canto inferior esquerdo; converter a linha `y` da grade para que a linha 0 fique no **topo** (ou inverter ao desenhar).
- Ordem de desenho: fundo → moldura → células → displays → carinha → overlay de menus (Scene2D `Stage.draw()` por último).
- Otimização: em tabuleiros 30×16 (480 células), desenhar todas as células a cada frame é trivial; não é necessário cache complexo.
- Renderização sob demanda opcional: chamar `Gdx.graphics.requestRendering()` em eventos de input e a cada tique do cronômetro.

---

## 13. Fluxo da aplicação

```
Launcher (lwjgl3)
  └─► MinesweeperGame.create()
        ├─ Assets.load()
        ├─ Settings.load() / HighScores.load()
        └─ setScreen(new GameScreen(difficultyAtual))

GameScreen.render(delta):
    engine.update(delta)
    input é tratado por callbacks (InputProcessor)
    Gdx.gl.glClear(...)
    boardRenderer.draw(engine)
    hud.draw(engine)        // contador, carinha, cronômetro
    stage.act(delta); stage.draw()
```

Mudança de dificuldade: `engine.newGame(d)` → redimensionar janela → recalcular viewport/layout → resetar estados visuais.

---

## 14. Requisitos não funcionais

| Requisito | Critério |
|---|---|
| Desempenho | ≥ 60 FPS estável no tabuleiro 30×24 em hardware modesto |
| Latência de input | Resposta visual < 1 frame após soltar o botão |
| Robustez | Nenhum `StackOverflowError` ou exceção em qualquer tamanho permitido |
| Portabilidade | Executa em Windows, Linux e macOS com JRE 17+ |
| Manutenibilidade | Pacote `model` com cobertura de testes ≥ 80% |
| Acessibilidade | Números distinguíveis também por contraste; opção de escala 2x/3x |

---

## 15. Testes

### 15.1 Testes unitários (JUnit 5, módulo `core`)
1. **Contagem de minas** posicionadas = valor configurado, para cada dificuldade.
2. **Primeiro clique seguro:** em 10.000 execuções com seeds diferentes, a célula do clique e suas vizinhas nunca têm mina.
3. **`adjacentMines`** correto em cantos, bordas e centro (tabuleiros construídos manualmente via método de teste que injeta minas).
4. **Flood fill:** abre região esperada; não passa por bandeiras; não abre minas; não estoura a pilha em 30×24 sem minas ou com poucas minas.
5. **Chord:** abre vizinhos quando bandeiras == número; não abre quando diferente; explode se bandeira estiver errada.
6. **Vitória:** detectada exatamente quando a última célula segura é aberta; minas restantes recebem bandeira.
7. **Derrota:** estados `EXPLODED`, `MINE_SHOWN`, `WRONG_FLAG` corretos.
8. **Contador de minas:** pode ficar negativo; interrogação não conta.
9. **Cronômetro:** não inicia ao marcar bandeira; para em 999; para ao fim do jogo.
10. **Validação do nível personalizado:** clamp de colunas/linhas/minas.

### 15.2 Testes manuais (checklist)
- [ ] Todos os níveis iniciam com janela de tamanho correto.
- [ ] Botão direito alterna bandeira (e `?` se habilitado).
- [ ] Combinação esquerdo+direito e botão do meio executam chord.
- [ ] Arrastar com botão pressionado cancela ao soltar fora.
- [ ] Carinha muda corretamente em todos os estados.
- [ ] Recorde é salvo e persiste após reiniciar o aplicativo.
- [ ] Redimensionar a janela mantém o jogo funcional.

---

## 16. Roadmap de implementação (sugerido)

| Fase | Entregas |
|---|---|
| **1. Núcleo** | `Cell`, `Board`, `MinePlacer`, `GameEngine` + testes unitários |
| **2. Protótipo visual** | `GameScreen` com `ShapeRenderer` e `BitmapFont`, input básico (revelar/bandeira) |
| **3. HUD** | Contador de minas, cronômetro, carinha, reinício |
| **4. Interação avançada** | Chord, feedback de botão pressionado, arrastar/cancelar |
| **5. Níveis** | Seletor de dificuldade, nível personalizado, redimensionamento da janela |
| **6. Persistência** | Recordes, configurações |
| **7. Polimento** | Atlas de sprites, sons, escala de UI, tema |
| **8. Empacotamento** | JAR executável (`lwjgl3:dist`), instaladores opcionais (jpackage) |

---

## 17. Extensões futuras (opcionais)

- Backends Android (toque longo = bandeira) e HTML5/GWT/TeaVM.
- Modo "sem adivinhação" (gerador que garante tabuleiros resolvíveis por lógica).
- Estatísticas (jogos jogados, taxa de vitória, sequência).
- Temas visuais e skins.
- Replay da partida e compartilhamento de seed.
- Dicas (usando solver) com penalidade de tempo.

---

## 18. Critérios de aceitação (v1.0)

1. As regras descritas nas seções 3.1–3.11 são cumpridas integralmente.
2. Os três níveis fixos e o personalizado funcionam com os parâmetros da seção 3.3.
3. O primeiro clique nunca é uma mina (e abre área quando possível).
4. A interface contém contador de minas, cronômetro, carinha e tabuleiro conforme a seção 4.
5. Recordes persistem entre execuções.
6. Todos os testes unitários da seção 15.1 passam.
7. O jogo compila e roda com `./gradlew lwjgl3:run` e gera JAR com `./gradlew lwjgl3:dist`.
8. Nenhum asset protegido por direitos autorais do jogo original é utilizado.

---

## 19. Apêndice: esqueleto de código

### 19.1 Entrada do jogo

```java
public class MinesweeperGame extends Game {
    @Override
    public void create() {
        Assets.load();
        setScreen(new GameScreen(this, Settings.lastDifficulty()));
    }

    @Override
    public void dispose() {
        getScreen().dispose();
        Assets.dispose();
    }
}
```

### 19.2 Conversão de coordenadas do mouse para célula

```java
Vector3 v = new Vector3(screenX, screenY, 0);
viewport.unproject(v);
int cx = (int) ((v.x - boardOriginX) / cellSize);
int cy = rows - 1 - (int) ((v.y - boardOriginY) / cellSize); // linha 0 no topo
if (engine.board().inBounds(cx, cy)) { /* ... */ }
```

### 19.3 Detecção de chord (esquerdo + direito)

```java
@Override
public boolean touchDown(int sx, int sy, int pointer, int button) {
    if (button == Buttons.LEFT)  leftDown = true;
    if (button == Buttons.RIGHT) rightDown = true;
    if (button == Buttons.MIDDLE) middleDown = true;
    chordPending = (leftDown && rightDown) || middleDown;
    updatePressedPreview(sx, sy);
    return true;
}

@Override
public boolean touchUp(int sx, int sy, int pointer, int button) {
    Point cell = toCell(sx, sy);
    if (chordPending && cell != null) {
        engine.chord(cell.x, cell.y);
    } else if (button == Buttons.LEFT && cell != null && !rightDown) {
        engine.reveal(cell.x, cell.y);
    } else if (button == Buttons.RIGHT && cell != null && !leftDown) {
        engine.toggleFlag(cell.x, cell.y);
    }
    if (button == Buttons.LEFT)  leftDown = false;
    if (button == Buttons.RIGHT) rightDown = false;
    if (button == Buttons.MIDDLE) middleDown = false;
    if (!leftDown && !rightDown && !middleDown) chordPending = false;
    clearPressedPreview();
    return true;
}
```

> Observação: o clique direito simples costuma ser tratado em `touchDown` na maioria das implementações para resposta imediata; a abordagem acima (executar ao soltar) prioriza a detecção correta de chord. Escolher uma abordagem e mantê-la consistente.

### 19.4 `build.gradle` (core) — dependências mínimas

```groovy
dependencies {
    api "com.badlogicgames.gdx:gdx:$gdxVersion"
    testImplementation "org.junit.jupiter:junit-jupiter:5.10.2"
}
test { useJUnitPlatform() }
```

---

*Fim da especificação.*
