package com.au.unimelb.comp90020.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.au.unimelb.comp90020.PacManGame.MultiplayerMode;
import com.au.unimelb.comp90020.actors.Ghost;
import com.au.unimelb.comp90020.actors.Pacman;
import com.au.unimelb.comp90020.actors.Pacman.Movement;
import com.au.unimelb.comp90020.framework.util.Assets;
import com.au.unimelb.comp90020.framework.util.Settings;
import com.au.unimelb.comp90020.multiplayer.networking.Message;
import com.au.unimelb.comp90020.multiplayer.networking.MessageListener;
import com.au.unimelb.comp90020.multiplayer.networking.Message.MessageType;
import com.au.unimelb.comp90020.screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;


/**
 * Represents the world where the game is performed. It updates the states of
 * every actor within it for each delta time.
 * 
 * @author Andres Chaves, Diego Montufar, Ilkan Esiyok (ID’s: 706801, 661608, 616394)
 *
 */
public class World implements MessageListener {

	/**
	 * WorldListener for sound effects 
	 */
	public final WorldListener listener;

	/**
	 * Map of Pacman players, the key is the ProcessId
	 */
	public Map<Long,Pacman> pacmans;
	/**
	 * Inky Ghost
	 */
	public Ghost inky;
	/**
	 * Blinky Ghost
	 */
	public Ghost blinky;
	/**
	 * Pinky Ghost
	 */
	public Ghost pinky;
	/**
	 * Clyde Ghost
	 */
	public Ghost clyde;

	/**
	 * Map of the world
	 */
	public TiledMap map;
	/**
	 * Layer of walls of the world (to check collision)
	 */
	public TiledMapTileLayer wallsLayer;
	/**
	 * Layer of Pacman food
	 */
	public TiledMapTileLayer pacdotsLayer;
	/**
	 * Layer of map objects
	 */
	public MapObjects objectsLayer;
	/**
	 * 
	 */
	public int[] wallsLayerIndex = new int[]{0}, objectsLayerIndex = new int[]{1}, collectablesLayerIndex = new int[]{2};

	/**
	 * 
	 */
	TiledMapTileSet pacmanTileSet;
	/**
	 * 
	 */
	ArrayList<TiledMapTileLayer.Cell> dotCellsInScene;
	/**
	 * 
	 */
	ArrayList<TiledMapTileLayer.Cell> dotBonusCellsInScene;
	/**
	 * 
	 */
	Map<String,TiledMapTile> dotTile;
	/**
	 * 
	 */
	Map<String,TiledMapTile> dotBonusTile;

	/**
	 * Counter variable to change Animation
	 */
	float elapsedSinceAnimation = 0.0f;

	/**
	 * Collaborative score
	 */
	public int score;
	/**
	 * Player's Lives
	 */
	public int lives;
	/**
	 * Number of Pacman dots eaten
	 */
	public int dots_eaten;

	/**
	 * Process ID of the Controlled Pacman
	 */
	private Long controlledPacman;
	/**
	 * Screen object that renders the world state
	 */
	private GameScreen screen;

	/**
	 * Class constructor
	 * @param listener WorldListener
	 * @param screen Screen where the world is rendered
	 */
	public World(WorldListener listener, GameScreen screen) {
		this.listener = listener;
		this.screen = screen;
		this.map = new TmxMapLoader().load("pacman.tmx");
		this.pacmanTileSet =  this.map.getTileSets().getTileSet("PacMan");
		this.wallsLayer = (TiledMapTileLayer) this.map.getLayers().get("Walls");
		this.pacdotsLayer = (TiledMapTileLayer) this.map.getLayers().get("Collectables");
		this.objectsLayer  = this.map.getLayers().get("Objects").getObjects();
		this.pacmans = new HashMap<Long,Pacman>();
		this.controlledPacman = Settings.getPID();
		Animation[] myAnimation = new Animation[4];
		TextureRegion myDefaultTextureRegion;
		myAnimation[0]=Assets.p1_pacmanUp;myAnimation[1]=Assets.p1_pacmanDown;
		myAnimation[2]=Assets.p1_pacmanLeft;myAnimation[3]=Assets.p1_pacmanRight;
		myDefaultTextureRegion = Assets.p1_pacman_looking_right_1;
		this.pacmans.put(this.controlledPacman,new Pacman(Settings.PAC_INITIAL_POS_X,Settings.PAC_INITIAL_POS_Y,myAnimation,myDefaultTextureRegion,wallsLayer)); 
		createGhosts();
		createDots();
		this.score = 0;
		this.lives = Settings.MAX_LIVES;
		this.dots_eaten = 0;		
	}

	/**
	 * Ghost object initialization
	 */
	private void createGhosts() {
		//Find the spot and create the ghosts
		float x,y,width;
		MapObject obj;
		MapObject houseDoor = this.objectsLayer.get("HouseDoor");
		obj = this.objectsLayer.get("InkySpawnPoint");
		x = (Float)obj.getProperties().get("x");
		y = (Float)obj.getProperties().get("y");
		width = (Float)obj.getProperties().get("width");		
		inky = new Ghost( x, y, 1, 1 ,width, wallsLayer, houseDoor );	
		obj = this.objectsLayer.get("PinkySpawnPoint");
		x = (Float)obj.getProperties().get("x");
		y = (Float)obj.getProperties().get("y");
		width = (Float)obj.getProperties().get("width");
		pinky = new Ghost( x, y, 300, 1, width, wallsLayer, houseDoor);
		obj = this.objectsLayer.get("ClydeSpawnPoint");
		x = (Float)obj.getProperties().get("x");
		y = (Float)obj.getProperties().get("y");
		width = (Float)obj.getProperties().get("width");
		clyde = new Ghost( x, y, 1, 300 ,width, wallsLayer, houseDoor );
		obj = this.objectsLayer.get("BlinkySpawnPoint");
		x = (Float)obj.getProperties().get("x");
		y = (Float)obj.getProperties().get("y");
		width = (Float)obj.getProperties().get("width");
		blinky = new Ghost( x, y, 300, 300, width, wallsLayer, houseDoor );

	}

	/**
	 * Here we are creating pacdots and pacdotbonuses so that then you can access them by 
	 * looping over the ArrayLists dotCellsInScene and dotBonusCellsInScene and do something like
	 * layer.getCell(0, 1).setTile(null); 
	 */
	public void createDots(){

		TiledMapTileLayer layer = this.pacdotsLayer;

		this.dotTile = new HashMap<String,TiledMapTile>();
		this.dotBonusTile = new HashMap<String,TiledMapTile>();
		for(TiledMapTile tile:this.pacmanTileSet){
			Object propertyPacDot = tile.getProperties().get("Pacdot");
			if(propertyPacDot != null) {
				dotTile.put((String)propertyPacDot,tile);
			}
			Object propertyPacBonus = tile.getProperties().get("Pacbonus");
			if(propertyPacBonus != null) {
				dotBonusTile.put((String)propertyPacBonus,tile);
			}
		}

		this.dotCellsInScene = new ArrayList<TiledMapTileLayer.Cell>();
		this.dotBonusCellsInScene = new ArrayList<TiledMapTileLayer.Cell>();
		for(int x = 0; x < layer.getWidth();x++){
			for(int y = 0; y < layer.getHeight();y++){
				TiledMapTileLayer.Cell cell = layer.getCell(x,y);
				if (cell != null){
					Object propertyPacDot = cell.getTile().getProperties().get("Pacdot");
					if(propertyPacDot != null){
						this.dotCellsInScene.add(cell);
					}
					Object propertyPacBonus = cell.getTile().getProperties().get("Pacbonus");
					if(propertyPacBonus != null){
						this.dotBonusCellsInScene.add(cell);
					}
				}
			}
		}
	}

	/**
	 * Updates every actor within it based on the elapsed time. After moving Pacman and  
	 * check collisions
	 * @param deltaTime Elapsed time
	 * @param move Movement
	 */
	public void update(float deltaTime,Movement move) {		
		updatePacman(deltaTime,move);
		updateGhosts(deltaTime);

		checkCollisions();
	}


	/**
	 * Check every possible collision event within the world. Possible collision are:
	 * Ghost - Pacman and Pacman - Pacman Food
	 */
	private void checkCollisions() {
		checkDotsCollisions();
		checkGhostsCollisions();
	}

	/**
	 * Check collisions between Pacman and Pacman food
	 */
	private void checkDotsCollisions() {

		Pacman pacman = this.pacmans.get(this.controlledPacman);
		Movement currentPacmanState = pacman.getCurrentState();
		float currentX = pacman.position.x, currentY =  pacman.position.y;

		if(currentPacmanState == Movement.RIGTH || currentPacmanState == Movement.LEFT || 
				currentPacmanState == Movement.UP || currentPacmanState == Movement.DOWN){
			checkEaten(currentX,currentY);
		}

	}

	/**
	 * Check if a food would be eaten if the Pacman moves to X and Y, if there is food,
	 * the game ask for the Mutual exclusion lock, eats the food, notifies the peers and
	 * then releases the lock
	 * @param currentX Pacman X position
	 * @param currentY Pacman Y position
	 */
	private void checkEaten(float currentX, float currentY) {		
		if (isCellFood(currentX, currentY)){
			screen.game.lock.requestCS();
			if (isCellFood(currentX, currentY)){
				removeFood(currentX, currentY);
				if ( this.screen.game.mode == MultiplayerMode.multicast ){
					StringBuilder sb = new StringBuilder();
					sb.append(this.screen.mp.getMyId());
					sb.append(",");
					sb.append(currentX);
					sb.append(",");
					sb.append(currentY);
					sb.append(",");
					sb.append(score);
					sb.append(",");
					sb.append(screen.game.lock.getClockValue());

					final Message m = new Message("localhost", sb.toString(), MessageType.FOOD_EATEN);				
					this.screen.game.peer.broadcastMessage(m);
				}	
			}
			screen.game.lock.releaseCS();	
		}		
	}

	/**
	 * Check if in a specific cell there is Pacman food
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return True if there is food, otherwise false
	 */
	private boolean isCellFood(float x, float y) {
		Cell cell = this.pacdotsLayer.getCell((int) (x / this.pacdotsLayer.getTileWidth()), (int) (y / this.pacdotsLayer.getTileHeight()));		
		return cell != null && cell.getTile() != null && cell.getTile()!=null;
	}

	/**
	 * Remove a specific food at the specified location, increases Score and Food eaten
	 * @param x X coordinate
	 * @param y Y coordinate
	 */
	private void removeFood(float x, float y) {		
		Cell cell = this.pacdotsLayer.getCell((int) (x / this.pacdotsLayer.getTileWidth()), (int) (y / this.pacdotsLayer.getTileHeight()));		
		cell.setTile(null);
		
		this.score++;
		this.dots_eaten++;
		
	}

	/**
	 * Check if the Pacman collided with a Ghost, substracts one live
	 * and reset Pacman position
	 */
	private void checkGhostsCollisions() {
		Pacman pacman = this.pacmans.get(this.controlledPacman);
		if (pacman.bounds.overlaps(this.inky.bounds) 
				|| pacman.bounds.overlaps(this.blinky.bounds) 
				|| pacman.bounds.overlaps(this.pinky.bounds) 
				|| pacman.bounds.overlaps(this.clyde.bounds)) {
			this.lives--;
			this.listener.playLifeLost();
			resetPositions();
		}

	}

	/**
	 * 
	 */
	private void resetPositions(){
		Pacman pacman = this.pacmans.get(this.controlledPacman);
		//Pacman go back to the starting point!
		pacman.position.x = Settings.PAC_INITIAL_POS_X;
		pacman.position.y = Settings.PAC_INITIAL_POS_Y;
		pacman.bounds.x = pacman.position.x - pacman.bounds.width / 2;
		pacman.bounds.y = pacman.position.y - pacman.bounds.height / 2;
		pacman.setCurrentState(Movement.NONE);

	}


	/**
	 * @param deltaTime
	 * @param move
	 */
	private void updatePacman(float deltaTime,Movement move) {
		Pacman pacman = this.pacmans.get(this.controlledPacman);
		pacman.update(deltaTime,move);
		
		if ( this.screen.game.mode == MultiplayerMode.multicast && move!=Movement.NONE ){
			StringBuilder sb = new StringBuilder();
			toPositionString(sb,String.valueOf(this.screen.mp.getMyId()),pacman);
			Message m = new Message("localhost", sb.toString(), MessageType.PACMAN_MOVEMENT);
			this.screen.game.peer.broadcastMessage(m);
		}
	}

	/**
	 * @param deltaTime
	 */
	private void updateGhosts(float deltaTime) {
		if (this.screen.mp.isMinPlayerId()){
			Pacman pacman = this.pacmans.get(this.controlledPacman);
			this.blinky.update(deltaTime,pacman.position.x, pacman.position.y);
			this.pinky.update(deltaTime,pacman.position.x, pacman.position.y);
			this.clyde.update(deltaTime,pacman.position.x, pacman.position.y);
			this.inky.update(deltaTime,pacman.position.x, pacman.position.y);
		}
		//Update Ghost positions

		
		if (this.screen.game.mode == MultiplayerMode.multicast && this.screen.mp.isMinPlayerId()){			  
			StringBuilder sb = new StringBuilder();
			sb.append(this.screen.mp.getMyId());
			sb.append(",");
			toPositionString(sb,"BLINKY",this.blinky);
			sb.append(",");
			toPositionString(sb,"PINKY",this.pinky);
			sb.append(",");
			toPositionString(sb,"CLYDE",this.clyde);
			sb.append(",");
			toPositionString(sb,"INKY",this.inky);
			Message m = new Message("localhost", sb.toString(), MessageType.GHOST_MOVEMENT);
			this.screen.game.peer.broadcastMessage(m);
		}
	}

	/**
	 * @param sb
	 * @param name
	 * @param ghost
	 */
	private void toPositionString(StringBuilder sb, String name, DynamicGameObject ghost) {
		sb.append(name);
		sb.append(",");
		sb.append(ghost.position.x);
		sb.append(",");
		sb.append(ghost.position.y);
	}

	/**
	 * @param pid
	 */
	public synchronized void addPacman(Long pid) {
		Animation[] myAnimation = new Animation[4];
		TextureRegion myDefaultTextureRegion;
		int x = (this.pacmans.values().size())*20;
		
		int numPacmans = this.pacmans.values().size()+1;
		
		switch(numPacmans){
			case 2:
				myAnimation[0]=Assets.p2_pacmanUp;myAnimation[1]=Assets.p2_pacmanDown;
				myAnimation[2]=Assets.p2_pacmanLeft;myAnimation[3]=Assets.p2_pacmanRight;
				myDefaultTextureRegion = Assets.p2_pacman_looking_right_1;
				break;
			case 3:
				myAnimation[0]=Assets.p3_pacmanUp;myAnimation[1]=Assets.p3_pacmanDown;
				myAnimation[2]=Assets.p3_pacmanLeft;myAnimation[3]=Assets.p3_pacmanRight;
				myDefaultTextureRegion = Assets.p3_pacman_looking_right_1;
				break;
			case 4:
				myAnimation[0]=Assets.p4_pacmanUp;myAnimation[1]=Assets.p4_pacmanDown;
				myAnimation[2]=Assets.p4_pacmanLeft;myAnimation[3]=Assets.p4_pacmanRight;
				myDefaultTextureRegion = Assets.p4_pacman_looking_right_1;
				break;
			default:
				myAnimation[0]=Assets.p1_pacmanUp;myAnimation[1]=Assets.p1_pacmanDown;
				myAnimation[2]=Assets.p1_pacmanLeft;myAnimation[3]=Assets.p1_pacmanRight;
				myDefaultTextureRegion = Assets.p1_pacman_looking_right_1;
				break;
		}
		this.pacmans.put(pid,new Pacman(Settings.PAC_INITIAL_POS_X+x,Settings.PAC_INITIAL_POS_Y,myAnimation,myDefaultTextureRegion,wallsLayer)); 
	}

	/**
	 * @param pacmanIdx
	 */
	public void setControlledPacman(Long pacmanIdx) {
		this.controlledPacman = pacmanIdx;

	}

	/* (non-Javadoc)
	 * @see com.au.unimelb.comp90020.multiplayer.networking.MessageListener#listen(com.au.unimelb.comp90020.multiplayer.networking.Message)
	 */
	@Override
	public void listen(Message m) {
		if (m.getType() == MessageType.GHOST_MOVEMENT){
			String body = m.getBody();
			String[] movements = body.split(",");
			Long pid = Long.valueOf(movements[0]);
			if (pid!=this.screen.mp.getMyId()){
				for (int i = 1; i < movements.length; i+=3){
					String name = movements[i];
					float x = Float.valueOf(movements[i+1]);
					float y = Float.valueOf(movements[i+2]);
					Ghost g = null;
					if(name.equals("BLINKY"))
						g = this.blinky;
					if(name.equals("PINKY"))
						g = this.pinky;
					if(name.equals("INKY"))
						g = this.inky;
					if(name.equals("CLYDE"))
						g = this.clyde;

					g.position.x = x;
					g.position.y = y;
					g.bounds.x = g.position.x - g.bounds.width / 2;
					g.bounds.y = g.position.y - g.bounds.height / 2;
				}
			}
		}
		if (m.getType() == MessageType.PACMAN_MOVEMENT){			
			String body = m.getBody();
			String[] movements = body.split(",");
			long pid = Long.valueOf(movements[0]).longValue();
			if (pid!=screen.mp.getMyId()){
				Pacman pacman = this.pacmans.get(pid);
				float x = Float.valueOf(movements[1]);
				float y = Float.valueOf(movements[2]);
				pacman.position.x = x;
				pacman.position.y = y;
				pacman.bounds.x = pacman.position.x - pacman.bounds.width / 2;
				pacman.bounds.y = pacman.position.y - pacman.bounds.height / 2;
			}
		}
		if (m.getType() == MessageType.FOOD_EATEN){		
			String body = m.getBody();
			String[] movements = body.split(",");
			long pid = Long.valueOf(movements[0]).longValue();
			if (pid!=screen.mp.getMyId()){				
				float x = Float.valueOf(movements[1]);
				float y = Float.valueOf(movements[2]);
				Cell cell = this.pacdotsLayer.getCell((int) (x / this.pacdotsLayer.getTileWidth()), (int) (y / this.pacdotsLayer.getTileHeight()));		
				cell.setTile(null);

				//score = Integer.valueOf(movements[3]);
				score++;
				this.dots_eaten++;
			}
		}


	}
}
