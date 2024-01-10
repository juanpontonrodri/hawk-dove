package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RL_Agent extends Agent {

    private State state;
    private AID mainAgent;
    private int myId, opponentId;
    private int N, S, R, I, P;
    private double reward; 
    private int nextState; 
    private ACLMessage msg;

    protected void setup() {
        state = State.s0NoConfig;

        //Registro en las páginas amarillas como jugador
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
        addBehaviour(new Play());
        System.out.println("QLAgent " + getAID().getName() + " está listo.");
    }

    protected void takeDown() {
        //Eliminar de las páginas amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private enum State {
        s0NoConfig, s1AwaitingGame, s2Round, s3AwaitingResult
    }

    private class Play extends CyclicBehaviour {
        private Map<String, Map<Character, Double>> qTable = new HashMap<>();
        private double learningRate = 0.2;
        private double discountFactor = 0.9;
        private double epsilon = 0.2; 
        private int currentState = 0;
        private int lastAction = 0;
        private static final int NUM_STATES = 4;
        private static final double INITIAL_Q_VALUE = 0.0;

        Random random = new Random();

        @Override
        public void action() {
            initializeQTable();
            //System.out.println(getAID().getName() + ":" + state.name());
            msg = blockingReceive();
            if (msg != null) {
                switch (state) {
                    case s0NoConfig:
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> go to state 1
                        //Else ERROR
                        if (msg.getContent().startsWith("Id#") && msg.getPerformative() == ACLMessage.INFORM) {
                            boolean parametersUpdated = false;
                            try {
                                parametersUpdated = validateSetupMessage(msg);
                            } catch (NumberFormatException e) {
                                //System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                            }
                            if (parametersUpdated) state = State.s1AwaitingGame;

                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                            System.out.println(msg.getContent());
                        }
                        break;
                    case s1AwaitingGame:
                        //If INFORM NEWGAME#_,_ PROCESS NEWGAME --> go to state 2
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> stay at s1
                        //Else ERROR
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            if (msg.getContent().startsWith("Id#")) { //Game settings updated
                                try {
                                    validateSetupMessage(msg);
                                } catch (NumberFormatException e) {
                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                            } else if (msg.getContent().startsWith("NewGame#")) {
                                boolean gameStarted = false;
                                try {

                                    gameStarted = validateNewGame(msg.getContent());
                                } catch (NumberFormatException e) {

                                    System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                                }
                                if (gameStarted) state = State.s2Round;
                            }
                        } else {

                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                    case s2Round:
                        //If REQUEST POSITION --> INFORM POSITION --> go to state 3
                        //If INFORM CHANGED stay at state 2
                        //If INFORM ENDGAME go to state 1
                        //Else error
                        if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().startsWith("Action")) {
                            currentState = nextState;
                            int chosenAction = selectAction(currentState);
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(mainAgent);
                            lastAction = chosenAction;
                            char actionChar = (chosenAction == 0) ? 'H' : 'D';
                            msg.setContent("Action#" + actionChar);
                            send(msg);

                            state = State.s3AwaitingResult;

                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Changed#")) {
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("GameOver#")) {
                            //saveQTableInfo();
                            state = State.s1AwaitingGame;

                        } else {
                        System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message:" + msg.getContent());
                        }
                    break;
                    case s3AwaitingResult: 
                        if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Results#")) {
                            String[] resultsContent = msg.getContent().split("#");
                            if (resultsContent.length == 4  ) {
                                String[] identifiers = resultsContent[1].split(",");
                                String[] actions = resultsContent[2].split(",");
                                String[] payoffs = resultsContent[3].split(",");                                
                    
                                if (identifiers.length == 2 && actions.length == 2 && payoffs.length == 2) {
                                    int identifier0 = Integer.parseInt(identifiers[0]);
                                    int identifier1 = Integer.parseInt(identifiers[1]);
                
                                    char action0 = actions[0].charAt(0);
                                    char action1 = actions[1].charAt(0);
                
                                    double payoff0 = Double.parseDouble(payoffs[0]);
                                    double payoff1 = Double.parseDouble(payoffs[1]);
            

                                    if (identifier0 == myId) {
                                        reward = payoff0;
                                        nextState = getNextStateBasedOnActions(currentState, lastAction, action1);
                                    } else if (identifier1 == myId) {
                                        reward = payoff1;
                                        nextState = getNextStateBasedOnActions(currentState, lastAction, action0);

                                    }
                                }
                                
                            }
                            updateQValues(currentState, lastAction, reward, nextState);
                            
                            state = State.s2Round;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                }
            }
        }


        private int getNextStateBasedOnActions(int currentState, int lastAction, char opponentAction) {
        
            // H & H
            if (lastAction == 0 && opponentAction == 'H') {
                return 0;
            }
        
            // H & D
            if (lastAction == 0 && opponentAction == 'D') {
                return 1;
            }
        
            // D & H
            if (lastAction == 1 && opponentAction == 'H') {
                return 2;
            }
        
            // D & D
            if (lastAction == 1 && opponentAction == 'D') {
                return 3;
            }
        
            
            return currentState; 
        }
        

        private int selectAction(int state) {
            if (!qTable.containsKey("State" + state) || qTable.get("State" + state).isEmpty()) {
                return new Random().nextInt(2);
            }
        
            if (Math.random() < epsilon) {
                // Random choice
                return new Random().nextInt(2);
            } else {
                double hawkValue = qTable.get("State" + state).get('H');
                double doveValue = qTable.get("State" + state).get('D');
                return hawkValue > doveValue ? 0 : 1;
            }
        }

        private void updateQValues(int currentState, int lastAction, double reward, int nextState) {
            String currentQState = "State" + currentState;
            char currentQAction = lastAction == 0 ? 'H' : 'D';
            double currentQValue = qTable.get(currentQState).get(currentQAction);
            double nextMaxQValue = qTable.get("State" + nextState).values().stream().max(Double::compare).orElse(0.0);
            double updatedQValue = currentQValue + learningRate * (reward + discountFactor * nextMaxQValue - currentQValue);
            qTable.get(currentQState).put(currentQAction, updatedQValue);

        }


        /* private void saveQTableInfo() {
            try {
                // Ruta del archivo de salida para la información de la qTable
                String filePath = "C:\\Users\\juanp\\OneDrive\\Escritorio\\GUI-Tournament\\PSI_21\\qTable_info.txt";

                // Abrir el archivo para escritura (se añadirá información al final del archivo existente)
                FileWriter fileWriter = new FileWriter(filePath, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                PrintWriter printWriter = new PrintWriter(bufferedWriter);

                // Escribir información relevante de la qTable en el archivo
                printWriter.println(qTable);
                printWriter.println(); // Separador para la siguiente actualización de la qTable
                printWriter.close(); // Cerrar el archivo después de escribir la información

                // Confirmar la escritura en la consola
                //System.out.println("Información de la qTable guardada en " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } */

        private void initializeQTable() {
            if (qTable.isEmpty()) {
                for (int i = 0; i < NUM_STATES; i++) {
                    Map<Character, Double> actionValues = new HashMap<>();
                    // Inicializar valores para Hawk ('H') y Dove ('D')
                    actionValues.put('H', INITIAL_Q_VALUE);
                    actionValues.put('D', INITIAL_Q_VALUE);
                    qTable.put("State" + i, actionValues);
                }
            }
        }
        
    
        

        /**
         * Validates and extracts the parameters from the setup message
         *
         * @param msg ACLMessage to process
         * @return true on success, false on failure
         */
        private boolean validateSetupMessage(ACLMessage msg) throws NumberFormatException {
            int tN, tS, tR, tI, tP, tMyId;
            String msgContent = msg.getContent();
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3) return false;
            if (!contentSplit[0].equals("Id")) return false;
            tMyId = Integer.parseInt(contentSplit[1]);
            String[] parametersSplit = contentSplit[2].split(",");
            if (parametersSplit.length != 2) return false;
            tN = Integer.parseInt(parametersSplit[0]);
            tR = Integer.parseInt(parametersSplit[1]);

            //At this point everything should be fine, updating class variables
            mainAgent = msg.getSender();
            N = tN;
            R = tR;
            myId = tMyId;
            return true;
        }

        /**
         * Processes the contents of the New Game message
         * @param msgContent Content of the message
         * @return true if the message is valid
         */
        public boolean validateNewGame(String msgContent) {
            int msgId0, msgId1;
            String[] contentSplit = msgContent.split("#");
            if (contentSplit.length != 3) return false;
            if (!contentSplit[0].equals("NewGame")) return false;
            msgId0 = Integer.parseInt(contentSplit[1]);
            msgId1 = Integer.parseInt(contentSplit[2]);
            if (myId == msgId0) {
                opponentId = msgId1;
                return true;
            } else if (myId == msgId1) {
                opponentId = msgId0;
                return true;
            }
            return false;
        }
    }
}
