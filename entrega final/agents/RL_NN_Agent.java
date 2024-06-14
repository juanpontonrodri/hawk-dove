package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class QLearningNeuron2 {
    private Map<String, Map<String, Double>> qTable;
    private final double gamma = 0.9;
    private final double epsilon = 0.1;
    private final double alpha = 0.1;
    private Random random;

    int posX;
    int posY;

    public QLearningNeuron2(int x, int y) {
        posX = x;
        posY = y;
        qTable = new HashMap<>();
        random = new Random();
    }

    public String decideAction(String state) {
        Map<String, Double> actions = qTable.getOrDefault(state, new HashMap<>());
        if (!actions.isEmpty()) {
            if (random.nextDouble() < epsilon) {
                return random.nextBoolean() ? "H" : "D";
            }
            return actions.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseGet(() -> {
                String[] actionArray = actions.keySet().toArray(new String[0]);
                return actionArray[random.nextInt(actionArray.length)];
            });
        } else {
            return random.nextBoolean() ? "H" : "D";
        }
    }

    public void updateQTable(String state, String action, String nextState, double reward) {
        double currentQ = qTable.getOrDefault(state, new HashMap<>()).getOrDefault(action, 0.0);
        double maxNextQ = qTable.getOrDefault(nextState, new HashMap<>()).values().stream().max(Double::compare).orElse(0.0);
        double newQ = currentQ + alpha * (reward + gamma * maxNextQ - currentQ);
        qTable.computeIfAbsent(state, k -> new HashMap<>()).put(action, newQ);
    }
}

class SelfOrganizingMap2 {
    private int gridSize;
    private int inputSize;
    public int[][] timesBMU;  // Changed access to public
    private int[] bmuPos = new int[2];
    private int radius;
    private double learnRate = 1.0;
    private double learnRateDecay = 0.999;
    private double[][][] grid;
    public QLearningNeuron2[][] neuronGrid;

    public SelfOrganizingMap2(int gridSide, int inputDim) {
        gridSize = gridSide;
        inputSize = inputDim;
        radius = gridSize / 10;
        grid = new double[gridSize][gridSize][inputSize];
        timesBMU = new int[gridSize][gridSize];
        neuronGrid = new QLearningNeuron2[gridSize][gridSize];
        resetValues();
    }

    public void resetValues() {
        learnRate = 1.0;
        timesBMU = new int[gridSize][gridSize];
        bmuPos[0] = -1;
        bmuPos[1] = -1;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                neuronGrid[i][j] = new QLearningNeuron2(i, j);
                for (int k = 0; k < inputSize; k++) {
                    grid[i][j][k] = Math.random();
                }
            }
        }
    }

    public String determineNextMove(double[] state) {
        String stateString = createDHString(state);
        return neuronGrid[bmuPos[0]][bmuPos[1]].decideAction(stateString);
    }

    public static String createDHString(double[] state) {
        StringBuilder sb = new StringBuilder();
        for (double val : state) {
            sb.append(val == 0 ? 'H' : 'D');
        }
        return sb.toString();
    }

    public void findBestBMU() {
        int bestX = -1, bestY = -1;
        int maxTimes = Integer.MIN_VALUE;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (timesBMU[i][j] > maxTimes) {
                    maxTimes = timesBMU[i][j];
                    bestX = i;
                    bestY = j;
                }
            }
        }
    }

    public void updateQTable(double[] state, String action, double[] nextState, double reward) {
        String stateString = createDHString(state);
        String nextStateString = createDHString(nextState);
        neuronGrid[bmuPos[0]][bmuPos[1]].updateQTable(stateString, action, nextStateString, reward);
    }

    public String getBMU(double[] inputVector, boolean isTraining) {
        int x = 0, y = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double dist = 0;
                for (int k = 0; k < inputSize; k++) {
                    dist += (inputVector[k] - grid[i][j][k]) * (inputVector[k] - grid[i][j][k]);
                }
                if (dist < minDist) {
                    minDist = dist;
                    x = i;
                    y = j;
                }
            }
        }

        if (isTraining) {
            for (int v = -radius; v <= radius; v++) {
                for (int h = -radius; h <= radius; h++) {
                    int xAux = (x + h + gridSize) % gridSize;
                    int yAux = (y + v + gridSize) % gridSize;
                    for (int k = 0; k < inputSize; k++) {
                        grid[xAux][yAux][k] += learnRate * (inputVector[k] - grid[xAux][yAux][k]) / (1 + v * v + h * h);
                    }
                }
            }
        }

        bmuPos[0] = x;
        bmuPos[1] = y;
        learnRate *= learnRateDecay;
        timesBMU[x][y]++;
        return x + "," + y;
    }
}

public class RL_NN_Agent extends Agent {
    private static final int GRID_DIMENSION = 5;
    private static final int INPUT_DIMENSION = 10;
    private static final Integer H = 0;
    private static final Integer D = 1;
    private int rounds = 0;
    private double[] inputRecord = new double[INPUT_DIMENSION];
    private SelfOrganizingMap2 som;

    private State currentState;
    private AID mainAgent;
    private int myId, opponentId;
    private int N, S, R, I, P;
    private ACLMessage msg;

    protected void setup() {
        som = new SelfOrganizingMap2(GRID_DIMENSION, INPUT_DIMENSION);
        currentState = State.NO_CONFIG;

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Player");
        sd.setName("agents");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new PlayBehaviour());
        System.out.println("RL_NN_Agent " + getAID().getName() + " is ready.");
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("RL_NN_Agent " + getAID().getName() + " terminating.");
    }

    private enum State {
        NO_CONFIG, AWAITING_GAME, ROUND, AWAITING_RESULT
    }

    private class PlayBehaviour extends CyclicBehaviour {
        Random random = new Random();

        @Override
        public void action() {
            msg = blockingReceive();
            if (msg != null) {
                System.out.println(getAID().getName() + ":" + msg.getContent());
                switch (currentState) {
                    case NO_CONFIG:
                        if (msg.getContent().startsWith("Id#") && msg.getPerformative() == ACLMessage.INFORM) {
                            boolean updated = false;
                            try {
                                updated = validateSetupMessage(msg);
                            } catch (NumberFormatException e) {
                                System.out.println(getAID().getName() + ": NO_CONFIG - Bad message");
                            }
                            if (updated) currentState = State.AWAITING_GAME;
                        } else {
                            System.out.println(getAID().getName() + ": NO_CONFIG - Unexpected message");
                        }
                        break;
                    case AWAITING_GAME:
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            if (msg.getContent().startsWith("Id#")) {
                                try {
                                    validateSetupMessage(msg);
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ": AWAITING_GAME - Bad message");
                                }
                            } else if (msg.getContent().startsWith("NewGame#")) {
                                boolean started = false;
                                try {
                                    started = validateNewGame(msg.getContent());
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ": AWAITING_GAME - Bad message");
                                }
                                if (started) currentState = State.ROUND;
                            }
                        } else {
                            System.out.println(getAID().getName() + ": AWAITING_GAME - Unexpected message");
                        }
                        break;
                    case ROUND:
                        if (msg.getPerformative() == ACLMessage.REQUEST) {
                            ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                            response.addReceiver(mainAgent);
                            rounds++;
                            if (rounds <= 10) {
                                String choice = random.nextBoolean() ? "D" : "H";
                                response.setContent("Action#" + choice);
                                send(response);
                            } else {
                                String coordinates = som.getBMU(inputRecord, true);
                                String choice = som.determineNextMove(inputRecord);
                                response.setContent("Action#" + choice);
                                send(response);
                            }
                            currentState = State.AWAITING_RESULT;
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Changed#")) {
                            // Process changed message
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("GameOver#")) {
                            som.findBestBMU();
                            som.timesBMU = new int[GRID_DIMENSION][GRID_DIMENSION];
                            rounds = 0;
                            currentState = State.AWAITING_GAME;
                        } else {
                            System.out.println(getAID().getName() + ": ROUND - Unexpected message:" + msg.getContent());
                        }
                        break;
                    case AWAITING_RESULT:
                        if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Results#")) {
                            processResults(msg.getContent());
                            currentState = State.ROUND;
                        } else {
                            System.out.println(getAID().getName() + ": AWAITING_RESULT - Unexpected message");
                        }
                        break;
                }
            }
        }

        private boolean validateSetupMessage(ACLMessage msg) throws NumberFormatException {
            String[] contentSplit = msg.getContent().split("#");
            if (contentSplit.length != 3 || !contentSplit[0].equals("Id")) return false;
            int tMyId = Integer.parseInt(contentSplit[1]);
            String[] params = contentSplit[2].split(",");
            if (params.length != 2) return false;
            int tN = Integer.parseInt(params[0]);
            int tR = Integer.parseInt(params[1]);

            mainAgent = msg.getSender();
            N = tN;
            R = tR;
            myId = tMyId;
            return true;
        }

        private boolean validateNewGame(String msgContent) {
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3 || !contentSplit[0].equals("NewGame")) return false;
            int id0 = Integer.parseInt(contentSplit[1]);
            int id1 = Integer.parseInt(contentSplit[2]);
            if (myId == id0) {
                opponentId = id1;
                return true;
            } else if (myId == id1) {
                opponentId = id0;
                return true;
            }
            return false;
        }

        private void processResults(String content) {
            String[] parts = content.substring(8).split("#");
            String[] playerIds = parts[0].split(",");
            String[] actions = parts[1].split(",");
            String[] rewards = parts[2].split(",");

            int myIndex = myId == Integer.parseInt(playerIds[0].trim()) ? 0 : 1;
            int opponentIndex = 1 - myIndex;

            String opponentMove = actions[opponentIndex].trim();
            double[] pastAction = inputRecord.clone();
            logRoundProgress(opponentMove);
            double reward = Integer.parseInt(rewards[myIndex].trim());
            String myAction = actions[myIndex].trim();

            if (rounds > 10) {
                som.updateQTable(pastAction, myAction, inputRecord, reward);
            }
        }
    }

    public void logRoundProgress(String result) {
        if (inputRecord == null || inputRecord.length != INPUT_DIMENSION) {
            throw new IllegalArgumentException("Invalid inputRecord length.");
        }
        if (!result.equals("H") && !result.equals("D")) {
            throw new IllegalArgumentException("Invalid result: must be 'H' or 'D'");
        }

        System.arraycopy(inputRecord, 1, inputRecord, 0, inputRecord.length - 1);
        inputRecord[inputRecord.length - 1] = result.equals("D") ? D : H;
    }
}
