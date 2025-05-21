// Ludo Game Logic
document.addEventListener("DOMContentLoaded", () => {
    // Game Configuration
    let gameConfig = {
        mode: "user-only", // "user-only" or "user-computer"
        playerCount: 4, // 2 or 4
        turnControl: "manual", // "manual" or "auto"
        soundEnabled: true
    };

    // Player Data
    const players = [
        { id: "red",    name: "Red Player",    pieces: {}, startCell: 1,  homeEntryCell: 51, yard: "red-yard", isComputer: false, active: true },
        { id: "green",  name: "Green Player",  pieces: {}, startCell: 14, homeEntryCell: 12, yard: "green-yard", isComputer: false, active: true },
        { id: "yellow", name: "Yellow Player", pieces: {}, startCell: 27, homeEntryCell: 25, yard: "yellow-yard", isComputer: false, active: true },
        { id: "blue",   name: "Blue Player",   pieces: {}, startCell: 40, homeEntryCell: 38, yard: "blue-yard", isComputer: false, active: true },
    ];
    const totalCellsOnTrack = 52;
    const cellsInHomePath = 6;

    let currentPlayerIndex = 0;
    let currentDiceRoll = { d1: 0, d2: 0, sum: 0, d1_used: false, d2_used: false, rolled: false };
    let selectedMoveValue = 0;
    let awaitingPieceChoice = false;
    let gameInitialized = false;

    // DOM Elements
    const settingsToggle = document.getElementById("settings-toggle");
    const settingsPanel = document.getElementById("settings-panel");
    const applySettingsBtn = document.getElementById("apply-settings");
    const resetGameBtn = document.getElementById("reset-game");
    const playerSelectionBtns = document.querySelectorAll(".player-select-btn");
    const diceDisplayD1 = document.getElementById("dice-display-d1");
    const diceDisplayD2 = document.getElementById("dice-display-d2");
    const diceDisplaySum = document.getElementById("dice-display-sum");
    const rollDiceBtn = document.getElementById("roll-dice-btn");
    const playerTurnIndicator = document.getElementById("player-turn-indicator");
    const messageArea = document.getElementById("message-area");
    const gameBoard = document.querySelector(".game-board");
    const playerSelection = document.getElementById("player-selection");

    // Audio Elements
    const soundDiceRoll = document.getElementById("sound-dice-roll");
    const soundPieceMove = document.getElementById("sound-piece-move");
    const soundPieceCapture = document.getElementById("sound-piece-capture");
    const soundPieceHome = document.getElementById("sound-piece-home");
    const soundGameWin = document.getElementById("sound-game-win");
    const soundBackgroundMusic = document.getElementById("sound-background-music");

    // --- Settings Menu Handlers ---
    settingsToggle.addEventListener("click", () => {
        settingsPanel.classList.toggle("active");
    });

    document.addEventListener("click", (event) => {
        if (!settingsPanel.contains(event.target) && event.target !== settingsToggle) {
            settingsPanel.classList.remove("active");
        }
    });

    applySettingsBtn.addEventListener("click", () => {
        // Get settings values
        const mode = document.querySelector('input[name="game-mode"]:checked').value;
        const playerCount = parseInt(document.querySelector('input[name="player-count"]:checked').value);
        const turnControl = document.querySelector('input[name="turn-control"]:checked').value;
        const soundEnabled = document.querySelector('input[name="sound"]:checked').value === "on";

        // Update game configuration
        gameConfig = {
            mode,
            playerCount,
            turnControl,
            soundEnabled
        };

        // Apply settings
        applyGameSettings();
        settingsPanel.classList.remove("active");
    });

    resetGameBtn.addEventListener("click", () => {
        resetGame();
        settingsPanel.classList.remove("active");
    });

    function applyGameSettings() {
        // Reset game state
        resetGame();

        // Configure players based on settings
        players.forEach((player, index) => {
            // For 2 players, only use red and yellow
            if (gameConfig.playerCount === 2) {
                player.active = (index === 0 || index === 2); // Red and Yellow
            } else {
                player.active = true;
            }

            // Set computer players if in user-computer mode
            if (gameConfig.mode === "user-computer") {
                // For 2 players, make yellow a computer
                if (gameConfig.playerCount === 2) {
                    player.isComputer = (index === 2); // Yellow is computer
                } else {
                    // For 4 players, make green and blue computers
                    player.isComputer = (index === 1 || index === 3); // Green and Blue are computers
                }
            } else {
                player.isComputer = false;
            }
        });

        // Update player selection buttons
        updatePlayerSelectionButtons();

        // Initialize game
        if (!gameInitialized) {
            initializeGame();
            gameInitialized = true;
        } else {
            resetTurnState(true);
            updatePlayerTurnIndicator();
        }

        // Show/hide player selection based on turn control
        playerSelection.style.display = gameConfig.turnControl === "manual" ? "flex" : "none";

        // Update message
        messageArea.textContent = "Game settings applied. Roll the dice!";
    }

    function updatePlayerSelectionButtons() {
        playerSelectionBtns.forEach(btn => {
            const playerId = btn.dataset.player;
            const player = players.find(p => p.id === playerId);
            
            if (!player.active) {
                btn.classList.add("disabled");
                btn.disabled = true;
            } else {
                btn.classList.remove("disabled");
                btn.disabled = false;
                
                if (player.isComputer) {
                    btn.classList.add("computer");
                    btn.textContent = `${playerId.charAt(0).toUpperCase() + playerId.slice(1)} (AI)`;
                } else {
                    btn.classList.remove("computer");
                    btn.textContent = playerId.charAt(0).toUpperCase() + playerId.slice(1);
                }
            }
        });
    }

    // --- Player Selection Handlers ---
    playerSelectionBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            if (gameConfig.turnControl !== "manual" || btn.disabled) return;
            
            const playerId = btn.dataset.player;
            const playerIndex = players.findIndex(p => p.id === playerId);
            
            if (playerIndex !== -1 && players[playerIndex].active) {
                currentPlayerIndex = playerIndex;
                updatePlayerTurnIndicator();
                resetTurnState(true);
                messageArea.textContent = `${players[currentPlayerIndex].name}'s Turn. Roll the dice!`;
                
                // Update active button
                playerSelectionBtns.forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
            }
        });
    });

    // --- Game Setup ---
    function initializeGame() {
        createBoardCells();
        initializePieces();
        resetTurnState(true);
        updatePlayerTurnIndicator();
        messageArea.textContent = "Roll the dice to start!";
        
        [diceDisplayD1, diceDisplayD2, diceDisplaySum].forEach(el => {
            el.addEventListener("click", handleDiceDisplayClick);
        });

        // Set first player button as active
        if (gameConfig.turnControl === "manual") {
            const firstActivePlayer = players.find(p => p.active);
            if (firstActivePlayer) {
                const btn = document.querySelector(`.player-select-btn[data-player="${firstActivePlayer.id}"]`);
                if (btn) btn.classList.add("active");
            }
        }

        if (gameConfig.soundEnabled && soundBackgroundMusic) {
            soundBackgroundMusic.volume = 0.3;
            soundBackgroundMusic.play().catch(e => {
                console.warn("Background music autoplay failed. User interaction might be needed.", e);
                document.body.addEventListener("click", () => {
                    if (soundBackgroundMusic.paused) {
                        soundBackgroundMusic.play().catch(err => console.warn("BG music play on click failed", err));
                    }
                }, { once: true });
            });
        }
    }

    function createBoardCells() {
        const pathCoordinates = [
            { r: 7, c: 1 }, { r: 7, c: 2 }, { r: 7, c: 3 }, { r: 7, c: 4 }, { r: 7, c: 5 }, { r: 7, c: 6 },
            { r: 6, c: 7 }, { r: 5, c: 7 }, { r: 4, c: 7 }, { r: 3, c: 7 }, { r: 2, c: 7 }, { r: 1, c: 7 },
            { r: 1, c: 8 },
            { r: 1, c: 9 }, { r: 2, c: 9 }, { r: 3, c: 9 }, { r: 4, c: 9 }, { r: 5, c: 9 }, { r: 6, c: 9 },
            { r: 7, c: 10 }, { r: 7, c: 11 }, { r: 7, c: 12 }, { r: 7, c: 13 }, { r: 7, c: 14 }, { r: 7, c: 15 },
            { r: 8, c: 15 },
            { r: 9, c: 15 }, { r: 9, c: 14 }, { r: 9, c: 13 }, { r: 9, c: 12 }, { r: 9, c: 11 }, { r: 9, c: 10 },
            { r: 10, c: 9 }, { r: 11, c: 9 }, { r: 12, c: 9 }, { r: 13, c: 9 }, { r: 14, c: 9 }, { r: 15, c: 9 },
            { r: 15, c: 8 },
            { r: 15, c: 7 }, { r: 14, c: 7 }, { r: 13, c: 7 }, { r: 12, c: 7 }, { r: 11, c: 7 }, { r: 10, c: 7 },
            { r: 9, c: 6 }, { r: 9, c: 5 }, { r: 9, c: 4 }, { r: 9, c: 3 }, { r: 9, c: 2 }, { r: 9, c: 1 },
            { r: 8, c: 1 }
        ];
        pathCoordinates.forEach((coord, i) => {
            const cell = document.createElement("div");
            cell.classList.add("cell", "track-cell");
            cell.id = `cell-${i}`;
            cell.style.gridRowStart = coord.r;
            cell.style.gridColumnStart = coord.c;
            if ([0, 8, 13, 21, 26, 34, 39, 47].includes(i)) { 
                cell.classList.add("safe-zone");
            }
            // Player start cells (actual landing cell from yard - which is the second cell of their path)
            // player.startCell is 1-indexed for the *first* cell of their path.
            // So, the 0-indexed *second* cell of their path is player.startCell itself.
            if (i === players[0].startCell) cell.classList.add("start-cell-red");
            if (i === players[1].startCell) cell.classList.add("start-cell-green");
            if (i === players[2].startCell) cell.classList.add("start-cell-yellow");
            if (i === players[3].startCell) cell.classList.add("start-cell-blue");
            gameBoard.appendChild(cell);
        });
        const homePathCoords = {
            red: [{r:8,c:2},{r:8,c:3},{r:8,c:4},{r:8,c:5},{r:8,c:6},{r:8,c:7}],
            green: [{r:2,c:8},{r:3,c:8},{r:4,c:8},{r:5,c:8},{r:6,c:8},{r:7,c:8}],
            yellow: [{r:8,c:14},{r:8,c:13},{r:8,c:12},{r:8,c:11},{r:8,c:10},{r:8,c:9}],
            blue: [{r:14,c:8},{r:13,c:8},{r:12,c:8},{r:11,c:8},{r:10,c:8},{r:9,c:8}]
        };
        players.forEach(player => {
            homePathCoords[player.id].forEach((coord, i) => {
                const cell = document.createElement("div");
                cell.classList.add("cell", `home-path-cell-${player.id}`);
                cell.id = `${player.id}-home-cell-${i}`;
                cell.style.gridRowStart = coord.r;
                cell.style.gridColumnStart = coord.c;
                gameBoard.appendChild(cell);
            });
        });
    }

    function initializePieces() {
        players.forEach(player => {
            player.pieces = {};
            const yard = document.getElementById(`${player.id}-yard`);
            const pieceElements = yard.querySelectorAll(`.piece-${player.id}`);
            pieceElements.forEach((pieceElement) => {
                const pieceId = parseInt(pieceElement.dataset.pieceId);
                player.pieces[pieceId] = {
                    element: pieceElement,
                    position: -1, 
                    isHome: false
                };
                pieceElement.addEventListener("click", () => handlePieceClick(player.id, pieceId));
            });
        });
    }

    function playSound(soundType) {
        if (!gameConfig.soundEnabled) return;
        
        // Check if running in Android WebView
        if (typeof Android !== "undefined") {
            // Call Android interface methods
            switch(soundType) {
                case "dice-roll": Android.playDiceRollSound(); break;
                case "piece-move": Android.playPieceMoveSound(); break;
                case "piece-capture": Android.playPieceCaptureSound(); break;
                case "piece-home": Android.playPieceHomeSound(); break;
                case "game-win": Android.playGameWinSound(); break;
                case "background-music": Android.playBackgroundMusic(true); break;
            }
        } else {
            // Use HTML5 audio
            let soundElement;
            switch(soundType) {
                case "dice-roll": soundElement = soundDiceRoll; break;
                case "piece-move": soundElement = soundPieceMove; break;
                case "piece-capture": soundElement = soundPieceCapture; break;
                case "piece-home": soundElement = soundPieceHome; break;
                case "game-win": soundElement = soundGameWin; break;
                case "background-music": soundElement = soundBackgroundMusic; break;
            }
            
            if (soundElement && soundElement !== soundBackgroundMusic) {
                soundElement.currentTime = 0;
            }
            if (soundElement) {
                soundElement.play()
                    .catch(e => {
                        console.warn("Audio play failed for " + soundType + ": ", e);
                    });
            }
        }
    }

    function updatePlayerTurnIndicator() {
        const currentPlayer = players[currentPlayerIndex];
        playerTurnIndicator.textContent = `${currentPlayer.name}\'s Turn`;
        playerTurnIndicator.style.color = currentPlayer.id;
        
        // Update player selection buttons
        if (gameConfig.turnControl === "manual") {
            playerSelectionBtns.forEach(btn => {
                if (btn.dataset.player === currentPlayer.id) {
                    btn.classList.add("active");
                } else {
                    btn.classList.remove("active");
                }
            });
        }
    }

    function resetTurnState(fullReset = true) {
        if (fullReset) {
            currentDiceRoll = { d1: 0, d2: 0, sum: 0, d1_used: false, d2_used: false, rolled: false };
            diceDisplayD1.textContent = "";
            diceDisplayD2.textContent = "";
            diceDisplaySum.textContent = "";
            diceDisplayD1.classList.remove("dice-rolled", "active-dice");
            diceDisplayD2.classList.remove("dice-rolled", "active-dice");
            diceDisplaySum.classList.remove("dice-rolled", "active-dice");
            rollDiceBtn.disabled = false;
        }
        selectedMoveValue = 0;
        awaitingPieceChoice = false;
        clearHighlights();
        if (!fullReset) {
            [diceDisplayD1, diceDisplayD2, diceDisplaySum].forEach(el => el.classList.remove("active-dice"));
        }
    }

    function resetGame() {
        // Clear the board
        const cells = document.querySelectorAll(".cell");
        cells.forEach(cell => {
            const pieces = cell.querySelectorAll(".piece");
            pieces.forEach(piece => {
                const playerId = piece.dataset.player;
                const pieceId = parseInt(piece.dataset.pieceId);
                const yard = document.getElementById(`${playerId}-yard`);
                const homes = yard.querySelectorAll(".piece-home");
                homes[pieceId].appendChild(piece);
            });
        });

        // Reset player pieces
        players.forEach(player => {
            for (const pieceId in player.pieces) {
                player.pieces[pieceId].position = -1;
                player.pieces[pieceId].isHome = false;
            }
        });

        // Reset game state
        currentPlayerIndex = 0;
        while (!players[currentPlayerIndex].active) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        }
        
        resetTurnState(true);
        updatePlayerTurnIndicator();
        messageArea.textContent = "Game reset. Roll the dice!";
    }

    rollDiceBtn.addEventListener("click", () => {
        if (currentDiceRoll.rolled && (!currentDiceRoll.d1_used || !currentDiceRoll.d2_used)) {
            messageArea.textContent = "Please complete your current move(s) or select a dice value.";
            return;
        }
        
        resetTurnState(true);
        rollDiceBtn.disabled = true;

        currentDiceRoll.d1 = Math.floor(Math.random() * 6) + 1;
        currentDiceRoll.d2 = Math.floor(Math.random() * 6) + 1;
        currentDiceRoll.sum = currentDiceRoll.d1 + currentDiceRoll.d2;
        currentDiceRoll.d1_used = false;
        currentDiceRoll.d2_used = false;
        currentDiceRoll.rolled = true;

        diceDisplayD1.textContent = currentDiceRoll.d1;
        diceDisplayD1.classList.add("dice-rolled");
        diceDisplayD2.textContent = currentDiceRoll.d2;
        diceDisplayD2.classList.add("dice-rolled");
        diceDisplaySum.textContent = currentDiceRoll.sum;
        diceDisplaySum.classList.add("dice-rolled");
        playSound("dice-roll");
        
        const currentPlayer = players[currentPlayerIndex];
        let hasPiecesOnTrack = false;
        for (const pieceId in currentPlayer.pieces) {
            if (currentPlayer.pieces[pieceId].position !== -1 && !currentPlayer.pieces[pieceId].isHome) {
                hasPiecesOnTrack = true;
                break;
            }
        }

        if (!hasPiecesOnTrack && currentDiceRoll.d1 !== 6 && currentDiceRoll.d2 !== 6) {
            messageArea.textContent = `Rolled ${currentDiceRoll.d1} & ${currentDiceRoll.d2}. No 6 to bring a piece out. Next player.`;
            currentDiceRoll.d1_used = true;
            currentDiceRoll.d2_used = true;
            diceDisplayD1.classList.remove("dice-rolled");
            diceDisplayD2.classList.remove("dice-rolled");
            diceDisplaySum.classList.remove("dice-rolled");
            setTimeout(() => switchPlayer(), 1500);
            return;
        }

        messageArea.textContent = `Rolled ${currentDiceRoll.d1} & ${currentDiceRoll.d2}. Click a dice value to use.`;
        
        // If current player is computer, make AI move
        if (currentPlayer.isComputer) {
            setTimeout(() => makeAIMove(), 1000);
        }
    });

    function handleDiceDisplayClick(event) {
        const currentPlayer = players[currentPlayerIndex];
        
        // If current player is computer, ignore clicks
        if (currentPlayer.isComputer) {
            messageArea.textContent = "It's the computer's turn. Please wait.";
            return;
        }
        
        if (!currentDiceRoll.rolled || awaitingPieceChoice) {
            messageArea.textContent = "Roll the dice first, or complete your current piece move.";
            return;
        }

        const choice = event.target.dataset.value;
        let tempSelectedValue = 0;

        if (choice === "sum") {
            if (currentDiceRoll.d1_used || currentDiceRoll.d2_used) {
                messageArea.textContent = "Cannot use sum if individual dice are already used.";
                return;
            }
            if (!diceDisplaySum.classList.contains("dice-rolled")) {
                messageArea.textContent = "Sum has already been used or is not applicable.";
                return;
            }
            tempSelectedValue = currentDiceRoll.sum;
        } else if (choice === "d1") {
            if (currentDiceRoll.d1_used || !diceDisplayD1.classList.contains("dice-rolled")) {
                messageArea.textContent = "Die 1 already used or not available.";
                return;
            }
            tempSelectedValue = currentDiceRoll.d1;
        } else if (choice === "d2") {
            if (currentDiceRoll.d2_used || !diceDisplayD2.classList.contains("dice-rolled")) {
                messageArea.textContent = "Die 2 already used or not available.";
                return;
            }
            tempSelectedValue = currentDiceRoll.d2;
        }

        if (tempSelectedValue === 0) return;

        const player = players[currentPlayerIndex];
        let isSixSelectedForYard = false;
        if (choice === "d1" && currentDiceRoll.d1 === 6) isSixSelectedForYard = true;
        if (choice === "d2" && currentDiceRoll.d2 === 6) isSixSelectedForYard = true;
        if (choice === "sum" && tempSelectedValue === 6 && (currentDiceRoll.d1 === 6 || currentDiceRoll.d2 === 6) ) isSixSelectedForYard = true;

        const movablePieces = getMovablePieces(player.id, tempSelectedValue, isSixSelectedForYard);
        
        if (movablePieces.length === 0) {
            messageArea.textContent = `No movable pieces for ${tempSelectedValue}. Try another dice value if available.`;
            return;
        }

        selectedMoveValue = tempSelectedValue; 
        [diceDisplayD1, diceDisplayD2, diceDisplaySum].forEach(el => el.classList.remove("active-dice"));
        event.target.classList.add("active-dice");

        highlightMovablePieces(movablePieces, true);
        awaitingPieceChoice = true;
        messageArea.textContent = `Click a piece to move ${selectedMoveValue} steps.`;
    }

    function handlePieceClick(playerId, pieceId) {
        const currentPlayer = players[currentPlayerIndex];
        
        // If current player is computer, ignore clicks
        if (currentPlayer.isComputer) {
            messageArea.textContent = "It's the computer's turn. Please wait.";
            return;
        }
        
        if (playerId !== currentPlayer.id || !awaitingPieceChoice || selectedMoveValue === 0) {
            return;
        }
        
        const piece = currentPlayer.pieces[pieceId];
        const isMovable = piece.element.classList.contains("movable"); 
        if (!isMovable) {
            messageArea.textContent = "This piece cannot be moved with the selected dice value.";
            return;
        }
        clearHighlights();
        movePiece(currentPlayer, pieceId, selectedMoveValue);
    }
    
    function getMovablePieces(playerId, moveValue, isYardExitAttemptWithSelectedSix) {
        const player = players.find(p => p.id === playerId);
        const movable = [];
        for (const pieceIdStr in player.pieces) {
            const pieceId = parseInt(pieceIdStr);
            const piece = player.pieces[pieceId];
            if (piece.isHome) continue;

            if (piece.position === -1) { // In yard
                 // player.startCell is 1-indexed for the *first* cell of their path.
                 // The 0-indexed *second* cell (actual landing spot) is player.startCell.
                if (isYardExitAttemptWithSelectedSix && moveValue === 6) { 
                    const targetBoardPos = player.startCell; 
                    movable.push({ playerId, pieceId, newPosition: targetBoardPos, isYardMove: true });
                }
            } else if (piece.position >= 100) { // On home path
                const currentHomePathPos = piece.position - 100; 
                if (currentHomePathPos + moveValue < cellsInHomePath) {
                    movable.push({ playerId, pieceId, newPosition: piece.position + moveValue });
                } else if (currentHomePathPos + moveValue === cellsInHomePath -1) { 
                     movable.push({ playerId, pieceId, newPosition: 200 }); 
                }
            } else { // On main track
                let potentialNewPos;
                const homeEntryCellIndex = player.homeEntryCell -1; 
                let entersHomePath = false;
                let newHomePathPos = -1;
                let currentGlobalPos = piece.position;
                let playerSpecificHomeEntry = player.homeEntryCell -1;

                // Placeholder for robust home entry logic
                if (currentGlobalPos <= playerSpecificHomeEntry && (currentGlobalPos + moveValue) > playerSpecificHomeEntry) {
                    const stepsIntoHomePath = (currentGlobalPos + moveValue) - (playerSpecificHomeEntry + 1);
                    if (stepsIntoHomePath >= 0 && stepsIntoHomePath < cellsInHomePath) {
                        entersHomePath = true; newHomePathPos = 100 + stepsIntoHomePath;
                    } else if (stepsIntoHomePath === cellsInHomePath -1) {
                        entersHomePath = true; newHomePathPos = 200;
                    }
                }
                
                if (entersHomePath) {
                    movable.push({ playerId, pieceId, newPosition: newHomePathPos });
                } else {
                    potentialNewPos = (piece.position + moveValue) % totalCellsOnTrack;
                    movable.push({ playerId, pieceId, newPosition: potentialNewPos });
                }
            }
        }
        return movable;
    }

    function highlightMovablePieces(movablePieces, highlight) {
        movablePieces.forEach(({ playerId, pieceId }) => {
            const player = players.find(p => p.id === playerId);
            if (player && player.pieces[pieceId]) {
                if (highlight) {
                    player.pieces[pieceId].element.classList.add("movable");
                } else {
                    player.pieces[pieceId].element.classList.remove("movable");
                }
            }
        });
    }

    function clearHighlights() {
        players.forEach(player => {
            for (const pieceId in player.pieces) {
                player.pieces[pieceId].element.classList.remove("movable");
            }
        });
    }

    function movePiece(player, pieceId, moveValue) {
        const piece = player.pieces[pieceId];
        const oldPosition = piece.position;
        let newPosition;
        let isYardMove = false;
        let isHomeMove = false;
        let isCapture = false;
        let capturedPiece = null;

        // Determine new position
        if (oldPosition === -1) { // Moving from yard
            newPosition = player.startCell;
            isYardMove = true;
        } else if (oldPosition >= 100) { // Moving on home path
            const currentHomePathPos = oldPosition - 100;
            if (currentHomePathPos + moveValue < cellsInHomePath) {
                newPosition = oldPosition + moveValue;
            } else if (currentHomePathPos + moveValue === cellsInHomePath - 1) {
                newPosition = 200; // Reached home
                isHomeMove = true;
            }
        } else { // Moving on main track
            const homeEntryCellIndex = player.homeEntryCell - 1;
            
            // Check if piece will enter home path
            if (oldPosition <= homeEntryCellIndex && (oldPosition + moveValue) > homeEntryCellIndex) {
                const stepsIntoHomePath = (oldPosition + moveValue) - (homeEntryCellIndex + 1);
                if (stepsIntoHomePath >= 0 && stepsIntoHomePath < cellsInHomePath) {
                    newPosition = 100 + stepsIntoHomePath;
                } else if (stepsIntoHomePath === cellsInHomePath - 1) {
                    newPosition = 200; // Reached home
                    isHomeMove = true;
                }
            } else {
                newPosition = (oldPosition + moveValue) % totalCellsOnTrack;
            }
        }

        // Update piece position
        piece.position = newPosition;

        // Move piece on the board
        if (isYardMove) {
            // Move from yard to board
            const targetCell = document.getElementById(`cell-${newPosition}`);
            targetCell.appendChild(piece.element);
            playSound("piece-move");
        } else if (isHomeMove) {
            // Move to center home
            const centerHome = document.querySelector(".center-home");
            centerHome.appendChild(piece.element);
            piece.isHome = true;
            playSound("piece-home");
        } else if (newPosition >= 100 && newPosition < 200) {
            // Move to home path
            const homePathIndex = newPosition - 100;
            const targetCell = document.getElementById(`${player.id}-home-cell-${homePathIndex}`);
            targetCell.appendChild(piece.element);
            playSound("piece-move");
        } else {
            // Move on main track
            const targetCell = document.getElementById(`cell-${newPosition}`);
            
            // Check for capture
            const existingPieces = targetCell.querySelectorAll(".piece");
            existingPieces.forEach(existingPiece => {
                const existingPlayerId = existingPiece.dataset.player;
                const existingPieceId = parseInt(existingPiece.dataset.pieceId);
                
                if (existingPlayerId !== player.id) {
                    // Found opponent piece to capture
                    const opponentPlayer = players.find(p => p.id === existingPlayerId);
                    if (opponentPlayer) {
                        capturedPiece = { player: opponentPlayer, pieceId: existingPieceId };
                        isCapture = true;
                    }
                }
            });
            
            // Move piece to target cell
            targetCell.appendChild(piece.element);
            
            // Handle capture
            if (isCapture && capturedPiece) {
                const opponentPiece = capturedPiece.player.pieces[capturedPiece.pieceId];
                opponentPiece.position = -1; // Back to yard
                
                // Move captured piece back to yard
                const opponentYard = document.getElementById(capturedPiece.player.yard);
                const opponentHome = opponentYard.querySelectorAll(".piece-home")[capturedPiece.pieceId];
                opponentHome.appendChild(opponentPiece.element);
                
                playSound("piece-capture");
                messageArea.textContent = `${player.name} captured ${capturedPiece.player.name}'s piece!`;
            } else {
                playSound("piece-move");
            }
        }

        // Update dice usage
        if (moveValue === currentDiceRoll.d1) {
            currentDiceRoll.d1_used = true;
            diceDisplayD1.classList.remove("dice-rolled");
        } else if (moveValue === currentDiceRoll.d2) {
            currentDiceRoll.d2_used = true;
            diceDisplayD2.classList.remove("dice-rolled");
        } else if (moveValue === currentDiceRoll.sum) {
            currentDiceRoll.d1_used = true;
            currentDiceRoll.d2_used = true;
            diceDisplayD1.classList.remove("dice-rolled");
            diceDisplayD2.classList.remove("dice-rolled");
            diceDisplaySum.classList.remove("dice-rolled");
        }

        // Reset for next move
        resetTurnState(false);

        // Check if player has won
        if (checkPlayerWin(player)) {
            playSound("game-win");
            messageArea.textContent = `${player.name} has won the game!`;
            rollDiceBtn.disabled = true;
            return;
        }

        // Check if player gets another turn (double sixes)
        const isDoubleSix = currentDiceRoll.d1 === 6 && currentDiceRoll.d2 === 6;
        
        // Check if there are more moves available
        if (!currentDiceRoll.d1_used || !currentDiceRoll.d2_used) {
            messageArea.textContent = "Select another dice value to continue your turn.";
        } else {
            // All dice used, check for double six
            if (isDoubleSix) {
                messageArea.textContent = "Double six! Roll again.";
                setTimeout(() => {
                    resetTurnState(true);
                    // For computer players, automatically roll again
                    if (player.isComputer) {
                        setTimeout(() => rollDiceBtn.click(), 1000);
                    }
                }, 1000);
            } else {
                // Switch to next player
                setTimeout(() => switchPlayer(), 1000);
            }
        }
        
        // If current player is computer and has more moves, continue AI turn
        if (player.isComputer && (!currentDiceRoll.d1_used || !currentDiceRoll.d2_used)) {
            setTimeout(() => makeAIMove(), 1000);
        }
    }

    function checkPlayerWin(player) {
        let allPiecesHome = true;
        for (const pieceId in player.pieces) {
            if (!player.pieces[pieceId].isHome) {
                allPiecesHome = false;
                break;
            }
        }
        return allPiecesHome;
    }

    function switchPlayer() {
        // Find next active player
        let nextPlayerIndex = currentPlayerIndex;
        do {
            nextPlayerIndex = (nextPlayerIndex + 1) % players.length;
        } while (!players[nextPlayerIndex].active && nextPlayerIndex !== currentPlayerIndex);
        
        // Update current player
        currentPlayerIndex = nextPlayerIndex;
        updatePlayerTurnIndicator();
        resetTurnState(true);
        
        const currentPlayer = players[currentPlayerIndex];
        messageArea.textContent = `${currentPlayer.name}'s Turn. Roll the dice!`;
        
        // If current player is computer, make AI move
        if (currentPlayer.isComputer) {
            setTimeout(() => {
                rollDiceBtn.click();
            }, 1000);
        }
    }

    // --- AI Logic ---
    function makeAIMove() {
        const currentPlayer = players[currentPlayerIndex];
        if (!currentPlayer.isComputer) return;
        
        // If dice not rolled yet, roll dice
        if (!currentDiceRoll.rolled) {
            rollDiceBtn.click();
            return;
        }
        
        // Choose dice value
        let bestDiceValue = 0;
        let bestMove = null;
        
        // Try sum first if available
        if (!currentDiceRoll.d1_used && !currentDiceRoll.d2_used) {
            const isSixSelectedForYard = currentDiceRoll.sum === 6 && (currentDiceRoll.d1 === 6 || currentDiceRoll.d2 === 6);
            const movesWithSum = getMovablePieces(currentPlayer.id, currentDiceRoll.sum, isSixSelectedForYard);
            if (movesWithSum.length > 0) {
                bestDiceValue = currentDiceRoll.sum;
                bestMove = evaluateBestMove(movesWithSum);
            }
        }
        
        // Try individual dice if sum not used
        if (bestDiceValue === 0) {
            // Try die 1
            if (!currentDiceRoll.d1_used) {
                const isSixSelectedForYard = currentDiceRoll.d1 === 6;
                const movesWithD1 = getMovablePieces(currentPlayer.id, currentDiceRoll.d1, isSixSelectedForYard);
                if (movesWithD1.length > 0) {
                    bestDiceValue = currentDiceRoll.d1;
                    bestMove = evaluateBestMove(movesWithD1);
                }
            }
            
            // Try die 2 if die 1 not used
            if (bestDiceValue === 0 && !currentDiceRoll.d2_used) {
                const isSixSelectedForYard = currentDiceRoll.d2 === 6;
                const movesWithD2 = getMovablePieces(currentPlayer.id, currentDiceRoll.d2, isSixSelectedForYard);
                if (movesWithD2.length > 0) {
                    bestDiceValue = currentDiceRoll.d2;
                    bestMove = evaluateBestMove(movesWithD2);
                }
            }
        }
        
        // Make the move if found
        if (bestDiceValue > 0 && bestMove) {
            // Highlight the selected dice
            if (bestDiceValue === currentDiceRoll.d1) {
                diceDisplayD1.classList.add("active-dice");
            } else if (bestDiceValue === currentDiceRoll.d2) {
                diceDisplayD2.classList.add("active-dice");
            } else if (bestDiceValue === currentDiceRoll.sum) {
                diceDisplaySum.classList.add("active-dice");
            }
            
            // Highlight the selected piece
            selectedMoveValue = bestDiceValue;
            awaitingPieceChoice = true;
            currentPlayer.pieces[bestMove.pieceId].element.classList.add("movable");
            
            // Show message
            messageArea.textContent = `Computer selects ${bestDiceValue} and moves a piece.`;
            
            // Make the move after a delay
            setTimeout(() => {
                clearHighlights();
                movePiece(currentPlayer, bestMove.pieceId, bestDiceValue);
            }, 1000);
        } else {
            // No valid moves, switch player
            messageArea.textContent = "Computer has no valid moves. Next player.";
            setTimeout(() => switchPlayer(), 1000);
        }
    }

    function evaluateBestMove(possibleMoves) {
        if (possibleMoves.length === 0) return null;
        if (possibleMoves.length === 1) return possibleMoves[0];
        
        // Score each move
        const scoredMoves = possibleMoves.map(move => {
            let score = 0;
            const currentPlayer = players.find(p => p.id === move.playerId);
            
            // Prioritize getting pieces out of yard
            if (move.isYardMove) {
                score += 50;
            }
            
            // Prioritize getting pieces home
            if (move.newPosition === 200) {
                score += 100;
            }
            
            // Prioritize moving pieces onto home path
            if (move.newPosition >= 100 && move.newPosition < 200) {
                score += 70;
            }
            
            // Check if move lands on safe zone
            if (move.newPosition < 100) {
                const safeZones = [0, 8, 13, 21, 26, 34, 39, 47];
                if (safeZones.includes(move.newPosition)) {
                    score += 30;
                }
            }
            
            // Check if move captures opponent
            const targetCell = move.newPosition < 100 ? document.getElementById(`cell-${move.newPosition}`) : null;
            if (targetCell) {
                const existingPieces = targetCell.querySelectorAll(".piece");
                existingPieces.forEach(existingPiece => {
                    const existingPlayerId = existingPiece.dataset.player;
                    if (existingPlayerId !== currentPlayer.id) {
                        score += 80; // High priority for captures
                    }
                });
            }
            
            // Add some randomness to avoid predictable AI
            score += Math.random() * 10;
            
            return { ...move, score };
        });
        
        // Sort by score (highest first) and return the best move
        scoredMoves.sort((a, b) => b.score - a.score);
        return scoredMoves[0];
    }

    // Initialize settings panel
    applyGameSettings();
});
