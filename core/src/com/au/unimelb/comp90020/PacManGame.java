package com.au.unimelb.comp90020;

import java.io.IOException;

import com.au.unimelb.comp90020.PacManGame.MultiplayerMode;
import com.au.unimelb.comp90020.framework.util.Assets;
import com.au.unimelb.comp90020.framework.util.Settings;
import com.au.unimelb.comp90020.multiplayer.concurrency.Lock;
import com.au.unimelb.comp90020.multiplayer.concurrency.RAMutex;
import com.au.unimelb.comp90020.multiplayer.networking.GameClient;
import com.au.unimelb.comp90020.multiplayer.networking.GameMulticastPeer;
import com.au.unimelb.comp90020.multiplayer.networking.GameServer;
import com.au.unimelb.comp90020.multiplayer.networking.Message;
import com.au.unimelb.comp90020.multiplayer.networking.Message.MessageType;
import com.au.unimelb.comp90020.multiplayer.networking.MessageListener;
import com.au.unimelb.comp90020.screens.GameScreen;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class PacManGame extends Game {
	
	public enum MultiplayerMode {none, server, client, multicast};

	public SpriteBatch batcher;
	public MultiplayerMode mode;
	
	public GameServer server;
	public GameClient client;
	public GameMulticastPeer peer;
	public Lock lock = null;
	
	public PacManGame(MultiplayerMode mode) {
		this.mode = mode;		
	}

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		batcher = new SpriteBatch();
		Assets.load();
		
		////
		if (mode == MultiplayerMode.multicast){
			try {
				peer =  new GameMulticastPeer();
				peer.start();
				
				lock = new RAMutex(peer);
				peer.registerListener(MessageType.LOCK_REQUEST, lock);
				peer.registerListener(MessageType.LOCK_REPLY, lock);
				
				GameScreen gs = new GameScreen(PacManGame.this);
				peer.registerListener(MessageType.JOIN, gs);
				peer.registerListener(MessageType.PEERS, gs);
				peer.registerListener(MessageType.GHOST_MOVEMENT, gs.world);
				peer.registerListener(MessageType.PACMAN_MOVEMENT, gs.world);
				peer.registerListener(MessageType.FOOD_EATEN, gs.world);
				peer.sendMessage(new Message("localhost",String.valueOf(Settings.getPID()),MessageType.JOIN));
				setScreen(gs);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		////
	}
	
}
