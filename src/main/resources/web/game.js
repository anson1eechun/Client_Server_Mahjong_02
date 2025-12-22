let socket;
let nickname;

document.getElementById('login-btn').addEventListener('click', connect);

function connect() {
    nickname = document.getElementById('nickname').value;
    if (!nickname) {
        alert("Please enter a nickname");
        return;
    }

    // Connect to WebSocket Server
    socket = new WebSocket("ws://localhost:8888");

    socket.onopen = function (e) {
        console.log("Connection established");
        // Send Login Packet
        const packet = {
            command: "LOGIN",
            data: { nickname: nickname }
        };
        socket.send(JSON.stringify(packet));
    };

    socket.onmessage = function (event) {
        const packet = JSON.parse(event.data);
        handlePacket(packet);
    };

    socket.onclose = function (event) {
        alert("Connection closed");
    };

    socket.onerror = function (error) {
        alert("Error: " + error.message);
    };
}

function handlePacket(packet) {
    const cmd = packet.command;
    const data = packet.data;

    if (cmd === "LOGIN_SUCCESS") {
        document.getElementById('login-screen').classList.add('hidden');
        document.getElementById('game-screen').classList.remove('hidden');
        log("Welcome " + nickname + "! Waiting for other players...");
    }
    else if (cmd === "GAME_UPDATE") {
        const d = packet.data;
        if (d.message) log(d.message);

        if (d.action === "STATE_UPDATE") {
            renderState(d);
        }
        else if (d.action === "DRAW") {
            log("You drew: " + d.tile);
            // Re-render handled by state usually, but for animation we could highlight
        }
    }
    else if (cmd === "ACTION_REQUEST") {
        const d = packet.data;
        if (d.action === "CHOOSE_ACTION") {
            showActionButtons(d.choices);
        }
    }
}

function playCard(tile) {
    const packet = {
        command: "PLAY_CARD",
        data: { tile: tile }
    };
    socket.send(JSON.stringify(packet)); // Optimistic update removed, wait for state
}

function log(msg) {
    const logArea = document.getElementById('log-area');
    logArea.innerHTML += `<div>${msg}</div>`;
    logArea.scrollTop = logArea.scrollHeight;
}

function showActionButtons(actions) {
    const btnArea = document.createElement('div');
    btnArea.id = "action-buttons";
    btnArea.style = "position:absolute; top:40%; left:50%; transform:translate(-50%, -50%); z-index:200; display:flex; gap:10px;";

    // Actions is an array of strings, e.g. ["PONG", "KONG"]
    actions.forEach(type => {
        const actionBtn = document.createElement('button');
        actionBtn.innerText = type;
        actionBtn.style = "font-size: 24px; padding: 10px 20px; background: gold; color: black; border: 2px solid white; border-radius: 8px; cursor: pointer;";
        actionBtn.onclick = () => {
            sendAction(type);
            btnArea.remove();
        };
        btnArea.appendChild(actionBtn);
    });

    const skipBtn = document.createElement('button');
    skipBtn.innerText = "SKIP";
    skipBtn.style = "font-size: 24px; padding: 10px 20px; background: gray; color: white; border: 2px solid white; border-radius: 8px; cursor: pointer;";
    skipBtn.onclick = () => {
        sendAction("SKIP");
        btnArea.remove();
    };

    btnArea.appendChild(skipBtn);
    document.body.appendChild(btnArea);
}

function sendAction(type) {
    const packet = {
        command: "ACTION",
        data: { type: type }
    };
    socket.send(JSON.stringify(packet));
}

function renderState(state) {
    // 1. Render Sea
    const seaDiv = document.getElementById('center-area');
    seaDiv.innerHTML = "";
    state.sea.forEach(t => {
        const el = document.createElement('div');
        el.className = 'sea-tile';
        el.innerText = t;
        seaDiv.appendChild(el);
    });

    // 2. Render My Hand
    const myArea = document.getElementById('my-area');
    myArea.innerHTML = "";

    // 2a. Render My Melds
    const myMelds = state.allMelds[state.myIndex];
    if (myMelds && myMelds.length > 0) {
        const meldDiv = document.createElement('div');
        meldDiv.className = 'meld-area';
        myMelds.forEach(t => {
            const el = document.createElement('div');
            el.className = 'exposed-tile';
            el.innerText = t;
            meldDiv.appendChild(el);
        });
        myArea.appendChild(meldDiv);
    }

    // 2b. Render My Standing Tiles
    state.myHand.forEach(t => {
        const el = document.createElement('div');
        el.className = 'my-tile';
        el.innerText = t;

        // Only checking turn for visual cue, logic blocked by server anyway
        if (state.myIndex === state.turnIndex) {
            el.onclick = () => playCard(t);
        }
        myArea.appendChild(el);
    });

    // Highlight if my turn
    if (state.myIndex === state.turnIndex) {
        myArea.classList.add('active-turn');
    } else {
        myArea.classList.remove('active-turn');
    }

    // 3. Render Opponents
    // Map relative positions:
    // (myIndex + 1) % 4 => Right
    // (myIndex + 2) % 4 => Top
    // (myIndex + 3) % 4 => Left
    renderOpponent(state, 1, 'right-area');
    renderOpponent(state, 2, 'top-area');
    renderOpponent(state, 3, 'left-area');
}

function renderOpponent(state, offset, divId) {
    const targetIdx = (state.myIndex + offset) % 4;
    const count = state.handCounts[targetIdx];
    const div = document.getElementById(divId);
    div.innerHTML = "";

    // Highlight if their turn
    if (targetIdx === state.turnIndex) div.classList.add('active-turn');
    else div.classList.remove('active-turn');

    // Render Melds
    const melds = state.allMelds[targetIdx];
    if (melds && melds.length > 0) {
        const meldDiv = document.createElement('div');
        meldDiv.className = 'meld-area';
        melds.forEach(t => {
            const el = document.createElement('div');
            el.className = 'exposed-tile';
            el.innerText = t;
            meldDiv.appendChild(el);
        });
        div.appendChild(meldDiv);
    }

    // Render Hidden Hand
    for (let i = 0; i < count; i++) {
        const el = document.createElement('div');
        el.className = 'hidden-tile';
        div.appendChild(el);
    }
}

function playCard(tile) {
    const packet = {
        command: "PLAY_CARD",
        data: { tile: tile }
    };
    socket.send(JSON.stringify(packet)); // Optimistic update removed, wait for state
}

function log(msg) {
    const logArea = document.getElementById('log-area');
    logArea.innerHTML += `<div>${msg}</div>`;
    logArea.scrollTop = logArea.scrollHeight;
}
