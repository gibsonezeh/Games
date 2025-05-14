// Ludo Game Logic
document.addEventListener("DOMContentLoaded", () => {
    const players = [
        { id: "red",    name: "Red Player",    pieces: {}, startCell: 1,  homeEntryCell: 51, yard: "red-yard" }, // startCell is 1-indexed, actual board cell index is startCell-1 for first path cell
        { id: "green",  name: "Green Player",  pieces: {}, startCell: 14, homeEntryCell: 12, yard: "green-yard" },
        { id: "yellow", name: "Yellow Player", pieces: {}, startCell: 27, homeEntryCell: 25, yard: "yellow-yard" },
        { id: "blue",   name: "Blue Player",   pieces: {}, startCell: 40, homeEntryCell: 38, yard: "blue-yard" },
    ];
    const totalCellsOnTrack = 52;
    const cellsInHomePath = 6;

    let currentPlayerIndex = 0;
    let currentDiceRoll = { d1: 0, d2: 0, sum: 0, d1_used: false, d2_used: false, rolled: false };
    let selectedMoveValue = 0;
    let awaitingPieceChoice = false;

    // DOM Elements
    const diceDisplayD1 = document.getElementById("dice-display-d1");
    const diceDisplayD2 = document.getElementById("dice-display-d2");
    const diceDisplaySum = document.getElementById("dice-display-sum");
    const rollDiceBtn = document.getElementById("roll-dice-btn");
    const playerTurnIndicator = document.getElementById("player-turn-indicator");
    const messageArea = document.getElementById("message-area");
    const gameBoard = document.querySelector(".game-board");

    // Audio Elements
    const soundDiceRoll = document.getElementById("sound-dice-roll");
    const soundPieceMove = document.getElementById("sound-piece-move");
    const soundPieceCapture = document.getElementById("sound-piece-capture");
    const soundPieceHome = document.getElementById("sound-piece-home");
    const soundGameWin = document.getElementById("sound-game-win");
    const soundBackgroundMusic = document.getElementById("sound-background-music");

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

        if (soundBackgroundMusic) {
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

    function playSound(soundElement) {
        if (soundElement && soundElement !== soundBackgroundMusic) {
            soundElement.currentTime = 0;
        }
        if (soundElement) {
            soundElement.play()
                .catch(e => {
                    console.warn("Audio play failed for " + (soundElement.id || "audio element") + ": ", e);
                });
        } else {
            console.warn("playSound called with a null soundElement.");
        }
    }

    function updatePlayerTurnIndicator() {
        playerTurnIndicator.textContent = `${players[currentPlayerIndex].name}\'s Turn`;
        playerTurnIndicator.style.color = players[currentPlayerIndex].id;
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
        playSound(soundDiceRoll);
        
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
    });

    function handleDiceDisplayClick(event) {
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
        if (playerId !== players[currentPlayerIndex].id || !awaitingPieceChoice || selectedMoveValue === 0) {
            return;
        }
        const player = players[currentPlayerIndex];
        const piece = player.pieces[pieceId];
        const isMovable = piece.element.classList.contains("movable"); 
        if (!isMovable) {
            messageArea.textContent = "This piece cannot be moved with the selected dice value.";
            return;
        }
        clearHighlights();
        movePiece(player, pieceId, selectedMoveValue);
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

    function clearHighlights() {
        const allPieces = document.querySelectorAll(".piece");
        allPieces.forEach(p => p.classList.remove("movable"));
    }

    function highlightMovablePieces(piecesToHighlight, highlight) {
        clearHighlights();
        if (highlight) {
            piecesToHighlight.forEach(pInfo => {
                const player = players.find(p => p.id === pInfo.playerId);
                player.pieces[pInfo.pieceId].element.classList.add("movable");
            });
        }
    }

    function movePiece(player, pieceId, moveValueToUse) {
        const pieceData = player.pieces[pieceId];
        let newPositionOnBoardId; 
        let newPieceDataPosition; 
        let capturedOpponentThisMove = false;
        let pieceReachedHomeThisMove = false;

        const isYardExit = pieceData.position === -1 && moveValueToUse === 6 && 
                           ((selectedMoveValue === currentDiceRoll.d1 && currentDiceRoll.d1 === 6) || 
                            (selectedMoveValue === currentDiceRoll.d2 && currentDiceRoll.d2 === 6) ||
                            (selectedMoveValue === currentDiceRoll.sum && (currentDiceRoll.d1 === 6 || currentDiceRoll.d2 === 6) && moveValueToUse === 6) );

        if (isYardExit) {
            newPieceDataPosition = player.startCell; // This is the 0-indexed second cell of the path
            newPositionOnBoardId = `cell-${newPieceDataPosition}`;
        } else {
            const isSixSelectedForYard = (moveValueToUse === 6 && 
                                         ((selectedMoveValue === currentDiceRoll.d1 && currentDiceRoll.d1 === 6) ||
                                          (selectedMoveValue === currentDiceRoll.d2 && currentDiceRoll.d2 === 6) ||
                                          (selectedMoveValue === currentDiceRoll.sum && (currentDiceRoll.d1 === 6 || currentDiceRoll.d2 === 6) && moveValueToUse === 6)));
            const confirmedMoves = getMovablePieces(player.id, moveValueToUse, isSixSelectedForYard);
            const specificMove = confirmedMoves.find(m => m.pieceId === pieceId);
            if (!specificMove) {
                console.error("Error: Piece was clicked but no valid move found in movePiece.");
                messageArea.textContent = "Error processing move. Try again.";
                resetTurnState(false); 
                messageArea.textContent = `Rolled ${currentDiceRoll.d1} & ${currentDiceRoll.d2}. Click a dice value to use.`;
                return;
            }
            newPieceDataPosition = specificMove.newPosition;
            if (newPieceDataPosition === 200) { pieceData.isHome = true; newPositionOnBoardId = null; pieceReachedHomeThisMove = true; }
            else if (newPieceDataPosition >= 100) { newPositionOnBoardId = `${player.id}-home-cell-${newPieceDataPosition - 100}`; }
            else { newPositionOnBoardId = `cell-${newPieceDataPosition}`; }
        }
        
        playSound(soundPieceMove);
        pieceData.position = newPieceDataPosition;
        const pieceElement = pieceData.element;

        if (newPositionOnBoardId) {
            const targetCell = document.getElementById(newPositionOnBoardId);
            if (targetCell) {
                if (newPieceDataPosition < 100 && !targetCell.classList.contains("safe-zone")) {
                    const opponentPieceElements = targetCell.querySelectorAll(".piece");
                    opponentPieceElements.forEach(opEl => {
                        if (opEl !== pieceElement && opEl.dataset.player !== player.id) {
                            capturePiece(opEl);
                            capturedOpponentThisMove = true; 
                        }
                    });
                }
                targetCell.appendChild(pieceElement);
            }
        } else if (newPieceDataPosition === 200) {
            pieceElement.style.display = "none"; 
            playSound(soundPieceHome);
        }

        if (selectedMoveValue === currentDiceRoll.d1 && !currentDiceRoll.d1_used) {
            currentDiceRoll.d1_used = true;
            diceDisplayD1.classList.remove("dice-rolled", "active-dice");
        } else if (selectedMoveValue === currentDiceRoll.d2 && !currentDiceRoll.d2_used) {
            currentDiceRoll.d2_used = true;
            diceDisplayD2.classList.remove("dice-rolled", "active-dice");
        } else if (selectedMoveValue === currentDiceRoll.sum) {
            currentDiceRoll.d1_used = true;
            currentDiceRoll.d2_used = true;
            diceDisplayD1.classList.remove("dice-rolled", "active-dice");
            diceDisplayD2.classList.remove("dice-rolled", "active-dice");
            diceDisplaySum.classList.remove("dice-rolled", "active-dice");
        }
        
        resetTurnState(false); 

        if (checkWinCondition(player.id)) {
            messageArea.textContent = `${player.name} Wins! Congratulations!`;
            playSound(soundGameWin);
            rollDiceBtn.disabled = true;
            return; 
        }

        if (!currentDiceRoll.d1_used || !currentDiceRoll.d2_used) {
            messageArea.textContent = "Move complete. Select another dice value if available.";
        } else {
            let bonusTurn = (currentDiceRoll.d1 === 6 && currentDiceRoll.d2 === 6);
            if (!bonusTurn && (capturedOpponentThisMove || pieceReachedHomeThisMove)) {
                bonusTurn = true;
                 messageArea.textContent = `${player.name} gets an extra turn! Roll again.`;
            } else if (bonusTurn) {
                 messageArea.textContent = `Double Sixes! ${player.name} gets an extra turn. Roll again.`;
            }

            if (bonusTurn) {
                resetTurnState(true); 
                updatePlayerTurnIndicator(); 
            } else {
                switchPlayer();
            }
        }
    }

    function capturePiece(opponentPieceElement) {
        const opponentPlayerId = opponentPieceElement.dataset.player;
        const opponentPieceId = parseInt(opponentPieceElement.dataset.pieceId);
        const opponentPlayer = players.find(p => p.id === opponentPlayerId);
        const opponentYard = document.getElementById(`${opponentPlayerId}-yard`);
        const homeSlots = opponentYard.querySelectorAll(".piece-home");
        let emptySlot = null;
        for(let slot of homeSlots) { if (!slot.querySelector(".piece")) { emptySlot = slot; break; } }
        if(emptySlot) { emptySlot.appendChild(opponentPieceElement); }
        opponentPlayer.pieces[opponentPieceId].position = -1; 
        playSound(soundPieceCapture);
    }

    function switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        resetTurnState(true);
        updatePlayerTurnIndicator();
        messageArea.textContent = `${players[currentPlayerIndex].name}, roll the dice.`;
    }

    function checkWinCondition(playerId) {
        const player = players.find(p => p.id === playerId);
        for (const pieceId in player.pieces) { if (!player.pieces[pieceId].isHome) return false; }
        return true;
    }

    initializeGame();
});

