package com.mahjong.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Structure to hold a group of actions for a specific priority tier
public class ActionGroup {
    int priority; // 1=HU, 2=PONG/KONG, 3=CHOW
    List<Integer> players = new ArrayList<>();
    // What actions each player can do? using map
    Map<Integer, List<String>> playerActions = new HashMap<>();

    public ActionGroup(int p) {
        this.priority = p;
    }

    public void addAction(int playerIdx, String action) {
        if (!players.contains(playerIdx))
            players.add(playerIdx);
        playerActions.computeIfAbsent(playerIdx, k -> new ArrayList<>()).add(action);
    }
}
