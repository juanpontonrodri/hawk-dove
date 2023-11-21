
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.table.DefaultTableModel;

public class MainAgent extends Agent {

    ArrayList<PlayerInformation> players = new ArrayList<>();


    private GUI gui;
    private AID[] playerAgents;
    private GameParametersStruct parameters = new GameParametersStruct();
    int gamesPlayed = 0;
    private long roundsPlayed = 0;

    private Semaphore semaphore = new Semaphore(1);
    //sem to stop and coninue the game

    @Override
    protected void setup() {
        gui = new GUI(this);
        System.setOut(new PrintStream(gui.getLoggingOutputStream()));

        updatePlayers();
        gui.logLine("Agent " + getAID().getName() + " is ready.");
    }

    public int updatePlayers() {
        gui.logLine("Updating player list");
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                gui.logLine("Found " + result.length + " players");
            }
            playerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                playerAgents[i] = result[i].getName();
            }
        } catch (FIPAException fe) {
            gui.logLine(fe.getMessage());
        }
        //Provisional
        String[] playerNames = new String[playerAgents.length];
        for (int i = 0; i < playerAgents.length; i++) {
            playerNames[i] = playerAgents[i].getName();
        }
        gui.setPlayersUI(playerNames);
        return 0;
    }

    public int newGame() {
        players.clear();
        addBehaviour(new GameManager());
        return 0;
    }

    /**
     * In this behavior this agent manages the course of a match during all the
     * rounds.
     */
    private class GameManager extends SimpleBehaviour {


        

        @Override
        public void action() {

            gamesPlayed=0;
            roundsPlayed=0;
            //Assign the IDs
            int lastId = 0;
            for (AID a : playerAgents) {
                players.add(new PlayerInformation(a, lastId++));
            }

            parameters.N = players.size();
            gui.updateParametersLabel();
            //Initialize (inform ID)
            for (PlayerInformation player : players) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Id#" + player.id + "#" + parameters.N + "," + parameters.R);
                msg.addReceiver(player.aid);
                send(msg);
            }
            //Organize the matches
            for (int i = 0; i < players.size(); i++) {
                for (int j = i + 1; j < players.size(); j++) { //too lazy to think, let's see if it works or it breaks
                    playGame(players.get(i), players.get(j));
                }
            }
        }

        

        private void playGame(PlayerInformation player1, PlayerInformation player2) {
            //Assuming player1.id < player2.id
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);
            msg.setContent("NewGame#" + player1.id + "#" + player2.id);
            send(msg);

            int[] results = new int[2];
            results[0] = 0;
            results[1] = 0;


            for (int i = 0; i < parameters.R; i++) {

                
                    semaphore.acquireUninterruptibly();
                
            
                char act1, act2;

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("Action");
                msg.addReceiver(player1.aid);
                send(msg);

                gui.logLine("Main Waiting for movement");
                ACLMessage move1 = blockingReceive();
                gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
                act1 = move1.getContent().split("#")[1].charAt(0);

                msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("Action");
                msg.addReceiver(player2.aid);
                send(msg);

                gui.logLine("Main Waiting for movement");
                ACLMessage move2 = blockingReceive();
                gui.logLine("Main Received " + move1.getContent() + " from " + move1.getSender().getName());
                act2 = move2.getContent().split("#")[1].charAt(0);

                int[] payoff = calculatePayoff((char) act1, (char) act2);

                results[0] += payoff[0];
                results[1] += payoff[1];

                msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(player1.aid);
                msg.addReceiver(player2.aid);

                msg.setContent("Results#" + player1.id + "," + player2.id + "#" + act1 + "," + act2 + "#" + payoff[0] + "," + payoff[1]);
                send(msg);

                //We add the points to the players and rounds played, then we update the table
                player1.points += payoff[0];
                player2.points += payoff[1];
                if(payoff[0]>payoff[1]){
                    player1.rounds1++;
                    player2.rounds2++;}
                else if(payoff[0]<payoff[1]){
                    player1.rounds2++;
                    player2.rounds1++;}
                else{
                    player1.rounds3++;
                    player2.rounds3++;}
                
                //System.out.println("Main sent " + msg.getContent());

                roundsPlayed++;
                gui.updateTableData();

                semaphore.release();
                

            }
            msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(player1.aid);
            msg.addReceiver(player2.aid);

            msg.setContent("GameOver#" + player1.id + "," + player2.id + "#" + results[0] + "," + results[1]);
            send(msg);

            // Increment the win and loss counters for player1 and player2 based on the game results

            gamesPlayed++;
            player1.addGameResult(results[0], results[1], player2.aid.getName().split("@")[0]);
            player2.addGameResult(results[1], results[0], player1.aid.getName().split("@")[0]);
            if(results[0]>results[1]){
                player1.wins++;
                player2.loses++;}
            else if(results[0]<results[1]){
                player1.loses++;
                player2.wins++;}
            else{
                player1.draws++;
                player2.draws++;}
            gui.updateParametersLabel();
            System.out.println("Main sent " + msg.getContent());

        }


        

        @Override
        public boolean done() {
            return true;
        }
    }

    public void stopGame() {
            semaphore.acquireUninterruptibly();
        
    }

    public void continueGame() {
        semaphore.release();
        
    }

    // Calculate the payoff for each player based on the actions they chose
    private int[] calculatePayoff(char actionPlayer1, char actionPlayer2) {
        int[] payoff = new int[2];
    
        int[][] payoffMatrix = {
            {-1, 10},
            {0, 5}
        };
    
        int indexPlayer1 = (actionPlayer1 == 'H') ? 0 : 1;
        int indexPlayer2 = (actionPlayer2 == 'H') ? 0 : 1;
    
        payoff[0] = payoffMatrix[indexPlayer1][indexPlayer2]; // Puntos obtenidos por Player 1
        payoff[1] = payoffMatrix[indexPlayer2][indexPlayer1]; // Puntos obtenidos por Player 2
    
        return payoff;
    }
    

    

    public class PlayerInformation {

        //here we will store the stats of the player and the history of the games

        private static final int MAX_GAME_HISTORY = 100;
        private static final int GAME_RESULT_COLUMNS = 4;

        private AID aid;
        private int id;
        private int points = 0;
        private Object[][] gameHistory;
        private int gamesStored = 0;
        private int wins=0;
        private int loses=0;
        private int draws=0;
        private int rounds1=0;
        private int rounds2=0;
        private int rounds3=0;

        public PlayerInformation(AID a, int i) {
            aid = a;
            id = i;
            gameHistory = new Object[MAX_GAME_HISTORY][GAME_RESULT_COLUMNS];
        }

        public void addPoints(int p) {
            points += p;
        }

        public void addGameResult(int playerPoints, int opponentPoints, String rival) {
        
        
            // Si el historial está completo, elimina el juego más antiguo
            if (gamesStored >= MAX_GAME_HISTORY) {
                System.arraycopy(gameHistory, 1, gameHistory, 0, MAX_GAME_HISTORY - 1);
                gamesStored--; // Reducir el contador de juegos almacenados para mantener el tamaño límite
            }
        
            // Agregar el nuevo juego al historial
            gameHistory[gamesStored++] = new Object[]{gamesStored, playerPoints, opponentPoints, rival};
        }
        
        
        

        public Object[][] getGameHistory() {
            return Arrays.copyOf(gameHistory, gamesStored);
        }

        public int[] getstats(){
            int[] stats = new int[6];
            stats[0]=wins;
            stats[1]=loses;
            stats[2]=draws;
            stats[3]=rounds1;
            stats[4]=rounds2;
            stats[5]=rounds3;
            return stats;
        }
        
    }

    public class GameParametersStruct {

        int N;
        int R;

        public GameParametersStruct() {
            N = 2;
            R = 1;
        }

        public void setR(int value) {
            R = value;
        }
        
    }

    public GameParametersStruct getGameParameters() { //to be able to modify the parameters
        return parameters;
    }


    public void resetPlayers() {
        for (PlayerInformation player : players) {
            player.points = 0;
            player.gamesStored = 0;
            player.wins=0;
            player.loses=0;
            player.draws=0;
            player.rounds1=0;
            player.rounds2=0;
            player.rounds3=0;
            
        }
        }

    public Object[][] getPlayerData() {
        // Obtén una copia de la lista de jugadores para evitar modificar la original
        List<PlayerInformation> playersCopy = new ArrayList<>(players);

        // Ordena la lista de jugadores por puntos (de mayor a menor)
        playersCopy.sort((player1, player2) -> Integer.compare(player2.points, player1.points));

        Object[][] data = new Object[playersCopy.size()][4]; //3 columnas: Puesto, Nombre, Puntos

        // Llena la matriz de datos con la información de los jugadores ordenados
        for (int i = 0; i < playersCopy.size(); i++) {
            data[i][0] = i + 1; // Puesto del jugador
            data[i][1] = playersCopy.get(i).aid.getName().split("@")[0]; // Nombre del agente
            data[i][2] = playersCopy.get(i).points; // Puntos del jugador
            
        }

        return data;
    }

    //public methods to get the data of the players by name
    public Object[][] getGameHistory(String agentName) {


        for (PlayerInformation player : players) {
            if (player.aid.getName().equals(agentName)) {
                return Arrays.copyOf(player.getGameHistory(), player.gamesStored);
            }
        }
        return null;
    }

    //method to print the game history of a player, usefull for debugging or further applications
    public void printGameHistory(String agentName) {
    Object[][] gameHistory = getGameHistory(agentName);

    if (gameHistory != null) {
        for (Object[] game : gameHistory) {
            int gameOrder = (int) game[0];
            int playerPoints = (int) game[1];
            int opponentPoints = (int) game[2];
            boolean isWinner = (boolean) game[3];

            String result = String.format("%d. %d:%d %s", gameOrder, playerPoints, opponentPoints, isWinner ? "(Winner)" : "(Loser)");
            System.out.println(result);
        }
    } else {
        System.out.println("No game history found for the player: " + agentName);
    }
}
        
    public int[] getstats(String agentName){
        for (PlayerInformation player : players) {
            if (player.aid.getName().equals(agentName)) {
                return player.getstats();
            }
        }
        return null;
    }

    public boolean removePlayerAgent(String agentName) {
        int indexToRemove = -1;
        for (int i = 0; i < playerAgents.length; i++) {
            if (playerAgents[i].getName().equals(agentName)) {
                indexToRemove = i;
                break;
            }
        }
    
        if (indexToRemove != -1) {
            AID[] updatedPlayerAgents = new AID[playerAgents.length - 1];
            System.arraycopy(playerAgents, 0, updatedPlayerAgents, 0, indexToRemove);
            System.arraycopy(playerAgents, indexToRemove + 1, updatedPlayerAgents, indexToRemove, playerAgents.length - indexToRemove - 1);
            playerAgents = updatedPlayerAgents;
            return true; // El agente se eliminó con éxito
        }
    
        return false; // El agente no se encontró en la lista
    }
    
    
    private void updatePlayersUI() {
        // Obtén los nombres de los jugadores restantes después de eliminar uno
        String[] playerNames = players.stream()
                .map(player -> player.aid.getName())
                .toArray(String[]::new);
    
        // Actualiza la interfaz gráfica con los nombres de los jugadores actualizados
        gui.setPlayersUI(playerNames);
    }
    
    public long getRoundsPlayed() {
        return roundsPlayed;
    }

    public int getPlayerPoints(String agentName) {
        for (PlayerInformation player : players) {
            if (player.aid.getName().equals(agentName)) {
                return player.points;
            }
        }
        return -1;
    }

        

}
