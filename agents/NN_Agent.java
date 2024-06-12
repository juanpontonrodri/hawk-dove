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

class NeuronaQlearning {

private Map<String, Map<String, Double>> qTable;
private final double dGamma = 0.9;
private final double dEpsilon = 0.1;
private final double dAlpha = 0.1;
private Random random;

int posicion_x;
int posicion_y;

//constructor
public NeuronaQlearning(int x,int y){
posicion_x = x;
posicion_y = y;
qTable = new HashMap<>();
random = new Random();
}

//Esta función determina la accion a través de una cadena de caracteres
public String decidirAccion(String state) {
    Map<String, Double> jugadas = qTable.getOrDefault(state, new HashMap<>()); //Busca en la tabla qtable asociada al estado que le llego
    //Si hay información disponible, decide aleatoriamente entre las opciones basándose en el factor de exploración dEpsilon
    if (!jugadas.isEmpty()) {
    if (random.nextDouble() < dEpsilon) {
        //String[] jugadasArray = jugadas.keySet().toArray(new String[0]);
        //return jugadasArray[random.nextInt(jugadasArray.length)];
        return random.nextBoolean() ? "H" : "D";
    }

    //Buscamos cual es la mejor opcion para jugar, explotacion
    return jugadas.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElseGet(() -> {
                      String[] jugadasArray2 = jugadas.keySet().toArray(new String[0]);
                      return jugadasArray2[random.nextInt(jugadasArray2.length)];
    });
    
    }else return random.nextBoolean() ? "H" : "D";
    /*else {
        return "H"; // Si no hay informacion disponible, devolvemos H o D aleatoriamente
    }*/
}

//Actualizar los valores de la tabla Q basándose en la recompensa recibida y los valores actuales y futuros asociados a un estado y una acción específicos 
public void actualizaTablaQTable(String estado, String accion, String nextestado, double recompensa) {
    //Obtiene el valor actual de la tabla Q para el estado y la acción proporcionados
    double actualQ = qTable.getOrDefault(estado, new HashMap<>()).getOrDefault(accion, 0.0);
    // Calculamos el max Q para el siguiente estado
    double maxSiguienteQ = qTable.getOrDefault(nextestado, new HashMap<>()).values().stream().max(Double::compare).orElse(0.0);
    // Actualizamos los valores
    // Calcula el nuevo valor de Q utilizando la fórmula de actualización de Q-learning
    double newQ = actualQ + dAlpha * (recompensa + dGamma * maxSiguienteQ - actualQ);
    //Actualiza la tabla Q
    qTable.computeIfAbsent(estado, k -> new HashMap<>()).put(accion, newQ);
    }
}

class SOM{

    private int iGridSide;            // Side of the SOM 2D grid
    private int iCellSize;            // Size in pixels of a SOM neuron in the grid
    int[][] iNumTimesBMU;         // Number of times a cell has been a BMU
    private int[] iBMU_Pos = new int[2];     // BMU position in the grid

    private int iInputSize;               // Size of the input vector
    private int iRadio;                 // BMU radio to modify neurons
    private double dLearnRate = 1.0;          // Learning rate for this SOM
    private double dDecLearnRate = 0.999;        // Used to reduce the learning rate
    private double[] dBMU_Vector = null;        // BMU state
    private double[][][] dGrid;             // SOM square grid + vector state per neuron
    public NeuronaQlearning[][] neuronas_grid;


    /**
     * This is the class constructor that creates the 2D SOM grid
     * 
     * @param iSideAux  the square side
     * @param iInputSizeAux  the dimensions for the input data
     * 
     */
    public SOM (int iSideAux, int iInputSizeAux) {
    iInputSize = iInputSizeAux;
    iGridSide = iSideAux;
    iRadio = iGridSide / 10;
    dBMU_Vector = new double[iInputSize];
    dGrid = new double [iGridSide][iGridSide][iInputSize];
    iNumTimesBMU = new int[iGridSide][iGridSide];
    neuronas_grid = new NeuronaQlearning[iGridSide][iGridSide];

    vResetValues();
    }

    public void vResetValues() {
    dLearnRate = 1.0;
    iNumTimesBMU = new int[iGridSide][iGridSide];
    iBMU_Pos[0] = -1;
    iBMU_Pos[1] = -1;
    
        for (int i=0; i<iGridSide; i++) {        // Initializing the SOM grid/network
            for (int j=0; j<iGridSide; j++)
            {
                NeuronaQlearning neuron = new NeuronaQlearning(i,j);
                neuronas_grid[i][j] = neuron;
                for (int k=0; k<iInputSize; k++){
                    dGrid[i][j][k] = Math.random(); 
                }
            }
        }
    }

    public double[] dvGetBMU_Vector() {
    return dBMU_Vector;
    }

    public double dGetLearnRate() {
    return dLearnRate;
    }

    public double[] dGetNeuronWeights (int x, int y) {
    return dGrid[x][y];
    }
    
    public String determineNextMove(double[] estado){

        String estado_aux = createDHString(estado); //Primero convierte el estado en una cadena de caracteres de H y D

        return neuronas_grid[iBMU_Pos[0]][iBMU_Pos[1]].decidirAccion(estado_aux); //Usa esa cadena para tomar una decision

    }

    //Esta función recibe un array de números y los convierte en una cadena de texto representando el estado.
    public static String createDHString(double[] state) {
    StringBuilder string = new StringBuilder();
    for (double value : state) {
        if (value == 0) {
            string.append('H'); //Si el valor es 0, agrega el carácter 'H' a un StringBuilder
        } else {
            string.append('D'); //De lo contrario, agrega el carácter 'D'. Luego, devuelve la cadena resultante
        }
    }
    return string.toString();
    }

    public void findBestBMU(){
      int mayor_x = -1, mayor_y = -1;
      int maximo_valor = Integer.MIN_VALUE;
          for (int i = 0; i < iGridSide; i++) {
            for (int j = 0; j < iGridSide; j++) {
                if (iNumTimesBMU[i][j] > maximo_valor) {
                    maximo_valor = iNumTimesBMU[i][j];
                    mayor_y = j;
                    mayor_x = i; 
                }
            }
        }
    }

    //Como su nombre indica, actualiza la qtable
    public void actualizarQtable(double[] estado, String accion, double[] record, double recompensa){
        //Convertimos los arrays de numeros en Strings
        String recordString = createDHString(record);
        String estadoString = createDHString(estado);

        //Una vez hecha la conversión, llamamos a la funcion actualizaTablaQtable de la neurona
        neuronas_grid[iBMU_Pos[0]][iBMU_Pos[1]].actualizaTablaQTable(estadoString, accion, recordString, recompensa);

    }

    /**
     * This is the main method that returns the coordinates of the BMU and trains its neighbors
     * 
     * @param dmInput  contains the input vector
     * @param bTrain  training or testing phases
     * 
     */
    public String sGetBMU (double[] dmInput, boolean bTrain){
            int x=0, y=0;
            double dNorm, dNormMin = Double.MAX_VALUE;
            String sReturn;

            for (int i=0; i<iGridSide; i++)            // Finding the BMU
            for (int j=0; j<iGridSide; j++) {
            dNorm = 0;
            for (int k=0; k<iInputSize; k++)           // Calculating the norm
                dNorm += (dmInput[k] - dGrid[i][j][k]) * ((dmInput[k] - dGrid[i][j][k]));
            
            if (dNorm < dNormMin) {
                dNormMin = dNorm; 
                x = i;
                y = j;
            }
            }                       // Leaving the loop with the x,y positions for the BMU

            if (bTrain) {
            int xAux=0;
            int yAux=0;
            for (int v=-iRadio; v<=iRadio; v++)       // Adjusting the neighborhood
            for (int h=-iRadio; h<=iRadio; h++) {
                xAux = x+h;
                yAux = y+v;
                
                if (xAux < 0)                // Assuming a torus world
                xAux += iGridSide;
                else if (xAux >= iGridSide)
                xAux -= iGridSide;
            
                if (yAux < 0)
                yAux += iGridSide;
                else if (yAux >= iGridSide)
                yAux -= iGridSide;
            
                for (int k=0; k<iInputSize; k++)
                dGrid[xAux][yAux][k] += dLearnRate * (dmInput[k] - dGrid[xAux][yAux][k]) / (1 + v*v + h*h);
            }

            }

            sReturn = "" + x + "," + y;
            iBMU_Pos[0] = x;
            iBMU_Pos[1] = y;
            dBMU_Vector = dGrid[x][y].clone();
            iNumTimesBMU[x][y]++;
            dLearnRate *= dDecLearnRate;
            
            return sReturn;
    }

} // from the class SOM

public class NN_Agent extends Agent {

    public static final int gridDimension = 5;
    public static final int inputDimension = 10;
    private final Integer H = 0;
    private final Integer D = 1;
    public int rondas = 0;
    double[] inputRecord = new double[inputDimension];
    SOM som;

    private State state;
    private AID mainAgent;
    private int myId, opponentId;
    private int N, S, R, I, P;
    private ACLMessage msg;

    protected void setup() {

        som = new SOM(gridDimension,inputDimension);
        state = State.s0NoConfig;

        //Register in the yellow pages as a player
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
        System.out.println("RandomAgent " + getAID().getName() + " is ready.");

    }

    protected void takeDown() {
        //Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println("RandomPlayer " + getAID().getName() + " terminating.");
    }

    private enum State {
        s0NoConfig, s1AwaitingGame, s2Round, s3AwaitingResult
    }

    private class Play extends CyclicBehaviour {

        Random random = new Random();  
        @Override
        public void action() {
            //Esperamos a que se reciba un mensaje
            msg = blockingReceive();
            if (msg != null) {
                //System.out.println(getAID().getName() + " received " + msg.getContent() + " from " + msg.getSender().getName()); //DELETEME
                System.out.println(getAID().getName() + ":" + msg.getContent());
                //-------- Agent logic
                switch (state) {
                    case s0NoConfig:
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> go to state 1
                        //Else ERROR
                        if (msg.getContent().startsWith("Id#") && msg.getPerformative() == ACLMessage.INFORM) {
                            boolean parametersUpdated = false;
                            try {
                                parametersUpdated = validateSetupMessage(msg);
                            } catch (NumberFormatException e) {
                                System.out.println(getAID().getName() + ":" + state.name() + " - Bad message");
                            }
                            if (parametersUpdated) state = State.s1AwaitingGame;

                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
                    case s1AwaitingGame:
                        //If INFORM NEWGAME#_,_ PROCESS NEWGAME --> go to state 2
                        //If INFORM Id#_#_,_,_,_ PROCESS SETUP --> stay at s1
                        //Else ERROR
                        //TODO I probably should check if the new game message comes from the main agent who sent the parameters
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
                        if (msg.getPerformative() == ACLMessage.REQUEST /*&& msg.getContent().startsWith("Position")*/) {
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.addReceiver(mainAgent);
                            rondas++;
                            if(rondas <= 10){
                              int randomValue = random.nextInt(2);// Generamos un numero aleatorio entre 0 y 1
                              String eleccion = (randomValue == 0) ? "D" : "H";//Si el numero de rondas es menor que 10, retornamos random ya que no tenemos informacion suficiente para predecir.
                              msg.setContent("Action#"+eleccion); 
                              send(msg);
                            }else{
                              String coordenadas = som.sGetBMU(inputRecord, true);
                              String eleccion = som.determineNextMove(inputRecord); //Determinamos el siguiente movimiento en base a lo jugado anteriormente por el oponente
                              msg.setContent("Action#"+eleccion); 
                              send(msg);                                
                            }

                            state = State.s3AwaitingResult;

                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Changed#")) {
                            // Process changed message, in this case nothing
                        } else if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("GameOver#")) {
                           
                            som.findBestBMU();
                            som.iNumTimesBMU = new int[gridDimension][gridDimension];
                            rondas = 0;
                            state = State.s1AwaitingGame;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message:" + msg.getContent());
                        }
                        break;
                    case s3AwaitingResult:
                        //If INFORM RESULTS --> go to state 2
                        //Else error
                        if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().startsWith("Results#")) {
                            //Utilizamos la informacion que obtenemos de los resultados
                            int yo= 0;
                            int oponente = 0;
                            String[] contenido = msg.getContent().substring(8).split("#");
                            // Separamos en 3 variables, los ids de los jugadores, las jugadas que hicieros y la ganancia que obtuvieron
                            String[] ids_jugadores = contenido[0].split(","); //Sacamos los id de los jugadores
                            String[] acciones = contenido[1].split(","); //Las acciones realizadas por esos agentes
                            String[] ganancias = contenido[2].split(","); //Y las ganancias obtenidas

                            //Buscamos el id que corresponde al propio agente y al del oponente
                            for(int i =0;i<2;i++){
                                if(myId == Integer.parseInt(ids_jugadores[i].trim())){
                                    yo = i;
                                }else{
                                    oponente = i;
                                } 
                            }
                            String jugado;
                            jugado = acciones[oponente].trim(); //Representa lo que jugo el oponente
                            double[] accion_pasada;
                            accion_pasada = inputRecord; //Representa las 10 jugadas pasadas del oponente
                            logRoundProgress(jugado); //Anade la nueva jugada del oponente al string que contiene todas las jugadas
                            double reward;
                            reward = Integer.parseInt(ganancias[yo].trim()); //Ganancia obtenida por el propio agente
                            String accion = acciones[yo].trim(); //Jugada del propio agente
                            if(rondas>10){
                              som.actualizarQtable(accion_pasada, accion, inputRecord, reward); //Si van mas de 10 rondas, actualizamos la qtable con la informacion actual que tenemos
                            }
                            state = State.s2Round;
                        } else {
                            System.out.println(getAID().getName() + ":" + state.name() + " - Unexpected message");
                        }
                        break;
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
            int tN, tR, tMyId;
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

  public void logRoundProgress(String resultado) {
    // Verificar si las jugadas del oponente son nulas o tiene una longitud diferente a inputDimension
    if (inputRecord == null || inputRecord.length != inputDimension) {
        throw new IllegalArgumentException("Nulo o de tamaño distinto inputDimension.");
    }
    // Verificar si el resultado de la ronda es válido ('H' o 'D')
    if (!resultado.equals("H") && !resultado.equals("D")) {
        throw new IllegalArgumentException("Valores validos: D o H");
    }

    // Desplazar los elementos hacia la izquierda
    for (int i = 0; i < inputRecord.length - 1; i++) {
        inputRecord[i] = inputRecord[i + 1];
    }
    // Agregar el nuevo resultado de la ronda al final del arreglo
    inputRecord[inputRecord.length - 1] = resultado.equals("D") ? D : H;
}

    
}
